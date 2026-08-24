package minecraftose.client.block.vanilla;

import jpize.util.region.TextureRegion;
import minecraftose.client.block.ClientBlock;
import minecraftose.client.block.model.BlockModel;
import minecraftose.client.block.model.Face;
import minecraftose.client.block.model.Quad;
import minecraftose.client.block.shape.BlockCollide;
import minecraftose.client.block.shape.BlockCursor;
import minecraftose.client.chunk.mesh.ChunkMeshType;
import minecraftose.client.resources.GameResources;
import minecraftose.main.Dir;
import minecraftose.main.audio.SoundType;
import minecraftose.main.chunk.ChunkUtils;

public class OakPlanksStairs extends ClientBlock{

    public OakPlanksStairs(int id){
        super(id);
    }

    @Override
    public void load(GameResources resources){
        final TextureRegion region = resources.getBlockRegion("oak_planks");
        final TextureRegion halfRegion = region.copy().extrude(1, 0.5F);
        final TextureRegion quarterRegion = region.copy().extrude(0.5F, 0.5F);

        final BlockModel model = new BlockModel(ChunkMeshType.CUSTOM)
            .setFacesTransparentForNeighbors(false, true, true, true, false, true)
            .nyFace(region)
            .pzFace(region)
            .face(new Face(Quad.getNzQuad().scale(1, 0.5, 1), halfRegion))
            .face(new Face(Quad.getNxQuad().scale(1, 0.5, 1), halfRegion))
            .face(new Face(Quad.getPxQuad().scale(1, 0.5, 1), halfRegion))
            .face(new Face(Quad.getNxQuad().scale(1, 0.5, 0.5).translate(0, 0.5F, 0.5F), quarterRegion))
            .face(new Face(Quad.getPxQuad().scale(1, 0.5, 0.5).translate(0, 0.5F, 0.5F), quarterRegion));

        newState()
            .setSolid(false)
            .setLightLevel(0)
            .setOpacity(ChunkUtils.MAX_LIGHT_LEVEL)
            .setTranslucent(false)
            .setSoundPack(SoundType.WOOD)
            .setFacing(Dir.NEGATIVE_Z)
            .setModel(model)
            .setCollide(BlockCollide.SOLID)
            .setCursor(BlockCursor.SOLID);

    }

}
