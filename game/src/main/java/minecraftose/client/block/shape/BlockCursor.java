package minecraftose.client.block.shape;


import generaloss.resourceflow.Disposable;
import jpize.opengl.tesselation.GLPrimitive;
import jpize.opengl.type.GLType;
import jpize.opengl.vertex.GLVertAttr;
import jpize.util.mesh.IndexedMesh;

@SuppressWarnings("StaticInitializerReferencesSubClass")
public class BlockCursor implements Disposable{
    
    public final static BlockCursor SOLID = new SolidBlockCursor();
    public final static BlockCursor GRASS = new GrassBlockCursor();
    public final static BlockCursor CACTUS = new CactusBlockCursor();

    
    private final IndexedMesh mesh;
    private final float[] vertices;
    private final int[] quadIndices;
    
    public BlockCursor(float[] vertices, int[] lineIndices, int[] quadIndices){
        this.vertices = vertices;
        this.quadIndices = quadIndices;

        this.mesh = new IndexedMesh(new GLVertAttr(3, GLType.FLOAT));
        this.mesh.setMode(GLPrimitive.LINES);
        this.mesh.vertices().setData(vertices);
        this.mesh.indices().setData(lineIndices);
    }

    public float[] getVertices(){
        return vertices;
    }

    public int[] getQuadIndices(){
        return quadIndices;
    }
    
    
    public void render(){
        mesh.render();
    }
    
    public IndexedMesh getMesh(){
        return mesh;
    }
    
    @Override
    public void dispose(){
        mesh.dispose();
    }
    
}
