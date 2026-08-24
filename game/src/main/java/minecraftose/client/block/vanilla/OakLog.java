package minecraftose.client.block.vanilla;

import minecraftose.client.block.ClientBlock;
import minecraftose.client.block.BlockRotation;
import minecraftose.client.block.model.BlockModel;
import minecraftose.client.block.shape.BlockCollide;
import minecraftose.client.block.shape.BlockCursor;
import minecraftose.client.chunk.mesh.ChunkMeshType;
import minecraftose.client.resources.GameResources;
import minecraftose.main.Dir;
import minecraftose.main.audio.SoundType;
import minecraftose.main.chunk.ChunkUtils;

public class OakLog extends ClientBlock{
    
    public OakLog(int id){
        super(id);
    }

    @Override
    public void load(GameResources resources){
        final BlockModel model = new BlockModel(ChunkMeshType.SOLID)
            .sideXZFaces(resources.getBlockRegion("oak_log"))
            .yFaces(resources.getBlockRegion("oak_log_top"));

        newState()
                .setSolid(true)
                .setLightLevel(0)
                .setOpacity(ChunkUtils.MAX_LIGHT_LEVEL)
                .setTranslucent(false)
                .setSoundPack(SoundType.WOOD)
                .setFacing(Dir.POSITIVE_Y)
                .setModel(model)
                .setCollide(BlockCollide.SOLID)
                .setCursor(BlockCursor.SOLID);

        newState()
                .setSolid(true)
                .setLightLevel(0)
                .setOpacity(ChunkUtils.MAX_LIGHT_LEVEL)
                .setTranslucent(false)
                .setSoundPack(SoundType.WOOD)
                .setFacing(Dir.NEGATIVE_Z)
                .setModel(model.rotated(BlockRotation.X90))
                .setCollide(BlockCollide.SOLID)
                .setCursor(BlockCursor.SOLID);

        newState()
                .setSolid(true)
                .setLightLevel(0)
                .setOpacity(ChunkUtils.MAX_LIGHT_LEVEL)
                .setTranslucent(false)
                .setSoundPack(SoundType.WOOD)
                .setFacing(Dir.POSITIVE_X)
                .setModel(model.rotated(BlockRotation.Z90))
                .setCollide(BlockCollide.SOLID)
                .setCursor(BlockCursor.SOLID);
    }
    
}