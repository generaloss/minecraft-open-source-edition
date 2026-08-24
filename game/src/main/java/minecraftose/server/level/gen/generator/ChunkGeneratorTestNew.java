package minecraftose.server.level.gen.generator;

import generaloss.spatialmath.ExtRandom;
import generaloss.spatialmath.Mathc;
import generaloss.spatialmath.Maths;
import minecraftose.client.block.ClientBlocks;
import minecraftose.main.biome.Biome;
import minecraftose.main.biome.chunk.BiomeMap;
import minecraftose.main.chunk.ChunkBase;
import minecraftose.main.chunk.storage.Heightmap;
import minecraftose.main.chunk.storage.HeightmapType;
import minecraftose.main.registry.Registry;
import minecraftose.server.level.chunk.ChunkS;
import minecraftose.server.level.LevelS;
import minecraftose.server.level.gen.decoration.*;
import minecraftose.server.level.gen.decoration.old.*;
import minecraftose.server.level.gen.generator.testnew.NoiseOctavesAlpha;
import minecraftose.server.level.gen.ChunkGenerator;
import minecraftose.server.level.gen.generator.testnew.BiomeVC;
import minecraftose.server.level.gen.generator.testnew.GenLayerVC;

import java.util.Random;

public class ChunkGeneratorTestNew extends ChunkGenerator{

    public static final ChunkGeneratorTestNew INSTANCE = new ChunkGeneratorTestNew();

    public static int terrainGenHiLevel = 67;
    public static int seaLevel = 128;
    public static final int seed = 1023123436;

    private final ExtRandom random;

    private final GenLayerVC noiseFieldModifier;
    private final NoiseOctavesAlpha noiseGen1;
    private final NoiseOctavesAlpha noiseGen2;
    private final NoiseOctavesAlpha noiseGen3;
    private final NoiseOctavesAlpha noiseGen4;
    private final NoiseOctavesAlpha noiseGen5;
    private final NoiseOctavesAlpha noiseGen6;
    private final Random rand;
    private double[] noiseArray;
    private double[] noise3;
    private double[] noise1;
    private double[] noise2;
    private double[] noise5;
    private double[] noise6;
    private int[] noiseFieldModifierArray;
    private int[] noiseFieldModifierArray2;
    private float[] parabolicField;
    private BiomeVC[] largerBiomeMap;

    public ChunkGeneratorTestNew(){
        this.rand = new Random(seed);
        this.noiseGen1 = new NoiseOctavesAlpha(this.rand, 2);
        this.noiseGen2 = new NoiseOctavesAlpha(this.rand, 16);
        this.noiseGen3 = new NoiseOctavesAlpha(this.rand, 8);
        this.noiseGen4 = new NoiseOctavesAlpha(this.rand, 4);
        this.noiseGen5 = new NoiseOctavesAlpha(this.rand, 2);
        this.noiseGen6 = new NoiseOctavesAlpha(this.rand, 1);

        this.noiseFieldModifier = GenLayerVC.genNoiseFieldModifier(seed, -80);

        this.random = new ExtRandom();
        random.setSeed(seed);
    }

    @Override
    public void generate(ChunkS chunk){
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();
        LevelS level = chunk.getLevel();

        final int biomeID = Maths.random(0, 22);
        this.largerBiomeMap = new BiomeVC[100];
        for(int i = 0; i < largerBiomeMap.length; i++)
            largerBiomeMap[i] = new BiomeVC(biomeID);

        final byte biomeID2 = (byte) (biomeID % 12);
        for(int i = 0; i < 16; i++)
            for(int j = 0; j < 16; j++)
                chunk.getBiomes().setBiome(i, j, Registry.biome.get(biomeID2));

        byte horizontalPart = 4;
        byte verticalPart = 20;

        int xSize = horizontalPart + 1;
        int ySize = verticalPart + 1;
        int zSize = horizontalPart + 1;

        // largerBiomeMap = world.getWorldChunkProvider().getBiomesForGeneration(largerBiomeMap, chunkX * 4 - 2, chunkZ * 4 - 2, xSize + 5, zSize + 5);

        this.noiseArray = this.initializeNoiseFieldHigh(this.noiseArray, chunkX * horizontalPart, 0, chunkZ * horizontalPart, xSize, ySize, zSize);

        double yLerp = 0.125D;
        double xLerp = 0.25D;
        double zLerp = 0.25D;

        for(int x = 0; x < horizontalPart; ++x){
            for(int z = 0; z < horizontalPart; ++z){
                for(int y = 0; y < verticalPart; ++y){

                    // Interpolation of 2x2x2 into 4x8x4

                    double lower_lefttop = this.noiseArray[((x) * zSize + z) * ySize + y];
                    double lower_leftbottom = this.noiseArray[((x) * zSize + z + 1) * ySize + y];
                    double lower_righttop = this.noiseArray[((x + 1) * zSize + z) * ySize + y];
                    double lower_rightbottom = this.noiseArray[((x + 1) * zSize + z + 1) * ySize + y];

                    double dy_lefttop = (this.noiseArray[((x) * zSize + z) * ySize + y + 1] - lower_lefttop) * yLerp;
                    double dy_leftbottom = (this.noiseArray[((x) * zSize + z + 1) * ySize + y + 1] - lower_leftbottom) * yLerp;
                    double dy_righttop = (this.noiseArray[((x + 1) * zSize + z) * ySize + y + 1] - lower_righttop) * yLerp;
                    double dy_rightbottom = (this.noiseArray[((x + 1) * zSize + z + 1) * ySize + y + 1] - lower_rightbottom) * yLerp;

                    for(int dy = 0; dy < 8; ++dy){


                        double topCounting = lower_lefttop;
                        double bottomCounting = lower_leftbottom;

                        double noisetopdx = (lower_righttop - lower_lefttop) * xLerp;
                        double noisedowndx = (lower_rightbottom - lower_leftbottom) * xLerp;

                        for(int dx = 0; dx < 4; ++dx){

                            double var49 = (bottomCounting - topCounting) * zLerp;
                            double var47 = topCounting - var49;

                            for(int dz = 0; dz < 4; ++dz){

                                if((var47 += var49) > 0.0D)
                                    chunk.setBlock(4 * x + dx, 8 * y + dy + terrainGenHiLevel, 4 * z + dz, ClientBlocks.STONE);
                                else if(y * 8 + dy + terrainGenHiLevel < seaLevel)
                                    chunk.setBlock(4 * x + dx, 8 * y + dy + terrainGenHiLevel, 4 * z + dz, ClientBlocks.WATER);
                                else
                                    chunk.setBlock(4 * x + dx, 8 * y + dy + terrainGenHiLevel, 4 * z + dz, ClientBlocks.AIR);


                            }

                            topCounting += noisetopdx;
                            bottomCounting += noisedowndx;
                        }

                        lower_lefttop += dy_lefttop;
                        lower_leftbottom += dy_leftbottom;
                        lower_righttop += dy_righttop;
                        lower_rightbottom += dy_rightbottom;
                    }
                }
            }
        }

        for(int x = 0; x < 16; x++){
            for(int z = 0; z < 16; z++){
                for(int y = terrainGenHiLevel; y > 0; y--){
                    if(chunk.getBlock(x, y, z) == ClientBlocks.AIR){
                        chunk.setBlock(x, y, z, ClientBlocks.STONE);
                    }
                }
            }
        }

        final Heightmap heightmapSurface = chunk.getHeightMap(HeightmapType.SURFACE);
        final BiomeMap biomes = chunk.getBiomes();

        for(int lx = 0; lx < 16; lx++){
            for(int lz = 0; lz < 16; lz++){
                final int height = heightmapSurface.getHeight(lx, lz);

                if(height <= seaLevel)
                    for(int ly = height + 1; ly <= seaLevel; ly++)
                        chunk.setBlock(lx, ly, lz, ClientBlocks.WATER);
                if(height < seaLevel)
                    continue;

                final Biome biome = biomes.getBiome(lx, lz);

                chunk.setBlock(lx, height, lz, biome.topBlock);
                for(int i = 1; i < random.nextInt(5 - 3) + 3; i++)
                    chunk.setBlock(lx, height - i, lz, biome.fillerBlock);
            }
        }
    }

    private double[] initializeNoiseFieldHigh(double[] outArray, int xPos, int yPos, int zPos, int xSize, int ySize, int zSize){
        int smoothingRadius = 2;

        noiseFieldModifierArray = noiseFieldModifier.getInts(xPos, zPos, xSize, zSize);
        //noiseFieldModifierArray2 = noiseFieldModifier2.getInts(xPos, zPos, xSize, zSize);

        if(outArray == null){
            outArray = new double[xSize * ySize * zSize];
        }

        if(this.parabolicField == null){
            this.parabolicField = new float[2 * smoothingRadius + 5 * 2 * smoothingRadius + 1];
            for(int xR = -smoothingRadius; xR <= smoothingRadius; ++xR){
                for(int zR = -smoothingRadius; zR <= smoothingRadius; ++zR){
                    float var10 = 10.0F / Mathc.sqrt(xR * xR + zR * zR + 0.2F);
                    this.parabolicField[xR + smoothingRadius + (zR + smoothingRadius) * 5] = var10;
                }
            }
        }

        double horizontalScale = 1300D;
        double verticalScale = 1000D;

        //this.noise5 = this.noiseGen5.generateNoiseOctaves(this.noise5, xPos, zPos, xSize, zSize, 1.121D, 1.121D, 0.5D);
        this.noise6 = this.noiseGen6.generateNoiseOctaves(this.noise6, xPos, zPos, xSize, zSize, 800.0D, 800.0D, 0.5D);

        // Seems to be the lowest octave
        this.noise1 = this.noiseGen1.generateNoiseOctaves(this.noise1, xPos, yPos, zPos, xSize, ySize, zSize, horizontalScale / 8000D, verticalScale / 10D, horizontalScale / 8000D);

        this.noise2 = this.noiseGen2.generateNoiseOctaves(this.noise2, xPos, yPos, zPos, xSize, ySize, zSize, horizontalScale, verticalScale, horizontalScale);

        // Seems to be a high or highest octave
        this.noise3 = this.noiseGen3.generateNoiseOctaves(this.noise3, xPos, yPos, zPos, xSize, ySize, zSize, horizontalScale / 60.0D, verticalScale / 120.0D, horizontalScale / 60.0D);


        boolean var43 = false;
        boolean var42 = false;
        int posIndex = 0;
        int counter = 0;

        for(int x = 0; x < xSize; ++x){
            for(int z = 0; z < zSize; ++z){

                float noisemodfactor = Math.max(0.1f, noiseFieldModifierArray[x + z * xSize] / 255f);
                float noisemoddiff = 1 - noisemodfactor;
                float maxBlendedHeight = 0.0F;
                float minBlendedHeight = 0.0F;
                float blendedHeightSum = 0.0F;
                BiomeVC baseBiome = largerBiomeMap[x + smoothingRadius + (z + smoothingRadius) * (xSize + 5)];

                for(int xR = -smoothingRadius; xR <= smoothingRadius; ++xR){
                    for(int zR = -smoothingRadius; zR <= smoothingRadius; ++zR){

                        System.out.println("index: " + x + xR + smoothingRadius + (z + zR + smoothingRadius) * (xSize + 5));
                        BiomeVC blendBiome = largerBiomeMap[x + xR + smoothingRadius + (z + zR + smoothingRadius) * (xSize + 5)];
                        float blendedHeight = this.parabolicField[xR + smoothingRadius + (zR + smoothingRadius) * 5] / 2.0F;
                        if(blendBiome.minHeight > baseBiome.minHeight)
                            blendedHeight *= 0.5F;

                        maxBlendedHeight += (blendBiome.maxHeight - noisemoddiff) * blendedHeight;
                        minBlendedHeight += (blendBiome.minHeight - noisemoddiff) * blendedHeight * noisemodfactor;
                        blendedHeightSum += blendedHeight;
                    }
                }

                maxBlendedHeight /= blendedHeightSum;
                minBlendedHeight /= blendedHeightSum;
                maxBlendedHeight = maxBlendedHeight * 0.9F + 0.1F;
                minBlendedHeight = (minBlendedHeight * 4.0F - 1.0F) / 8.0F;


                maxBlendedHeight /= 10;


                double noise6var = this.noise6[counter] / 8000.0D;

                if(noise6var < 0.0D)
                    noise6var = -noise6var * 0.3D;
                noise6var = noise6var * 3.0D - 2.0D;

                if(noise6var < 0.0D){
                    noise6var /= 2.0D;
                    if(noise6var < -1.0D)
                        noise6var = -1.0D;
                    noise6var /= 1.4D;
                    noise6var /= 2.0D;
                }else{
                    if(noise6var > 1.0D)
                        noise6var = 1.0D;
                    noise6var /= 8.0D;
                }

                ++counter;
                for(int y = 0; y < ySize; ++y){
                    double minblendhgvar = minBlendedHeight;
                    minblendhgvar += noise6var * 0.2D;
                    minblendhgvar = minblendhgvar * ySize / 16.0D;

                    double adjustedminhg = ySize / 2.0D + minblendhgvar * 4.0D;
                    double result;
                    double theheight = (y - adjustedminhg) * 12.0D / (2.70 + (double) maxBlendedHeight);   // * 256.0D / 256.0D

                    if(theheight < 0.0D){
                        theheight *= 4.0D;
                    }

					/*double noise1var = this.noise1[posIndex] / 512.0D;
					double noise2var = this.noise2[posIndex] / 512.0D;
					double noise3var = (this.noise3[posIndex] / 10.0D + 1.0D) / 2.0D;

					if (noise3var < 0.0D) {
						result = noise1var;
					} else if (noise3var > 1.0D) {
						result = noise2var;
					} else {
						result = noise1var + (noise2var - noise1var) * noise3var;
					}*/

                    result = 2D * noise1[posIndex] + (noise2[posIndex] / 512D + (this.noise3[posIndex] / 10.0D + 1.0D) / 8.0D) * noisemodfactor;
                    //result = noise1var;

                    result -= theheight;

                    if(y > ySize - 4){
                        double var40 = (y - (ySize - 4)) / 3.0F;
                        result = result * (1.0D - var40) + -10.0D * var40;
                    }

                    outArray[posIndex] = result;
                    ++posIndex;


                }

            }
        }

        return outArray;
    }

    @Override
    public void decorate(ChunkS chunk){
        final LevelS level = chunk.getLevel();

        final int seed = chunk.getLevel().getConfiguration().getSeed();
        final int baseX = chunk.pos().x * ChunkBase.SIZE;
        final int baseZ = chunk.pos().z * ChunkBase.SIZE;

        // final Heightmap heightmapUnderwaterSurface = chunk.getHeightMap(HeightmapType.UNDERWATER_SURFACE);
        final Heightmap heightmapSurface = chunk.getHeightMap(HeightmapType.SURFACE);
        final BiomeMap biomes = chunk.getBiomes();

        /* STRUCTURES */

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
    }

    @Override
    public byte getID(){
        return 3;
    }

}
