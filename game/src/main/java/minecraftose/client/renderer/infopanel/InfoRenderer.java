package minecraftose.client.renderer.infopanel;

import jpize.context.Jpize;
import jpize.util.font.Font;
import generaloss.spatialmath.Maths;
import generaloss.spatialmath.vector.Vec3f;
import generaloss.spatialmath.vector.Vec3i;
import generaloss.chronokit.TimeUtils;
import jpize.util.mesh.TextureBatch;
import minecraftose.client.Minecraft;
import minecraftose.client.audio.SoundPlayer;
import minecraftose.client.chunk.builder.ChunkBuilder;
import minecraftose.client.control.BlockRayCast;
import minecraftose.client.control.camera.PlayerCamera;
import minecraftose.client.entity.LocalPlayer;
import minecraftose.client.level.LevelC;
import minecraftose.client.options.KeyMapping;
import minecraftose.client.options.Options;
import minecraftose.client.renderer.GameRenderer;
import minecraftose.client.renderer.text.TextComponentBatch;
import minecraftose.main.chunk.ChunkBase;
import minecraftose.main.chunk.ChunkUtils;
import minecraftose.main.chunk.storage.ChunkPos;
import minecraftose.main.modification.loader.Modification;
import minecraftose.main.text.Component;
import minecraftose.main.text.TextColor;
import minecraftose.main.time.GameTime;
import generaloss.resourceflow.Disposable;

import java.util.Collection;

public class InfoRenderer implements Disposable{
    
    private final GameRenderer gameRenderer;
    
    private int infoLineNum, hintLineNum;
    private final TextureBatch batch;
    private final TextComponentBatch textBatch;
    
    private boolean open, animationEnded;
    private float animationTimeLine, panelOffsetX; // For open animation
    
    public InfoRenderer(GameRenderer gameRenderer){
        this.gameRenderer = gameRenderer;
        
        textBatch = new TextComponentBatch();
        batch = new TextureBatch();

        animationEnded = true;
        
        // Open
        new Thread(()->{
            TimeUtils.delayMillis(500);
            setOpen(true);
        }).start();
    }
    
    
    private void animate(){
        final int width = Jpize.getWidth() / 2;
        
        if(!animationEnded){
            final float animationSpeed = Math.max(0.03F, 1 - animationTimeLine * animationTimeLine) * Jpize.getDeltaTime() * 5;
            
            if(open){ // open
                animationTimeLine += animationSpeed;
                
                if(animationTimeLine >= 1){
                    animationTimeLine = 1;
                    animationEnded = true;
                }
                
            }else{ // close
                animationTimeLine -= animationSpeed;
                
                if(animationTimeLine <= 0){
                    animationTimeLine = 0;
                    animationEnded = true;
                }
            }
            
            panelOffsetX = (1 - animationTimeLine) * -width;
        }
    }
    
    public void render(){
        textBatch.updateScale();
        animate();
        
        if(!open && animationEnded)
            return;

        final Minecraft minecraft = gameRenderer.getMinecraft();
        final PlayerCamera camera = minecraft.getCamera();
        
        if(camera == null)
            return;
        
        final Options options = minecraft.getOptions();
        
        final LevelC level = minecraft.getLevel();
        if(level == null)
            return;

        final ChunkBuilder chunkBuilder = level.getChunkProvider().getBuilder().getChunkBuilder();
        final GameTime time = minecraft.getTime();
        
        final LocalPlayer player = minecraft.getPlayer();
        final Vec3f playerPos = player.getLerpPosition();
        
        final BlockRayCast blockRayCast = minecraft.getBlockRayCast();
        final Vec3i blockPos = blockRayCast.getSelectedBlockPosition();
        final Vec3i imaginaryBlockPos = blockRayCast.getImaginaryBlockPosition();
        final int lightLevel = level.getLight(imaginaryBlockPos.x, imaginaryBlockPos.y, imaginaryBlockPos.z);

        infoLineNum = 0;
        hintLineNum = 0;
        
        batch.setup();
        textBatch.setBackgroundColor(0, 0, 0, 0.2);
        
        /* -------- INFO -------- */
        
        // Game Version
        final Collection<Modification> loadedMods = minecraft.getModLoader().getLoadedMods();
        final String modLoaderState = loadedMods.isEmpty() ? "Vanilla" : loadedMods.size() + " Mod(s) loaded";
        info(new Component().color(TextColor.DARK_GRAY)
            .text("Minecraft Open Source Edition")
            .text(" " + minecraft.getVersion().getName() + " (" + modLoaderState + ")")
        );
        
        // FPS
        info(new Component().color(TextColor.YELLOW).text(Jpize.getFPS() + " fps"));
        
        // Packets
        infoNextLine();
        info(TextColor.GRAY, "Packets sent", TextColor.YELLOW, minecraft.getConnection().getTX());
        info(TextColor.GRAY, "Packets received", TextColor.YELLOW, minecraft.getConnection().getRX());
        
        // Position
        infoNextLine();
        info(new Component()
            .color(TextColor.RED  ).text("X")
            .color(TextColor.GREEN).text("Y")
            .color(TextColor.AQUA ).text("Z")
            .reset().text(": ")
            .color(TextColor.RED  ).text(String.format("%.3f", playerPos.x) + "  ")
            .color(TextColor.GREEN).text(String.format("%.3f", playerPos.y) + "  ")
            .color(TextColor.AQUA ).text(String.format("%.3f", playerPos.z))
        );
        
        // Block
        info(new Component()
            .color(TextColor.RED).text("Block").reset().text(": ")
            .color(TextColor.RED  ).text(blockPos.x + " ")
            .color(TextColor.GREEN).text(blockPos.y + " ")
            .color(TextColor.AQUA ).text(blockPos.z)
        );
        
        // Chunk Relative
        info(new Component()
            .color(TextColor.RED).text("Local").reset().text(": ")
            .color(TextColor.RED  ).text(ChunkBase.clampToLocal(playerPos.xFloor()) + " ")
            .color(TextColor.GREEN).text(ChunkBase.clampToLocal(playerPos.yFloor()) + " ")
            .color(TextColor.AQUA ).text(ChunkBase.clampToLocal(playerPos.zFloor()))
        );
        
        // Chunk Coordinates
        info(new Component()
            .color(TextColor.RED).text("Chunk").reset().text(": ")
            .color(TextColor.RED  ).text(      ChunkPos.fromGlobal(playerPos.xFloor()) + " ")
            .color(TextColor.GREEN).text(ChunkBase.getSectionIndex(playerPos.yFloor()) + " ")
            .color(TextColor.AQUA ).text(      ChunkPos.fromGlobal(playerPos.zFloor()))
        );
        
        // Level
        infoNextLine();
        info(TextColor.DARK_GREEN, "Level", TextColor.AQUA, level.getConfiguration().getName());

        // Biome
        info(TextColor.DARK_GREEN, "Biome", TextColor.AQUA, level.getBiome(playerPos.xFloor(), playerPos.zFloor()).name);

        // Rotation
        info(TextColor.DARK_GREEN, "Rotation", TextColor.AQUA,
            "Yaw: " + String.format("%.1f", Maths.frac(camera.rotation().yaw, -180, 180)) +
            " Pitch: " + String.format("%.1f", camera.rotation().pitch)
        );
        
        // Speed
        info(TextColor.DARK_GREEN, "Move Speed: ", TextColor.AQUA, String.format("%.2f", player.getVelocity().len() * GameTime.TICKS_IN_SECOND) + " m/s");
        
        // Render Distance
        infoNextLine();
        info(TextColor.BLUE, "Render Distance", TextColor.YELLOW, options.getRenderDistance());
        
        // Chunks Rendered
        info(new Component()
            .color(TextColor.BLUE).text("Chunks (")
            .color(TextColor.YELLOW).text("rendered").color(TextColor.BLUE).text("/").color(TextColor.ORANGE).text("total").color(TextColor.BLUE).text(")").reset().text(": ")
            .color(TextColor.YELLOW).text(gameRenderer.getWorldRenderer().getChunkRenderer().getRenderedChunks())
            .color(TextColor.BLUE).text("/")
            .color(TextColor.ORANGE).text(level.getChunkProvider().getChunks().getChunks().size())
        );
        
        // Chunk Build Time
        info(new Component()
            .color(TextColor.BLUE).text("Chunk Build Time (")
            .color(TextColor.YELLOW).text("time").color(TextColor.BLUE).text("/").color(TextColor.ORANGE).text("vertices").color(TextColor.BLUE).text(")").reset().text(": ")
            .color(TextColor.YELLOW).text(chunkBuilder.buildTime + " ms")
            .color(TextColor.BLUE).text("/")
            .color(TextColor.ORANGE).text(chunkBuilder.verticesNum)
        );

        // Light
        infoNextLine();
        info(new Component()
                .color(TextColor.YELLOW).text("Light Level")
                .reset().text(": ")
                .color(TextColor.AQUA).text(lightLevel)
                .reset().text("/")
                .color(TextColor.AQUA).text(ChunkUtils.MAX_LIGHT_LEVEL)
        );

        // Time
        infoNextLine();
        info(TextColor.YELLOW, "Day: ", TextColor.AQUA, time.getDayNumber());
        info(TextColor.YELLOW, "Time: ", TextColor.AQUA, time.getTime());

        // Sounds
        infoNextLine();
        info(new Component()
                .color(TextColor.YELLOW).text("Sounds")
                .reset().text(": ")
                .color(TextColor.AQUA).text(minecraft.getSoundPlayer().getPlayingSoundsNum())
                .reset().text("/")
                .color(TextColor.AQUA).text(SoundPlayer.MAX_SOUND_SOURCES)
        );

        final int memoryTotal = Maths.round(Runtime.getRuntime().totalMemory() / 1024F / 1024);
        final int memoryFree = Maths.round(Runtime.getRuntime().freeMemory() / 1024F / 1024);
        info(new Component()
                .color(TextColor.YELLOW).text("Used Memory")
                .reset().text(": ")
                .color(TextColor.AQUA).text(memoryTotal - memoryFree)
                .reset().text("/")
                .color(TextColor.AQUA).text(memoryTotal + " MB")
        );

        // info("Threads:");
        // if(serverWorld != null) info("chunk find tps: " + serverWorld.getChunkProvider().findTps.get());
        // if(serverWorld != null) info("chunk load tps: " + serverWorld.getChunkProvider().loadTps.get());
        // info("chunk build tps: " + level.getChunkProvider().buildTps.get());
        // info("chunk check tps: " + level.getChunkProvider().checkTps.get());
        // info("Light time (I/D): " + WorldLight.increaseTime + " ms, " + WorldLight.decreaseTime + " ms");
        // Vec3i imaginaryPos = rayCast.getImaginaryBlockPosition();
        // Vec3i selectedPos = rayCast.getSelectedBlockPosition();
        // info("Selected light level (F/B): " + level.getLight(imaginaryPos.x, imaginaryPos.y, imaginaryPos.z) + ", " + level.getLight(selectedPos.x, selectedPos.y, selectedPos.z));
        
        // HINTS
        hint(new Component().color(TextColor.ORANGE).text("F9 / F8").color(TextColor.GRAY).text(" - Polygon Mode (Line / Fill)"));
        hint(new Component().color(TextColor.ORANGE).text("F3 + G").color(TextColor.GRAY).text(" - Show chunk border"));
        hint(new Component().color(TextColor.ORANGE).text("F3 + C").color(TextColor.GRAY).text(" - Update current chunk"));
        hint(new Component().color(TextColor.ORANGE).text("F3 + H").color(TextColor.GRAY).text(" - Update all chunks"));
        hintNextLine();
        hint(new Component().color(TextColor.ORANGE).text("ESCAPE").color(TextColor.GRAY).text(" - Exit"));
        hint(new Component().color(TextColor.ORANGE).text("M").color(TextColor.GRAY).text(" - Show mouse"));
        hint(new Component().color(TextColor.ORANGE).text(options.getKey(KeyMapping.CHAT)).color(TextColor.GRAY).text(" - Chat"));
        hint(new Component().color(TextColor.ORANGE).text(options.getKey(KeyMapping.TOGGLE_PERSPECTIVE)).color(TextColor.GRAY).text(" - Toggle perspective"));
        hint(new Component().color(TextColor.ORANGE).text(options.getKey(KeyMapping.ZOOM) + " + Mouse Wheel").color(TextColor.GRAY).text(" - zoom"));

        batch.render();
    }
    
    
    private void infoNextLine(){
        infoLineNum++;
    }

    private void hintNextLine(){
        hintLineNum++;
    }
    
    private void info(TextColor keyColor, String key, TextColor valueColor, String value){
        info(new Component().color(keyColor).text(key).reset().text(": ").color(valueColor).text(value));
    }
    
    private void info(TextColor keyColor, String key, TextColor valueColor, Object value){
        info(keyColor, key, valueColor, String.valueOf(value));
    }
    
    private void info(Component text){
        final Font font = textBatch.getFont();

        infoLineNum++;
        final float x = 5 + panelOffsetX;
        final float y = Jpize.getHeight() - 5 - font.getLineAdvanceScaled() * infoLineNum;
        textBatch.drawComponent(text, x, y);
    }
    
    private void hint(Component text){
        final Font font = textBatch.getFont();

        hintLineNum++;
        final float x = Jpize.getWidth() - 5 - textBatch.getFont().getTextWidth(text.toString()) - panelOffsetX;
        final float y = Jpize.getHeight() - 5 - font.getLineAdvanceScaled() * hintLineNum;
        textBatch.drawComponent(text, x, y);
    }
    

    public boolean isOpen(){
        return open;
    }

    public void setOpen(boolean opened){
        if(!animationEnded)
            return;
        
        this.open = opened;
        animationEnded = false;
    }
    
    public void toggleOpen(){
        setOpen(!open);
    }
    
    
    @Override
    public void dispose(){
        batch.dispose();
        textBatch.dispose();
    }
    
}
