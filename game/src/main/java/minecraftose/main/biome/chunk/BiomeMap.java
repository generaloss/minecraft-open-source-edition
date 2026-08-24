package minecraftose.main.biome.chunk;

import minecraftose.main.biome.Biome;
import minecraftose.main.chunk.ChunkBase;

import java.util.Arrays;

import static minecraftose.main.chunk.ChunkUtils.*;

public class BiomeMap{

    protected final Biome[] biomes;

    public BiomeMap(){
        this.biomes = new Biome[ChunkBase.AREA];
        Arrays.fill(biomes, Biome.VOID);
    }


    public Biome getBiome(int x, int z){
        return biomes[getIndex(x, z)];
    }

    public void setBiome(int x, int z, Biome biome){
        biomes[getIndex(x, z)] = biome;
    }


    public Biome[] getValues(){
        return biomes;
    }

    public void setValues(Biome[] biomes){
        System.arraycopy(biomes, 0, this.biomes, 0, biomes.length);
    }

}
