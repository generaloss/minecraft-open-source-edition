package minecraftose.server.chunk;

import minecraftose.client.block.Block;
import minecraftose.client.block.BlockProps;
import minecraftose.client.block.Blocks;
import minecraftose.main.block.BlockData;
import minecraftose.main.chunk.LevelChunk;
import minecraftose.main.chunk.storage.ChunkPos;
import minecraftose.main.chunk.storage.Heightmap;
import minecraftose.server.level.ServerLevel;

public class ServerChunk extends LevelChunk{

    public boolean decorated;


    public ServerChunk(ServerLevel level, ChunkPos position){
        super(level, position);
    }
    
    public ServerLevel getLevel(){
        return (ServerLevel) level;
    }

    @Override
    public boolean setBlockData(int lx, int y, int lz, short blockData){
        final boolean result = super.setBlockData(lx, y, lz, blockData);
        if(result){
            final boolean blockPlaced = BlockData.getID(blockData) != Blocks.AIR.getID();
            for(Heightmap heightmap: heightmaps.values())
                heightmap.update(lx, y, lz, blockPlaced);

            return true;
        }

        return false;
    }


    public void setBlockFast(int lx, int y, int lz, Block block){
        super.setBlock(lx, y, lz, block);
    }

    public void setBlockDataFast(int lx, int y, int lz, short data){
        super.setBlockData(lx, y, lz, data);
    }


    @Override
    public boolean setBlock(int lx, int y, int lz, Block block){
        final BlockProps oldBlock = super.getBlockProps(lx, y, lz);

        final boolean result = super.setBlock(lx, y, lz, block);
        if(result){
            final boolean blockPlaced = block != Blocks.AIR;
            for(Heightmap heightmap: heightmaps.values())
                heightmap.update(lx, y, lz, blockPlaced);

            if(block.getState(0).isGlow())
                getLevel().getBlockLight().increase(this, lx, y, lz, block.getState(0).getLightLevel());
            else if(oldBlock.isGlow())
                getLevel().getBlockLight().decrease(this, lx, y, lz);

            return true;
        }

        return false;
    }

}