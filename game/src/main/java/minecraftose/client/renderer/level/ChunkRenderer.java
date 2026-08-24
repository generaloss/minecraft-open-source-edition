package minecraftose.client.renderer.level;

import jpize.opengl.gl.GL;
import jpize.opengl.glenum.GLTarget;
import jpize.opengl.shader.Shader;
import jpize.opengl.texture.Texture2D;
import jpize.util.color.Color;
import generaloss.resourceflow.resource.Resource;
import minecraftose.client.chunk.ChunkC;
import minecraftose.client.control.camera.PlayerCamera;
import minecraftose.client.level.LevelC;
import minecraftose.client.options.Options;
import minecraftose.main.chunk.ChunkBase;
import minecraftose.main.time.GameTime;
import generaloss.resourceflow.Disposable;

import java.util.Collection;

public class ChunkRenderer implements Disposable{
    
    private final LevelRenderer levelRenderer;
    private final Shader shader, packedShader;
    private int renderedChunks;
    
    public ChunkRenderer(LevelRenderer levelRenderer){
        this.levelRenderer = levelRenderer;
        
        this.shader = new Shader(
                Resource.internal("/shader/level/chunk/custom-blocks.vert"),
                Resource.internal("/shader/level/chunk/custom-blocks.frag")
        );
        this.packedShader = new Shader(
                Resource.internal("/shader/level/chunk/packed-voxel.vert"),
                Resource.internal("/shader/level/chunk/packed-voxel.frag")
        );

        GL.polygonOffset(1, 1); // For BlockSelector line rendering
    }
    
    
    public void render(PlayerCamera camera){
        setupShaders(camera);
        GL.enable(GLTarget.POLYGON_OFFSET_FILL);
        renderMeshes(camera);
        GL.disable(GLTarget.POLYGON_OFFSET_FILL);
    }
    
    
    private void renderMeshes(PlayerCamera camera){
        // Level
        final LevelC level = levelRenderer.getGameRenderer().getMinecraft().getLevel();
        if(level == null)
            return;

        // Chunks
        final Collection<ChunkC> chunks =
            level.getChunkProvider().getChunks().getChunks() // Get all chunks
            .stream().filter(camera::isChunkSeen).toList(); // Frustum culling

        // Rendered chunks for the info panel
        renderedChunks = chunks.size();

        // Atlas
        final Texture2D blockAtlas = levelRenderer.getGameRenderer().getMinecraft().getResources().getBlocks();


        // Update translation matrix
        for(ChunkC chunk: chunks)
            chunk.updateTranslationMatrix(camera);

        // Render custom blocks
        GL.disable(GLTarget.CULL_FACE);
        shader.bind();
        for(ChunkC chunk: chunks){
            shader.uniform("u_model", chunk.getTranslationMatrix());
            chunk.getMeshStack().getCustom().render();

            if(chunk.getMeshStack().getCustom().isHasPacked()){
                packedShader.bind();
                packedShader.uniform("u_model", chunk.getTranslationMatrix());
                chunk.getMeshStack().getCustom().renderPacked();
                shader.bind();
            }
        }
        GL.enable(GLTarget.CULL_FACE);

        // Render solid blocks
        for(ChunkC chunk: chunks){
            shader.uniform("u_model", chunk.getTranslationMatrix());
            chunk.getMeshStack().getSolid().render();

            if(chunk.getMeshStack().getSolid().isHasPacked()){
                packedShader.bind();
                packedShader.uniform("u_model", chunk.getTranslationMatrix());
                chunk.getMeshStack().getSolid().renderPacked();
                shader.bind();
            }
        }

        // Render translucent blocks
        //GL.disable(GLTarget.CULL_FACE);
        for(ChunkC chunk: chunks){
            shader.uniform("u_model", chunk.getTranslationMatrix());
            chunk.getMeshStack().getTranslucent().render();

            if(chunk.getMeshStack().getTranslucent().isHasPacked()){
                packedShader.bind();
                packedShader.uniform("u_model", chunk.getTranslationMatrix());
                chunk.getMeshStack().getTranslucent().renderPacked();
                shader.bind();
            }
        }
        //GL.enable(GLTarget.CULL_FACE);
    }
    
    private void setupShaders(PlayerCamera camera){
        final Options options = levelRenderer.getGameRenderer().getMinecraft().getOptions();
        final Color fogColor = levelRenderer.getSkyRenderer().getFogColor(camera);
        final float fogStart = levelRenderer.getSkyRenderer().getFogStart();
        final float skyBrightness = levelRenderer.getSkyRenderer().getSkyBrightness();
        final Texture2D blockAtlas = levelRenderer.getGameRenderer().getMinecraft().getResources().getBlocks();
        final GameTime gameTime = levelRenderer.getGameRenderer().getMinecraft().getTime();

        // Shader
        shader.bind();
        shader.uniform("u_projection", camera.getProjection());
        shader.uniform("u_view", camera.getView());
        shader.uniform("u_atlas", blockAtlas);
        
        shader.uniform("u_renderDistanceBlocks", (options.getRenderDistance() - 1) * ChunkBase.SIZE);
        shader.uniform("u_fogEnabled", options.isFogEnabled());
        shader.uniform("u_fogColor", fogColor);
        shader.uniform("u_fogStart", fogStart);
        shader.uniform("u_skyBrightness", skyBrightness);

        // Packed Shader
        packedShader.bind();
        packedShader.uniform("u_projection", camera.getProjection());
        packedShader.uniform("u_view", camera.getView());
        packedShader.uniform("u_atlas", blockAtlas);

        packedShader.uniform("u_renderDistanceBlocks", (options.getRenderDistance() - 1) * ChunkBase.SIZE);
        packedShader.uniform("u_fogEnabled", options.isFogEnabled());
        packedShader.uniform("u_fogColor", fogColor);
        packedShader.uniform("u_fogStart", fogStart);
        packedShader.uniform("u_skyBrightness", skyBrightness);
    }
    
    public int getRenderedChunks(){
        return renderedChunks;
    }
    
    @Override
    public void dispose(){
        shader.dispose();
        packedShader.dispose();
    }
    
}
