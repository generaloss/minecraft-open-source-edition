package minecraftose.server.level.gen.decoration;

import generaloss.spatialmath.ExtRandom;
import minecraftose.main.level.structure.Structure;
import minecraftose.server.level.LevelS;

public class MiniPyramid extends Structure{

    public static void generate(LevelS level, int x, int y, int z, ExtRandom random){
        loadTo(level, "mini_pyramid", x, y, z);
    }

}
