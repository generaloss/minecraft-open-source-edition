package minecraftose.server.level;

import generaloss.spatialmath.vector.Vec2f;
import minecraftose.main.level.LevelConfiguration;
import minecraftose.server.level.gen.ChunkGenerator;

public class LevelConfigurationS extends LevelConfiguration{
    
    private ChunkGenerator generator;
    private Vec2f spawn;
    private int seed;
    
    
    public void load(String name, String seed, ChunkGenerator generator){
        super.load(name);

        this.setSeed(seed);
        this.generator = generator;
        this.spawn = new Vec2f().add(0.5); // [-718, -953] [-1858, -579]
    }
    
    public ChunkGenerator getGenerator(){
        return generator;
    }
    
    public Vec2f getWorldSpawn(){
        return spawn;
    }
    
    public void setWorldSpawn(double x, double z){
        spawn.set(x, z);
    }

    public int getSeed(){
        return seed;
    }

    private void setSeed(String seedLiteral){
        if(seedLiteral == null){
            seed = 0;
            return;
        }

        seed = seedLiteral.hashCode();

        try{
            seed = Integer.parseInt(seedLiteral);
        }catch(Exception ignored){ }
    }

}
