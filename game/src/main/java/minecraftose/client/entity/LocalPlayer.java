package minecraftose.client.entity;

import generaloss.spatialmath.Mathc;
import generaloss.spatialmath.Maths;
import generaloss.spatialmath.vector.Vec3f;
import generaloss.chronokit.Stopwatch;
import minecraftose.client.Minecraft;
import minecraftose.client.block.BlockProps;
import minecraftose.client.block.ClientBlocks;
import minecraftose.client.control.PlayerInput;
import minecraftose.client.level.LevelC;
import minecraftose.main.Dir;
import minecraftose.main.audio.Sound;
import minecraftose.main.audio.SoundGroup;
import minecraftose.main.audio.SoundType;
import minecraftose.main.inventory.PlayerInventory;
import minecraftose.main.item.ItemStack;
import minecraftose.main.item.Items;

public class LocalPlayer extends AbstractClientPlayer{

    protected final PlayerInput input;
    protected float jumpDownY, lastVelocityY, fallHeight;
    protected final PlayerInventory inventory;
    protected float walkSpeed;
    protected float jumpHeight;
    protected Stopwatch stepTimer;

    public LocalPlayer(Minecraft minecraft, LevelC level, String name){
        super(minecraft, level, name);

        this.input = new PlayerInput(this);
        this.inventory = new PlayerInventory();
        this.inventory.setItemStack(0, new ItemStack(Items.AIR));
        this.walkSpeed = 1;
        this.jumpHeight = 1;
        this.stepTimer = new Stopwatch().start();
    }

    public void setWalkSpeedFactor(float walkSpeed){
        this.walkSpeed = walkSpeed;
    }

    public void setJumpHeightFactor(float jumpHeight){
        this.jumpHeight = jumpHeight;
    }

    @Override
    public void tick(){
        super.tick();
        input.tick();

        final float t = minecraft.getTime().getTickLerpFactor();

        // -------- Vertical Move --------

        // Jumping
        if(isJumping()){
            if(isOnGround()){
                onGround.set(false);
                // Jump
                velocity.y = 0.42F * jumpHeight;
                
                // Jump-boost
                if(isSprinting()){
                    final float yaw = rotation.yaw;
                    final float jumpBoost = 0.2F;
                    velocity.x += jumpBoost * Maths.cosDeg(yaw);
                    velocity.z += jumpBoost * Maths.sinDeg(yaw);
                }
            }
        }

        // Interrupt Flying
        if(isOnGround(t) && isFlying())
            setFlying(false);

        if(isFlying()){
            if(isSneaking())
                velocity.y -= 0.2F;
            
            if(isJumping())
                velocity.y += 0.2F;
            
            if(!isFlyEnabled())
                setFlying(false);
        }

        // In Water
        if(level.getBlockState(position.xFloor(), position.yFloor(), position.zFloor()) == ClientBlocks.WATER.getID()){
            Vec3f push = new Vec3f(
                Maths.random(2, 10) * Maths.cosDeg(rotation.yaw),
                5F,
                Maths.random(2, 10) * Maths.sinDeg(rotation.yaw)
            );
            velocity.add(push);
            minecraft.getCamera().push(push);
            Minecraft.syncExecutor.exec(() -> minecraft.getSoundPlayer().play(SoundGroup.EXPLODE.random(), 1, 1, position.x, position.y, position.z) );
        }

        // Gravity
        if(!isOnGround() && !isFlying())
            velocity.y -= 0.08F * Mathc.sqrt(jumpHeight);
        
        // Reduce Vertical Motion
        if(isFlying())
            velocity.y *= 0.6F;
        else
            velocity.y *= 0.98F;
        
        
        // -------- Horizontal Move --------
        
        // Movement multiplier
        float movementMul = 0.98F * walkSpeed; // Default
        if(flying){
            movementMul *= 3;
            if(sprinting)
                movementMul *= 4F; // Sprinting
        }else{
            if(sneaking)
                movementMul *= 0.3F; // Sneaking
            else if(sprinting)
                movementMul *= 1.3F; // Sprinting
        }
        
        
        // Slipperiness multiplier
        float slipperinessMul = 1; // Air
        if(super.isOnGround(t))
            slipperinessMul *= 0.6F; // Ground
        
        // Reduce Last Motion
        final float reduceHorizontal = slipperinessMul * 0.91F;
        velocity.mul(reduceHorizontal, 1, reduceHorizontal);
        
        // Horizontal Move
        final Vec3f moveControl = input.getHorizontalMove().getMotion().mul(0.98);
        float moveControlLen = moveControl.len();
        if(moveControlLen > 0){
            final Vec3f acceleration = new Vec3f(moveControl.x, 0, moveControl.z);
            
            if(super.isOnGround(t)){
                final float slipperiness = 0.6F / slipperinessMul;
                acceleration.mul(0.1 * movementMul * slipperiness * slipperiness * slipperiness);
            }else
                acceleration.mul(0.02 * movementMul);

            velocity.add(acceleration);
        }
        
        
        // -------- Other --------
        
        // Fall height
        if(velocity.y < 0 && lastVelocityY >= 0)
            jumpDownY = position.y;
        
        if(super.isOnGround(t) && jumpDownY != 0){
            fallHeight = jumpDownY - position.y;
            jumpDownY = 0;

            // Play fall sound
            if(fallHeight > 7)
                minecraft.getSoundPlayer().play(Sound.FALL_BIG, 1, 1, position.x + 0.4F, position.y, position.z + 0.4F);
            else if(fallHeight > 3)
                minecraft.getSoundPlayer().play(Sound.FALL_SMALL, 0.5F, 1, position.x + 0.4F, position.y, position.z + 0.4F);

            if(fallHeight > 3){
                final SoundType sounds = getFloorBlockSounds();
                if(sounds != null)
                    minecraft.getSoundPlayer().play(sounds.getStepSounds().random(), 0.5F, 1, position.x + 0.4F, position.y, position.z + 0.4F);
            }
        }
        
        lastVelocityY = velocity.y;

        // Sneaking
        if(sneaking && super.isOnGround()){ //: жоские костыли
            final Vec3f movement = velocity.copy();
            movement.y = -1e-5F;
            if(!isOverlapsAt(movement)){
                velocity.x = 0;
                velocity.z = 0;
            }
        }

        // Move
        velocity.zeroCompsThatLess(0.003);
        final Vec3f collideMovement = collideMovement(super.velocity);
        velocity.zeroCompsThatBigger(collideMovement);

        // Walk dist
        walkDist.add(Math.min(0.25F, collideMovement.lenh() * 0.6F));
        moveDist.add(collideMovement.len() * 0.6F);

        // Step sounds
        if((super.isOnGround() || jumping) && !sneaking){
            if(stepTimer.getMillis() > 80 / collideMovement.len()){
                stepTimer.reset();

                final SoundType sounds = getFloorBlockSounds();
                if(sounds != null)
                    minecraft.getSoundPlayer().play(sounds.getStepSounds().random(), 0.5F, 1, position.x + 0.4F, position.y, position.z + 0.4F);
            }
        }

        onGround.set(isCollidedTo(Dir.NEGATIVE_Y));
    }

    private SoundType getFloorBlockSounds(){
        final BlockProps block = level.getBlockProps(position.xFloor(), position.yFloor() - 1, position.zFloor());
        if(block == null)
            return null;

        return block.getSoundPack();
    }

    
    public float getFallHeight(){
        return fallHeight;
    }

    public PlayerInput getInput(){
        return input;
    }

    public PlayerInventory getInventory(){
        return inventory;
    }
    
}
