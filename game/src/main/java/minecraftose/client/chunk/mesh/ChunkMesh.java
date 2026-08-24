package minecraftose.client.chunk.mesh;

import generaloss.rawlist.FloatList;
import generaloss.rawlist.IntList;
import jpize.opengl.buffer.GLBufferUsage;
import generaloss.resourceflow.Disposable;
import jpize.opengl.type.GLType;
import jpize.opengl.vertex.GLVertAttr;
import jpize.util.mesh.Mesh;
import minecraftose.client.Minecraft;

public class ChunkMesh implements Disposable{

    private final ChunkMeshType type;
    private final FloatList vertices;
    private final IntList verticesPacked;
    private Mesh mesh, pmesh;
    private final transient Object lock = new Object();
    private final transient Object lockPacked = new Object();
    private boolean hasPacked;

    public ChunkMesh(ChunkMeshType type){
        this.type = type;
        this.vertices = new FloatList();
        this.verticesPacked = new IntList();

        Minecraft.syncExecutor.exec(() -> {
            this.mesh = new Mesh(
                new GLVertAttr(3, GLType.FLOAT), // position
                new GLVertAttr(2, GLType.FLOAT), // uv
                new GLVertAttr(4, GLType.FLOAT), // color
                new GLVertAttr(3, GLType.FLOAT)  // ao, skyLight, blockLight
            );

            this.pmesh = new Mesh(
                new GLVertAttr(1, GLType.FLOAT), // packed_1
                new GLVertAttr(1, GLType.FLOAT), // packed_2
                new GLVertAttr(1, GLType.FLOAT)  // packed_3
            );
        });
    }

    
    public void render(){
        if(mesh != null)
            mesh.render();
    }

    public void renderPacked(){
        if(pmesh != null)
            pmesh.render();
    }
    
    @Override
    public void dispose(){
        if(mesh != null){
            mesh.dispose();
            pmesh.dispose();
        }
    }


    public int updateVertices(){
        // # Exceptions:
        // #     verticesList.size() <= i

        final float[] verticesArray = vertices.trim().array().clone();
        final int[] verticesPackedArray = verticesPacked.trim().array().clone();
        vertices.clear();
        verticesPacked.clear();

        Minecraft.syncExecutor.exec(() -> {
            mesh.vertices().setData(verticesArray, GLBufferUsage.STATIC_DRAW);
            pmesh.vertices().setData(verticesPackedArray, GLBufferUsage.STATIC_DRAW);
        });

        hasPacked = (verticesPackedArray.length != 0);
        return verticesArray.length + verticesPackedArray.length;
    }

    public boolean isHasPacked(){
        return hasPacked;
    }


    public void vertex(float... vertex) {
                       // float x, float y, float z,
                       // float u, float v,
                       // float r, float g, float b, float a,
                       // float ao, float skyLight, float blockLight){
        if(vertex.length != 12)
            throw new IllegalArgumentException("vertex length must be 12");

        synchronized (lock) {
            vertices.add(vertex);
        }
    }

    public void vertexPacked(int x, int y, int z,
                             float u, float v,
                             float r, float g, float b, float a,
                             float ao, float skyLight, float blockLight){

        final int UV_TILES_X = 32;
        final int UV_TILES_Y = 32;

        // Position, UV
        final int packed_1 = ( // used 32 / 32 bit // x, y, z,  u, v
                (x      ) | // 5 bit
                (y << 5 ) | // 9 bit
                (z << 14) | // 5 bit

                ((int) (u * UV_TILES_X) << 19) | // 6 bit
                ((int) (v * UV_TILES_Y) << 25)   // 6 bit
        );

        // Color
        final int packed_2 = ( // used 32 / 32 bit // r, g, b, a
                ((int) (r * 255)      ) | // 8 bit
                ((int) (g * 255) << 8 ) | // 8 bit
                ((int) (b * 255) << 16) | // 8 bit
                ((int) (a * 255) << 24)   // 8 bit
        );

        // AmbientOcclusion, SkyLight, BlockLight
        final int packed_3 = ( // used 12 / 32 bit // ao, sl, bl
                ((int) (ao         * 15)     ) | // 4 bit
                ((int) (skyLight   * 15) << 4) | // 4 bit
                ((int) (blockLight * 15) << 8)   // 4 bit
        );

        synchronized (lockPacked){
            verticesPacked.add(new int[] {packed_1, packed_2, packed_3});
        }
    }

    
    public ChunkMeshType getType(){
        return type;
    }
    
}
