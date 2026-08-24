package minecraftose.main.biome;

import jpize.util.color.Color;
import minecraftose.client.block.ClientBlock;
import minecraftose.client.block.ClientBlocks;
import minecraftose.main.registry.Registry;

public class BiomeBuilder{

    private final byte ID;
    private final String name;
    private Color grassColor = new Color(0.44, 0.67, 0.28, 1);
    private Color waterColor = new Color(0, 0.2, 0.9, 1);
    private ClientBlock topBlock = ClientBlocks.GRASS_BLOCK;
    private ClientBlock fillerBlock = ClientBlocks.DIRT;


    public BiomeBuilder(int ID, String name){
        this.ID = (byte) ID;
        this.name = name;
    }

    public Biome build(){
        final Biome biome = new Biome(ID, name, grassColor, waterColor, topBlock, fillerBlock);
        Registry.biome.register(ID, biome);
        return biome;
    }


    public BiomeBuilder grassColor(Color grassColor){
        this.grassColor = grassColor;
        return this;
    }

    public BiomeBuilder waterColor(Color waterColor){
        this.waterColor = waterColor;
        return this;
    }

    public BiomeBuilder topBlock(ClientBlock topBlock){
        this.topBlock = topBlock;
        return this;
    }

    public BiomeBuilder fillerBlock(ClientBlock fillerBlock){
        this.fillerBlock = fillerBlock;
        return this;
    }


    public BiomeBuilder colors(Color grass, Color water){
        this.grassColor = grass;
        this.waterColor = water;
        return this;
    }

    public BiomeBuilder colors(double grassR, double grassG, double grassB, double waterR, double waterG, double waterB){
        this.grassColor = new Color(grassR, grassG, grassB);
        this.waterColor = new Color(waterR, waterG, waterB);
        return this;
    }

    public BiomeBuilder blocks(ClientBlock top, ClientBlock filler){
        this.topBlock = top;
        this.fillerBlock = filler;
        return this;
    }

    public BiomeBuilder blocks(ClientBlock topFiller){
        this.topBlock = topFiller;
        this.fillerBlock = topFiller;
        return this;
    }

}
