package minecraftose.server.network;

import generaloss.spatialmath.vector.Vec3f;
import generaloss.networkforge.tcp.TCPConnection;
import generaloss.networkforge.packet.VisitorNetPacket;
import minecraftose.client.block.BlockProps;
import minecraftose.client.block.ClientBlock;
import minecraftose.client.block.ClientBlocks;
import minecraftose.main.audio.SoundGroup;
import minecraftose.main.audio.SoundType;
import minecraftose.main.block.ChunkBlockData;
import minecraftose.main.block.BlockSetType;
import minecraftose.main.chunk.ChunkBase;
import minecraftose.main.chunk.storage.HeightmapType;
import minecraftose.main.command.source.CommandServerPlayerSource;
import minecraftose.main.entity.Entity;
import minecraftose.main.network.packet.c2s.game.*;
import minecraftose.main.network.packet.c2s.game.move.C2SPacketMove;
import minecraftose.main.network.packet.c2s.game.move.C2SPacketMoveAndRot;
import minecraftose.main.network.packet.c2s.game.move.C2SPacketRot;
import minecraftose.main.network.packet.s2c.game.*;
import minecraftose.main.text.Component;
import minecraftose.main.text.TextColor;
import minecraftose.server.Server;
import minecraftose.server.level.chunk.ChunkS;
import minecraftose.server.level.LevelS;
import minecraftose.server.player.ServerPlayer;

public class ServerPlayerGameConnection implements ServerPacketHandler {
    
    private final ServerPlayer player;
    private final Server server;
    private final TCPConnection connection;
    private final CommandServerPlayerSource commandSource;
    
    public ServerPlayerGameConnection(ServerPlayer player, TCPConnection connection){
        this.player = player;
        this.server = player.getServer();
        this.connection = connection;
        this.commandSource = new CommandServerPlayerSource(player);
    }
    
    
    public ServerPlayer getPlayer(){
        return player;
    }

    public void disconnect(){
        connection.close();
    }
    
    public void sendPacket(VisitorNetPacket<?> packet){
        if(connection.isConnected())
            connection.send(packet);
    }


    public void hitEntity(C2SPacketHitEntity packet){
        final Entity entity = player.getLevel().getEntity(packet.entityUUID);
        System.out.println(player.getName() + " hit entity " + packet.entityUUID);
        if(entity == null){
            System.out.println("Entity is null");
            return;
        }
        player.getLevel().playSound(SoundGroup.HIT.random(), 1, 1, entity.getPosition());

        final Vec3f direction = new Vec3f(entity.getPosition()).sub(player.getPosition()).nor().add(0, 1, 0).mul(0.5);
        entity.getVelocity().add(direction);

        if(entity instanceof ServerPlayer playerVictim){
            playerVictim.sendPacket(new S2CPacketPlayerVelocity(entity.getVelocity()));
            System.out.println("Entity is player " + playerVictim.getName());
        }
    }
    
    public void chunkRequest(C2SPacketChunkRequest packet){ //: deprecated chunks request from client
        // final LevelS level = player.getLevel();

        // final ChunkRequestPull chunkRequestPull = level.getChunkProvider().getChunkRequestPull();
        // chunkRequestPull.require(player, packet.packedChunkPos);
    }
    
    public void playerBlockSet(C2SPacketPlayerBlockSet packet){
        final LevelS level = player.getLevel();
        final BlockProps oldBlock = level.getBlockProps(packet.x, packet.y, packet.z);
        final int oldHeightLight = level.getHeight(HeightmapType.LIGHT_SURFACE, packet.x, packet.z);

        // Set Block on the Server //
        final boolean result = level.setBlockState(packet.x, packet.y, packet.z, packet.blockData);
        if(!result)
            return;
        
        final BlockProps block = ChunkBlockData.getProps(packet.blockData);
        final int heightLight = level.getHeight(HeightmapType.LIGHT_SURFACE, packet.x, packet.z);
        final BlockSetType setType = BlockSetType.from(oldBlock.isEmpty(), block.isEmpty());

        // Send Set Block packet //
        server.getPlayerList().broadcastPacketExcept(new S2CPacketBlockUpdate(packet.x, packet.y, packet.z, packet.blockData), player);

        // Sound //
        final SoundType soundPack = switch(setType){
            case PLACE, REPLACE -> block.getSoundPack();
            case DESTROY -> oldBlock.getSoundPack();
        };
        if(soundPack != null)
            level.playSound(soundPack.randomSound(setType), 1, 1, packet.x + 0.5F, packet.y + 0.5F, packet.z + 0.5F);

        // Process grass
        if(setType == BlockSetType.DESTROY){
            int destroyedBlockY = packet.y + 1;
            ClientBlock destroyedBlock = level.getBlock(packet.x, destroyedBlockY, packet.z);
            if(destroyedBlock == ClientBlocks.GRASS){
                level.setBlock(packet.x, packet.y + 1, packet.z, ClientBlocks.AIR);
                player.sendPacket(new S2CPacketBlockUpdate(packet.x, packet.y + 1, packet.z, ClientBlocks.AIR.getDefaultData()));
                level.playSound(SoundType.GRASS.randomDestroySound(), 1, 1, packet.x + 0.5F, packet.y + 1.5F, packet.z + 0.5F);

            }else if(destroyedBlock == ClientBlocks.CACTUS){
                while(destroyedBlock == ClientBlocks.CACTUS){
                    level.setBlock(packet.x, destroyedBlockY, packet.z, ClientBlocks.AIR);
                    player.sendPacket(new S2CPacketBlockUpdate(packet.x, destroyedBlockY, packet.z, ClientBlocks.AIR.getDefaultData()));
                    level.playSound(SoundType.CLOTH.randomDestroySound(), 1, 1, packet.x + 0.5F, destroyedBlockY + 0.5F, packet.z + 0.5F);
                    destroyedBlock = level.getBlock(packet.x, ++destroyedBlockY, packet.z);
                }
            }
        }

        // Process light
        final ChunkS chunk = level.getBlockChunk(packet.x, packet.z);
        final int lx = ChunkBase.clampToLocal(packet.x);
        final int lz = ChunkBase.clampToLocal(packet.z);

        if(block.isLightTranslucent())
            level.getSkyLight().destroyBlockUpdate(chunk, heightLight, lx, packet.y, lz);
        else
            level.getSkyLight().placeBlockUpdate(chunk, oldHeightLight, lx, packet.y, lz);

        if(block.isGlow())
            level.getBlockLight().increase(chunk, lx, packet.y, lz, block.getLightLevel());
        else if(oldBlock.isGlow())
            level.getBlockLight().decrease(chunk, lx, packet.y, lz);

        level.getSkyLight().sendSections(chunk, packet.y);
    }
    
    public void moveAndRotate(C2SPacketMoveAndRot packet){
        player.getPosition().set(packet.position);
        player.getVelocity().set(packet.velocity);
        player.getRotation().set(packet.rotation);

        server.getPlayerList().broadcastPacketLevelExcept(player.getLevel(), new S2CPacketEntityMove(player), player);
    }

    public void move(C2SPacketMove packet){
        player.getPosition().set(packet.position);
        player.getVelocity().set(packet.velocity);

        server.getPlayerList().broadcastPacketLevelExcept(player.getLevel(), new S2CPacketEntityMove(player), player);
    }

    public void rotate(C2SPacketRot packet){
        player.getRotation().set(packet.rotation);

        server.getPlayerList().broadcastPacketLevelExcept(player.getLevel(), new S2CPacketEntityMove(player), player);
    }
    
    public void renderDistance(C2SPacketRenderDistance packet){
        player.setRenderDistance(packet.renderDistance);
    }
    
    public void sneaking(C2SPacketPlayerSneaking packet){
        player.setSneaking(packet.sneaking);
        
        server.getPlayerList().broadcastPacketExcept(new S2CPacketPlayerSneaking(player), player);
    }
    
    public void chatMessage(C2SPacketChatMessage packet){
        String message = packet.message;
        
        if(message.startsWith("/"))
            server.getCommandDispatcher().executeCommand(message.substring(1), commandSource);
        else
            player.sendToChat(new Component().color(TextColor.DARK_GREEN).text("<" + player.getName() + "> ").reset().text(packet.message));
    }

    public void ping(C2SPacketPing packet){
        sendPacket(new S2CPacketPong(packet.timeNanos));
    }
    
}
