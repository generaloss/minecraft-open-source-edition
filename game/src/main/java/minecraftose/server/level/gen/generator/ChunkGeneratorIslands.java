package minecraftose.server.level.gen.generator;

import generaloss.spatialmath.FastNoise;
import generaloss.spatialmath.Maths;
import minecraftose.client.block.ClientBlocks;
import minecraftose.main.chunk.ChunkBase;
import minecraftose.main.chunk.storage.HeightmapType;
import minecraftose.server.level.chunk.ChunkS;
import minecraftose.server.level.gen.ChunkGenerator;

public class ChunkGeneratorIslands extends ChunkGenerator{

    public static ChunkGeneratorIslands INSTANCE = new ChunkGeneratorIslands();
    
    private final FastNoise continentalnessNoise, erosionNoise, peaksValleysNoise, temperatureNoise, humidityNoise;
    private final FastNoise noiseLight = new FastNoise();
    
    private ChunkGeneratorIslands(){
        continentalnessNoise = new FastNoise();
        erosionNoise = new FastNoise();
        peaksValleysNoise = new FastNoise();
        temperatureNoise = new FastNoise();
        humidityNoise = new FastNoise();
        
        continentalnessNoise.setFrequency(0.002F);
        continentalnessNoise.setFractalType(FastNoise.FractalType.FBM);
        continentalnessNoise.setFractalOctaves(7);
        
        erosionNoise.setFrequency(0.002F);
        erosionNoise.setFractalType(FastNoise.FractalType.FBM);
        erosionNoise.setFractalOctaves(5);
        
        noiseLight.setFrequency(0.03F);
    }
    
    @Override
    public void generate(ChunkS chunk){
        final int baseX = ChunkBase.SIZE * chunk.pos().x;
        final int baseZ = ChunkBase.SIZE * chunk.pos().z;
        
        final int seed = chunk.getLevel().getConfiguration().getSeed();
        
        continentalnessNoise.setSeed(seed);
        erosionNoise.setSeed(seed);
        peaksValleysNoise.setSeed(seed);
        temperatureNoise.setSeed(seed);
        humidityNoise.setSeed(seed);
        
        // Stopwatch timer = new Stopwatch().start();
        for(int lx = 0; lx < ChunkBase.SIZE; lx++){
            final int x = lx + baseX;
            for(int lz = 0; lz < ChunkBase.SIZE; lz++){
                final int z = lz + baseZ;
                
                final float erosion = (erosionNoise.get(x, z) + 1) * 0.5F;
                
                final float density = 0.1F / erosion;
                int height = Maths.round(continentalnessNoise.get(x, z) * 16 + 128);
                for(int y = height; y < ChunkBase.HEIGHT; y++){
                    
                    float continentalness3D = (continentalnessNoise.get(x, y, z) + 1) * 0.5F;
                    if(continentalness3D < ((float) y / (ChunkBase.HEIGHT - height)) * density)
                        chunk.setBlockFast(lx, y, lz, ClientBlocks.STONE);
                }
            }
        }
        
        chunk.getHeightMap(HeightmapType.SURFACE).update();
        
        for(int lx = 0; lx < ChunkBase.SIZE; lx++){
            for(int lz = 0; lz < ChunkBase.SIZE; lz++){
                
                int height = chunk.getHeightMap(HeightmapType.SURFACE).getHeight(lx, lz);
                if(height > 60)
                    chunk.setBlockFast(lx, height, lz, ClientBlocks.GRASS_BLOCK);
            }
        }

        // System.out.println("Gen: " + timer.getMillis());
    }

    @Override
    public byte getID(){
        return 2;
    }
    
}
