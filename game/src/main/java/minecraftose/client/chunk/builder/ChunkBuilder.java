package minecraftose.client.chunk.builder;

import minecraftose.client.block.BlockProps;
import minecraftose.client.block.Blocks;
import minecraftose.client.block.model.BlockModel;
import minecraftose.client.chunk.ClientChunk;
import minecraftose.client.chunk.mesh.ChunkMesh;
import minecraftose.client.chunk.mesh.ChunkMeshStack;
import minecraftose.client.level.ClientChunkManager;
import minecraftose.main.biome.Biome;
import minecraftose.main.block.BlockData;
import minecraftose.main.chunk.LevelChunkSection;
import minecraftose.main.chunk.storage.Heightmap;
import minecraftose.main.chunk.storage.HeightmapType;
import jpize.util.time.Stopwatch;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static minecraftose.main.chunk.ChunkUtils.*;

public class ChunkBuilder{

    private final ClientChunkManager chunkManager;

    public ChunkBuilder(ClientChunkManager chunkManager){
        this.chunkManager = chunkManager;
    }

    public ClientChunkManager getChunkManager(){
        return chunkManager;
    }


    public ClientChunk chunk;
    public ChunkMesh solidMesh, customMesh, translucentMesh;
    public Biome currentBiome;

    public double buildTime;
    public int verticesNum;

    private ClientChunk[] neighborChunks;

    private final AtomicInteger state = new AtomicInteger();


    public int getState(){
        return state.get();
    }


    public boolean build(ClientChunk chunk){
        final Stopwatch timer = new Stopwatch().start();

        // Init
        this.chunk = chunk;
        this.neighborChunks = new ClientChunk[]{ // Rows - X, Columns - Z
            chunk.getNeighborChunk(-1, -1), chunk.getNeighborChunk(0, -1) , chunk.getNeighborChunk(1, -1),
            chunk.getNeighborChunk(-1,  0), chunk                         , chunk.getNeighborChunk(1,  0),
            chunk.getNeighborChunk(-1,  1), chunk.getNeighborChunk(0,  1) , chunk.getNeighborChunk(1,  1)
        };

        // Get Meshes
        final ChunkMeshStack meshStack = chunk.getMeshStack();
        this.solidMesh = meshStack.getSolid();
        this.customMesh = meshStack.getCustom();
        this.translucentMesh = meshStack.getTranslucent();

        // Build
        final Heightmap heightmapSurface = chunk.getHeightMap(HeightmapType.SURFACE);

        final LevelChunkSection[] sections = chunk.getSections();

        if(!chunk.isEmpty())
            for(int lx = 0; lx < SIZE; lx++){
                state.incrementAndGet();
                for(int lz = 0; lz < SIZE; lz++){

                    currentBiome = chunk.getBiomes().getBiome(lx, lz);
                    final int height = heightmapSurface.getHeight(lx, lz) + 1;

                    for(int y = 0; y < height; y++){
                        final int sectionIndex = getSectionIndex(y);
                        if(sections[sectionIndex] == null)
                            y += SIZE;

                        final short blockData = chunk.getBlockState(lx, y, lz);
                        final BlockProps block = BlockData.getProps(blockData);
                        if(block.isEmpty())
                            continue;
                        
                        final BlockModel model = block.getModel();
                        if(model != null)
                            model.build(this, block, lx, y, lz);
                    }
                }
            }

        // Update meshes
        verticesNum = 0;
        verticesNum += solidMesh.updateVertices();
        verticesNum += customMesh.updateVertices();
        verticesNum += translucentMesh.updateVertices();

        // Time
        buildTime = timer.getMillis();

        return true;
    }
    
    public boolean isGenSolidFace(int lx, int y, int lz, int normalX, int normalY, int normalZ, BlockProps block){
        if(isOutOfBounds(y))
            return true;

        final BlockProps neighbor = getBlockProps(lx + normalX, y + normalY, lz + normalZ);

        if(neighbor.getID() == Blocks.VOID_AIR.getID())
            return false;
        
        return (neighbor.isSolid() && neighbor.getModel().isFaceTransparentForNeighbors(-normalX, -normalY, -normalZ)) || neighbor.isEmpty() || (neighbor.isLightTranslucent() && !block.isLightTranslucent());
    }


    private void getChunk(int lx, int lz, ChunkCallback chunkCallback){
        // Находим соседний чанк в массиве 3x3 (neighborChunks)
        int signX = 0;
        int signZ = 0;

        if(lx > SIZE_IDX){
            signX = 1;
            lx -= SIZE;
        }else if(lx < 0){
            signX = -1;
            lx += SIZE;
        }

        if(lz > SIZE_IDX){
            signZ = 1;
            lz -= SIZE;
        }else if(lz < 0){
            signZ = -1;
            lz += SIZE;
        }

        final ClientChunk chunk = neighborChunks[(signZ + 1) * 3 + (signX + 1)];
        if(chunk != null)
            chunkCallback.invoke(chunk, lx, lz);
    }


    public BlockProps getBlockProps(int lx, int y, int lz){
        return BlockData.getProps(getBlock(lx, y, lz));
    }

    public short getBlock(int lx, int y, int lz){
        final AtomicInteger block = new AtomicInteger(Blocks.VOID_AIR.getDefaultData());
        getChunk(lx, lz, (chunk, clx, clz) -> block.set(chunk.getBlockState(clx, y, clz)));
        return (short) block.get();
    }

    public int getSkyLight(int lx, int y, int lz){
        final AtomicInteger light = new AtomicInteger(MAX_LIGHT_LEVEL);
        getChunk(lx, lz, (chunk, clx, clz) -> light.set(chunk.getSkyLight(clx, y, clz)));
        return light.get();
    }

    public int getBlockLight(int lx, int y, int lz){
        final AtomicInteger light = new AtomicInteger();
        getChunk(lx, lz, (chunk, clx, clz) -> light.set(chunk.getBlockLight(clx, y, clz)));
        return light.get();
    }

    public Biome getBiome(int lx, int lz){
        AtomicReference<Biome> biome = new AtomicReference<>();
        getChunk(lx, lz, (chunk, clx, clz) -> biome.set(chunk.getBiomes().getBiome(clx, clz)));
        return biome.get();
    }
    
    
    public float getAO(int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3, int x4, int y4, int z4){
        final BlockProps block1 = getBlockProps(x1, y1, z1);
        final BlockProps block2 = getBlockProps(x2, y2, z2);
        final BlockProps block3 = getBlockProps(x3, y3, z3);
        final BlockProps block4 = getBlockProps(x4, y4, z4);

        float result = 0;
        if(!(block1.isEmpty() || block1.isLightTranslucent())) result++;
        if(!(block2.isEmpty() || block2.isLightTranslucent())) result++;
        if(!(block3.isEmpty() || block3.isLightTranslucent())) result++;

        final int normalX = (x1 + x2 + x3 + x4) / 2;
        final int normalY = (y1 + y2 + y3 + y4) / 2;
        final int normalZ = (z1 + z2 + z3 + z4) / 2;

        if(!(block4.isEmpty() || block4.isLightTranslucent()) || (block4.isSolid() && block4.getModel().isFaceTransparentForNeighbors(normalX, normalY, normalZ))) result++;

        return 1 - result / 5;
    }
    
    public float getSkyLight(int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3, int x4, int y4, int z4){
        float result = 0;
        byte n = 0;
        
        if(getBlockProps(x1, y1, z1).isLightTranslucent()){
            result += getSkyLight(x1, y1, z1);
            n++;
        }
        if(getBlockProps(x2, y2, z2).isLightTranslucent()){
            result += getSkyLight(x2, y2, z2);
            n++;
        }
        if(getBlockProps(x3, y3, z3).isLightTranslucent()){
            result += getSkyLight(x3, y3, z3);
            n++;
        }
        if(getBlockProps(x4, y4, z4).isLightTranslucent()){
            result += getSkyLight(x4, y4, z4);
            n++;
        }
        
        if(n == 0)
            return 0;

        return result / n;
    }

    public float getBlockLight(int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3, int x4, int y4, int z4){
        float result = 0;
        byte n = 0;

        if(getBlockProps(x1, y1, z1).isLightTranslucent()){
            result += getBlockLight(x1, y1, z1);
            n++;
        }
        if(getBlockProps(x2, y2, z2).isLightTranslucent()){
            result += getBlockLight(x2, y2, z2);
            n++;
        }
        if(getBlockProps(x3, y3, z3).isLightTranslucent()){
            result += getBlockLight(x3, y3, z3);
            n++;
        }
        if(getBlockProps(x4, y4, z4).isLightTranslucent()){
            result += getBlockLight(x4, y4, z4);
            n++;
        }

        if(n == 0)
            return 0;

        return result / n;
    }

    public BiomeMix getBiomeMix(int x1, int z1, int x2, int z2, int x3, int z3, int x4, int z4){
        return new BiomeMix(
            getBiome(x1, z1),
            getBiome(x2, z2),
            getBiome(x3, z3),
            getBiome(x4, z4)
        );
    }

}