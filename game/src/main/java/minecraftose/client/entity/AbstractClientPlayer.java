package minecraftose.client.entity;

import jpize.context.Jpize;
import generaloss.spatialmath.EulerAngles;
import generaloss.spatialmath.vector.Vec3f;
import minecraftose.client.Minecraft;
import minecraftose.client.entity.model.PlayerModel;
import minecraftose.client.time.var.TickFloat;
import minecraftose.main.entity.Player;
import minecraftose.client.level.LevelC;

public class AbstractClientPlayer extends Player{

    protected final Minecraft minecraft;

    protected volatile PlayerModel model;
    protected final Vec3f lastPosition, lerpPosition;
    protected final EulerAngles lastRotation, lerpRotation;
    protected final TickFloat bobbing, modelBobbing;

    public AbstractClientPlayer(Minecraft minecraft, LevelC level, String name){
        super(level, name);
        this.minecraft = minecraft;

        this.bobbing = new TickFloat();
        this.modelBobbing = new TickFloat();

        // Interpolation
        this.lastPosition = new Vec3f();
        this.lerpPosition = new Vec3f();
        this.lastRotation = new EulerAngles();
        this.lerpRotation = new EulerAngles();

        // Player model
        Jpize.syncExecutor().exec(() -> this.model = new PlayerModel(this));
    }


    public Minecraft getMinecraft(){
        return minecraft;
    }

    @Override
    public LevelC getLevel(){
        return (LevelC) level;
    }

    @Override
    public void tick(){
        // Player tick
        super.tick();

        // Interpolate
        lastPosition.set(super.position);
        lastRotation.set(super.rotation);

        // View Bobbing
        if(super.onGround.value())
            bobbing.add((Math.min(0.1F, super.velocity.lenh()) - bobbing.value()) * 0.4F);
        else
            bobbing.sub(bobbing.value() * 0.4F);
        // Model bobbing
        modelBobbing.add((Math.min(0.1F, super.velocity.lenh()) - modelBobbing.value()) * 0.4F);
    }
    
    public PlayerModel getModel(){
        return model;
    }


    public float getModelBobbing(){
        final float t = minecraft.getTime().getTickLerpFactor();
        return modelBobbing.getLerp(t);
    }

    public void updateInterpolation(){ //: used
        // final float t = game.getTime().getTickLerpFactor();
        // lerpPosition.lerp(lastPosition, super.position, t);
        // lerpRotation.lerp(lastRotation, super.rotation, t);
    }


    public float getBobbing(){
        final float t = minecraft.getTime().getTickLerpFactor();
        return bobbing.getLerp(t);
    }

    public Vec3f getLerpPosition(){
        final float t = minecraft.getTime().getTickLerpFactor();
        lerpPosition.lerp(lastPosition, super.position, t);
        return lerpPosition;
    }
    
    public EulerAngles getLerpRotation(){
        final float t = minecraft.getTime().getTickLerpFactor();
        lerpRotation.lerp(lastRotation, super.rotation, t);
        return lerpRotation;
    }
    
    public boolean isPositionChanged(){
        return !lastPosition.equals(super.position);
    }
    
    public boolean isRotationChanged(){
        return !lastRotation.equals(super.rotation);
    }
    
}
