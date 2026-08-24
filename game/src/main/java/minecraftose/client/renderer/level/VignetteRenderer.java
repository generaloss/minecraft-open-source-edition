package minecraftose.client.renderer.level;

import jpize.context.Jpize;
import jpize.opengl.texture.Texture2D;
import jpize.util.mesh.TextureBatch;
import generaloss.resourceflow.Disposable;
import generaloss.spatialmath.vector.Vec3f;
import minecraftose.client.Minecraft;
import minecraftose.client.level.LevelC;

import static minecraftose.main.chunk.ChunkUtils.MAX_LIGHT_LEVEL;

public class VignetteRenderer implements Disposable{
    
    final LevelRenderer levelRenderer;
    private final Texture2D vignetteTexture;
    float vignette;
    
    public VignetteRenderer(LevelRenderer levelRenderer){
        this.levelRenderer = levelRenderer;
        
        vignetteTexture = new Texture2D("/texture/misc/vignette.png");
    }
    
    
    public void render(TextureBatch batch){
        batch.setup();
        
        // Get light level
        final Minecraft minecraft = levelRenderer.getGameRenderer().getMinecraft();
        final Vec3f playerPos = minecraft.getPlayer().getPosition();
        final LevelC level = minecraft.getLevel();
        if(level == null)
            return;

        final float light = level.getSkyLight(playerPos.xFloor(), playerPos.yFloor(), playerPos.zFloor());
        
        // Interpolation
        vignette += ((1 - light / MAX_LIGHT_LEVEL) - vignette) / 100F;
        
        // Render
        batch.setAlpha(vignette);
        batch.draw(vignetteTexture, 0, 0, Jpize.getWidth(), Jpize.getHeight());
        batch.resetColor();
        batch.render();
    }
    
    @Override
    public void dispose(){
        vignetteTexture.dispose();
    }
    
}
