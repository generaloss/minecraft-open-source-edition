package minecraftose.client.level;

import generaloss.chronokit.TickGenerator;
import minecraftose.client.Minecraft;
import minecraftose.client.chunk.ChunkC;
import minecraftose.client.entity.LocalPlayer;
import minecraftose.client.options.Options;
import minecraftose.main.chunk.storage.ChunkPos;
import minecraftose.main.level.ChunkHolder;
import minecraftose.main.level.ChunkMap;
import minecraftose.server.level.ChunkLoaderS;

public class ChunkProviderC extends ChunkHolder<ChunkC>{

    private final LevelC level;
    private final ChunkMap<ChunkC> chunks;
    private final LevelBuilder builder;
    private final ChunkLoaderC unloader;

    public ChunkProviderC(LevelC level){
        this.level = level;
        this.chunks = new ChunkMap<>();
        this.builder = new LevelBuilder(this);
        this.unloader = new ChunkLoaderC(this);

        final TickGenerator providerTicks = new TickGenerator(20);
        providerTicks.startAsync(() -> {
            unloader.tick();
            builder.tick();
        });
    }

    public LevelC getLevel(){
        return level;
    }

    public ChunkMap<ChunkC> getChunks(){
        return chunks;
    }

    public LevelBuilder getBuilder(){
        return builder;
    }


    @Override
    public ChunkC getChunk(long packedChunkPos){
        return chunks.getChunk(packedChunkPos);
    }

    @Override
    public boolean hasChunk(long packedChunkPos){
        return chunks.hasChunk(packedChunkPos);
    }

    @Override
    public boolean putChunk(ChunkC chunk){
        if(chunks.putChunk(chunk)){
            builder.buildChunk(chunk, false);
            builder.buildChunk(getChunk(chunk.getNeighborPos( 0,  1)), false);
            builder.buildChunk(getChunk(chunk.getNeighborPos( 1,  0)), false);
            builder.buildChunk(getChunk(chunk.getNeighborPos( 0, -1)), false);
            builder.buildChunk(getChunk(chunk.getNeighborPos(-1,  0)), false);
            builder.buildChunk(getChunk(chunk.getNeighborPos( 1,  1)), false);
            builder.buildChunk(getChunk(chunk.getNeighborPos( 1, -1)), false);
            builder.buildChunk(getChunk(chunk.getNeighborPos(-1,  1)), false);
            builder.buildChunk(getChunk(chunk.getNeighborPos(-1, -1)), false);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeChunk(long packedChunkPos){
        return chunks.removeChunk(packedChunkPos);
    }

    @Override
    public void removeAllChunks(){
        chunks.removeAllChunks();
    }


    public boolean isNotSeen(long packedChunkPos){
        final Minecraft minecraft = level.getMinecraft();
        final Options options = minecraft.getOptions();
        final LocalPlayer player = minecraft.getPlayer();

        final int chunkX = ChunkPos.unpackX(packedChunkPos);
        final int chunkZ = ChunkPos.unpackZ(packedChunkPos);
        return ChunkLoaderS.gridDistToChunk(chunkX, chunkZ, player.getPosition()) > options.getRenderDistance();
    }



    /*
    public static final int BUILD_CHUNK_CORES = 1;

    
    private final ClientLevel level;
    public final FpsCounter tps;

    private final ChunkBuilder[] chunkBuilders;
    private final ExecutorService executor;

    private Thread thread;
    private volatile boolean chunksLoadIsEnd;

    private final ConcurrentHashMap<Long, Long> requestedChunks;
    private final CopyOnWriteArrayList<Long> frontiers;
    private final ChunkMap<ChunkC> chunks;
    private final ConcurrentLinkedDeque<Long> toBuildDeque;


    public ChunkProviderC(ClientLevel level){
        this.level = level;

        this.chunkBuilders = new ChunkBuilder[BUILD_CHUNK_CORES];
        for(int i = 0; i < chunkBuilders.length; i++)
            chunkBuilders[i] = new ChunkBuilder(this);

        this.executor = Executors.newScheduledThreadPool(BUILD_CHUNK_CORES, runnable->{
            final Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            thread.setPriority(Thread.MIN_PRIORITY);
            return thread;
        });

        this.requestedChunks = new ConcurrentHashMap<>();
        this.frontiers = new CopyOnWriteArrayList<>();
        this.chunks = new ChunkMap<>();
        this.toBuildDeque = new ConcurrentLinkedDeque<>();
        
        this.tps = new FpsCounter();
    }
    
    public ClientLevel getLevel(){
        return level;
    }


    public ChunkBuilder[] getChunkBuilders(){
        return chunkBuilders;
    }

    private int getFreeBuilderIndex(){
        for(int i = 0; i < chunkBuilders.length; i++)
            if(chunkBuilders[i].getState() == 0)
                return i;
            // else
            //     System.out.print(" " + i + ": " + chunkBuilders[i].getState());
        return -1;
    }

    private int index;

    private void build(ChunkC chunk){
        chunkBuilders[0].build(chunk);

        // System.out.println("wait for index");
        // while(index == -1 || chunkBuilders[index].getState() != 0)
        //     index = getFreeBuilderIndex();
        // System.out.println(index);

        //final int finalIndex = index;
        //executor.execute(() -> {
        //    try{
        //        chunkBuilders[finalIndex].build(chunk);
        //    }catch(Exception e){
        //        System.out.println(e);
        //    }
        //});

        //index++;
        //if(index == BUILD_CHUNK_CORES)
        //    index = 0;
    }
    
    
    public void startLoadChunks(){
        thread = new Thread(() -> {
            chunksLoadIsEnd = false;
            try{
                while(!Thread.currentThread().isInterrupted()){
                    tps.count();
                    findChunks();
                    buildChunks();
                    checkChunks();

                    Thread.yield();
                }
            }catch(Exception ignored){ }
            chunksLoadIsEnd = true;
        }, "ClientChunkProvider-Thread");

        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setDaemon(true);
        thread.start();
    }
    
    public void dispose(){
        executor.shutdownNow();
        try{
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        thread.interrupt();
        while(!chunksLoadIsEnd) Thread.onSpinWait();
        System.out.println("ENDED");
        clear();
    }

    public void reset(){
        dispose();
        clear();
        startLoadChunks();
    }

    public void clear(){
        toBuildDeque.clear();
        requestedChunks.clear();
        frontiers      .clear();

        for(ChunkC chunk: chunks.getChunks())
            chunk.getMeshStack().dispose();
        chunks.clear();
    }

    public void reload(){
        dispose();
        clear();
        startLoadChunks();
    }
    
    
    private void findChunks(){
        if(frontiers.isEmpty()){
            final LocalPlayer player = level.getMinecraft().getPlayer();

            putFrontier(ChunkPos.pack(
                ChunkPos.fromGlobal(player.getPosition().xFloor()),
                ChunkPos.fromGlobal(player.getPosition().zFloor())
            ));
        }
        
        for(long frontierPos: frontiers){
            ensureFrontier(ChunkPos.getNeighborPacked(frontierPos,-1,  0));
            ensureFrontier(ChunkPos.getNeighborPacked(frontierPos, 1,  0));
            ensureFrontier(ChunkPos.getNeighborPacked(frontierPos, 0, -1));
            ensureFrontier(ChunkPos.getNeighborPacked(frontierPos, 0,  1));
        }

        frontiers.removeIf(this::isOffTheGrid);
    }
    
    private void ensureFrontier(long packedChunkPos){
        if(frontiers.contains(packedChunkPos) || isOffTheGrid(packedChunkPos))
            return;

        putFrontier(packedChunkPos);
    }

    private void putFrontier(long packedChunkPos){
        frontiers.add(packedChunkPos);

        if(!chunks.hasChunk(packedChunkPos) && !requestedChunks.containsKey(packedChunkPos) && toBuildDeque.stream().noneMatch(pos -> pos == packedChunkPos)){
            getLevel().getMinecraft().getConnection().sendPacket(new C2SPacketChunkRequest(packedChunkPos));
            requestedChunks.put(packedChunkPos, System.currentTimeMillis());
        }
    }
    
    private void buildChunks(){
        while(!toBuildDeque.isEmpty()){
            final long packedChunkPos = toBuildDeque.poll();
            if(isOffTheGrid(packedChunkPos))
                continue;

            final ChunkC chunk = getChunk(packedChunkPos);
            if(chunk != null)
                build(chunk);
        }
    }
    
    
    public void checkChunks(){
        for(ChunkC chunk: chunks.getChunks())
            if(isOffTheGrid(chunk.pos().pack()))
                unloadChunk(chunk);
        
        for(Map.Entry<Long, Long> entry: requestedChunks.entrySet())
            if(System.currentTimeMillis() - entry.getValue() > 500)
                requestedChunks.remove(entry.getKey());
    }
    
    
    public void receivedChunk(S2CPacketChunk packet){
        final ChunkC chunk = packet.getChunk(level);

        chunks.putChunk(chunk);
        requestedChunks.remove(chunk.pos().pack());

        chunk.rebuild(false);
        ChunkProviderUtils.rebuildNeighborChunks(chunk);
    }
    
    
    public void unloadChunk(ChunkC chunk){
        Jpize.syncExecutor().exec(() -> chunk.getMeshStack().dispose());
        chunks.removeChunk(chunk.pos());
    }
    
    public void rebuildChunk(ChunkC chunk, boolean important){
        final long packedChunkPos = chunk.posPacked();
        if(!toBuildDeque.contains(packedChunkPos)){
            if(important)
                toBuildDeque.addFirst(packedChunkPos);
            else
                toBuildDeque.addLast(packedChunkPos);
        }
    }


    @Override
    public boolean hasChunk(long packedChunkPos){
        return chunks.hasChunk(packedChunkPos);
    }

    @Override
    public boolean putChunk(ChunkC chunk){
        return chunks.putChunk(chunk);
    }

    @Override
    public boolean removeChunk(long packedChunkPos){
        return chunks.removeChunk(packedChunkPos);
    }

    @Override
    public ChunkC getChunk(long packedChunkPos){
        return chunks.getChunk(packedChunkPos);
    }
    
    
    public Collection<ChunkC> getChunks(){
        return chunks.getChunks();
    }

    
    private boolean isOffTheGrid(long packedChunkPos){
        final int chunkX = ChunkPos.unpackX(packedChunkPos);
        final int chunkZ = ChunkPos.unpackZ(packedChunkPos);

        final LocalPlayer player = level.getMinecraft().getPlayer();
        if(player == null)
            return true;

        final int renderDist = level.getMinecraft().getOptions().getRenderDistance();
        final float distToChunk = ChunkLoader.distToChunk(chunkX, chunkZ, player.getPosition()); //: server ChunkLoader on client

        return distToChunk > renderDist;
    }
    */
}
