package minecraftose.client.renderer.level;

import jpize.opengl.gl.GL;
import jpize.opengl.glenum.GLTarget;
import jpize.opengl.shader.Shader;
import jpize.opengl.tesselation.GLPrimitive;
import jpize.opengl.texture.GLWrap;
import jpize.opengl.texture.Texture2D;
import jpize.opengl.type.GLType;
import jpize.opengl.vertex.GLVertAttr;
import generaloss.spatialmath.vector.Vec2f;
import generaloss.spatialmath.vector.Vec3f;
import jpize.util.mesh.Mesh;
import generaloss.resourceflow.resource.Resource;
import minecraftose.client.Minecraft;
import minecraftose.client.control.camera.PlayerCamera;
import minecraftose.client.entity.LocalPlayer;
import minecraftose.client.options.Options;
import minecraftose.client.time.ClientGameTime;
import minecraftose.main.chunk.ChunkBase;
import generaloss.resourceflow.Disposable;

public class CloudsRenderer implements Disposable{

    private final LevelRenderer levelRenderer;
    private final Shader shader;
    private final Mesh mesh;
    private final Mesh mesh2;
    private final Texture2D cloudsTexture;
    private final int renderDistance;

    public CloudsRenderer(LevelRenderer levelRenderer){
        this.levelRenderer = levelRenderer;
        cloudsTexture = new Texture2D()
            .setWrapST(GLWrap.REPEAT)
            .setImage("/texture/environment/clouds.png");

        final Options options = levelRenderer.getGameRenderer().getMinecraft().getOptions();
        renderDistance = options.getRenderDistance() + 1;

        this.shader = new Shader(
                Resource.internal("/shader/level/sky/clouds.vert"),
                Resource.internal("/shader/level/sky/clouds.frag")
        );

        final int size = (renderDistance * 2 + 1) * ChunkBase.SIZE;
        final int start = -size / 2;
        float maxUv = renderDistance * 2F / cloudsTexture.getWidth();

        this.mesh = new Mesh(new GLVertAttr(3, GLType.FLOAT), new GLVertAttr(2, GLType.FLOAT));
        this.mesh.vertices().setData(
                start       , 194F, start + size,  0    , 0    ,
                start       , 194F, start       ,  0    , maxUv,
                start + size, 194F, start       ,  maxUv, maxUv,
                start + size, 194F, start + size,  maxUv, 0
        );
        this.mesh.setMode(GLPrimitive.QUADS);

        this.mesh2 = new Mesh(new GLVertAttr(3, GLType.FLOAT), new GLVertAttr(2, GLType.FLOAT));
        this.mesh2.vertices().setData(
                start       , 312F, start + size,  0        , 0        ,
                start       , 312F, start       ,  0        , maxUv * 2,
                start + size, 312F, start       ,  maxUv * 2, maxUv * 2,
                start + size, 312F, start + size,  maxUv * 2, 0
        );
        this.mesh2.setMode(GLPrimitive.QUADS);
    }

    public void render(PlayerCamera camera){
        final Options options = levelRenderer.getGameRenderer().getMinecraft().getOptions();
        final SkyRenderer skyRenderer = levelRenderer.getSkyRenderer();
        final Vec2f position = getCloudsPosition(0.1);

        GL.disable(GLTarget.CULL_FACE);
        shader.bind();
        shader.uniform("u_projection", camera.getProjection());
        shader.uniform("u_view", camera.getView());
        shader.uniform("u_clouds", cloudsTexture);
        shader.uniform("u_position", position);

        shader.uniform("u_renderDistanceBlocks", (renderDistance - 1) * ChunkBase.SIZE);
        shader.uniform("u_fogEnabled", options.isFogEnabled());
        shader.uniform("u_fogColor", skyRenderer.getFogColor(camera));
        shader.uniform("u_fogStart", skyRenderer.getFogStart());
        shader.uniform("u_skyBrightness", skyRenderer.getSkyBrightness());

        mesh.render();

        position.set(getCloudsPosition(1));
        shader.uniform("u_position", position);
        mesh2.render();

        GL.enable(GLTarget.CULL_FACE);
    }

    private Vec2f getCloudsPosition(double speed){
        final Minecraft minecraft = levelRenderer.getGameRenderer().getMinecraft();
        final ClientGameTime gameTime = minecraft.getTime();
        final float time = gameTime.getLerpSeconds();

        final LocalPlayer player = minecraft.getPlayer();
        final Vec3f playerPos = player.getLerpPosition();

        final float pixelUv = 1F / cloudsTexture.getWidth();
        return new Vec2f(
                ( playerPos.x / ChunkBase.SIZE + time * speed) * pixelUv,
                (-playerPos.z / ChunkBase.SIZE               ) * pixelUv
        );
    }

    @Override
    public void dispose(){
        mesh.dispose();
    }

}
