package minecraftose.client.entity.model;

import jpize.opengl.shader.Shader;
import jpize.opengl.texture.Texture2D;
import generaloss.resourceflow.resource.Resource;
import minecraftose.client.control.camera.PlayerCamera;
import minecraftose.main.entity.Player;
import minecraftose.client.entity.AbstractClientPlayer;

public class HumanoidModel{
    
    private static final float t = 1 / 64F; // Pixel size on texture
    private static final float w = 1.8F / 32; // Pixel size in world
    
    
    protected final AbstractClientPlayer player;
    protected final ModelPart torso, head, leftLeg, rightLeg, leftArm, rightArm;
    protected final Shader shader;
    protected final Texture2D skinTexture;
    
    public HumanoidModel(AbstractClientPlayer player){
        this.player = player;
        
        shader = new Shader(Resource.internal("/shader/level/entity/model.vert"), Resource.internal("/shader/level/entity/model.frag"));
        final int skinID = (Math.abs(player.getName().hashCode()) % 20 + 1);
        skinTexture = new Texture2D("/texture/skin/skin" + skinID + ".png");
        
        head = new ModelPart(new BoxBuilder(-4 * w, 0 * w, -4 * w,  4 * w, 8 * w, 4 * w)
            .nx(1, 1, 1, 1, 24 * t, 8  * t, 32 * t, 16 * t)
            .px(1, 1, 1, 1, 8  * t, 8  * t, 16 * t, 16 * t)
            .ny(1, 1, 1, 1, 16 * t, 0  * t, 24 * t, 8  * t)
            .py(1, 1, 1, 1, 8  * t, 0  * t, 16 * t, 8  * t)
            .pz(1, 1, 1, 1, 16 * t, 8  * t, 24 * t, 16 * t)
            .nz(1, 1, 1, 1, 0  * t, 8  * t, 8  * t, 16 * t)
            .end());
        head.setInitialPose(0, 24 * w, 0);
        
        torso = new ModelPart(new BoxBuilder(-2 * w, -6 * w, -4 * w,  2 * w, 6 * w, 4 * w)
            .nx(1, 1, 1, 1, 32 * t, 20 * t, 40 * t, 32 * t)
            .px(1, 1, 1, 1, 20 * t, 20 * t, 28 * t, 32 * t)
            .ny(1, 1, 1, 1, 28 * t, 16 * t, 36 * t, 20 * t)
            .py(1, 1, 1, 1, 20 * t, 16 * t, 28 * t, 20 * t)
            .pz(1, 1, 1, 1, 28 * t, 20 * t, 32 * t, 32 * t)
            .nz(1, 1, 1, 1, 16 * t, 20 * t, 20 * t, 32 * t)
            .end());
        torso.setInitialPose(0, 18 * w, 0);
        
        leftLeg = new ModelPart(new BoxBuilder(-2 * w, -10 * w, -2 * w,  2 * w, 2 * w, 2 * w)
            .nx(1, 1, 1, 1, 28 * t, 52 * t, 32 * t, 64 * t)
            .px(1, 1, 1, 1, 20 * t, 52 * t, 24 * t, 64 * t)
            .ny(1, 1, 1, 1, 24 * t, 48 * t, 28 * t, 52 * t)
            .py(1, 1, 1, 1, 20 * t, 48 * t, 24 * t, 52 * t)
            .pz(1, 1, 1, 1, 24 * t, 52 * t, 28 * t, 64 * t)
            .nz(1, 1, 1, 1, 16 * t, 52 * t, 20 * t, 64 * t)
            .end());
        leftLeg.setParent(torso);
        leftLeg.setInitialPose(0, -8 * w, 2 * w);
        
        rightLeg = new ModelPart(new BoxBuilder(-2 * w, -10 * w, -2 * w,  2 * w, 2 * w, 2 * w)
            .nx(1, 1, 1, 1, 12 * t, 20 * t, 16 * t, 32 * t)
            .px(1, 1, 1, 1, 4  * t, 20 * t, 8  * t, 32 * t)
            .ny(1, 1, 1, 1, 8  * t, 16 * t, 12 * t, 20 * t)
            .py(1, 1, 1, 1, 4  * t, 16 * t, 8  * t, 20 * t)
            .pz(1, 1, 1, 1, 8  * t, 20 * t, 12 * t, 32 * t)
            .nz(1, 1, 1, 1, 0  * t, 20 * t, 4  * t, 32 * t)
            .end());
        rightLeg.setParent(torso);
        rightLeg.setInitialPose(0, -8 * w, -2 * w);
        
        leftArm = new ModelPart(new BoxBuilder(-2 * w, -10 * w, -2 * w,  2 * w, 2 * w, 2 * w)
            .nx(1, 1, 1, 1, 44 * t, 52 * t, 48 * t, 64 * t)
            .px(1, 1, 1, 1, 36 * t, 52 * t, 40 * t, 64 * t)
            .ny(1, 1, 1, 1, 40 * t, 48 * t, 44 * t, 52 * t)
            .py(1, 1, 1, 1, 36 * t, 48 * t, 40 * t, 52 * t)
            .pz(1, 1, 1, 1, 40 * t, 52 * t, 44 * t, 64 * t)
            .nz(1, 1, 1, 1, 32 * t, 52 * t, 36 * t, 64 * t)
            .end());
        leftArm.setParent(torso);
        leftArm.setInitialPose(0, 4 * w, 6 * w);
        
        rightArm = new ModelPart(new BoxBuilder(-2 * w, -10 * w, -2 * w,  2 * w, 2 * w, 2 * w)
            .nx(1, 1, 1, 1, 52 * t, 20 * t, 56 * t, 32 * t)
            .px(1, 1, 1, 1, 44 * t, 20 * t, 48 * t, 32 * t)
            .ny(1, 1, 1, 1, 48 * t, 16 * t, 52 * t, 20 * t)
            .py(1, 1, 1, 1, 44 * t, 16 * t, 48 * t, 20 * t)
            .pz(1, 1, 1, 1, 48 * t, 20 * t, 52 * t, 32 * t)
            .nz(1, 1, 1, 1, 40 * t, 20 * t, 44 * t, 32 * t)
            .end());
        rightArm.setParent(torso);
        rightArm.setInitialPose(0, 4 * w, -6 * w);
    }
    
    public Player getPlayer(){
        return player;
    }
    
    
    public void render(PlayerCamera camera){
        if(camera == null)
            return;
        
        shader.bind();
        shader.uniform("u_projection", camera.getProjection());
        shader.uniform("u_view", camera.getView());
        shader.uniform("u_texture", skinTexture);
        shader.uniform("u_skyBrightness", camera.getMinecraft().getRenderer().getWorldRenderer().getSkyRenderer().getSkyBrightness());

        torso.render(camera, shader, "u_model");
        head.render(camera, shader, "u_model");
        leftLeg.render(camera, shader, "u_model");
        rightLeg.render(camera, shader, "u_model");
        leftArm.render(camera, shader, "u_model");
        rightArm.render(camera, shader, "u_model");
    }


    public ModelPart getRightArm(){
        return rightArm;
    }
    
    
    public void dispose(){
        torso.getMesh().dispose();
        head.getMesh().dispose();
        leftLeg.getMesh().dispose();
        rightLeg.getMesh().dispose();
        leftArm.getMesh().dispose();
        rightArm.getMesh().dispose();
        
        shader.dispose();
        skinTexture.dispose();
    }
    
}
