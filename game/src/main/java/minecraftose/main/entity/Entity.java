package minecraftose.main.entity;

import generaloss.spatialmath.EulerAngles;
import generaloss.spatialmath.Maths;
import generaloss.spatialmath.axisaligned.AABox;
import generaloss.spatialmath.axisaligned.AABoxBody;
import generaloss.spatialmath.axisaligned.AABoxCollider;
import generaloss.spatialmath.vector.Vec3f;
import minecraftose.client.block.BlockProps;
import minecraftose.client.block.ClientBlocks;
import minecraftose.client.block.shape.BlockCollide;
import minecraftose.client.time.var.TickBool;
import minecraftose.client.time.var.TickFloat;
import minecraftose.main.Dir;
import minecraftose.main.Tickable;
import minecraftose.main.block.ChunkBlockData;
import minecraftose.main.chunk.ChunkBase;
import minecraftose.main.level.Level;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Entity implements Tickable{
    
    protected Level level;
    protected final EntityType<?> entityType;
    protected final Vec3f position;
    protected final EulerAngles rotation;
    protected final Vec3f velocity;
    protected UUID uuid;
    protected AABoxBody aabb;
    
    protected final List<AABoxBody> blockBoxes;
    protected final TickBool onGround;
    protected final TickFloat walkDist;
    protected final TickFloat moveDist;

    public Entity(EntityType<?> entityType, Level level){
        this.level = level;
        this.entityType = entityType;
        this.position = new Vec3f();
        this.rotation = new EulerAngles();
        this.velocity = new Vec3f();
        this.uuid = UUID.randomUUID();
        this.aabb = new AABoxBody(entityType.getBoundingBox(), position);

        this.blockBoxes = new CopyOnWriteArrayList<>();

        this.onGround = new TickBool();
        this.walkDist = new TickFloat();
        this.moveDist = new TickFloat();
    }
    
    
    public Level getLevel(){
        return level;
    }
    
    public void setLevel(Level level){
        this.level = level;
    }


    public EntityType<?> getEntityType(){
        return entityType;
    }


    public Vec3f getPosition(){
        return position;
    }

    public float getX(){
        return position.x;
    }

    public float getY(){
        return position.y;
    }

    public float getZ(){
        return position.z;
    }


    public EulerAngles getRotation(){
        return rotation;
    }

    public float getYaw(){
        return rotation.yaw;
    }

    public float getPitch(){
        return rotation.pitch;
    }

    public float getRoll(){
        return rotation.roll;
    }


    public Vec3f getVelocity(){
        return velocity;
    }


    public UUID getUUID(){
        return uuid;
    }
    
    public void setUUID(UUID uuid){
        this.uuid = uuid;
    }


    public AABoxBody getAABB(){
        return aabb;
    }

    public float getEyeHeight(){
        return aabb.box().getSizeY() * 0.85F;
    }
    

    @Override
    public void tick(){
        // Check is chunk loaded
        // if(level.getBlockChunk(position.xFloor(), position.zFloor()) == null)
        //     return;
        
        // Update blocks around player
        updateBlockBoxes();
        // Check is entity on a ground
        onGround.set(isCollidedTo(Dir.NEGATIVE_Y));
    }


    public float getWalkDist(float t){
        return walkDist.getLerp(t);
    }

    public float getMoveDist(float t){
        return moveDist.getLerp(t);
    }

    
    public boolean isOnGround(float t){
        return onGround.getLerp(t);
    }

    public boolean isOnGround(){
        return isOnGround(0) || isOnGround(1);
    }
    
    public boolean isCollidedTo(Dir face){
        return isOverlapsAt(face.getNormal().castFloat().mul(1e-5));
    }
    
    public boolean isCollidedTo(Vec3f direction){
        return isOverlapsAt(direction.copy().nor().mul(1e-5));
    }

    public boolean isOverlapsAt(Vec3f movement){
        return AABoxCollider.intersects(movement, aabb, blockBoxes);
    }


    public Vec3f collideMovement(Vec3f velocity){ //: private
        if(blockBoxes == null)
            return velocity;

        final Vec3f collideMovement = AABoxCollider.clipMovement(velocity, aabb, blockBoxes);
        position.add(collideMovement);
        return collideMovement;
    }

    /** Get Bounding Boxes of blocks around Entity */
    public void updateBlockBoxes(){ //: private
        blockBoxes.clear();

        final Vec3f velocity = getVelocity();
        final Vec3f min = aabb.min();
        final Vec3f max = aabb.max();

        final int beginX = Maths.floor(min.x - 1 + Math.min(0, velocity.x));
        final int beginY = Math.max(0, Math.min(ChunkBase.HEIGHT_IDX,
            Maths.floor(min.y - 1 + Math.min(0, velocity.y))
        ));
        final int beginZ = Maths.floor(min.z - 1 + Math.min(0, velocity.z));

        final int endX = Maths.ceil(max.x + 1 + Math.max(0, velocity.x));
        final int endY = Math.max(0, Math.min(ChunkBase.HEIGHT,
            Maths.ceil(max.y + 1 + Math.max(0, velocity.y))
        ));
        final int endZ = Maths.ceil(max.z + 1 + Math.max(0, velocity.z));

        for(int x = beginX; x < endX; x++){
            for(int y = beginY; y < endY; y++){
                for(int z = beginZ; z < endZ; z++){

                    final short blockData = level.getBlockState(x, y, z);
                    final BlockProps block = ChunkBlockData.getProps(blockData);

                    if(block.getID() == ClientBlocks.AIR.getID())
                        continue;

                    /*
                    if(block.getID() == ClientBlocks.VOID_AIR.getID()){ //: chunk borders
                        final AABoxBody box = new AABoxBody(BlockCollide.SOLID.getBoxes()[0]);
                        box.getPosition().set(x, y, z);
                        blockBoxes.add(box); //: может произойти out of memory если сущность имеет слишом большую скорость
                        continue;
                    }
                    */

                    final BlockCollide shape = block.getCollide();
                    if(shape == null)
                        continue;

                    for(AABox boundingBox: shape.getBoxes()){
                        final AABoxBody box = new AABoxBody(boundingBox);
                        box.position().set(x, y, z);

                        blockBoxes.add(box);
                    }
                }
            }
        }
    }

}
