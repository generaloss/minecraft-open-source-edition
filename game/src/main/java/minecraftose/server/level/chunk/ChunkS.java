package minecraftose.server.level.chunk;

import minecraftose.client.block.ClientBlock;
import minecraftose.client.block.BlockProps;
import minecraftose.client.block.ClientBlocks;
import minecraftose.main.block.ChunkBlockData;
import minecraftose.main.chunk.ChunkBase;
import minecraftose.main.chunk.storage.ChunkPos;
import minecraftose.main.chunk.storage.Heightmap;
import minecraftose.server.level.LevelS;

public class ChunkS extends ChunkBase{

    public boolean decorated;


    public ChunkS(LevelS level, ChunkPos position){
        super(level, position);
    }
    
    public LevelS getLevel(){
        return (LevelS) level;
    }

    @Override
    public boolean setBlockData(int lx, int y, int lz, short blockData){
        final boolean result = super.setBlockData(lx, y, lz, blockData);
        if(result){
            final boolean blockPlaced = ChunkBlockData.getID(blockData) != ClientBlocks.AIR.getID();
            for(Heightmap heightmap: heightmaps.values())
                heightmap.update(lx, y, lz, blockPlaced);

            return true;
        }

        return false;
    }


    public void setBlockFast(int lx, int y, int lz, ClientBlock block){
        super.setBlock(lx, y, lz, block);
    }

    public void setBlockDataFast(int lx, int y, int lz, short data){
        super.setBlockData(lx, y, lz, data);
    }


    @Override
    public boolean setBlock(int lx, int y, int lz, ClientBlock block){
        final BlockProps oldBlock = super.getBlockProps(lx, y, lz);

        final boolean result = super.setBlock(lx, y, lz, block);
        if(result){
            final boolean blockPlaced = block != ClientBlocks.AIR;
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

    public boolean setBlockTest(int lx, int y, int lz, ClientBlock block){
        final BlockProps oldBlock = super.getBlockProps(lx, y, lz);

        final short blockData = block.getDefaultData();
        final boolean result = super.setBlockData(lx, y, lz, blockData);
        if(result){
            final boolean blockPlaced = block != ClientBlocks.AIR;
            for(Heightmap heightmap: heightmaps.values())
                heightmap.update(lx, y, lz, blockPlaced);

            if(block.getState(0).isGlow())
                getLevel().getBlockLight().increase(this, lx, y, lz, block.getState(0).getLightLevel());
            else if(oldBlock.isGlow())
                getLevel().getBlockLight().decrease(this, lx, y, lz);

            return true;
        }else{
            System.out.println("bret");
        }

        return false;
    }

}
