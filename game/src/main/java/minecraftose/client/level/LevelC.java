package minecraftose.client.level;

import generaloss.spatialmath.Maths;
import minecraftose.client.Minecraft;
import minecraftose.client.block.ClientBlock;
import minecraftose.client.block.ClientBlocks;
import minecraftose.client.chunk.ChunkC;
import minecraftose.main.biome.Biome;
import minecraftose.main.chunk.ChunkBase;
import minecraftose.main.chunk.storage.ChunkPos;
import minecraftose.main.chunk.storage.HeightmapType;
import minecraftose.main.level.Level;

import static minecraftose.main.chunk.ChunkUtils.MAX_LIGHT_LEVEL;

public class LevelC extends Level{

    private final Minecraft minecraft;
    private final ChunkProviderC chunkProvider;
    private final ClientLevelConfiguration configuration;

    public LevelC(Minecraft minecraft, String levelName){
        this.minecraft = minecraft;
        this.chunkProvider = new ChunkProviderC(this);
        this.configuration = new ClientLevelConfiguration();

        configuration.load(levelName);
    }

    public Minecraft getMinecraft(){
        return minecraft;
    }


    @Override
    public void tick(){
        super.tick();
    }


    @Override
    public short getBlockState(int x, int y, int z){
        final ChunkC targetChunk = getBlockChunk(x, z);
        if(targetChunk != null)
            return targetChunk.getBlockState(ChunkBase.clampToLocal(x), y, ChunkBase.clampToLocal(z));

        return ClientBlocks.VOID_AIR.getDefaultData();
    }

    @Override
    public ClientBlock getBlock(int x, int y, int z){
        final ChunkC targetChunk = getBlockChunk(x, z);
        if(targetChunk != null)
            return targetChunk.getBlock(ChunkBase.clampToLocal(x), y, ChunkBase.clampToLocal(z));

        return ClientBlocks.VOID_AIR;
    }

    @Override
    public boolean setBlockState(int x, int y, int z, short state){
        final ChunkC targetChunk = getBlockChunk(x, z);
        if(targetChunk != null)
            return targetChunk.setBlockData(ChunkBase.clampToLocal(x), y, ChunkBase.clampToLocal(z), state);

        return false;
    }

    @Override
    public boolean setBlock(int x, int y, int z, ClientBlock block){
        final ChunkC targetChunk = getBlockChunk(x, z);
        if(targetChunk != null)
            return targetChunk.setBlock(ChunkBase.clampToLocal(x), y, ChunkBase.clampToLocal(z), block);

        return false;
    }

    @Override
    public int getHeight(HeightmapType heightmapType, int x, int z){
        final ChunkC targetChunk = getBlockChunk(x, z);
        if(targetChunk != null)
            return targetChunk.getHeightMap(heightmapType).getHeight(ChunkBase.clampToLocal(x), ChunkBase.clampToLocal(z));

        return 0;
    }


    @Override
    public int getSkyLight(int x, int y, int z){
        final ChunkC targetChunk = getBlockChunk(x, z);
        if(targetChunk != null)
            return targetChunk.getSkyLight(ChunkBase.clampToLocal(x), y, ChunkBase.clampToLocal(z));

        return MAX_LIGHT_LEVEL;
    }

    @Override
    public void setSkyLight(int x, int y, int z, int level){
        final ChunkC targetChunk = getBlockChunk(x, z);
        if(targetChunk != null)
            targetChunk.setSkyLight(ChunkBase.clampToLocal(x), y, ChunkBase.clampToLocal(z), level);
    }

    @Override
    public int getBlockLight(int x, int y, int z){
        final ChunkC targetChunk = getBlockChunk(x, z);
        if(targetChunk != null)
            return targetChunk.getBlockLight(ChunkBase.clampToLocal(x), y, ChunkBase.clampToLocal(z));

        return MAX_LIGHT_LEVEL;
    }

    @Override
    public void setBlockLight(int x, int y, int z, int level){
        final ChunkC targetChunk = getBlockChunk(x, z);
        if(targetChunk != null)
            targetChunk.setBlockLight(ChunkBase.clampToLocal(x), y, ChunkBase.clampToLocal(z), level);
    }

    public int getLight(int x, int y, int z){
        final int skyLight = getSkyLight(x, y, z);
        final int blockLight = getBlockLight(x, y, z);
        final float skyBrightness = minecraft.getRenderer().getWorldRenderer().getSkyRenderer().getSkyBrightness();

        return Math.max(Maths.round(skyLight * skyBrightness), blockLight);
    }


    public Biome getBiome(int x, int z){
        final ChunkC targetChunk = getBlockChunk(x, z);
        if(targetChunk != null)
            return targetChunk.getBiomes().getBiome(ChunkBase.clampToLocal(x), ChunkBase.clampToLocal(z));

        return Biome.VOID;
    }


    @Override
    public ClientLevelConfiguration getConfiguration(){
        return configuration;
    }

    @Override
    public ChunkProviderC getChunkProvider(){
        return chunkProvider;
    }

    @Override
    public ChunkC getBlockChunk(int blockX, int blockZ){
        return chunkProvider.getChunk(ChunkPos.fromGlobal(blockX), ChunkPos.fromGlobal(blockZ));
    }

}
