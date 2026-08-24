package minecraftose.client.control.camera;

import generaloss.spatialmath.vector.Vec3f;
import minecraftose.client.Minecraft;
import minecraftose.client.options.KeyMapping;
import minecraftose.client.options.Options;
import minecraftose.client.control.PlayerInput;
import minecraftose.client.entity.LocalPlayer;

public class HorizontalMoveController{
    
    private final PlayerInput playerController;
    private final Vec3f motion;

    public HorizontalMoveController(PlayerInput playerInput){
        this.playerController = playerInput;
        motion = new Vec3f();
    }
    
    public void update(){
        motion.zero();
        
        final Minecraft minecraft = playerController.getPlayer().getMinecraft();
        final Options options = minecraft.getOptions();
        
        final LocalPlayer player = minecraft.getPlayer();
        final Vec3f dir = player.getRotation().getDirectionHorizontal(new Vec3f());
        
        if(options.getKey(KeyMapping.FORWARD).pressed())
            motion.add(dir.x, 0, dir.z);
        if(options.getKey(KeyMapping.BACK).pressed())
            motion.sub(dir.x, 0, dir.z);
        if(options.getKey(KeyMapping.LEFT).pressed())
            motion.add(-dir.z, 0, dir.x);
        if(options.getKey(KeyMapping.RIGHT).pressed())
            motion.sub(-dir.z, 0, dir.x);
        
        motion.nor();
    }
    
    public Vec3f getMotion(){
        return motion;
    }
    
}
