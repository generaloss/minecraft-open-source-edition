package minecraftose.main.audio;

import generaloss.spatialmath.Maths;

public class SoundGroup{

    public static final SoundGroup DIG_CLOTH  = new SoundGroup(Sound.DIG_CLOTH_1,  Sound.DIG_CLOTH_2,  Sound.DIG_CLOTH_3,  Sound.DIG_CLOTH_4 );
    public static final SoundGroup DIG_GRASS  = new SoundGroup(Sound.DIG_GRASS_1,  Sound.DIG_GRASS_2,  Sound.DIG_GRASS_3,  Sound.DIG_GRASS_4 );
    public static final SoundGroup DIG_GRAVEL = new SoundGroup(Sound.DIG_GRAVEL_1, Sound.DIG_GRAVEL_2, Sound.DIG_GRAVEL_3, Sound.DIG_GRAVEL_4);
    public static final SoundGroup DIG_SAND   = new SoundGroup(Sound.DIG_SAND_1,   Sound.DIG_SAND_2,   Sound.DIG_SAND_3,   Sound.DIG_SAND_4  );
    public static final SoundGroup DIG_SNOW   = new SoundGroup(Sound.DIG_SNOW_1,   Sound.DIG_SNOW_2,   Sound.DIG_SNOW_3,   Sound.DIG_SNOW_4  );
    public static final SoundGroup DIG_STONE  = new SoundGroup(Sound.DIG_STONE_1,  Sound.DIG_STONE_2,  Sound.DIG_STONE_3,  Sound.DIG_STONE_4 );
    public static final SoundGroup DIG_WOOD   = new SoundGroup(Sound.DIG_WOOD_1,   Sound.DIG_WOOD_2,   Sound.DIG_WOOD_3,   Sound.DIG_WOOD_4  );

    public static final SoundGroup STEP_CLOTH  = new SoundGroup(Sound.STEP_CLOTH_1,  Sound.STEP_CLOTH_2,  Sound.STEP_CLOTH_3,  Sound.STEP_CLOTH_4 );
    public static final SoundGroup STEP_GRASS  = new SoundGroup(Sound.STEP_GRASS_1,  Sound.STEP_GRASS_2,  Sound.STEP_GRASS_3,  Sound.STEP_GRASS_4 );
    public static final SoundGroup STEP_GRAVEL = new SoundGroup(Sound.STEP_GRAVEL_1, Sound.STEP_GRAVEL_2, Sound.STEP_GRAVEL_3, Sound.STEP_GRAVEL_4);
    public static final SoundGroup STEP_LADDER = new SoundGroup(Sound.STEP_LADDER_1, Sound.STEP_LADDER_2, Sound.STEP_LADDER_3, Sound.STEP_LADDER_4);
    public static final SoundGroup STEP_SAND   = new SoundGroup(Sound.STEP_SAND_1,   Sound.STEP_SAND_2,   Sound.STEP_SAND_3,   Sound.STEP_SAND_4  );
    public static final SoundGroup STEP_SNOW   = new SoundGroup(Sound.STEP_SNOW_1,   Sound.STEP_SNOW_2,   Sound.STEP_SNOW_3,   Sound.STEP_SNOW_4  );
    public static final SoundGroup STEP_STONE  = new SoundGroup(Sound.STEP_STONE_1,  Sound.STEP_STONE_2,  Sound.STEP_STONE_3,  Sound.STEP_STONE_4 );
    public static final SoundGroup STEP_WOOD   = new SoundGroup(Sound.STEP_WOOD_1,   Sound.STEP_WOOD_2,   Sound.STEP_WOOD_3,   Sound.STEP_WOOD_4  );

    public static final SoundGroup GLASS      = new SoundGroup(Sound.GLASS_1, Sound.GLASS_2, Sound.GLASS_3);
    public static final SoundGroup HIT        = new SoundGroup(Sound.HIT_1,   Sound.HIT_2,   Sound.HIT_3  );

    public static final SoundGroup EXPLODE    = new SoundGroup(Sound.EXPLODE_1, Sound.EXPLODE_2, Sound.EXPLODE_3, Sound.EXPLODE_4);


    private final Sound[] sounds;

    public SoundGroup(Sound... sounds){
        this.sounds = sounds;
    }

    public Sound[] getSounds(){
        return sounds;
    }

    public Sound random(){
        return sounds[Maths.random(sounds.length - 1)];
    }

}
