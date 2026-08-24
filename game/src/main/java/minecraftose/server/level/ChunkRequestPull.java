package minecraftose.server.level;

import minecraftose.main.network.packet.s2c.game.S2CPacketChunk;
import minecraftose.server.level.chunk.ChunkS;
import minecraftose.server.player.ServerPlayer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkRequestPull{

    private final ChunkProviderS provider;
    private final Map<Long, ServerPlayer> required;

    public ChunkRequestPull(ChunkProviderS provider){
        this.provider = provider;
        this.required = new ConcurrentHashMap<>();
    }

    public void sendIfRequired(ChunkS chunk){
        final long packedChunkPos = chunk.pos().pack();
        if(!required.containsKey(packedChunkPos))
            return;

        final ServerPlayer player = required.remove(packedChunkPos);
        player.sendPacket(new S2CPacketChunk(chunk));
    }

    public void require(ServerPlayer player, long packedChunkPos){
        if(provider.hasChunk(packedChunkPos)){
            final ChunkS chunk = provider.getChunk(packedChunkPos);
            if(chunk != null){
                player.sendPacket(new S2CPacketChunk(chunk));
                return;
            }
        }
        required.putIfAbsent(packedChunkPos, player);
    }

}
