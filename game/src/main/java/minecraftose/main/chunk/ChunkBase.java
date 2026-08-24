package minecraftose.main.chunk;

import minecraftose.client.block.ClientBlock;
import minecraftose.client.block.BlockProps;
import minecraftose.client.block.ClientBlocks;
import minecraftose.client.chunk.ChunkC;
import minecraftose.main.biome.chunk.BiomeMap;
import minecraftose.main.block.ChunkBlockData;
import minecraftose.main.chunk.storage.ChunkNeighbors;
import minecraftose.main.chunk.storage.ChunkPos;
import minecraftose.main.chunk.storage.Heightmap;
import minecraftose.main.chunk.storage.HeightmapType;
import minecraftose.main.chunk.storage.SectionPos;
import minecraftose.main.level.Level;
import minecraftose.server.level.chunk.ChunkS;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class ChunkBase{

    public static final int SIZE_SHIFT = 4; // Степень двойки
    public static final int SIZE = 1 << SIZE_SHIFT; // SIZE = 2 ^ SIZE_SHIFT
    public static final int SIZE_IDX = SIZE - 1;

    public static final int SECTIONS_NUM = 16;
    public static final int SECTIONS_NUM_IDX = SECTIONS_NUM - 1;

    public static final int HEIGHT = SECTIONS_NUM * SIZE;
    public static final int HEIGHT_IDX = HEIGHT - 1;

    public static final int AREA = SIZE * SIZE;
    public static final int VOLUME = AREA * SIZE;

    public static int toGridPos(int xyz){
        return xyz >> SIZE_SHIFT;
    }

    public static int getSectionIndex(int y){
        return toGridPos(y);
    }

    public static int getSectionBaseY(int index){
        return index << SIZE_SHIFT;
    }

    public static boolean isOutOfBounds(int x, int y, int z){
        return x > SIZE_IDX || y > HEIGHT_IDX || z > SIZE_IDX || x < 0 || y < 0 || z < 0;
    }

    public static boolean isOutOfBounds(int x, int z){
        return x > SIZE_IDX || z > SIZE_IDX || x < 0 || z < 0;
    }

    public static boolean isOutOfBounds(int y){
        return y > HEIGHT_IDX || y < 0;
    }

    public static int clampToLocal(int xyz){
        return xyz & SIZE_IDX;
    }
    
    
    protected final Level level;
    protected final ChunkPos position;
    protected final ChunkNeighbors neighbors;
    protected final ChunkSection[] sections;
    protected int highestSectionIndex;
    protected final Map<HeightmapType, Heightmap> heightmaps;
    protected final BiomeMap biomes;
    private final long spawnTimeMillis;

    public ChunkBase(Level level, ChunkPos position){
        this.level = level;
        this.position = position;

        this.spawnTimeMillis = System.currentTimeMillis();
        this.neighbors = new ChunkNeighbors(this, 1);
        this.sections = new ChunkSection[SECTIONS_NUM];
        for(int i = 0; i < SECTIONS_NUM; i++)
            createSection(i);
        this.highestSectionIndex = -1;
        
        // Heightmaps
        this.heightmaps = new HashMap<>();
        for(HeightmapType type: HeightmapType.values())
            heightmaps.put(type, new Heightmap(this, type));

        this.biomes = new BiomeMap();
    }
    
    public Level getLevel(){
        return level;
    }


    public ChunkPos pos(){
        return position;
    }

    public long posPacked(){
        return ChunkPos.pack(position.x, position.z);
    }

    public int getX(){
        return position.x;
    }

    public int getZ(){
        return position.z;
    }


    public long lifeTimeMillis(){
        return System.currentTimeMillis() - spawnTimeMillis;
    }



    public ChunkBase asBaseChunk(){
        return this;
    }

    public ChunkC asClientChunk(){
        return (ChunkC) this;
    }

    public ChunkS asServerChunk(){
        return (ChunkS) this;
    }


    public short getBlockState(int lx, int y, int lz){
        if(isOutOfBounds(lx, y, lz))
            return ClientBlocks.AIR.getDefaultData();

        // Находим по Y нужную секцию
        final int sectionIndex = getSectionIndex(y);
        final ChunkSection section = getSection(sectionIndex);
        // if(section == null)
        //     return ClientBlocks.AIR.getDefaultData();

        // Возвращаем блок
        return section.getBlockState(lx, clampToLocal(y), lz);
    }

    public boolean setBlockData(int lx, int y, int lz, short blockData){
        if(isOutOfBounds(lx, y, lz))
            return false;
        
        // Проверяем является ли устанавливаемый блок воздухом
        final int airID = ClientBlocks.AIR.getID();
        final int blockID = ChunkBlockData.getID(blockData);
        final boolean isBlockAir = (blockID == airID);
        
        // Находим по Y нужную секцию
        final int sectionIndex = getSectionIndex(y);
        final ChunkSection section = getSection(sectionIndex);

        // if(section != null){
        //     // Если устанавливаемый блок воздух, и в секции остался один блок
        //     // - удаляем секцию, чтобы не загружать память секцией с 4096 блоками воздуха, и завершаем метод
        //     if(section.getBlocksNum() == 1 && isBlockAir && (getSection(sectionIndex - 1) == null || getSection(sectionIndex + 1) == null)){
        //         removeSection(sectionIndex);
        //         return true;
        //     }
        //
        // // Если секция равна null
        // }else{
        //     // Если устанавливаемый блок воздух - ничего не делаем и завершаем метод
        //     if(isBlockAir)
        //         return false;
        //
        //     // Если устанавливаемый блок не воздух - создаем секцию
        //     section = createSection(sectionIndex);
        // }
        
        // Проверка на равенство устанавливаемого блока и текущего
        final int ly = clampToLocal(y);
        final int oldBlockID = ChunkBlockData.getID(section.getBlockState(lx, ly, lz));
        if(blockID == oldBlockID)
            return false;
        
        // Подсчет блоков в секции
        if(isBlockAir)
            section.blocksNum--; // Если блок был заменен воздухом - кол-во блоков в секции уменьшаем на 1
        else
            section.blocksNum++; // Если воздух был заменен блоком - увеличиваем на 1
        
        // УСТАНАВЛИВАЕМ БЛОК
        section.setBlockState(lx, ly, lz, blockData);
        return true;
    }
    
    
    public ChunkSection[] getSections(){
        return sections;
    }
    
    public ChunkSection getSection(int index){
        return sections[index];
    }

    public ChunkSection getBlockSection(int y){
        return getSection(getSectionIndex(y));
    }
    
    public int getHighestSectionIndex(){
        return highestSectionIndex;
    }
    
    protected void removeSection(int index){
        sections[index] = null;
        if(highestSectionIndex != index)
            return;
        
        for(int i = SECTIONS_NUM_IDX; i >= 0; i--)
            if(getSection(i) != null){
                highestSectionIndex = i;
                return;
            }
        
        highestSectionIndex = -1;
    }
    
    protected ChunkSection createSection(int index){
        highestSectionIndex = Math.max(highestSectionIndex, index);
        return sections[index] = new ChunkSection(new SectionPos(position.x, index, position.z));
    }
    
    public void setSections(ChunkSection[] sections, int highestSectionIndex){
        System.arraycopy(sections, 0, this.sections, 0, sections.length);
        this.highestSectionIndex = highestSectionIndex;
    }


    public ClientBlock getBlock(int lx, int y, int lz){
        return ChunkBlockData.getBlock(getBlockState(lx, y, lz));
    }

    public BlockProps getBlockProps(int lx, int y, int lz){
        return ChunkBlockData.getProps(getBlockState(lx, y, lz));
    }

    public boolean setBlock(int lx, int y, int lz, ClientBlock block){
        return setBlockData(lx, y, lz, block.getDefaultData());
    }


    public boolean isEmpty(){
        return highestSectionIndex == -1;
    }
    
    
    public Collection<Heightmap> getHeightmaps(){
        return heightmaps.values();
    }
    
    public void setHeightmaps(Map<HeightmapType, short[]> heightmaps){
        for(Map.Entry<HeightmapType, short[]> heightmapEntry: heightmaps.entrySet()){
            final Heightmap heightmap = new Heightmap(this, heightmapEntry.getKey(), heightmapEntry.getValue());
            this.heightmaps.put(heightmap.getType(), heightmap);
        }
    }
    
    public Heightmap getHeightMap(HeightmapType type){
        return heightmaps.get(type);
    }
    
    
    public int getSkyLight(int lx, int y, int lz){
        if(isOutOfBounds(y))
            return ChunkUtils.MAX_LIGHT_LEVEL;
        
        // Находим по Y нужную секцию
        final int sectionIndex = getSectionIndex(y);
        final ChunkSection section = getSection(sectionIndex);
        if(section == null)
            return ChunkUtils.MAX_LIGHT_LEVEL;
        
        // Возвращаем блок
        return section.getSkyLight(lx, clampToLocal(y), lz);
    }

    public int getBlockLight(int lx, int y, int lz){
        if(isOutOfBounds(y))
            return ChunkUtils.MAX_LIGHT_LEVEL;

        // Находим по Y нужную секцию
        final int sectionIndex = getSectionIndex(y);
        final ChunkSection section = getSection(sectionIndex);
        // if(section == null)
        //     return ChunkUtils.MAX_LIGHT_LEVEL;

        // Возвращаем блок
        return section.getBlockLight(lx, clampToLocal(y), lz);
    }
    
    public void setSkyLight(int lx, int y, int lz, int level){
        if(isOutOfBounds(lx, y, lz))
            return;
        
        // Находим по Y нужную секцию
        final int sectionIndex = getSectionIndex(y);
        ChunkSection section = getSection(sectionIndex);
        // if(section == null)
        //     section = createSection(sectionIndex);
        
        section.setSkyLight(lx, clampToLocal(y), lz, level);

        // if(getLevel() instanceof ServerLevel serverLevel)
        //     serverLevel.getServer().getPlayerList().broadcastPacket(new CBPacketLightUpdate(section));
    }

    public void setBlockLight(int lx, int y, int lz, int level){
        if(isOutOfBounds(lx, y, lz))
            return;

        // Находим по Y нужную секцию
        final int sectionIndex = getSectionIndex(y);
        ChunkSection section = getSection(sectionIndex);
        if(section == null)
            section = createSection(sectionIndex);

        section.setBlockLight(lx, clampToLocal(y), lz, level);

        // if(getLevel() instanceof ServerLevel serverLevel)
        //     serverLevel.getServer().getPlayerList().broadcastPacket(new CBPacketLightUpdate(section));
    }


    public BiomeMap getBiomes(){
        return biomes;
    }


    public long[] getNeighbors(){
        return neighbors.array();
    }

    public long getNeighborPos(int neighborX, int neighborZ){
        return neighbors.getNeighborPackedPos(neighborX, neighborZ);
    }

    public ChunkBase getNeighborChunk(int neighborX, int neighborZ){
        return neighbors.getNeighborChunk(neighborX, neighborZ);
    }

    public ChunkBase[] getNeighborChunks(){
        return neighbors.getChunks();
    }
    
    
    @Override
    public boolean equals(Object object){
        if(object == this)
            return true;
        if(object == null || object.getClass() != getClass())
            return false;
        final ChunkBase chunk = (ChunkBase) object;
        return position.equals(chunk.position);
    }

    @Override
    public int hashCode(){
        return position.hashCode();
    }
    
}
