package minecraftose.client.renderer.level;

import jpize.opengl.shader.Shader;
import jpize.opengl.tesselation.GLFace;
import jpize.opengl.vertex.GLVertAttr;
import generaloss.resourceflow.Disposable;
import jpize.opengl.gl.GL;
import jpize.opengl.tesselation.GLPrimitive;
import jpize.opengl.type.GLType;
import jpize.util.Skybox;
import jpize.util.color.Color;
import generaloss.spatialmath.Mathc;
import generaloss.spatialmath.Maths;
import generaloss.spatialmath.matrix.Matrix4f;
import jpize.util.mesh.Mesh;
import generaloss.resourceflow.resource.Resource;
import minecraftose.client.control.camera.PlayerCamera;
import minecraftose.client.renderer.BufferBuilder;
import minecraftose.main.time.GameTime;

public class SkyRenderer implements Disposable {

    private final LevelRenderer levelRenderer;

    private final Skybox skyBox;
    private final Matrix4f skyViewMat;

    private final Shader shader;
    private final Mesh mesh;

    private float skyBrightness;

    public SkyRenderer(LevelRenderer levelRenderer) {
        this.levelRenderer = levelRenderer;

        this.skyBox = new Skybox("/texture/skybox/1/skybox_positive_x.png", "/texture/skybox/1/skybox_negative_x.png", "/texture/skybox/1/skybox_positive_y.png", "/texture/skybox/1/skybox_negative_y.png", "/texture/skybox/1/skybox_positive_z.png", "/texture/skybox/1/skybox_negative_z.png");

        this.skyViewMat = new Matrix4f();
        this.shader = new Shader(Resource.internal("/shader/level/sky/sky.vert"), Resource.internal("/shader/level/sky/sky.frag"));

        this.mesh = new Mesh(new GLVertAttr(3, GLType.FLOAT)); // pos3
        this.mesh.setMode(GLPrimitive.TRIANGLE_FAN);
        buildSkyDisc().end(mesh.vertices());
    }


    private BufferBuilder buildSkyDisc() {
        final float radius = 16;
        final BufferBuilder builder = new BufferBuilder();

        builder.vertex(0.0, radius, 0.0);

        for(int i = 0; i <= 360; i += 120){
            builder.vertex(radius * Maths.cosDeg(i), 0, radius * Maths.sinDeg(i));
        }

        return builder;
    }


    public void render(PlayerCamera camera) {
        // Matrix
        skyViewMat.set(camera.getView());
        skyViewMat.cullPosition();

        // Skybox
        // [Skybox]: skyBox.render(camera.getProjection(), skyViewMat);

        // Calc brightness
        final GameTime time = levelRenderer.getGameRenderer().getMinecraft().getTime();
        this.skyBrightness = calcSkyBrightness(time);

        // Color
        final Color skyColor = getSkyColor();
        final Color fogColor = getFogColor(camera);

        GL.clearColor(fogColor);
        GL.depthMask(false);
        GL.cullFace(GLFace.FRONT);

        shader.bind();
        shader.uniform("u_projection", camera.getProjection());
        shader.uniform("u_view", skyViewMat);
        shader.uniform("u_skyColor", skyColor);
        mesh.render();

        GL.cullFace(GLFace.BACK);
        GL.depthMask(true);
    }


    private float calcSkyBrightness(GameTime time) {
        final float sin = Maths.cosDeg(time.getDays() * 360);
        final float nor = sin * 0.5F + 0.5F;
        final float pow = Mathc.pow(nor, 2);
        return Maths.map(pow, 0, 1, 0.15F, 1F);
    }

    public float getSkyBrightness() {
        return skyBrightness;
    }

    public Color getFogColor(PlayerCamera camera) {
        final float playerY = camera.getPlayer().getLerpPosition().y;
        final float voidFactor = Maths.clamp(playerY + 10, 0, 10) / 10F;
        final float brightness = camera.getPlayer().getMinecraft().getOptions().getBrightness() * 0.1F + 0.9F;
        return new Color(0.6, 0.75, 0.9, 1).mulRGB(skyBrightness * voidFactor * brightness);
    }

    public Color getSkyColor() {
        return new Color(0.1, 0.4, 0.9, 1F).mulRGB(skyBrightness);
    }

    public float getFogStart() {
        return 0.6F;
    }


    @Override
    public void dispose() {
        skyBox.dispose();
        shader.dispose();
        mesh.dispose();
    }

}
