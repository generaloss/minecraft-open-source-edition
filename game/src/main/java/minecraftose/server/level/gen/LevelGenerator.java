package minecraftose.server.level.gen;

import minecraftose.main.chunk.storage.ChunkPos;
import minecraftose.main.chunk.storage.HeightmapType;
import minecraftose.server.level.LevelS;
import minecraftose.main.level.ChunkMap;
import minecraftose.server.level.ChunkProviderS;
import minecraftose.server.level.chunk.ChunkS;

import java.util.function.LongPredicate;

public class LevelGenerator{

    private final ChunkProviderS provider;
    private final LevelS level;
    protected final ChunkMap<ChunkS> generated, decorated, illuminated;

    public LevelGenerator(ChunkProviderS provider){
        this.provider = provider;
        this.level = provider.getLevel();

        // Gen steps
        this.generated = new ChunkMap<>();
        this.decorated = new ChunkMap<>();
        this.illuminated = new ChunkMap<>();
    }


    public ChunkMap<ChunkS> getGeneratedChunks(){
        return generated;
    }

    public ChunkMap<ChunkS> getDecoratedChunks(){
        return decorated;
    }

    public ChunkMap<ChunkS> getIlluminatedChunks(){
        return illuminated;
    }


    public void generate(ChunkS chunk){
        final ChunkGenerator generator = level.getConfiguration().getGenerator();
        generator.generate(chunk);
        generated.putChunk(chunk);
    }

    public void generate(ChunkPos chunkPos){
        generate(new ChunkS(level, chunkPos));
    }

    public void generate(int chunkX, int chunkZ){
        generate(new ChunkPos(chunkX, chunkZ));
    }

    public void generate(long packedChunkPos){
        generate(new ChunkPos(packedChunkPos));
    }


    private void checkNeighbors(ChunkMap<ChunkS> chunks, ChunkHandler<ChunkS> handler, LongPredicate predicate){
        cycle:
        for(ChunkS chunk: chunks.getChunks()){
            for(long neighborPosPacked: chunk.getNeighbors())
                if(!predicate.test(neighborPosPacked))
                    continue cycle;

            handler.handle(chunk);
        }
    }

    public void update(){
        checkNeighbors(generated,   this::decorate,   pos -> provider.hasChunk(pos) || illuminated.hasChunk(pos) || decorated.hasChunk(pos) || generated.hasChunk(pos));
        checkNeighbors(decorated,   this::illuminate, pos -> provider.hasChunk(pos) || illuminated.hasChunk(pos) || decorated.hasChunk(pos));
        checkNeighbors(illuminated, this::load,       pos -> provider.hasChunk(pos) || illuminated.hasChunk(pos));
    }

    private void decorate(ChunkS chunk){
        final ChunkGenerator generator = level.getConfiguration().getGenerator();
        generator.decorate(chunk);
        decorated.putChunk(chunk);
        generated.removeChunk(chunk);
    }

    private void illuminate(ChunkS chunk){
        chunk.getHeightMap(HeightmapType.LIGHT_SURFACE).update();
        chunk.getHeightMap(HeightmapType.SURFACE).update();
        chunk.getLevel().getSkyLight().updateSkyLight(chunk);

        illuminated.putChunk(chunk);
        decorated.removeChunk(chunk);
    }

    private void load(ChunkS chunk){
        provider.putChunk(chunk);
        illuminated.removeChunk(chunk);
    }

}
