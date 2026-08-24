package minecraftose.client.network;

import generaloss.spatialmath.Maths;
import generaloss.spatialmath.vector.Vec3f;
import minecraftose.client.Minecraft;
import minecraftose.client.chunk.ChunkC;
import minecraftose.client.entity.LocalPlayer;
import minecraftose.client.entity.RemotePlayer;
import minecraftose.client.level.LevelC;
import minecraftose.client.network.pipeline.security.AESKey;
import minecraftose.main.chat.source.MessageSourceServer;
import minecraftose.main.entity.Entity;
import minecraftose.main.network.packet.c2s.login.C2SPacketAuth;
import minecraftose.main.network.packet.c2s.login.C2SPacketEncryptEnd;
import minecraftose.main.network.packet.c2s.game.C2SPacketRenderDistance;
import minecraftose.main.network.packet.s2c.game.*;
import minecraftose.main.network.packet.s2c.login.S2CPacketEncryptStart;
import minecraftose.main.text.Component;

public class ClientPacketHandler {

    private final ClientConnection connectionHandler;
    private final Minecraft minecraft;
    private final AESKey encryptKey;

    public ClientPacketHandler(ClientConnection connectionHandler){
        this.connectionHandler = connectionHandler;
        this.minecraft = connectionHandler.getMinecraft();
        this.encryptKey = new AESKey(256);
    }

    public Minecraft getMinecraft(){
        return minecraft;
    }


    public void updateVelocity(S2CPacketPlayerVelocity packet){
        minecraft.getPlayer().getVelocity().set(packet.velocity);
    }

    public void time(S2CPacketTime packet){
        minecraft.getTime().setTicks(packet.gameTimeTicks);
    }

    public void teleportPlayer(S2CPacketTeleportPlayer packet){
        final LocalPlayer localPlayer = minecraft.getPlayer();
        if(localPlayer == null)
            return;

        // Load another level
        final LevelC level = localPlayer.getLevel();
        if(level == null)
            return;

        if(!packet.levelName.equals(level.getConfiguration().getName())){
            minecraft.createClientLevel(packet.levelName);
            // minecraft.getLevel().getChunkProvider().startLoadChunks(); //: deprecated method
            localPlayer.setLevel(minecraft.getLevel());
        }

        localPlayer.getPosition().set(packet.position);
        localPlayer.getRotation().set(packet.rotation);
    }

    public void spawnPlayer(S2CPacketSpawnPlayer packet){
        final RemotePlayer remotePlayer = new RemotePlayer(minecraft, minecraft.getLevel(), packet.playerName);
        remotePlayer.getPosition().set(packet.position);
        remotePlayer.getRotation().set(packet.rotation);
        remotePlayer.setUUID(packet.uuid);

        minecraft.getLevel().addEntity(remotePlayer);
    }

    public void spawnInfo(S2CPacketSpawnInfo packet){
        minecraft.createClientLevel(packet.levelName);
        minecraft.getTime().setTicks(packet.gameTime);
        minecraft.getPlayer().setLevel(minecraft.getLevel());
        minecraft.getPlayer().getPosition().set(packet.position);
        // minecraft.getLevel().getChunkProvider().startLoadChunks(); //: deprecated method

        connectionHandler.sendPacket(new C2SPacketRenderDistance(minecraft.getOptions().getRenderDistance()));
    }

    public void spawnEntity(S2CPacketSpawnEntity packet){
        final Entity entity = packet.type.createEntity(minecraft.getLevel());
        entity.getPosition().set(packet.position);
        entity.getRotation().set(packet.rotation);
        entity.setUUID(packet.uuid);

        minecraft.getLevel().addEntity(entity);
    }

    public void removeEntity(S2CPacketRemoveEntity packet){
        minecraft.getLevel().removeEntity(packet.uuid);
    }

    public void pong(S2CPacketPong packet){
        final String message = "Ping - " + String.format("%.5f", (System.nanoTime() - packet.timeNanos) / Maths.NANOS_IN_SECf) + " ms";
        minecraft.getChat().putMessage(new MessageSourceServer(), new Component().text(message));
        System.out.println("[Client]: " + message);
    }

    public void playSound(S2CPacketPlaySound packet){
        Minecraft.syncExecutor.exec(() ->
            minecraft.getSoundPlayer().play(
                packet.sound,
                packet.volume, packet.pitch,
                packet.x,
                packet.y,
                packet.z
            )
        );
    }

    public void playerSneaking(S2CPacketPlayerSneaking packet){
        final Entity targetEntity = minecraft.getLevel().getEntity(packet.playerUUID);
        if(targetEntity instanceof RemotePlayer player)
            player.setSneaking(packet.sneaking);
    }

    public void lightUpdate(S2CPacketLightUpdate packet){
        final ChunkC chunk = minecraft.getLevel().getChunkProvider().getChunk(packet.position.x, packet.position.z);
        if(chunk == null)
            return;

        final byte[] chunkLight = chunk.getSection(packet.position.y).light;
        System.arraycopy(packet.light, 0, chunkLight, 0, chunkLight.length);

        chunk.rebuild(true);
    }

    public void entityMove(S2CPacketEntityMove packet){
        Entity targetEntity = minecraft.getLevel().getEntity(packet.uuid);
        if(targetEntity == null && minecraft.getPlayer().getUUID() == packet.uuid)
            targetEntity = minecraft.getPlayer();

        if(targetEntity != null){
            targetEntity.getPosition().set(packet.position);
            targetEntity.getRotation().set(packet.rotation);
            targetEntity.getVelocity().set(packet.velocity);
        }
    }

    public void encryptStart(S2CPacketEncryptStart packet){
        final byte[] encryptedClientKey = packet.publicServerKey.encrypt(encryptKey.getKey().getEncoded());

        connectionHandler.sendPacket( new C2SPacketEncryptEnd(encryptedClientKey) );
        connectionHandler.encode(encryptKey);// * шифрование *
        connectionHandler.sendPacket(new C2SPacketAuth(minecraft.getSessionToken()));
    }

    public void disconnect(S2CPacketDisconnect packet){
        minecraft.disconnect();
        System.out.println("[Client]: Connection closed: " + packet.reasonComponent);
    }

    public void chunk(S2CPacketChunk packet){
        minecraft.getLevel().getChunkProvider().putChunk(packet.getChunk(minecraft.getLevel()));
    }

    public void chatMessage(S2CPacketChatMessage packet){
        minecraft.getChat().putMessage(packet);
    }

    public void blockUpdate(S2CPacketBlockUpdate packet){
        minecraft.getLevel().setBlockState(packet.x, packet.y, packet.z, packet.blockData);


        if(packet.blockData == 0)
            for(int i = 0; i < 100; i++)
                minecraft.spawnParticle(minecraft.BREAK_PARTICLE, new Vec3f(
                    packet.x + Maths.random(1F),
                    packet.y + Maths.random(1F),
                    packet.z + Maths.random(1F)
                ));
    }

    public void abilities(S2CPacketAbilities packet){
        minecraft.getPlayer().setFlyEnabled(packet.flyEnabled);
    }

}
