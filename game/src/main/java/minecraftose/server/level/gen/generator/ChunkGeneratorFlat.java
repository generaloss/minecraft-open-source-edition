package minecraftose.server.level.gen.generator;

import generaloss.spatialmath.ExtRandom;
import minecraftose.client.block.ClientBlocks;
import minecraftose.main.chunk.ChunkBase;
import minecraftose.server.level.chunk.ChunkS;
import minecraftose.server.level.LevelS;
import minecraftose.server.level.gen.ChunkGenerator;
import minecraftose.server.level.gen.decoration.*;
import minecraftose.server.level.gen.decoration.old.*;

import java.util.Objects;

public class ChunkGeneratorFlat extends ChunkGenerator{

    public static ChunkGeneratorFlat INSTANCE = new ChunkGeneratorFlat();

    private final ExtRandom random;

    public ChunkGeneratorFlat(){
        this.random = new ExtRandom();
    }

    @Override
    public void generate(ChunkS chunk){
        for(int i = 0; i < ChunkBase.SIZE; i++)
            for(int j = 0; j < ChunkBase.SIZE; j++)
                for(int y = 0; y < 5; y++){
                    if(y == 0)
                        chunk.setBlockFast(i, y, j, ClientBlocks.STONE);
                    else if(y == 4)
                        chunk.setBlockFast(i, y, j, ClientBlocks.GRASS_BLOCK);
                    else
                        chunk.setBlockFast(i, y, j, ClientBlocks.DIRT);
                }
    }

    @Override
    public void decorate(ChunkS chunk){
        final LevelS level = chunk.getLevel();

        final int seed = chunk.getLevel().getConfiguration().getSeed();
        final int baseX = chunk.pos().x * ChunkBase.SIZE;
        final int baseZ = chunk.pos().z * ChunkBase.SIZE;

        random.setSeed(Objects.hash(seed, baseX, baseZ));

        if(random.nextBoolean(0.01)){
            final int x = random.nextInt(15) + baseX;
            final int z = random.nextInt(15) + baseZ;

            switch(random.nextInt(10)){
                case 1 -> DesertPyramid.generate(level, x, 3, z, random);
                case 2 -> MiniPyramid.generate(level, x, 4, z, random);
                case 3 -> Tower.generate(level, x, 4, z, random);
                case 4 -> OldGenOakTree.generate(level, random, x, 4, z);
                case 5 -> OldGenBirchTree.generate(level, random, x, 4, z);
                case 6 -> OldGenTaigaTree1.generate(level, random, x, 4, z);
                case 7 -> OldGenTaigaTree2.generate(level, random, x, 4, z);
                case 8 -> Cactus.generate(level, x, 5, z, random);
                case 9 -> OldGenDungeons.generate(level, random, x, -2, z);
                default -> House.generate(level, x, 4, z, random);
            }
        }
    }

    @Override
    public byte getID(){
        return 1;
    }

}