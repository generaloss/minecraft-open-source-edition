package minecraftose.server.level.gen.generator;

import generaloss.spatialmath.ExtRandom;
import generaloss.spatialmath.FastNoise;
import generaloss.spatialmath.Mathc;
import generaloss.spatialmath.Maths;
import minecraftose.client.block.ClientBlock;
import minecraftose.client.block.ClientBlocks;
import minecraftose.main.biome.Biome;
import minecraftose.main.biome.chunk.BiomeMap;
import minecraftose.main.chunk.ChunkBase;
import minecraftose.main.chunk.storage.Heightmap;
import minecraftose.main.chunk.storage.HeightmapType;
import minecraftose.server.level.chunk.ChunkS;
import minecraftose.server.level.LevelS;
import minecraftose.server.level.gen.ChunkGenerator;
import minecraftose.server.level.gen.decoration.*;
import minecraftose.server.level.gen.decoration.old.*;

import java.util.Objects;

public class ChunkGeneratorDefault extends ChunkGenerator{

    public static ChunkGeneratorDefault INSTANCE = new ChunkGeneratorDefault();

    private static final int SEA_LEVEL = 64;
    private static final float RIVER_MIN = 0.485F;
    private static final float RIVER_MAX = 0.505F;

    private final FastNoise continentalnessNoise, erosionNoise, peaksValleysNoise, temperatureNoise, humidityNoise, riverNoise;
    private final ExtRandom random;

    public ChunkGeneratorDefault(){
        this.continentalnessNoise = new FastNoise();
        this.erosionNoise = new FastNoise();
        this.peaksValleysNoise = new FastNoise();
        this.temperatureNoise = new FastNoise();
        this.humidityNoise = new FastNoise();
        this.riverNoise = new FastNoise();

        this.continentalnessNoise.setFractalType(FastNoise.FractalType.FBM);
        this.continentalnessNoise.setFrequency(0.001F); // FIX
        this.continentalnessNoise.setFractalOctaves(7);

        this.humidityNoise.setFractalType(FastNoise.FractalType.FBM);
        this.humidityNoise.setFrequency(0.0004F); // FIX
        this.humidityNoise.setFractalOctaves(7);

        this.erosionNoise.setFractalType(FastNoise.FractalType.FBM);
        this.erosionNoise.setFrequency(0.004F); // FIX
        this.erosionNoise.setFractalOctaves(7);

        this.peaksValleysNoise.setFractalType(FastNoise.FractalType.FBM);
        this.peaksValleysNoise.setFrequency(0.02F);
        this.peaksValleysNoise.setFractalOctaves(7);

        this.temperatureNoise.setFractalType(FastNoise.FractalType.FBM);
        this.temperatureNoise.setFrequency(0.0008F); // FIX
        this.temperatureNoise.setFractalOctaves(7);

        this.riverNoise.setFractalType(FastNoise.FractalType.FBM);
        this.riverNoise.setFrequency(0.0009F); // FIX
        this.riverNoise.setFractalOctaves(7);

        this.random = new ExtRandom();
    }


    @Override
    public void generate(ChunkS chunk){
        final int seed = chunk.getLevel().getConfiguration().getSeed();
        final int baseX = chunk.pos().x * ChunkBase.SIZE;
        final int baseZ = chunk.pos().z * ChunkBase.SIZE;

        continentalnessNoise.setSeed(seed);
        erosionNoise.setSeed(seed);
        peaksValleysNoise.setSeed(seed);
        temperatureNoise.setSeed(seed);
        humidityNoise.setSeed(seed);
        riverNoise.setSeed(seed);
        random.setSeed(Objects.hash(seed, baseX, baseZ));

        final Heightmap heightmapUnderwaterSurface = chunk.getHeightMap(HeightmapType.UNDERWATER_SURFACE);
        final Heightmap heightmapSurface = chunk.getHeightMap(HeightmapType.SURFACE);
        final BiomeMap biomes = chunk.getBiomes();

        /* GENERATE SURFACE */

        for(int lx = 0; lx < ChunkBase.SIZE; lx++){
            final int x = lx + baseX;
            for(int lz = 0; lz < ChunkBase.SIZE; lz++){
                // Calculate base height
                final int z = lz + baseZ;
                final float continentalness = Mathc.pow((continentalnessNoise.get(x, z) - 0.15), 3);
                float height = SEA_LEVEL + continentalness * 32;

                // Detail base height
                float detail = 0;
                for(int y = 0; y < 24; y++)
                    detail += continentalnessNoise.get(x, height + y, z) / 32;

                height += detail * 32;

                // Detail base height 2
                detail = 0;
                for(int y = 0; y < 24; y++)
                    detail += erosionNoise.get(x, height + y, z) / 32;

                height += detail * 64;

                // Search rivers around and smooth
                if(height > SEA_LEVEL){
                    float river = riverNoise.get(x - 64, z - 64) / 2 + 0.5F;
                    if(river > RIVER_MIN && river < RIVER_MAX){

                        final float range = RIVER_MAX - RIVER_MIN;
                        final float cp = (RIVER_MAX + RIVER_MIN) / 2;
                        final float t = Math.abs(river - cp) / range * 2;
                        height = Maths.lerp(SEA_LEVEL - 2, SEA_LEVEL, t);

                    }else{

                        final int transitionRadius = 7;

                        float minRiverDistance = 100;
                        for(int i = lx - transitionRadius; i <= lx + transitionRadius; i++){
                            for(int j = lz - transitionRadius; j <= lz + transitionRadius; j++){

                                river = riverNoise.get(i + baseX - 64, j + baseZ - 64) / 2 + 0.5F;
                                if(river > RIVER_MIN && river < RIVER_MAX){

                                    final float distance = Mathc.sqrt((i - lx) * (i - lx) + (j - lz) * (j - lz));
                                    minRiverDistance = Math.min(minRiverDistance, distance);
                                }
                            }
                        }
                        if(minRiverDistance != 100){
                            final float min = Math.min(SEA_LEVEL, height);
                            final float max = Math.max(SEA_LEVEL, height);
                            final float t = Maths.quintic(Math.min(1, minRiverDistance / transitionRadius));
                            height = Maths.lerp(min, max, t);
                        }
                    }
                }

                // Fill stone blocks
                for(int y = 0; y <= height; y++)
                    chunk.setBlockFast(lx, y, lz, ClientBlocks.STONE);
            }
        }

        heightmapSurface.update();
        heightmapUnderwaterSurface.updateFrom(heightmapSurface);

        /* BIOMES */

        for(int lx = 0; lx < ChunkBase.SIZE; lx++){
            final int x = lx + baseX;
            for(int lz = 0; lz < ChunkBase.SIZE; lz++){
                final int z = lz + baseZ;

                final float temperature = temperatureNoise.get(x, z) / 2 + 0.5F;
                final float height = heightmapSurface.getHeight(lx, lz);
                final float humidity = humidityNoise.get(x, z) / 2 + 0.5F;
                final float river = riverNoise.get(x - 64, z - 64) / 2 + 0.5F;

                final Biome biome;
                if(river > RIVER_MIN && river < RIVER_MAX){ // RIVER
                    if(temperature < 0.3)
                        biome = Biome.ICE_RIVER;
                    else
                        biome = Biome.RIVER;

                }else if(height < SEA_LEVEL){ // OCEAN
                    if(temperature < 0.3)
                        biome = Biome.ICE_SEA;
                    else
                        biome = Biome.SEA;

                }else if(height <= SEA_LEVEL + 7 * peaksValleysNoise.get(x, z)){ // BEACH
                    if(temperature < 0.3)
                        biome = Biome.SNOWY_BEACH;
                    else
                        biome = Biome.BEACH;

                }else{ // SURFACE
                    if(temperature < 0.4){
                        if(humidity < 0.8)
                            biome = Biome.FOREST;
                        else if(humidity < 0.5)
                            biome = Biome.TAIGA;
                        else// if(humidity < 1)
                            biome = Biome.SNOWY_TAIGA;

                    }else if(temperature < 0.7){
                        if(humidity < 0.1)
                            biome = Biome.SAVANNA;
                        else if(humidity < 0.65)
                            biome = Biome.FOREST;
                        else// if(humidity < 1)
                            biome = Biome.TAIGA;

                    }else{// if(temperature < 1)
                        if(humidity < 0.7)
                            biome = Biome.DESERT;
                        else if(humidity < 0.4)
                            biome = Biome.WINDSWEPT_HILLS;
                        else// if(humidity < 1)
                            biome = Biome.FOREST;
                    }
                }

                biomes.setBiome(lx, lz, biome);
            }
        }

        /* SURFACE */

        for(int lx = 0; lx < ChunkBase.SIZE; lx++){
            final int x = lx + baseX;
            for(int lz = 0; lz < ChunkBase.SIZE; lz++){
                final int z = lz + baseZ;

                final int height = heightmapSurface.getHeight(lx, lz);
                final Biome biome = biomes.getBiome(lx, lz);

                if(chunk.getBlock(lx, height, lz) == ClientBlocks.WATER)
                    continue;

                // SELECT SURFACE BLOCKS
                final ClientBlock topBlock = biome.topBlock;
                final ClientBlock fillerBlock = biome.fillerBlock;

                // SET SURFACE BLOCKS
                chunk.setBlockFast(lx, height, lz, topBlock);

                final int subsurfaceLayerHeight = random.nextInt(5 - 2) + 2;
                for(int y = height - subsurfaceLayerHeight; y < height; y++)
                    chunk.setBlockFast(lx, y, lz, fillerBlock);

                // WATER, ICE
                if(biome == Biome.RIVER || biome == Biome.SEA){
                    for(int y = height; y <= SEA_LEVEL; y++)
                        if(chunk.getBlock(lx, y, lz) == ClientBlocks.AIR)
                            chunk.setBlockFast(lx, y, lz, ClientBlocks.WATER);

                }else if(biome == Biome.ICE_RIVER || biome == Biome.ICE_SEA){
                    for(int y = height; y <= SEA_LEVEL; y++)
                        if(chunk.getBlock(lx, y, lz) == ClientBlocks.AIR)
                            chunk.setBlockFast(lx, y, lz, ClientBlocks.ICE);
                }

            }
        }

        /* GRASS */

        for(int lx = 0; lx < ChunkBase.SIZE; lx++){
            for(int lz = 0; lz < ChunkBase.SIZE; lz++){
                final int height = heightmapSurface.getHeight(lx, lz);
                final Biome biome = biomes.getBiome(lx, lz);

                if(biome == Biome.FOREST){
                    if(random.nextBoolean(0.05))
                        chunk.setBlockFast(lx, height + 1, lz, ClientBlocks.GRASS);
                }else if(biome == Biome.TAIGA || biome == Biome.SNOWY_TAIGA){
                    if(random.nextBoolean(0.005))
                        chunk.setBlockFast(lx, height + 1, lz, ClientBlocks.GRASS);
                }

            }
        }

    }

    @Override
    public void decorate(ChunkS chunk){
        final LevelS level = chunk.getLevel();

        final int seed = level.getConfiguration().getSeed();
        final int baseX = chunk.pos().x * ChunkBase.SIZE;
        final int baseZ = chunk.pos().z * ChunkBase.SIZE;

        continentalnessNoise.setSeed(seed);
        erosionNoise.setSeed(seed);
        peaksValleysNoise.setSeed(seed);
        temperatureNoise.setSeed(seed);
        humidityNoise.setSeed(seed);
        riverNoise.setSeed(seed);
        random.setSeed(Objects.hash(seed, baseX, baseZ));

        // final Heightmap heightmapUnderwaterSurface = chunk.getHeightMap(HeightmapType.UNDERWATER_SURFACE);
        final Heightmap heightmapSurface = chunk.getHeightMap(HeightmapType.SURFACE);
        final BiomeMap biomes = chunk.getBiomes();

        // STRUCTURES

        for(int lx = 0; lx < ChunkBase.SIZE; lx++){
            final int x = lx + baseX;
            boolean prevCactusGen = false;

            for(int lz = 0; lz < ChunkBase.SIZE; lz++){
                final int z = lz + baseZ;

                final int height = heightmapSurface.getHeight(lx, lz);
                final Biome biome = biomes.getBiome(lx, lz);

                if(biome == Biome.DESERT){

                    if(random.nextBoolean(0.008) && !prevCactusGen){
                        Cactus.generate(level, x, height + 1, z, random);
                        prevCactusGen = true;
                    }else if(random.nextBoolean(0.00001))
                        DesertPyramid.generate(level, x, height - 1, z, random);
                    else if(random.nextBoolean(0.00005))
                        MiniPyramid.generate(level, x, height, z, random);

                }else if(biome == Biome.FOREST){

                    if(random.nextBoolean(0.03)){
                        if(random.nextBoolean(0.7))
                            OldGenOakTree.generate(level, random, x, height + 1, z);
                        else
                            OldGenBirchTree.generate(level, random, x, height + 1, z);
                    }else if(random.nextBoolean(0.00002))
                        House.generate(level, x, height, z, random);

                }else if(biome == Biome.TAIGA){

                    if(random.nextBoolean(0.02))
                        OldGenTaigaTree2.generate(level, random, x, height + 1, z);
                    else if(random.nextBoolean(0.02))
                        OldGenTaigaTree1.generate(level, random, x, height + 1, z);

                    else if(random.nextBoolean(0.00005))
                        Tower.generate(level, x, height, z, random);

                }else if(biome == Biome.SNOWY_TAIGA){

                    if(random.nextBoolean(0.02))
                        OldGenTaigaTree2.generate(level, random, x, height + 1, z);
                    else if(random.nextBoolean(0.02))
                        OldGenTaigaTree1.generate(level, random, x, height + 1, z);

                    else if(random.nextBoolean(0.00008))
                        Tower.generate(level, x, height, z, random);

                }

                if(random.nextBoolean(0.00001))
                    OldGenDungeons.generate(level, random, x, height - 5, z);

            }
        }

        ClientBlock block = ((chunk.getX() + chunk.getZ()) % 2 == 0) ? ClientBlocks.WATER : ClientBlocks.STONE;
        chunk.setBlock(0                     , ChunkBase.HEIGHT_IDX, 1                     , block);
        chunk.setBlock(1                     , ChunkBase.HEIGHT_IDX, 0                     , block);
        chunk.setBlock(0                     , ChunkBase.HEIGHT_IDX, ChunkBase.SIZE_IDX - 1, block);
        chunk.setBlock(1                     , ChunkBase.HEIGHT_IDX, ChunkBase.SIZE_IDX    , block);
        chunk.setBlock(ChunkBase.SIZE_IDX    , ChunkBase.HEIGHT_IDX, 1                     , block);
        chunk.setBlock(ChunkBase.SIZE_IDX - 1, ChunkBase.HEIGHT_IDX, 0                     , block);
        chunk.setBlock(ChunkBase.SIZE_IDX    , ChunkBase.HEIGHT_IDX, ChunkBase.SIZE_IDX - 1, block);
        chunk.setBlock(ChunkBase.SIZE_IDX - 1, ChunkBase.HEIGHT_IDX, ChunkBase.SIZE_IDX    , block);

        if(!level.getChunkProvider().getChunk(chunk.getNeighborPos( 1,  1)).setBlockTest(0                 , ChunkBase.HEIGHT_IDX, 0                 , block));
        if(!level.getChunkProvider().getChunk(chunk.getNeighborPos(-1,  1)).setBlockTest(ChunkBase.SIZE_IDX, ChunkBase.HEIGHT_IDX, 0                 , block));
        if(!level.getChunkProvider().getChunk(chunk.getNeighborPos( 1, -1)).setBlockTest(0                 , ChunkBase.HEIGHT_IDX, ChunkBase.SIZE_IDX, block));
        if(!level.getChunkProvider().getChunk(chunk.getNeighborPos(-1, -1)).setBlockTest(ChunkBase.SIZE_IDX, ChunkBase.HEIGHT_IDX, ChunkBase.SIZE_IDX, block));
    }

    @Override
    public byte getID(){
        return 0;
    }

}
