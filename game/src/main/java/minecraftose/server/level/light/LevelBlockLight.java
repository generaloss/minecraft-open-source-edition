package minecraftose.server.level.light;

import generaloss.spatialmath.vector.Vec3i;
import minecraftose.client.block.BlockProps;
import minecraftose.main.Dir;
import minecraftose.main.chunk.ChunkBase;
import minecraftose.server.level.LevelS;

import java.util.concurrent.ConcurrentLinkedQueue;

public class LevelBlockLight{


    /**            --------- ALGORITHM ---------            **/


    private final LevelS level;
    private final ConcurrentLinkedQueue<LightNode> bfsIncreaseQueue, bfsDecreaseQueue;

    public LevelBlockLight(LevelS level){
        this.level = level;
        this.bfsIncreaseQueue = new ConcurrentLinkedQueue<>();
        this.bfsDecreaseQueue = new ConcurrentLinkedQueue<>();
    }


    /** Распространение света */
    public synchronized void increase(ChunkBase chunk, int lx, int y, int lz, int level){
        if(chunk.getBlockLight(lx, y, lz) >= level)
            return;

        addIncreaseWithLightSet(chunk, lx, y, lz, level);
        propagateIncrease();
    }

    private synchronized void addIncrease(ChunkBase chunk, int lx, int y, int lz, int level){
        bfsIncreaseQueue.add(new LightNode(chunk, lx, y, lz, level));
    }

    private synchronized void addIncreaseWithLightSet(ChunkBase chunk, int lx, int y, int lz, int level){
        chunk.setBlockLight(lx, y, lz, level);
        addIncrease(chunk, lx, y, lz, level);
    }

    /** Алгоритм распространения света */
    private synchronized void propagateIncrease(){
        ChunkBase neighborChunk;
        int neighborX, neighborY, neighborZ;
        int targetLevel;

        // Итерируемся по нодам в очереди
        while(!bfsIncreaseQueue.isEmpty()){
            final LightNode lightEntry = bfsIncreaseQueue.poll();

            final ChunkBase chunk = lightEntry.chunk;
            final byte x = lightEntry.lx;
            final short y = lightEntry.y;
            final byte z = lightEntry.lz;
            final byte level = lightEntry.level;

            // Проверка каждого из 6 блоков вокруг текущего[x, y, z]
            for(int i = 0; i < 6; i++){
                // Находим нормаль
                final Vec3i normal = Dir.getNormal3(i);

                // Координаты соседнего блока
                neighborX = x + normal.x;
                neighborZ = z + normal.z;

                // Находим чанк соседнего блока
                if(neighborX > ChunkBase.SIZE_IDX || neighborZ > ChunkBase.SIZE_IDX || neighborX < 0 || neighborZ < 0){
                    // Если координаты выходят за границы чанка - найти соответствующий чанк
                    neighborChunk = chunk.getNeighborChunk(normal.x, normal.z);
                    if(neighborChunk == null)
                        continue;

                    // Нормализуем координаты для найденного чанка
                    neighborX = ChunkBase.clampToLocal(neighborX);
                    neighborZ = ChunkBase.clampToLocal(neighborZ);
                }else
                    // Если нет - выбрать данный чанк
                    neighborChunk = chunk;

                // Координата Y соседнего блока
                neighborY = y + normal.y;
                if(neighborY < 0 || neighborY > ChunkBase.HEIGHT_IDX)
                    continue;

                // Узнать уровень освещенности соседнего блока
                final int neighborLevel = neighborChunk.getBlockLight(neighborX, neighborY, neighborZ);

                // Если соседний уровень равен данному, или же больше - его увеличивать не нужно, так как этот свет уже исходит от другого источника
                if(neighborLevel >= level - 1)
                    continue;

                // Находим уровень освещенности который должен быть у соседнего, учитывая непрозрачность блока
                final BlockProps neighborProperties = neighborChunk.getBlockProps(neighborX, neighborY, neighborZ);
                targetLevel = level - Math.max(1, neighborProperties.getOpacity());

                // Если имеет смысл - распространяем свет уже от соседнего блока
                if(targetLevel > neighborLevel)
                    addIncreaseWithLightSet(neighborChunk, neighborX, neighborY, neighborZ, targetLevel);
            }
        }
    }


    /** Удаление света */
    public synchronized void decrease(ChunkBase chunk, int lx, int y, int lz){
        final int level = chunk.getBlockLight(lx, y, lz);
        if(level == 0)
            return;

        addDecreaseWithLightSet(chunk, lx, y, lz, level);
        propagateDecrease();
    }

    private synchronized void addDecrease(ChunkBase chunk, int lx, int y, int lz, int level){
        bfsDecreaseQueue.add(new LightNode(chunk, lx, y, lz, level));
    }

    private synchronized void addDecreaseWithLightSet(ChunkBase chunk, int lx, int y, int lz, int level){
        chunk.setBlockLight(lx, y, lz, 0);
        addDecrease(chunk, lx, y, lz, level);
    }

    /** Алгоритм удаления света **/
    private synchronized void propagateDecrease(){
        ChunkBase neighborChunk;
        int neighborX, neighborY, neighborZ;

        // Итерируемся по нодам в очереди
        while(!bfsDecreaseQueue.isEmpty()){
            final LightNode lightEntry = bfsDecreaseQueue.poll();

            final ChunkBase chunk = lightEntry.chunk;
            final byte x = lightEntry.lx;
            final short y = lightEntry.y;
            final byte z = lightEntry.lz;
            final byte level = lightEntry.level;

            // Проверка каждого из 6 блоков вокруг текущего[x, y, z]
            for(int i = 0; i < 6; i++){
                // Находим нормаль
                final Vec3i normal = Dir.getNormal3(i);

                // Координаты соседнего блока
                neighborX = x + normal.x;
                neighborZ = z + normal.z;

                // Находим чанк соседнего блока
                if(neighborX > ChunkBase.SIZE_IDX || neighborZ > ChunkBase.SIZE_IDX || neighborX < 0 || neighborZ < 0){
                    // Если координаты выходят за границы чанка - найти соответствующий чанк
                    neighborChunk = chunk.getNeighborChunk(normal.x, normal.z);
                    if(neighborChunk == null)
                        continue;

                    // Нормализуем координаты для найденного чанка
                    neighborX = ChunkBase.clampToLocal(neighborX);
                    neighborZ = ChunkBase.clampToLocal(neighborZ);
                }else
                    // Если нет - выбрать данный чанк
                    neighborChunk = chunk;

                // Координата Y соседнего блока
                neighborY = y + normal.y;
                if(neighborY < 0 || neighborY > ChunkBase.HEIGHT_IDX)
                    continue;

                // Узнать уровень освещенности соседнего блока
                final int neighborLevel = neighborChunk.getBlockLight(neighborX, neighborY, neighborZ);
                // Если он равен 0 - уменьшать дальше нечего
                // Если соседний уровень освещенности меньше данного - зануляем его и уменьшаем освещение с его позиции
                if(neighborLevel != 0 && neighborLevel < level){
                    final BlockProps neighborBlock = neighborChunk.getBlockProps(neighborX, neighborY, neighborZ);
                    // Находим уровень света, учитывая непрозрачность блока
                    // (всегда будет на 0-15 больше чем уровень освещенности в данном блоке)
                    final int decreaseLevel = Math.max(level, neighborLevel + neighborBlock.getOpacity());
                    // Math.max(0, neighborLevel - neighborBlock.getOpacity())
                    addDecreaseWithLightSet(neighborChunk, neighborX, neighborY, neighborZ, decreaseLevel);
                }else if(neighborLevel >= level)
                    // Если соседний уровень равен данному, или же больше - его уменьшать нельзя, так как этот свет уже исходит от другого источника
                    // Просто увеличиваем от него свет, так как до этого все зануляли
                    addIncrease(neighborChunk, neighborX, neighborY, neighborZ, neighborLevel);
            }
        }

        propagateIncrease();
    }

}













