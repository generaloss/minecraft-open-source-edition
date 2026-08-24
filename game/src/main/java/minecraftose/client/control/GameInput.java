package minecraftose.client.control;

import jpize.context.Jpize;
import jpize.context.input.MouseBtn;
import jpize.opengl.gl.GL;
import jpize.context.input.Key;
import generaloss.spatialmath.Maths;
import generaloss.spatialmath.axisaligned.AABox;
import generaloss.spatialmath.axisaligned.AABoxBody;
import generaloss.spatialmath.vector.Vec3f;
import generaloss.spatialmath.vector.Vec3i;
import jpize.opengl.tesselation.GLFace;
import jpize.opengl.tesselation.GLPolygonMode;
import minecraftose.client.Minecraft;
import minecraftose.client.block.ClientBlock;
import minecraftose.client.block.BlockProps;
import minecraftose.client.block.ClientBlocks;
import minecraftose.client.block.shape.BlockCollide;
import minecraftose.client.chat.Chat;
import minecraftose.client.control.camera.PlayerCamera;
import minecraftose.client.entity.LocalPlayer;
import minecraftose.client.level.LevelC;
import minecraftose.client.options.KeyMapping;
import minecraftose.client.options.Options;
import minecraftose.client.renderer.infopanel.ChunkInfoRenderer;
import minecraftose.client.renderer.infopanel.InfoRenderer;
import minecraftose.main.block.ChunkBlockData;
import minecraftose.main.chunk.storage.ChunkPos;
import minecraftose.main.entity.Entity;
import minecraftose.main.item.BlockItem;
import minecraftose.main.item.Item;
import minecraftose.main.item.ItemStack;
import minecraftose.main.item.Items;
import minecraftose.main.network.packet.c2s.game.C2SPacketChunkRequest;
import minecraftose.main.network.packet.c2s.game.C2SPacketHitEntity;
import minecraftose.main.network.packet.c2s.game.C2SPacketPing;
import minecraftose.main.network.packet.c2s.game.C2SPacketPlayerBlockSet;

import java.util.Collection;

public class GameInput{
    
    private final Minecraft minecraft;
    private boolean f3Plus;
    
    public GameInput(Minecraft minecraft){
        this.minecraft = minecraft;
    }
    
    public Minecraft getMinecraft(){
        return minecraft;
    }
    
    
    public void update(){
        final Options options = minecraft.getOptions();
        final PlayerCamera camera = minecraft.getCamera();
        final BlockRayCast blockRayCast = minecraft.getBlockRayCast();
        final LevelC level = minecraft.getLevel();
        
        /* Window **/
        
        // Fullscreen
        if(Key.F11.down())
            Jpize.window.setFullscreen(Jpize.window.isFullscreen());
        
        /* Chat **/
        
        final Chat chat = minecraft.getChat();
        
        if(chat.isOpened()){
            if(Key.ENTER.down()){
                chat.enter();
                chat.close();
            }
            
            if(Key.UP.down())
                chat.historyUp();
            if(Key.DOWN.down())
                chat.historyDown();
            
            if(Key.LCTRL.pressed()){
                if(Key.C.down())
                    Jpize.input.setClipboardString(chat.getEnteringText());
                if(Key.V.down())
                    chat.getInput().insert(Jpize.input.getClipboardString());
            }
            
            if(Key.ESCAPE.down())
                chat.close();
            
            return; // Abort subsequent control
            
        }else if(options.getKey(KeyMapping.CHAT).down())
            chat.open();
        else if(options.getKey(KeyMapping.COMMAND).down()){
            chat.openAsCommandLine();
        }
        
        /* Game **/

        if(camera == null)
            return;

        // F3 + ...
        if(Key.F3.pressed()){
            // G - Chunk border
            if(Key.G.down()){
                minecraft.getRenderer().getWorldRenderer().getChunkBorderRenderer().toggleShow();
                f3Plus = true;
                return;
            }

            // R - Reload chunks
            // if(Key.H.down()){ //: deprecated chunk reload
            //     minecraft.getLevel().getChunkProvider().reload();
            //     f3Plus = true;
            //     return;
            // }

            // C - Reload chunks
            if(Key.C.down()){
                getMinecraft().getConnection().sendPacket(new C2SPacketChunkRequest(ChunkPos.pack(camera.chunkX(), camera.chunkZ())));
                f3Plus = true;
                return;
            }

            // F - Toggle fog
            if(Key.F.down()){
                options.setFog(!options.isFogEnabled());
                f3Plus = true;
                return;
            }
        }

        // Info Panel
        if(Key.F3.up()){
            if(!f3Plus){
                final InfoRenderer info = minecraft.getRenderer().getInfoRenderer();
                final ChunkInfoRenderer chunkInfo = minecraft.getRenderer().getChunkInfoRenderer();

                info.toggleOpen();

                if(info.isOpen()){
                    if(Key.LSHIFT.pressed())
                        chunkInfo.setOpen(true);
                }else
                    chunkInfo.setOpen(false);
            }

            f3Plus = false;
        }

        // Show mouse
        if(Key.M.down())
            minecraft.getPlayer().getInput().getRotation().toggleEnabled();

        // Camera zoom
        if(options.getKey(KeyMapping.ZOOM).down())
            camera.setZoom(10);
        else if(options.getKey(KeyMapping.ZOOM).pressed()){
            camera.setZoom(camera.getZoom() + Jpize.getScroll() * (camera.getZoom() / 8));
        }else if(options.getKey(KeyMapping.ZOOM).up())
            camera.setZoom(1);

        // Ping server
        if(Key.P.down())
            minecraft.getConnection().sendPacket(new C2SPacketPing(System.nanoTime()));

        // Polygon Mode
        if(Key.F9.down())
            GL.polygonMode(GLFace.FRONT_AND_BACK, GLPolygonMode.LINE);
        if(Key.F8.down())
            GL.polygonMode(GLFace.FRONT_AND_BACK, GLPolygonMode.FILL);

        // Exit
        if(Key.ESCAPE.down())
            Jpize.exit();

        // Place/Destroy/Copy block
        if(blockRayCast.isSelected()){
            final LocalPlayer player = minecraft.getPlayer();
            
            if(MouseBtn.LEFT.down() || Key.U.pressed()){
                final Collection<Entity> entities = minecraft.getLevel().getEntities();

                // Check intersect with player & entity
                for(Entity entity: entities){
                    if(entity.getAABB().intersect(blockRayCast.getRay())){
                        minecraft.getConnection().sendPacket(new C2SPacketHitEntity(entity.getUUID()));
                        System.out.println("You hit entity" + entity.getUUID());
                        return;
                    }
                }

                final Vec3i blockPos = blockRayCast.getSelectedBlockPosition();
                level.setBlock(blockPos.x, blockPos.y, blockPos.z, ClientBlocks.AIR);
                minecraft.getConnection().sendPacket(new C2SPacketPlayerBlockSet(blockPos.x, blockPos.y, blockPos.z, ClientBlocks.AIR.getDefaultData()));
                
                for(int i = 0; i < 100; i++){
                    minecraft.spawnParticle(minecraft.BREAK_PARTICLE, new Vec3f(
                        blockPos.x + Maths.random(1F),
                        blockPos.y + Maths.random(1F),
                        blockPos.z + Maths.random(1F)
                    ));
                }
            }else if(MouseBtn.RIGHT.down() || Key.J.pressed()){
                final Item item = player.getInventory().getSelectedItemStack().getItem();
                if(item instanceof BlockItem blockItem)
                    placeBlock(ClientBlocks.COBBLESTONE); // blockItem.getBlock()
            }else if(MouseBtn.MIDDLE.down()){
                final Vec3i blockPos = blockRayCast.getSelectedBlockPosition();

                final BlockProps blockProps = level.getBlockProps(blockPos.x, blockPos.y, blockPos.z);
                final Item item = Items.COBBLESTONE; // Registry.block_item.get(blockProps.getBlock())

                if(item != null){
                    final ItemStack stack = new ItemStack(item, item.maxStack());
                    player.getInventory().setSelectedItemStack(stack);
                }
            }
        }
    }
    
    private void placeBlock(ClientBlock block){
        final BlockRayCast blockRayCast = minecraft.getBlockRayCast();
        final LevelC level = minecraft.getLevel();
        final Vec3i blockPos = blockRayCast.getImaginaryBlockPosition();
        final LocalPlayer player = minecraft.getPlayer();

        final int blockStates = block.getStates().size();

        byte blockState = 0;

        if(block == ClientBlocks.OAK_LOG || block == ClientBlocks.BIRCH_LOG || block == ClientBlocks.SPRUCE_LOG){
            // F:0  -Y:2  +Y:1
            int pitch = (int) ((player.getRotation().pitch % 360 / 90 % 4 + 4.5F) % 4);
            if(pitch == 3)
                pitch--;

            // -X:2  +X:0  -Z:3  +Z:1
            if(pitch == 0){
                int yaw = (int) ((player.getRotation().yaw % 360 / 90 % 4 + 4.5F) % 4);
                if(yaw == 2 || yaw == 0)
                    blockState = 2;

                if(yaw == 3 || yaw == 1)
                    blockState = 1;
            }
        }

        else if(blockStates > 1)
            blockState = (byte) Maths.random(0, blockStates - 1);

        final short blockData = ChunkBlockData.getData(block, blockState);
        
        final BlockCollide collideShape = block.getState(blockState).getCollide();
        if(collideShape != null){
            
            final AABox[] blockBoxes = collideShape.getBoxes();
            final AABoxBody blockBox = new AABoxBody(new Vec3f(blockPos));
            final Collection<Entity> entities = minecraft.getLevel().getEntities();
            
            // Check intersect with player & entity
            for(AABox box: blockBoxes){
                blockBox.box().resize(box);
                
                if(player.getAABB().overlaps(blockBox))
                    return;
                
                for(Entity entity: entities)
                    if(entity.getAABB().overlaps(blockBox))
                        return;
            }
        }
        
        level.setBlockState(blockPos.x, blockPos.y, blockPos.z, blockData);
        minecraft.getConnection().sendPacket(new C2SPacketPlayerBlockSet(blockPos.x, blockPos.y, blockPos.z, blockData));
    }
    
}
