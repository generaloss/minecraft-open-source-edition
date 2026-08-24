package minecraftose.server.level;

import generaloss.spatialmath.vector.Vec2f;
import generaloss.spatialmath.vector.Vec3f;
import generaloss.chronokit.TickGenerator;
import minecraftose.main.chunk.ChunkBase;
import minecraftose.main.chunk.storage.ChunkPos;
import minecraftose.main.entity.Entity;
import minecraftose.server.level.gen.LevelGenerator;
import minecraftose.server.player.ServerPlayer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChunkLoaderS{

    private final ChunkProviderS provider;
    private final LevelS level;
    private final List<Long> frontiers;
    private final TickGenerator providerTicks;

    public ChunkLoaderS(ChunkProviderS provider){
        this.provider = provider;
        this.level = provider.getLevel();
        this.frontiers = new CopyOnWriteArrayList<>();
        this.providerTicks = new TickGenerator(20);
    }

    public void shutdown(){
        providerTicks.stop();
    }

    public void run(){
        providerTicks.startAsync(() -> {
            try{
                findChunksToLoad();
                unloadChunks();
                provider.getLevelGenerator().update();
            }catch(Exception e){
                e.printStackTrace();
            }
        });
    }


    public void unloadChunks(){
        final LevelGenerator generator = provider.getLevelGenerator();

        for(long packedChunkPos: provider.getChunks().getPositions())
            if(isNobodySees(packedChunkPos, 0))
                provider.removeChunk(packedChunkPos);

        for(long packedChunkPos: generator.getIlluminatedChunks().getPositions())
            if(isNobodySees(packedChunkPos, 1))
                generator.getIlluminatedChunks().removeChunk(packedChunkPos);

        for(long packedChunkPos: generator.getDecoratedChunks().getPositions())
            if(isNobodySees(packedChunkPos, 2))
                generator.getDecoratedChunks().removeChunk(packedChunkPos);

        for(long packedChunkPos: generator.getGeneratedChunks().getPositions())
            if(isNobodySees(packedChunkPos, 3))
                generator.getGeneratedChunks().removeChunk(packedChunkPos);
    }


    public void loadChunkForPlayer(ServerPlayer player){
        final long packedChunkPos = ChunkPos.pack(
            player.getPosition().xFloor(),
            player.getPosition().zFloor()
        );
        ensureFrontier(packedChunkPos);
    }


    private void findChunksToLoad(){
        // Load spawn chunks
        if(frontiers.isEmpty()){
            final Vec2f spawn = level.getConfiguration().getWorldSpawn();
            ensureFrontier(ChunkPos.pack(spawn.xFloor(),spawn.yFloor()));
        }

        // Load players chunks
        for(ServerPlayer player: level.getServer().getPlayerList().getPlayers())
            ensureFrontier(ChunkPos.pack(player.getPosition().xFloor(), player.getPosition().zFloor()));

        // Fast flood fill
        for(long frontierPackedPos: frontiers){
            ensureFrontier(ChunkPos.getNeighborPacked(frontierPackedPos, -1,  0));
            ensureFrontier(ChunkPos.getNeighborPacked(frontierPackedPos,  1,  0));
            ensureFrontier(ChunkPos.getNeighborPacked(frontierPackedPos,  0, -1));
            ensureFrontier(ChunkPos.getNeighborPacked(frontierPackedPos,  0,  1));
        }

        frontiers.removeIf(pos -> isNobodySees(pos, 0));
    }


    private void ensureFrontier(long packedChunkPos){
        if(frontiers.contains(packedChunkPos) || isNobodySees(packedChunkPos, 0))
            return;

        frontiers.add(packedChunkPos);
        provider.getLevelGenerator().generate(packedChunkPos);
    }


    private boolean isNobodySees(long packedChunkPos, float increaseDist){
        final int chunkX = ChunkPos.unpackX(packedChunkPos);
        final int chunkZ = ChunkPos.unpackZ(packedChunkPos);

        // Spawn area
        final Vec2f worldSpawn = level.getServer().getLevelManager().getDefaultLevel().getConfiguration().getWorldSpawn();
        if(gridDistToChunk(chunkX, chunkZ, worldSpawn) <= level.getServer().getConfiguration().getMaxRenderDistance() + increaseDist)
            return false;

        // Players areas
        for(Entity entity: level.getEntities())
            if(entity instanceof ServerPlayer player)
                if(gridDistToChunk(chunkX, chunkZ, player.getPosition()) <= player.getRenderDistance() + increaseDist)
                    return false;

        return true;
    }


    public static float gridDistToChunk(int chunkX, int chunkZ, float x, float z){
        return Vec2f.len(
            chunkX + 0.5F - x / ChunkBase.SIZE,
            chunkZ + 0.5F - z / ChunkBase.SIZE
        );
    }

    public static float gridDistToChunk(int chunkX, int chunkZ, Vec2f vector){
        return gridDistToChunk(chunkX, chunkZ, vector.x, vector.y);
    }

    public static float gridDistToChunk(int chunkX, int chunkZ, Vec3f vector){
        return gridDistToChunk(chunkX, chunkZ, vector.x, vector.z);
    }

}
