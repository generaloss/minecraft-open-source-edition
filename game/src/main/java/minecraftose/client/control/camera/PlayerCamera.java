package minecraftose.client.control.camera;

import generaloss.spatialmath.vector.Vec2d;
import jpize.context.Jpize;
import jpize.util.camera.PerspectiveCamera;
import generaloss.spatialmath.Mathc;
import generaloss.spatialmath.Maths;
import generaloss.spatialmath.vector.Vec3f;
import minecraftose.client.Minecraft;
import minecraftose.client.block.BlockProps;
import minecraftose.client.block.ClientBlocks;
import minecraftose.client.chunk.ChunkC;
import minecraftose.client.entity.LocalPlayer;
import minecraftose.client.level.LevelC;
import minecraftose.client.options.Options;
import minecraftose.main.chunk.ChunkBase;
import minecraftose.main.chunk.storage.ChunkPos;

public class PlayerCamera extends PerspectiveCamera {

    private final Minecraft minecraft;
    
    private final LocalPlayer player;
    private CameraPerspective perspective;
    private float notInterpolatedFov;
    private float zoom;
    private boolean inWater;
    private final Vec3f hitDirection;
    private float eyeHeight;

    public PlayerCamera(Minecraft minecraft, double near, double far, double fieldOfView){
        super(near, far, fieldOfView);
        
        this.minecraft = minecraft;
        this.player = minecraft.getPlayer();

        this.zoom = 1;
        
        this.perspective = CameraPerspective.FIRST_PERSON;
        this.hitDirection = new Vec3f();
        this.eyeHeight = player.getEyeHeight();
        
        setImaginaryOrigins(true, false, true);
    }
    
    public Minecraft getMinecraft(){
        return minecraft;
    }
    
    public LocalPlayer getPlayer(){
        return player;
    }


    public void update(){
        if(minecraft == null)
            return;
        final LevelC level = minecraft.getLevel();
        if(level == null)
            return;

        final float deltaTime = Jpize.getDeltaTime();

        // Set pos & rot
        super.position.set(player.getLerpPosition());
        super.rotation().clampPitch();

        // Perspective
        final float thirdCameraDist = 4;
        if(!perspective.isFirstPerson())
            super.position.sub(super.getDirection().mul(perspective.isMirrored() ? -thirdCameraDist : thirdCameraDist));
        if(perspective.isMirrored())
            super.rotation().pitch += 180;

        // Eye Height
        eyeHeight += (player.getEyeHeight() - eyeHeight) * deltaTime * 12;
        super.position.y += eyeHeight;

        // Player
        final Options options = minecraft.getOptions();
        
        float fov = options.getFieldOfView() / zoom;
        if(player.isSprinting())
            fov *= 1.125F;

        fov = Maths.clamp(Mathc.pow(fov, 1F + player.getVelocity().len() * (player.getVelocity().dot(super.getDirection()) * 0.5 + 0.5) * 0.01), 30, 150);
        
        setFov(fov);

        // Directed Hit
        hitDirection.mul(0.8);
        hitDirection.clampLength(3F);
        final Vec3f hitLocalDirection = hitDirection.copy().rotateY(super.rotation().yaw);
        super.rotation().pitch -= hitLocalDirection.x * 1.5F;
        super.rotation().roll -= hitLocalDirection.z * 1.5F;

        // Jumps
        //this.rotation.pitch -= Maths.clamp(player.getVelocity().y / 2, -10, 10);

        // View Bobbing
        // this.bobView(minecraft.getTime().getTickLerpFactor());

        // Interpolate FOV
        final float currentFOV = getFOV();
        super.setFOV(currentFOV + (notInterpolatedFov - currentFOV) * deltaTime * 9);
        super.update();

        // Update is camera in water
        final BlockProps block = level.getBlockProps(position.xFloor(), position.yFloor(), position.zFloor());
        inWater = block.getID() == ClientBlocks.WATER.getID();
    }

    private void bobView(float t){
        final float walkDist = player.getWalkDist(t) * Maths.PI;
        final float bobbing = player.getBobbing();

        final float sin = Mathc.sin(walkDist) * bobbing;

        final Vec2d dx = new Vec2d(sin * 0.5).rotate(-super.rotation().yaw);
        final double dy = -Math.abs(Mathc.cos(walkDist)) * bobbing;
        super.position.add(0, dy, 0).add(dx);

        final float rz = sin * 3;
        final float rx = Math.abs(Mathc.cos(walkDist - 0.2)) * bobbing * 5;

        System.out.println("Bobbing: " + rz + "  " + rx);

        super.rotation().roll += rz;
        super.rotation().pitch += rx;
    }
    
    
    public void setDistance(int renderDistanceInChunks){
        // setFar(Math.max((renderDistanceInChunks + 0.5F) * SIZE, HEIGHT * 4));
    }
    
    
    public CameraPerspective getPerspective(){
        return perspective;
    }
    
    public void nextPerspective(){
        perspective = perspective.next();
    }


    public void push(Vec3f hitDirection){
        this.hitDirection.add(hitDirection.nor());
    }
    
    
    public float getZoom(){
        return zoom;
    }
    
    public void setZoom(float zoom){
        this.zoom = Maths.clamp(zoom, 1, 200);
    }
    
    
    public void setFov(float fieldOfView){
        notInterpolatedFov = fieldOfView;
    }

    
    public boolean isChunkSeen(ChunkC chunk){
        final ChunkPos position = chunk.pos();
        
        return super.frustum().isAABoxIn(
            position.x * ChunkBase.SIZE,
            0,
            position.z * ChunkBase.SIZE,
            position.x * ChunkBase.SIZE + ChunkBase.SIZE,
            (chunk.getMaxY() + 1),
            position.z * ChunkBase.SIZE + ChunkBase.SIZE
        );
    }
    
    
    public int chunkX(){
        return Maths.floor(getX() / ChunkBase.SIZE);
    }
    
    public int chunkZ(){
        return Maths.floor(getZ() / ChunkBase.SIZE);
    }


    public boolean isInWater(){
        return inWater;
    }

}
