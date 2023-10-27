package minecraftose.client.renderer.level;

import jpize.Jpize;
import jpize.util.Disposable;
import jpize.graphics.texture.Texture;
import jpize.graphics.util.batch.TextureBatch;
import jpize.math.vecmath.vector.Vec3f;
import minecraftose.client.ClientGame;
import minecraftose.client.level.ClientLevel;

import static minecraftose.main.chunk.ChunkUtils.MAX_LIGHT_LEVEL;

public class VignetteRenderer implements Disposable{
    
    final LevelRenderer levelRenderer;
    private final Texture vignetteTexture;
    float vignette;
    
    public VignetteRenderer(LevelRenderer levelRenderer){
        this.levelRenderer = levelRenderer;
        
        vignetteTexture = new Texture("texture/misc/vignette.png");
    }
    
    
    public void render(TextureBatch batch){
        batch.begin();
        
        // Get light level
        final ClientGame game = levelRenderer.getGameRenderer().getSession().getGame();
        final Vec3f playerPos = game.getPlayer().getPosition();
        final ClientLevel level = game.getLevel();
        if(level == null)
            return;

        final float light = level.getSkyLight(playerPos.xFloor(), playerPos.yFloor(), playerPos.zFloor());
        
        // Interpolation
        vignette += ((1 - light / MAX_LIGHT_LEVEL) - vignette) / 100F;
        
        // Render
        batch.setAlpha(vignette);
        batch.draw(vignetteTexture, 0, 0, Jpize.getWidth(), Jpize.getHeight());
        batch.resetColor();
        batch.end();
    }
    
    @Override
    public void dispose(){
        vignetteTexture.dispose();
    }
    
}