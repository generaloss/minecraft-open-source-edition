package minecraftose.client.renderer.particle;

import jpize.opengl.shader.Shader;
import jpize.opengl.tesselation.GLPrimitive;
import jpize.opengl.texture.Texture2D;
import jpize.opengl.vertex.GLVertAttr;
import generaloss.resourceflow.Disposable;
import jpize.opengl.buffer.GLBufferUsage;
import jpize.opengl.type.GLType;
import jpize.util.camera.PerspectiveCamera;
import jpize.util.color.Color;
import generaloss.spatialmath.matrix.Matrix4f;
import generaloss.spatialmath.vector.Vec3f;
import jpize.util.mesh.Mesh;
import jpize.util.region.Region;
import generaloss.resourceflow.resource.Resource;
import minecraftose.client.control.camera.PlayerCamera;

import java.util.concurrent.CopyOnWriteArrayList;

public class ParticleBatch implements Disposable{

    public static final int QUAD_VERTICES = 4;
    public static final int QUAD_INDICES = 6;

    final CopyOnWriteArrayList<ParticleInstance> instances;
    final Shader shader;
    final Matrix4f rotateMatrix;
    final Mesh mesh;
    final int size;
    Texture2D lastTexture;
    final Color currentColor;
    final float[] vertices;
    int vertexIndex, particleIndex;
    
    public ParticleBatch(int size){
        this.size = size;
        this.instances = new CopyOnWriteArrayList<>();
        this.shader = new Shader(Resource.internal("/shader/level/particle/particle-batch.vert"), Resource.internal("/shader/level/particle/particle-batch.frag"));
        // Matrices
        this.rotateMatrix = new Matrix4f();
        // Buffer
        this.mesh = new Mesh(
                new GLVertAttr(3, GLType.FLOAT), // pos3
                new GLVertAttr(4, GLType.FLOAT), // col4
                new GLVertAttr(2, GLType.FLOAT)  // uv2
        );
        this.mesh.vertices().allocateData(size * QUAD_VERTICES);
        this.mesh.setMode(GLPrimitive.QUADS);
        // Vertices array
        this.vertices = new float[QUAD_VERTICES * size * mesh.vertices().getVertexSize()];
        // Color
        this.currentColor = new Color();
    }
    
    
    public void spawnParticle(Particle particle, Vec3f position){
        final ParticleInstance instance = particle.createInstance(position);
        instances.add(instance);
    }
    
    
    public void render(PlayerCamera camera){
        setup(camera); // Setup shader
        
        for(ParticleInstance instance: instances){
            instance.update();
            
            if(instance.getElapsedTimeSeconds() > instance.lifeTimeSeconds){
                instances.remove(instance);
                continue;
            }
            
            renderInstance(instance, camera);
        }
        
        flush(); // Render
    }
    
    private void renderInstance(ParticleInstance instance, PerspectiveCamera camera){
        if(particleIndex >= size)
            flush();
        
        // Texture2D
        final Texture2D texture = instance.getParticle().texture;
        if(texture != lastTexture){
            flush();
            lastTexture = texture;
        }
        final Region region = instance.region;
        
        // Color
        currentColor.set(instance.color);
        currentColor.setAlpha(currentColor.alpha * instance.getAlpha());
        
        // Matrix
        rotateMatrix.setRotationZ(instance.rotation).mul(new Matrix4f().setLookAlong(camera.getDirection()));
        
        // Setup vertices
        final Vec3f v0 = new Vec3f(0, -0.5,  0.5) .mulMat4(rotateMatrix) .mul(instance.size) .add(instance.position);
        final Vec3f v1 = new Vec3f(0, -0.5, -0.5) .mulMat4(rotateMatrix) .mul(instance.size) .add(instance.position);
        final Vec3f v2 = new Vec3f(0,  0.5, -0.5) .mulMat4(rotateMatrix) .mul(instance.size) .add(instance.position);
        final Vec3f v3 = new Vec3f(0,  0.5,  0.5) .mulMat4(rotateMatrix) .mul(instance.size) .add(instance.position);
        
        // Add vertices
        addVertex(v0.x, v0.y, v0.z, region.u1(), region.v1());
        addVertex(v1.x, v1.y, v1.z, region.u1(), region.v2());
        addVertex(v2.x, v2.y, v2.z, region.u2(), region.v2());
        addVertex(v3.x, v3.y, v3.z, region.u2(), region.v1());
        
        particleIndex++;
    }
    
    private void addVertex(float x, float y, float z, float u, float v){
        final int pointer = vertexIndex * mesh.vertices().getVertexSize();
        
        // pos3
        vertices[pointer    ] = x;
        vertices[pointer + 1] = y;
        vertices[pointer + 2] = z;
        // col4
        vertices[pointer + 3] = currentColor.red;
        vertices[pointer + 4] = currentColor.green;
        vertices[pointer + 5] = currentColor.blue;
        vertices[pointer + 6] = currentColor.alpha;
        // uv2
        vertices[pointer + 7] = u;
        vertices[pointer + 8] = v;
        
        vertexIndex++;
    }
    
    private void setup(PlayerCamera camera){
        shader.bind();
        shader.uniform("u_projection", camera.getProjection());
        shader.uniform("u_skyBrightness", camera.getMinecraft().getRenderer().getWorldRenderer().getSkyRenderer().getSkyBrightness());
        shader.uniform("u_view", camera.getImaginaryView());
    }
    
    private void flush(){
        if(lastTexture == null)
            return;
        
        shader.uniform("u_texture", lastTexture);
        mesh.vertices().setData(vertices, GLBufferUsage.STREAM_DRAW);
        mesh.render(particleIndex * QUAD_INDICES);
        
        vertexIndex = 0;
        particleIndex = 0;
    }
    
    @Override
    public void dispose(){
        shader.dispose();
        mesh.dispose();
    }
    
}