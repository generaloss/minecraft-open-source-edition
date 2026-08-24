package minecraftose.client.renderer.particle;

import jpize.context.Jpize;
import jpize.opengl.texture.Texture2D;
import generaloss.spatialmath.Maths;

public enum Particles{
    
    BLOCK_BREAK(new Particle()
        .init(instance->{
            instance.size = Maths.random(0.05F, 0.25F);
            instance.velocity.set(Maths.random(-0.05F, 0.05F), 0, Maths.random(-0.05F, 0.05F));
        })
        .texture(new Texture2D("/texture/block/planks.png"))
        .alphaFunc(time->1 - time)
        .animate(instance->{
            instance.velocity.y -= Jpize.getDeltaTime() * 0.5F;
            instance.velocity.mul(0.95);
            instance.position.add(instance.velocity);
        })
    );
    
    
    private final Particle particle;
    
    Particles(Particle particle){
        this.particle = particle;
    }
    
    public Particle getParticle(){
        return particle;
    }
    
}
