package minecraftose.client.block;

import minecraftose.client.Minecraft;
import minecraftose.client.block.air.Air;
import minecraftose.client.block.air.VoidAir;
import minecraftose.client.resources.GameResources;
import minecraftose.client.block.vanilla.*;

import java.util.HashMap;
import java.util.Map;

public class ClientBlocks{

    public static final ClientBlock VOID_AIR              = new VoidAir            (-1);
    public static final ClientBlock AIR                   = new Air                ( 0);

    public static final ClientBlock DIRT                  = new Dirt(1 );
    public static final ClientBlock GRASS_BLOCK           = new GrassBlock         (2 );
    public static final ClientBlock STONE                 = new Stone(3 );

    public static final ClientBlock OAK_LOG               = new OakLog             (4 );
    public static final ClientBlock OAK_LEAVES            = new OakLeaves          (5 );
    public static final ClientBlock OAK_PLANKS            = new OakPlanks          (29);
    public static final ClientBlock OAK_PLANKS_STAIRS     = new OakPlanksStairs    (30);
    public static final ClientBlock SPRUCE_LOG            = new SpruceLog          (6 );
    public static final ClientBlock SPRUCE_LEAVES         = new SpruceLeaves       (7 );
    public static final ClientBlock BIRCH_LOG             = new BirchLog           (8 );
    public static final ClientBlock BIRCH_LEAVES          = new BirchLeaves        (9 );

    public static final ClientBlock CACTUS                = new Cactus             (10);
    public static final ClientBlock GRASS                 = new Grass              (11);
    public static final ClientBlock SAND                  = new Sand               (12);
    public static final ClientBlock WATER                 = new Water              (13);
    public static final ClientBlock ICE                   = new Ice                (14);

    public static final ClientBlock SNOWY_GRASS_BLOCK     = new SnowyGrassBlock    (15);
    public static final ClientBlock MYCELIUM              = new Mycelium           (16);
    public static final ClientBlock PODZOL                = new Podzol             (17);

    public static final ClientBlock GLASS                 = new Glass              (18);
    public static final ClientBlock LAMP                  = new Lamp               (19);

    public static final ClientBlock STONE_BRICKS          = new StoneBricks        (20);
    public static final ClientBlock COBBLESTONE           = new Cobblestone        (21);
    public static final ClientBlock MOSSY_COBBLESTONE     = new MossyCobblestone   (22);
    public static final ClientBlock GRAVEL                = new Gravel             (23);

    public static final ClientBlock POLISHED_DEEPSLATE    = new PolishedDeepslate  (24);
    public static final ClientBlock DEEPSLATE_TILES       = new DeepslateTiles     (25);

    public static final ClientBlock COARSE_DIRT           = new CoarseDirt         (26);
    public static final ClientBlock CHISELED_STONE_BRICKS = new ChiseledStoneBricks(27);
    public static final ClientBlock SANDSTONE             = new Sandstone          (28);



    private static final Map<Byte, ClientBlock> blocks = new HashMap<>();

    public static void loadBlocks(Minecraft minecraft){
        System.out.println("[Resources] Load Blocks");
        final GameResources resources = minecraft.getResources();
        for(ClientBlock block: blocks.values())
            block.load(resources);
    }

    private static void reg(ClientBlock block){
        blocks.put(block.getID(), block);
    }

    public static ClientBlock get(byte ID){
        return blocks.get(ID);
    }


    public static void register(){
        reg(VOID_AIR             );
        reg(AIR                  );
        reg(DIRT                 );
        reg(GRASS_BLOCK          );
        reg(STONE                );
        reg(OAK_LOG              );
        reg(OAK_LEAVES           );
        reg(OAK_PLANKS           );
        reg(OAK_PLANKS_STAIRS    );
        reg(SPRUCE_LOG           );
        reg(SPRUCE_LEAVES        );
        reg(BIRCH_LOG            );
        reg(BIRCH_LEAVES         );
        reg(CACTUS               );
        reg(GRASS                );
        reg(SAND                 );
        reg(WATER                );
        reg(ICE                  );
        reg(SNOWY_GRASS_BLOCK    );
        reg(MYCELIUM             );
        reg(PODZOL               );
        reg(GLASS                );
        reg(LAMP                 );
        reg(STONE_BRICKS         );
        reg(COBBLESTONE          );
        reg(MOSSY_COBBLESTONE    );
        reg(GRAVEL               );
        reg(POLISHED_DEEPSLATE   );
        reg(DEEPSLATE_TILES      );
        reg(COARSE_DIRT          );
        reg(CHISELED_STONE_BRICKS);
        reg(SANDSTONE            );
    }

}
