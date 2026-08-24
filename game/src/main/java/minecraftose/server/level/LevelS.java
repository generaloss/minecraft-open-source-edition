package minecraftose.server.level;

import generaloss.spatialmath.vector.Vec2f;
import generaloss.spatialmath.vector.Vec3f;
import minecraftose.client.block.ClientBlock;
import minecraftose.client.block.ClientBlocks;
import minecraftose.main.audio.Sound;
import minecraftose.main.chunk.ChunkBase;
import minecraftose.main.chunk.storage.ChunkPos;
import minecraftose.main.chunk.storage.HeightmapType;
import minecraftose.main.level.Level;
import minecraftose.main.network.packet.s2c.game.S2CPacketPlaySound;
import minecraftose.server.Server;
import minecraftose.server.level.chunk.ChunkS;
import minecraftose.server.level.light.LevelBlockLight;
import minecraftose.server.level.light.LevelSkyLight;
import minecraftose.server.player.ServerPlayer;

import static minecraftose.main.chunk.ChunkUtils.*;

public class LevelS extends Level{

    private final Server server;
    private final ChunkProviderS chunkProvider;
    private final LevelConfigurationS configuration;
    private final LevelSkyLight skyLight;
    private final LevelBlockLight blockLight;

    public LevelS(Server server){
        this.server = server;
        this.configuration = new LevelConfigurationS();
        this.chunkProvider = new ChunkProviderS(this);
        this.skyLight = new LevelSkyLight(this);
        this.blockLight = new LevelBlockLight(this);
    }
    
    public Server getServer(){
        return server;
    }
    
    
    @Override
    public short getBlockState(int x, int y, int z){
        final ChunkS targetChunk = getBlockChunk(x, z);
        if(targetChunk != null)
            return targetChunk.getBlockState(ChunkBase.clampToLocal(x), y, ChunkBase.clampToLocal(z));

        return ClientBlocks.VOID_AIR.getDefaultData();
    }
    
    @Override
    public boolean setBlockState(int x, int y, int z, short blockData){
        final ChunkPos chunkPos = new ChunkPos(ChunkPos.fromGlobal(x), ChunkPos.fromGlobal(z));
        ChunkS targetChunk = chunkProvider.getChunk(chunkPos);

        final int lx = ChunkBase.clampToLocal(x);
        final int lz = ChunkBase.clampToLocal(z);

        if(targetChunk != null)
            return targetChunk.setBlockData(lx, y, lz, blockData);

        targetChunk = chunkProvider.getChunk(chunkPos);
        if(targetChunk != null)
            return targetChunk.setBlockData(lx, y, lz, blockData);

        return false;
    }


    @Override
    public ClientBlock getBlock(int x, int y, int z){
        final ChunkS targetChunk = getBlockChunk(x, z);
        if(targetChunk != null)
            return targetChunk.getBlock(ChunkBase.clampToLocal(x), y, ChunkBase.clampToLocal(z));

        return ClientBlocks.VOID_AIR;
    }

    @Override
    public boolean setBlock(int x, int y, int z, ClientBlock block){
        final ChunkPos chunkPos = new ChunkPos(ChunkPos.fromGlobal(x), ChunkPos.fromGlobal(z));
        ChunkS targetChunk = chunkProvider.getChunk(chunkPos);

        final int lx = ChunkBase.clampToLocal(x);
        final int lz = ChunkBase.clampToLocal(z);

        if(targetChunk != null)
            return targetChunk.setBlock(lx, y, lz, block);

        targetChunk = chunkProvider.getChunk(chunkPos);
        if(targetChunk != null)
            return targetChunk.setBlock(lx, y, lz, block);

        return false;
    }

    public void genBlock(int x, int y, int z, ClientBlock block){
        final ChunkPos chunkPos = new ChunkPos(ChunkPos.fromGlobal(x), ChunkPos.fromGlobal(z));
        ChunkS targetChunk = chunkProvider.getChunk(chunkPos);

        final int lx = ChunkBase.clampToLocal(x);
        final int lz = ChunkBase.clampToLocal(z);

        if(targetChunk != null)
            targetChunk.setBlockFast(lx, y, lz, block);

        targetChunk = chunkProvider.getChunk(chunkPos);
        if(targetChunk != null)
            targetChunk.setBlockFast(lx, y, lz, block);
    }

    @Override
    public int getHeight(HeightmapType heightmapType, int x, int z){
        final ChunkS targetChunk = getBlockChunk(x, z);
        if(targetChunk != null)
            return targetChunk.getHeightMap(heightmapType).getHeight(ChunkBase.clampToLocal(x), ChunkBase.clampToLocal(z));
        
        return 0;
    }
    
    
    @Override
    public int getSkyLight(int x, int y, int z){
        final ChunkS targetChunk = getBlockChunk(x, z);
        if(targetChunk != null)
            return targetChunk.getSkyLight(ChunkBase.clampToLocal(x), y, ChunkBase.clampToLocal(z));
        
        return MAX_LIGHT_LEVEL;
    }
    
    @Override
    public void setSkyLight(int x, int y, int z, int level){
        final ChunkS targetChunk = getBlockChunk(x, z);
        if(targetChunk != null)
            targetChunk.setSkyLight(ChunkBase.clampToLocal(x), y, ChunkBase.clampToLocal(z), level);
    }

    @Override
    public int getBlockLight(int x, int y, int z){
        final ChunkS targetChunk = getBlockChunk(x, z);
        if(targetChunk != null)
            return targetChunk.getBlockLight(ChunkBase.clampToLocal(x), y, ChunkBase.clampToLocal(z));

        return 0;
    }

    @Override
    public void setBlockLight(int x, int y, int z, int level){
        final ChunkS targetChunk = getBlockChunk(x, z);
        if(targetChunk != null)
            targetChunk.setBlockLight(ChunkBase.clampToLocal(x), y, ChunkBase.clampToLocal(z), level);
    }
    
    
    public Vec3f getSpawnPosition(){
        final Vec2f spawn = getConfiguration().getWorldSpawn();
        final int spawnY = getHeight(HeightmapType.SURFACE, spawn.xFloor(), spawn.yFloor()) + 1;
        return new Vec3f(spawn.x, spawnY, spawn.y);
    }


    public void playSound(Sound sound, float volume, float pitch, float x, float y, float z){
        server.getPlayerList().broadcastPacketLevel(
            this, new S2CPacketPlaySound(sound, volume, pitch, x, y, z)
        );
    }

    public void playSound(Sound sound, float volume, float pitch, Vec3f position){
        server.getPlayerList().broadcastPacketLevel(
            this, new S2CPacketPlaySound(sound, volume, pitch, position)
        );
    }

    public void playSoundExcept(ServerPlayer except, Sound sound, float volume, float pitch, float x, float y, float z){
        server.getPlayerList().broadcastPacketLevelExcept(
            this,
            new S2CPacketPlaySound(sound, volume, pitch, x, y, z),
            except
        );
    }


    public LevelSkyLight getSkyLight(){
        return skyLight;
    }

    public LevelBlockLight getBlockLight(){
        return blockLight;
    }
    
    @Override
    public LevelConfigurationS getConfiguration(){
        return configuration;
    }
    
    @Override
    public ChunkProviderS getChunkProvider(){
        return chunkProvider;
    }
    
    @Override
    public ChunkS getBlockChunk(int blockGlobalX, int blockGlobalZ){
        return chunkProvider.getChunk(ChunkPos.fromGlobal(blockGlobalX), ChunkPos.fromGlobal(blockGlobalZ));
    }
    
}
