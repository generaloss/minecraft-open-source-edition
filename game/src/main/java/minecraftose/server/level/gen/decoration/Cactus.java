package minecraftose.server.level.gen.decoration;

import generaloss.spatialmath.ExtRandom;
import minecraftose.client.block.ClientBlocks;
import minecraftose.server.level.LevelS;

public class Cactus{

    public static void generate(LevelS level, int x, int y, int z, ExtRandom random){
        final int cactusHeight = random.nextInt(3 - 1) + 1;
        for(int i = 0; i < cactusHeight; i++)
            level.genBlock(x, y + i, z, ClientBlocks.CACTUS);
    }

}
