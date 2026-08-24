package minecraftose.main.biome;

import jpize.util.color.Color;
import minecraftose.client.block.ClientBlock;

import static minecraftose.client.block.ClientBlocks.*;

public class Biome{

    public static final Biome VOID            = new BiomeBuilder(-1, "Void"           ) .blocks(AIR,                AIR) .colors(0.45, 0.8 , 0.35,  0, 0.5, 0.75) .build();
    public static final Biome SEA             = new BiomeBuilder( 0, "Sea"            ) .blocks(DIRT,              DIRT) .colors(0.4 , 0.7 , 0.4 ,  0, 0.5, 1   ) .build();
    public static final Biome ICE_SEA         = new BiomeBuilder( 1, "Ice Sea"        ) .blocks(DIRT,              DIRT) .colors(0.4 , 0.7 , 0.4 ,  0, 0.4, 0.9 ) .build();
    public static final Biome RIVER           = new BiomeBuilder( 2, "River"          ) .blocks(DIRT,              DIRT) .colors(0.4 , 0.7 , 0.4 ,  0, 0.5, 1   ) .build();
    public static final Biome ICE_RIVER       = new BiomeBuilder( 3, "Ice River"      ) .blocks(DIRT,              DIRT) .colors(0.4 , 0.7 , 0.4 ,  0, 0.4, 0.9 ) .build();
    public static final Biome DESERT          = new BiomeBuilder( 4, "Desert"         ) .blocks(SAND                   ) .colors(0.7 , 0.8 , 0.1 ,  1, 1  , 0.7 ) .build();
    public static final Biome BEACH           = new BiomeBuilder( 5, "Beach"          ) .blocks(SAND                   ) .colors(0.7 , 0.8 , 0.1 ,  0, 0.5, 0.75) .build();
    public static final Biome SNOWY_BEACH     = new BiomeBuilder( 6, "Snowy Beach"    ) .blocks(SAND                   ) .colors(0.7 , 0.8 , 0.1 ,  0, 0.5, 0.75) .build();
    public static final Biome FOREST          = new BiomeBuilder( 7, "Forest"         ) .blocks(GRASS_BLOCK,       DIRT) .colors(0.45, 0.8 , 0.35,  0, 0.5, 0.75) .build();
    public static final Biome SAVANNA         = new BiomeBuilder( 8, "Savanna"        ) .blocks(GRASS_BLOCK,       DIRT) .colors(0.5 , 0.8 , 0.1 ,  0, 0.5, 0.75) .build();
    public static final Biome TAIGA           = new BiomeBuilder( 9, "Taiga"          ) .blocks(PODZOL,            DIRT) .colors(0.3 , 0.7 , 0.45,  0, 0.4, 0.8 ) .build();
    public static final Biome SNOWY_TAIGA     = new BiomeBuilder(10, "Snowy Taiga"    ) .blocks(SNOWY_GRASS_BLOCK, DIRT) .colors(0.14, 0.55, 0.28,  0, 0.4, 0.9 ) .build();
    public static final Biome WINDSWEPT_HILLS = new BiomeBuilder(11, "Windswept Hills") .blocks(GRASS_BLOCK,       DIRT) .colors(0.7 , 0.6 , 0.1 ,  0, 0.5, 0.75) .build();


    public final byte ID;
    public final String name;
    public final Color grassColor;
    public final Color waterColor;
    public final ClientBlock topBlock;
    public final ClientBlock fillerBlock;

    public Biome(byte ID, String name, Color grass, Color water, ClientBlock top, ClientBlock filler){
        this.ID = ID;
        this.name = name;
        this.grassColor = grass;
        this.waterColor = water;
        this.topBlock = top;
        this.fillerBlock = filler;
    }

}
