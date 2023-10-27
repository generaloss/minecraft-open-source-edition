package minecraftose.server.net;

import jpize.net.tcp.TcpConnection;
import jpize.net.tcp.packet.IPacket;
import jpize.net.tcp.packet.PacketHandler;
import minecraftose.client.block.BlockProps;
import minecraftose.client.block.Blocks;
import minecraftose.main.audio.BlockSoundPack;
import minecraftose.main.block.BlockData;
import minecraftose.main.block.BlockSetType;
import minecraftose.main.chunk.ChunkUtils;
import minecraftose.main.chunk.storage.ChunkPos;
import minecraftose.main.chunk.storage.HeightmapType;
import minecraftose.main.command.source.CommandSourcePlayer;
import minecraftose.main.net.packet.clientbound.CBPacketBlockUpdate;
import minecraftose.main.net.packet.clientbound.CBPacketEntityMove;
import minecraftose.main.net.packet.clientbound.CBPacketPlayerSneaking;
import minecraftose.main.net.packet.clientbound.CBPacketPong;
import minecraftose.main.net.packet.serverbound.*;
import minecraftose.main.text.Component;
import minecraftose.main.text.TextColor;
import minecraftose.server.Server;
import minecraftose.server.chunk.ServerChunk;
import minecraftose.server.level.ServerLevel;
import minecraftose.server.player.ServerPlayer;

public class PlayerGameConnection implements PacketHandler{
    
    private final ServerPlayer player;
    private final Server server;
    private final TcpConnection connection;
    private final CommandSourcePlayer commandSource;
    
    public PlayerGameConnection(ServerPlayer player, TcpConnection connection){
        this.player = player;
        this.server = player.getServer();
        this.connection = connection;
        this.commandSource = new CommandSourcePlayer(player);
    }
    
    
    public ServerPlayer getPlayer(){
        return player;
    }

    public TcpConnection getConnection(){
        return connection;
    }
    
    public void sendPacket(IPacket<?> packet){
        connection.send(packet);
    }
    
    
    public void chunkRequest(SBPacketChunkRequest packet){
        final ServerLevel level = player.getLevel();
        
        level.getChunkManager().requestedChunk(
            player,
            new ChunkPos(packet.chunkX, packet.chunkZ)
        );
    }
    
    public void playerBlockSet(SBPacketPlayerBlockSet packet){
        final ServerLevel level = player.getLevel();
        final BlockProps oldBlock = level.getBlockProps(packet.x, packet.y, packet.z);
        final int oldHeightLight = level.getHeight(HeightmapType.LIGHT_SURFACE, packet.x, packet.z);

        // Set Block on the Server //
        final boolean result = level.setBlockState(packet.x, packet.y, packet.z, packet.blockData);
        if(!result)
            return;
        
        final BlockProps block = BlockData.getProps(packet.blockData);
        final int heightLight = level.getHeight(HeightmapType.LIGHT_SURFACE, packet.x, packet.z);
        final BlockSetType setType = BlockSetType.from(oldBlock.isEmpty(), block.isEmpty());

        // Send Set Block packet //
        server.getPlayerList().broadcastPacketExcept(new CBPacketBlockUpdate(packet.x, packet.y, packet.z, packet.blockData), player);

        // Sound //
        final BlockSoundPack soundPack;
        if(setType.ordinal() > 0)
            soundPack = block.getSoundPack();
        else
            soundPack = oldBlock.getSoundPack();

        if(soundPack != null)
            level.playSound(soundPack.randomSound(setType), 1, 1, packet.x + 0.5F, packet.y + 0.5F, packet.z + 0.5F);

        // Process grass
        if(setType == BlockSetType.DESTROY && level.getBlockProps(packet.x, packet.y + 1, packet.z).getID() == Blocks.GRASS.getID()){
            level.setBlock(packet.x, packet.y + 1, packet.z, Blocks.AIR);
            player.sendPacket(new CBPacketBlockUpdate(packet.x, packet.y + 1, packet.z, Blocks.AIR.getDefaultData()));
            level.playSound(BlockSoundPack.GRASS.randomDestroySound(), 1, 1, packet.x + 0.5F, packet.y + 1.5F, packet.z + 0.5F);
        }

        // Process light
        final ServerChunk chunk = level.getBlockChunk(packet.x, packet.z);
        final int lx = ChunkUtils.getLocalCoord(packet.x);
        final int lz = ChunkUtils.getLocalCoord(packet.z);

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
    
    public void move(SBPacketMove packet){
        player.getPosition().set(packet.position);
        player.getRotation().set(packet.rotation);
        player.getVelocity().set(packet.velocity);
        
        server.getPlayerList().broadcastPacketLevelExcept(player.getLevel(), new CBPacketEntityMove(player), player);
    }
    
    public void renderDistance(SBPacketRenderDistance packet){
        player.setRenderDistance(packet.renderDistance);
    }
    
    public void sneaking(SBPacketPlayerSneaking packet){
        player.setSneaking(packet.sneaking);
        
        server.getPlayerList().broadcastPacketExcept(new CBPacketPlayerSneaking(player), player);
    }
    
    public void chatMessage(SBPacketChatMessage packet){
        String message = packet.message;
        
        if(message.startsWith("/"))
            server.getCommandDispatcher().executeCommand(message.substring(1), commandSource);
        else
            player.sendToChat(new Component().color(TextColor.DARK_GREEN).text("<" + player.getName() + "> ").reset().text(packet.message));
    }

    public void ping(SBPacketPing packet){
        sendPacket(new CBPacketPong(packet.timeNanos));
    }
    
}