package minecraftose.client.renderer.level;

import jpize.context.Jpize;
import jpize.opengl.framebuffer.Framebuffer2D;
import jpize.opengl.framebuffer.Framebuffer3D;
import jpize.opengl.gl.GL;
import jpize.opengl.glenum.GLTarget;
import jpize.opengl.shader.Shader;
import jpize.opengl.texture.Texture2D;
import jpize.util.mesh.TextureBatch;
import jpize.util.color.Color;
import generaloss.resourceflow.resource.Resource;
import minecraftose.client.control.camera.PlayerCamera;
import minecraftose.client.renderer.GameRenderer;
import minecraftose.client.renderer.particle.ParticleBatch;
import generaloss.resourceflow.Disposable;

public class LevelRenderer implements Disposable {

    private final GameRenderer gameRenderer;
    
    // Renderers
    private final SkyRenderer skyRenderer;
    private final CloudsRenderer cloudsRenderer;
    private final ChunkRenderer chunkRenderer;
    private final ChunkBorderRenderer chunkBorderRenderer;
    private final BlockSelectorRenderer blockSelectorRenderer;
    private final EntityRenderer entityRenderer;
    private final PlayerRenderer playerRenderer;
    private final VignetteRenderer vignetteRenderer;
    private final ParticleBatch particleBatch;
    
    // Postprocessing
    private final TextureBatch batch;
    private final Framebuffer2D batchFramebuffer;
    private final Texture2D cursorTexture;
    private final Shader postShader;
    private final Framebuffer3D postFramebuffer;
    private final Color screenColor;
    
    public LevelRenderer(GameRenderer gameRenderer){
        this.gameRenderer = gameRenderer;
        
        // Renderers
        this.skyRenderer = new SkyRenderer(this); // Sky
        this.cloudsRenderer = new CloudsRenderer(this);
        this.chunkRenderer = new ChunkRenderer(this); // Chunks
        this.chunkBorderRenderer = new ChunkBorderRenderer(this); // Chunk Border (F3 + G)
        this.blockSelectorRenderer = new BlockSelectorRenderer(this); // Block Selector
        this.entityRenderer = new EntityRenderer(this); // Entities
        this.playerRenderer = new PlayerRenderer(this); // Player
        this.vignetteRenderer = new VignetteRenderer(this); // Vignette
        this.particleBatch = new ParticleBatch(64); // Particles
        
        // Postprocessing
        this.batch = new TextureBatch();
        this.batchFramebuffer = new Framebuffer2D();
        this.cursorTexture = new Texture2D("/texture/gui/cursor.png");
        this.postShader = new Shader(Resource.internal("/shader/post.vert"), Resource.internal("/shader/post.frag"));
        this.postFramebuffer = new Framebuffer3D();
        this.screenColor = new Color();

        // For ChunkBorder and BlockSelector line rendering
        GL.lineWidth(2);
    }
    
    public GameRenderer getGameRenderer(){
        return gameRenderer;
    }
    
    
    public void render(){
        // Get camera
        final PlayerCamera camera = gameRenderer.getMinecraft().getCamera();
        if(camera == null)
            return;

        // Set water color is camera in water
        if(camera.isInWater())
            screenColor.set(0.4, 0.6, 1);
        else
            screenColor.reset();
        
        // Render world
        // postFramebuffer.begin();
        // {
            GL.enable(GLTarget.DEPTH_TEST);
            
            skyRenderer.render(camera);    // Sky
            cloudsRenderer.render(camera); // Clouds
            chunkRenderer.render(camera);  // Chunks
            playerRenderer.render(camera); // Player
            entityRenderer.render(camera); // Entities
            blockSelectorRenderer.render(camera); // Block selector
            chunkBorderRenderer.render(camera);   // Chunk border
            particleBatch.render(camera);  // Particles
            
            GL.disable(GLTarget.DEPTH_TEST);
        // }
        // postFramebuffer.end();
        
        // Render cursor
        // batchFramebuffer.begin();
        // {
            batch.setup();
            final float cursorSize = Jpize.getHeight() / 48F;
            batch.draw(cursorTexture, Jpize.getWidth() / 2F - cursorSize / 2, Jpize.getHeight() / 2F - cursorSize / 2, cursorSize, cursorSize);
            batch.render();
        // }
        // batchFramebuffer.end();
        // postShader.bind();
        // postShader.uniform("u_frame", postFramebuffer.getFrameTexture());
        // postShader.uniform("u_batch", batchFramebuffer.getFrameTexture());
        // postShader.uniform("u_color", screenColor);
        // ScreenQuad.render();
        
        // Render Vignette
        vignetteRenderer.render(batch);
    }
    
    
    public void resize(int width, int height){
        // Postprocessing
        postFramebuffer.resize(width, height);
        batchFramebuffer.resize(width, height);
    }
    
    @Override
    public void dispose(){
        // Renderers
        skyRenderer.dispose();
        cloudsRenderer.dispose();
        chunkRenderer.dispose();
        chunkBorderRenderer.dispose();
        blockSelectorRenderer.dispose();
        entityRenderer.dispose();
        playerRenderer.dispose();
        vignetteRenderer.dispose();
        particleBatch.dispose();
        
        // Postprocessing
        batch.dispose();
        cursorTexture.dispose();
        postFramebuffer.dispose();
        batchFramebuffer.dispose();
    }
    
    
    public final SkyRenderer getSkyRenderer(){
        return skyRenderer;
    }

    public final CloudsRenderer getCloudsRenderer(){
        return cloudsRenderer;
    }

    public final ChunkRenderer getChunkRenderer(){
        return chunkRenderer;
    }
    
    public final ChunkBorderRenderer getChunkBorderRenderer(){
        return chunkBorderRenderer;
    }
    
    public final BlockSelectorRenderer getBlockSelectorRenderer(){
        return blockSelectorRenderer;
    }
    
    public final EntityRenderer getEntityRenderer(){
        return entityRenderer;
    }
    
    public PlayerRenderer getPlayerRenderer(){
        return playerRenderer;
    }
    
    public final VignetteRenderer getVignetteRenderer(){
        return vignetteRenderer;
    }
    
    public final ParticleBatch getParticleBatch(){
        return particleBatch;
    }

    public final Color getScreenColor(){
        return screenColor;
    }

}
