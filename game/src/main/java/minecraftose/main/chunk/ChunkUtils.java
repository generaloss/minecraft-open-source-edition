package minecraftose.main.chunk;

import minecraftose.main.chunk.storage.ChunkPos;
import minecraftose.server.level.chunk.ChunkS;
import minecraftose.client.chunk.ChunkC;

public class ChunkUtils{

    // Light
    public static final int MAX_LIGHT_LEVEL = 15;


    public static int getIndex(int x, int y, int z){
        return x + z * ChunkBase.SIZE + y * ChunkBase.AREA;
    }

    public static int getIndex(int x, int z){
        return x + z * ChunkBase.SIZE;
    }

    
    public static ChunkBase getNeighborChunk(ChunkBase chunk, int x, int z){
        return chunk.getLevel().getChunkProvider().getChunk(chunk.getNeighborPos(x, z));
    }
    
    public static ChunkC getNeighborChunk(ChunkC chunk, int x, int z){
        return chunk.getLevel().getChunkProvider().getChunk(new ChunkPos(chunk.getNeighborPos(x, z)));
    }
    
    public static ChunkS getNeighborChunk(ChunkS chunk, int x, int z){
        return chunk.getLevel().getChunkProvider().getChunk(chunk.getNeighborPos(x, z));
    }

}
