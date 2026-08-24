package minecraftose.client.block.model;

import jpize.util.color.Color;
import jpize.util.region.Region;
import minecraftose.client.block.BlockProps;
import minecraftose.client.block.BlockRotation;
import minecraftose.client.chunk.builder.BiomeMix;
import minecraftose.client.chunk.mesh.ChunkMesh;
import minecraftose.client.chunk.mesh.ChunkMeshType;
import minecraftose.client.chunk.builder.ChunkBuilder;
import minecraftose.main.Dir;
import minecraftose.main.biome.Biome;

import java.util.*;

import static minecraftose.main.chunk.ChunkUtils.MAX_LIGHT_LEVEL;

public class BlockModel{

    private final ChunkMeshType meshType;
    private final List<Face> faces, nxFaces, pxFaces, nyFaces, pyFaces, nzFaces, pzFaces;
    private final Map<Dir, List<Face>> facesFromDirection;
    private final boolean[] transparentForNeighbors;

    public BlockModel(ChunkMeshType meshType){
        this.meshType = meshType;

        this.faces = new ArrayList<>();
        this.nxFaces = new ArrayList<>();
        this.pxFaces = new ArrayList<>();
        this.nyFaces = new ArrayList<>();
        this.pyFaces = new ArrayList<>();
        this.nzFaces = new ArrayList<>();
        this.pzFaces = new ArrayList<>();

        this.facesFromDirection = new HashMap<>();

        facesFromDirection.put(Dir.NEGATIVE_X, nxFaces);
        facesFromDirection.put(Dir.POSITIVE_X, pxFaces);
        facesFromDirection.put(Dir.NEGATIVE_Y, nyFaces);
        facesFromDirection.put(Dir.POSITIVE_Y, pyFaces);
        facesFromDirection.put(Dir.NEGATIVE_Z, nzFaces);
        facesFromDirection.put(Dir.POSITIVE_Z, pzFaces);
        facesFromDirection.put(Dir.NONE, faces);

        this.transparentForNeighbors = new boolean[6];
    }

    public ChunkMeshType getMeshType(){
        return meshType;
    }

    private ChunkMesh getChunkBuilderMesh(ChunkBuilder chunkBuilder){
        return switch(meshType){
            case SOLID -> chunkBuilder.solidMesh;
            case CUSTOM -> chunkBuilder.customMesh;
            case TRANSLUCENT -> chunkBuilder.translucentMesh;
        };
    }


    public List<Face> getDirectionFaces(Dir dir){
        return facesFromDirection.get(dir);
    }

    public List<Face> getFacesFromNormal(int x, int y, int z){
        return getDirectionFaces(Dir.fromNormal(x, y, z));
    }


    public boolean isFaceTransparentForNeighbors(int x, int y, int z){
        final int index = Dir.fromNormal(x, y, z).ordinal();
        if(index > 5)
            return false;

        return transparentForNeighbors[index];
    }

    public BlockModel setFacesTransparentForNeighbors(boolean... transparentForNeighbors){
        System.arraycopy(transparentForNeighbors, 0, this.transparentForNeighbors, 0, transparentForNeighbors.length);
        return this;
    }

    public BlockModel setFacesTransparentForNeighbors(boolean transparentForNeighbors){
        Arrays.fill(this.transparentForNeighbors, transparentForNeighbors);
        return this;
    }


    public BlockModel face(Face face){
        faces.add(face);
        return this;
    }

    public BlockModel face(Dir dir, Face face){
        switch(dir){
            case NEGATIVE_X -> nxFaces.add(face);
            case POSITIVE_X -> pxFaces.add(face);
            case NEGATIVE_Y -> nyFaces.add(face);
            case POSITIVE_Y -> pyFaces.add(face);
            case NEGATIVE_Z -> nzFaces.add(face);
            case POSITIVE_Z -> pzFaces.add(face);
            case NONE -> faces.add(face);
        }
        return this;
    }


    public BlockModel nxFace(Region region, Color color, byte faceData){
        nxFaces.add(
                new Face(Quad.getNxQuad(), region, color)
                .setFaceData(faceData)
        );
        return this;
    }

    public BlockModel pxFace(Region region, Color color, byte faceData){
        pxFaces.add(
                new Face(Quad.getPxQuad(), region, color)
                .setFaceData(faceData)
        );
        return this;
    }

    public BlockModel nyFace(Region region, Color color, byte faceData){
        nyFaces.add(
                new Face(Quad.getNyQuad(), region, color)
                .setFaceData(faceData)
        );
        return this;
    }

    public BlockModel pyFace(Region region, Color color, byte faceData){
        pyFaces.add(
                new Face(Quad.getPyQuad(), region, color)
                .setFaceData(faceData)
        );
        return this;
    }

    public BlockModel nzFace(Region region, Color color, byte faceData){
        nzFaces.add(
                new Face(Quad.getNzQuad(), region, color)
                .setFaceData(faceData)
        );
        return this;
    }

    public BlockModel pzFace(Region region, Color color, byte faceData){
        pzFaces.add(
                new Face(Quad.getPzQuad(), region, color)
                .setFaceData(faceData)
        );
        return this;
    }


    public BlockModel nxFace(Region region, byte faceData){
        return nxFace(region, new Color(), faceData);
    }

    public BlockModel pxFace(Region region, byte faceData){
        return pxFace(region, new Color(), faceData);
    }

    public BlockModel nyFace(Region region, byte faceData){
        return nyFace(region, new Color(), faceData);
    }

    public BlockModel pyFace(Region region, byte faceData){
        return pyFace(region, new Color(), faceData);
    }

    public BlockModel nzFace(Region region, byte faceData){
        return nzFace(region, new Color(), faceData);
    }

    public BlockModel pzFace(Region region, byte faceData){
        return pzFace(region, new Color(), faceData);
    }


    public BlockModel nxFace(Region region, Color color){
        return nxFace(region, color, (byte) 0);
    }

    public BlockModel pxFace(Region region, Color color){
        return pxFace(region, color, (byte) 0);
    }

    public BlockModel nyFace(Region region, Color color){
        return nyFace(region, color, (byte) 0);
    }

    public BlockModel pyFace(Region region, Color color){
        return pyFace(region, color, (byte) 0);
    }

    public BlockModel nzFace(Region region, Color color){
        return nzFace(region, color, (byte) 0);
    }

    public BlockModel pzFace(Region region, Color color){
        return pzFace(region, color, (byte) 0);
    }


    public BlockModel allFaces(Region region, Color color, byte faceData){
        nxFace(region, color, faceData);
        pxFace(region, color, faceData);
        nyFace(region, color, faceData);
        pyFace(region, color, faceData);
        nzFace(region, color, faceData);
        pzFace(region, color, faceData);
        return this;
    }

    public BlockModel allFaces(Region region, Color color){
        return allFaces(region, color, (byte) 0);
    }

    public BlockModel allFaces(Region region, byte faceData){
        return allFaces(region, new Color(), faceData);
    }

    public BlockModel sideXZFaces(Region region, Color color, byte faceData){
        nxFace(region, color, faceData);
        pxFace(region, color, faceData);
        nzFace(region, color, faceData);
        pzFace(region, color, faceData);
        return this;
    }

    public BlockModel sideXZFaces(Region region, Color color){
        return sideXZFaces(region, color, (byte) 0);
    }

    public BlockModel sideXZFaces(Region region, byte faceData){
        return sideXZFaces(region, new Color(), faceData);
    }

    public BlockModel sideXYFaces(Region region, Color color){
        nxFace(region, color);
        pxFace(region, color);
        nyFace(region, color);
        pyFace(region, color);
        return this;
    }

    public BlockModel sideZYFaces(Region region, Color color){
        nzFace(region, color);
        pzFace(region, color);
        nyFace(region, color);
        pyFace(region, color);
        return this;
    }

    public BlockModel xFaces(Region region, Color color){
        nxFace(region, color);
        pxFace(region, color);
        return this;
    }

    public BlockModel yFaces(Region region, Color color){
        nyFace(region, color);
        pyFace(region, color);
        return this;
    }

    public BlockModel zFaces(Region region, Color color){
        nzFace(region, color);
        pzFace(region, color);
        return this;
    }


    public BlockModel nxFace(Region region){
        return nxFace(region, new Color());
    }

    public BlockModel pxFace(Region region){
        return pxFace(region, new Color());
    }

    public BlockModel nyFace(Region region){
        return nyFace(region, new Color());
    }

    public BlockModel pyFace(Region region){
        return pyFace(region, new Color());
    }

    public BlockModel nzFace(Region region){
        return nzFace(region, new Color());
    }

    public BlockModel pzFace(Region region){
        return pzFace(region, new Color());
    }

    public BlockModel allFaces(Region region){
        return allFaces(region, new Color());
    }

    public BlockModel sideXZFaces(Region region){
        return sideXZFaces(region, new Color());
    }

    public BlockModel sideXYFaces(Region region){
        return sideXYFaces(region, new Color());
    }

    public BlockModel sideZYFaces(Region region){
        return sideZYFaces(region, new Color());
    }

    public BlockModel xFaces(Region region){
        return xFaces(region, new Color());
    }

    public BlockModel yFaces(Region region){
        return yFaces(region, new Color());
    }

    public BlockModel zFaces(Region region){
        return zFaces(region, new Color());
    }


    public Color pickFaceColor(Face face, Biome biome){
        final Color color;
        if(face.isGrassColoring())
            color = biome.grassColor;
        else if(face.isWaterColoring())
            color = biome.waterColor;
        else
            color = new Color();

        return color;
    }

    public Color pickFaceColor(Face face, BiomeMix biome){
        final Color color;
        if(face.isGrassColoring())
            color = biome.getGrassColor();
        else if(face.isWaterColoring())
            color = biome.getWaterColor();
        else
            color = new Color();

        return color;
    }


    public void build(ChunkBuilder builder, BlockProps block, int lx, int y, int lz){
        // Custom faces
        final float skyLight = 1.000F;//(float) builder.chunk.getSkyLight(lx, y, lz) / MAX_LIGHT_LEVEL;
        final float blockLight = (float) builder.chunk.getBlockLight(lx, y, lz) / MAX_LIGHT_LEVEL;
        for(Face face: faces){
            final Color color = pickFaceColor(face, builder.currentBiome);
            face.putFloats(builder.customMesh,  lx, y, lz,  color, color, color, color,  1, 1, 1, 1,  skyLight, skyLight, skyLight, skyLight,  blockLight, blockLight, blockLight, blockLight);
        }

        // Solid faces
        buildSolidFaces(builder, block, lx, y, lz);
    }

    private void buildSolidFaces(ChunkBuilder builder, BlockProps block, int lx, int y, int lz){
        if(builder.isGenSolidFace(lx, y, lz, -1,  0,  0, block))
            for(Face face: nxFaces) buildNxFace(builder, face, lx, y, lz);
        if(builder.isGenSolidFace(lx, y, lz, +1,  0,  0, block))
            for(Face face: pxFaces) buildPxFace(builder, face, lx, y, lz);
        if(builder.isGenSolidFace(lx, y, lz,  0, -1,  0, block))
            for(Face face: nyFaces) buildNyFace(builder, face, lx, y, lz);
        if(builder.isGenSolidFace(lx, y, lz,  0, +1,  0, block))
            for(Face face: pyFaces) buildPyFace(builder, face, lx, y, lz);
        if(builder.isGenSolidFace(lx, y, lz,  0,  0, -1, block))
            for(Face face: nzFaces) buildNzFace(builder, face, lx, y, lz);
        if(builder.isGenSolidFace(lx, y, lz,  0,  0, +1, block))
            for(Face face: pzFaces) buildPzFace(builder, face, lx, y, lz);
    }

    private void buildNxFace(ChunkBuilder builder, Face face, int x, int y, int z){
        final float skyLight1 = 1.000F;//builder.getSkyLight(x-1, y  , z,  x-1, y, z+1,  x-1, y+1, z+1,  x-1, y+1, z  ) / MAX_LIGHT_LEVEL;
        final float skyLight2 = 1.000F;//builder.getSkyLight(x-1, y  , z,  x-1, y, z+1,  x-1, y-1, z+1,  x-1, y-1, z  ) / MAX_LIGHT_LEVEL;
        final float skyLight3 = 1.000F;//builder.getSkyLight(x-1, y  , z,  x-1, y, z-1,  x-1, y-1, z-1,  x-1, y-1, z  ) / MAX_LIGHT_LEVEL;
        final float skyLight4 = 1.000F;//builder.getSkyLight(x-1, y  , z,  x-1, y, z-1,  x-1, y+1, z-1,  x-1, y+1, z  ) / MAX_LIGHT_LEVEL;

        final float blockLight1 = builder.getBlockLight(x-1, y  , z,  x-1, y, z+1,  x-1, y+1, z+1,  x-1, y+1, z  ) / MAX_LIGHT_LEVEL;
        final float blockLight2 = builder.getBlockLight(x-1, y  , z,  x-1, y, z+1,  x-1, y-1, z+1,  x-1, y-1, z  ) / MAX_LIGHT_LEVEL;
        final float blockLight3 = builder.getBlockLight(x-1, y  , z,  x-1, y, z-1,  x-1, y-1, z-1,  x-1, y-1, z  ) / MAX_LIGHT_LEVEL;
        final float blockLight4 = builder.getBlockLight(x-1, y  , z,  x-1, y, z-1,  x-1, y+1, z-1,  x-1, y+1, z  ) / MAX_LIGHT_LEVEL;

        final BiomeMix biomes1 = builder.getBiomeMix(x-1, z,  x-1, z+1,  x-1, z+1,  x-1, z  );
        final BiomeMix biomes2 = builder.getBiomeMix(x-1, z,  x-1, z+1,  x-1, z+1,  x-1, z  );
        final BiomeMix biomes3 = builder.getBiomeMix(x-1, z,  x-1, z-1,  x-1, z-1,  x-1, z  );
        final BiomeMix biomes4 = builder.getBiomeMix(x-1, z,  x-1, z-1,  x-1, z-1,  x-1, z  );

        final float shadow = 0.8F;

        final float ao1 = builder.getAO(x-1, y+1, z,  x-1, y, z+1,  x-1, y+1, z+1,  x-1, y  , z  ) * shadow;
        final float ao2 = builder.getAO(x-1, y-1, z,  x-1, y, z+1,  x-1, y-1, z+1,  x-1, y  , z  ) * shadow;
        final float ao3 = builder.getAO(x-1, y-1, z,  x-1, y, z-1,  x-1, y-1, z-1,  x-1, y  , z  ) * shadow;
        final float ao4 = builder.getAO(x-1, y+1, z,  x-1, y, z-1,  x-1, y+1, z-1,  x-1, y  , z  ) * shadow;

        final Color col1 = pickFaceColor(face, biomes1);
        final Color col2 = pickFaceColor(face, biomes2);
        final Color col3 = pickFaceColor(face, biomes3);
        final Color col4 = pickFaceColor(face, biomes4);

        final ChunkMesh mesh =
            (meshType == ChunkMeshType.TRANSLUCENT) ?
                builder.translucentMesh :
                (meshType == ChunkMeshType.CUSTOM) ? builder.customMesh :
                    builder.solidMesh;
        face.putFacePacked(mesh,  x, y, z,  col1, col2, col3, col4,  ao1, ao2, ao3, ao4,  skyLight1, skyLight2, skyLight3, skyLight4,  blockLight1, blockLight2, blockLight3, blockLight4);
    }

    private void buildPxFace(ChunkBuilder builder, Face face, int x, int y, int z){
        final float skyLight1 = 1.000F;//builder.getSkyLight(x+1, y, z,  x+1, y, z-1,  x+1, y+1, z-1,  x+1, y+1, z  ) / MAX_LIGHT_LEVEL;
        final float skyLight2 = 1.000F;//builder.getSkyLight(x+1, y, z,  x+1, y, z-1,  x+1, y-1, z-1,  x+1, y-1, z  ) / MAX_LIGHT_LEVEL;
        final float skyLight3 = 1.000F;//builder.getSkyLight(x+1, y, z,  x+1, y, z+1,  x+1, y-1, z+1,  x+1, y-1, z  ) / MAX_LIGHT_LEVEL;
        final float skyLight4 = 1.000F;//builder.getSkyLight(x+1, y, z,  x+1, y, z+1,  x+1, y+1, z+1,  x+1, y+1, z  ) / MAX_LIGHT_LEVEL;

        final float blockLight1 = builder.getBlockLight(x+1, y, z,  x+1, y, z-1,  x+1, y+1, z-1,  x+1, y+1, z  ) / MAX_LIGHT_LEVEL;
        final float blockLight2 = builder.getBlockLight(x+1, y, z,  x+1, y, z-1,  x+1, y-1, z-1,  x+1, y-1, z  ) / MAX_LIGHT_LEVEL;
        final float blockLight3 = builder.getBlockLight(x+1, y, z,  x+1, y, z+1,  x+1, y-1, z+1,  x+1, y-1, z  ) / MAX_LIGHT_LEVEL;
        final float blockLight4 = builder.getBlockLight(x+1, y, z,  x+1, y, z+1,  x+1, y+1, z+1,  x+1, y+1, z  ) / MAX_LIGHT_LEVEL;

        final BiomeMix biomes1 = builder.getBiomeMix(x+1, z,  x+1, z-1,  x+1, z-1,  x+1, z  );
        final BiomeMix biomes2 = builder.getBiomeMix(x+1, z,  x+1, z-1,  x+1, z-1,  x+1, z  );
        final BiomeMix biomes3 = builder.getBiomeMix(x+1, z,  x+1, z+1,  x+1, z+1,  x+1, z  );
        final BiomeMix biomes4 = builder.getBiomeMix(x+1, z,  x+1, z+1,  x+1, z+1,  x+1, z  );

        final float shadow = 0.8F;

        final float ao1 = builder.getAO(x+1, y+1, z,  x+1, y, z-1,  x+1, y+1, z-1,  x+1, y  , z  ) * shadow;
        final float ao2 = builder.getAO(x+1, y-1, z,  x+1, y, z-1,  x+1, y-1, z-1,  x+1, y  , z  ) * shadow;
        final float ao3 = builder.getAO(x+1, y-1, z,  x+1, y, z+1,  x+1, y-1, z+1,  x+1, y  , z  ) * shadow;
        final float ao4 = builder.getAO(x+1, y+1, z,  x+1, y, z+1,  x+1, y+1, z+1,  x+1, y  , z  ) * shadow;

        final Color col1 = pickFaceColor(face, biomes1);
        final Color col2 = pickFaceColor(face, biomes2);
        final Color col3 = pickFaceColor(face, biomes3);
        final Color col4 = pickFaceColor(face, biomes4);

        final ChunkMesh mesh =
            (meshType == ChunkMeshType.TRANSLUCENT) ?
                builder.translucentMesh :
                (meshType == ChunkMeshType.CUSTOM) ? builder.customMesh :
                    builder.solidMesh;
        face.putFacePacked(mesh,  x, y, z,  col1, col2, col3, col4,  ao1, ao2, ao3, ao4,  skyLight1, skyLight2, skyLight3, skyLight4,  blockLight1, blockLight2, blockLight3, blockLight4);
    }

    private void buildNyFace(ChunkBuilder builder, Face face, int x, int y, int z){
        final float skyLight1 = 1.000F;//builder.getSkyLight(x, y-1, z,  x, y-1, z+1,  x+1, y-1, z+1,  x+1, y-1, z  ) / MAX_LIGHT_LEVEL;
        final float skyLight2 = 1.000F;//builder.getSkyLight(x, y-1, z,  x, y-1, z-1,  x+1, y-1, z-1,  x+1, y-1, z  ) / MAX_LIGHT_LEVEL;
        final float skyLight3 = 1.000F;//builder.getSkyLight(x, y-1, z,  x, y-1, z-1,  x-1, y-1, z-1,  x-1, y-1, z  ) / MAX_LIGHT_LEVEL;
        final float skyLight4 = 1.000F;//builder.getSkyLight(x, y-1, z,  x, y-1, z+1,  x-1, y-1, z+1,  x-1, y-1, z  ) / MAX_LIGHT_LEVEL;

        final float blockLight1 = builder.getBlockLight(x, y-1, z,  x, y-1, z+1,  x+1, y-1, z+1,  x+1, y-1, z  ) / MAX_LIGHT_LEVEL;
        final float blockLight2 = builder.getBlockLight(x, y-1, z,  x, y-1, z-1,  x+1, y-1, z-1,  x+1, y-1, z  ) / MAX_LIGHT_LEVEL;
        final float blockLight3 = builder.getBlockLight(x, y-1, z,  x, y-1, z-1,  x-1, y-1, z-1,  x-1, y-1, z  ) / MAX_LIGHT_LEVEL;
        final float blockLight4 = builder.getBlockLight(x, y-1, z,  x, y-1, z+1,  x-1, y-1, z+1,  x-1, y-1, z  ) / MAX_LIGHT_LEVEL;

        final BiomeMix biomes1 = builder.getBiomeMix(x, z,  x, z+1,  x+1, z+1,  x+1, z  );
        final BiomeMix biomes2 = builder.getBiomeMix(x, z,  x, z-1,  x+1, z-1,  x+1, z  );
        final BiomeMix biomes3 = builder.getBiomeMix(x, z,  x, z-1,  x-1, z-1,  x-1, z  );
        final BiomeMix biomes4 = builder.getBiomeMix(x, z,  x, z+1,  x-1, z+1,  x-1, z  );

        final float shadow = 0.6F;

        final float ao1 = builder.getAO(x+1, y-1, z,  x, y-1, z+1,  x+1, y-1, z+1,  x  , y-1, z  ) * shadow;
        final float ao2 = builder.getAO(x+1, y-1, z,  x, y-1, z-1,  x+1, y-1, z-1,  x  , y-1, z  ) * shadow;
        final float ao3 = builder.getAO(x-1, y-1, z,  x, y-1, z-1,  x-1, y-1, z-1,  x  , y-1, z  ) * shadow;
        final float ao4 = builder.getAO(x-1, y-1, z,  x, y-1, z+1,  x-1, y-1, z+1,  x  , y-1, z  ) * shadow;

        final Color col1 = pickFaceColor(face, biomes1);
        final Color col2 = pickFaceColor(face, biomes2);
        final Color col3 = pickFaceColor(face, biomes3);
        final Color col4 = pickFaceColor(face, biomes4);

        final ChunkMesh mesh =
            (meshType == ChunkMeshType.TRANSLUCENT) ?
                builder.translucentMesh :
                (meshType == ChunkMeshType.CUSTOM) ? builder.customMesh :
                    builder.solidMesh;
        face.putFacePacked(mesh,  x, y, z,  col1, col2, col3, col4,  ao1, ao2, ao3, ao4,  skyLight1, skyLight2, skyLight3, skyLight4,  blockLight1, blockLight2, blockLight3, blockLight4);
    }

    private void buildPyFace(ChunkBuilder builder, Face face, int x, int y, int z){
        final float skyLight1 = 1.000F;//builder.getSkyLight(x, y+1, z,  x, y+1, z-1,  x+1, y+1, z-1,  x+1, y+1, z  ) / MAX_LIGHT_LEVEL;
        final float skyLight2 = 1.000F;//builder.getSkyLight(x, y+1, z,  x, y+1, z+1,  x+1, y+1, z+1,  x+1, y+1, z  ) / MAX_LIGHT_LEVEL;
        final float skyLight3 = 1.000F;//builder.getSkyLight(x, y+1, z,  x, y+1, z+1,  x-1, y+1, z+1,  x-1, y+1, z  ) / MAX_LIGHT_LEVEL;
        final float skyLight4 = 1.000F;//builder.getSkyLight(x, y+1, z,  x, y+1, z-1,  x-1, y+1, z-1,  x-1, y+1, z  ) / MAX_LIGHT_LEVEL;

        final float blockLight1 = builder.getBlockLight(x, y+1, z,  x, y+1, z-1,  x+1, y+1, z-1,  x+1, y+1, z  ) / MAX_LIGHT_LEVEL;
        final float blockLight2 = builder.getBlockLight(x, y+1, z,  x, y+1, z+1,  x+1, y+1, z+1,  x+1, y+1, z  ) / MAX_LIGHT_LEVEL;
        final float blockLight3 = builder.getBlockLight(x, y+1, z,  x, y+1, z+1,  x-1, y+1, z+1,  x-1, y+1, z  ) / MAX_LIGHT_LEVEL;
        final float blockLight4 = builder.getBlockLight(x, y+1, z,  x, y+1, z-1,  x-1, y+1, z-1,  x-1, y+1, z  ) / MAX_LIGHT_LEVEL;

        final BiomeMix biomes1 = builder.getBiomeMix(x, z,  x, z-1,  x+1, z-1,  x+1, z  );
        final BiomeMix biomes2 = builder.getBiomeMix(x, z,  x, z+1,  x+1, z+1,  x+1, z  );
        final BiomeMix biomes3 = builder.getBiomeMix(x, z,  x, z+1,  x-1, z+1,  x-1, z  );
        final BiomeMix biomes4 = builder.getBiomeMix(x, z,  x, z-1,  x-1, z-1,  x-1, z  );

        final float shadow = 1;

        final float ao1 = builder.getAO(x+1, y+1, z,  x, y+1, z-1,  x+1, y+1, z-1,  x  , y+1, z  ) * shadow;
        final float ao2 = builder.getAO(x+1, y+1, z,  x, y+1, z+1,  x+1, y+1, z+1,  x  , y+1, z  ) * shadow;
        final float ao3 = builder.getAO(x-1, y+1, z,  x, y+1, z+1,  x-1, y+1, z+1,  x  , y+1, z  ) * shadow;
        final float ao4 = builder.getAO(x-1, y+1, z,  x, y+1, z-1,  x-1, y+1, z-1,  x  , y+1, z  ) * shadow;

        final Color col1 = pickFaceColor(face, biomes1);
        final Color col2 = pickFaceColor(face, biomes2);
        final Color col3 = pickFaceColor(face, biomes3);
        final Color col4 = pickFaceColor(face, biomes4);

        final ChunkMesh mesh =
            (meshType == ChunkMeshType.TRANSLUCENT) ?
                builder.translucentMesh :
                (meshType == ChunkMeshType.CUSTOM) ? builder.customMesh :
                    builder.solidMesh;
        face.putFacePacked(mesh,  x, y, z,  col1, col2, col3, col4,  ao1, ao2, ao3, ao4,  skyLight1, skyLight2, skyLight3, skyLight4,  blockLight1, blockLight2, blockLight3, blockLight4);
    }

    private void buildNzFace(ChunkBuilder builder, Face face, int x, int y, int z){
        final float skyLight1 = 1.000F;//builder.getSkyLight(x, y, z-1,  x, y+1, z-1,  x-1, y+1, z-1,  x, y+1, z-1  ) / MAX_LIGHT_LEVEL;
        final float skyLight2 = 1.000F;//builder.getSkyLight(x, y, z-1,  x, y-1, z-1,  x-1, y-1, z-1,  x, y-1, z-1  ) / MAX_LIGHT_LEVEL;
        final float skyLight3 = 1.000F;//builder.getSkyLight(x, y, z-1,  x, y-1, z-1,  x+1, y-1, z-1,  x, y-1, z-1  ) / MAX_LIGHT_LEVEL;
        final float skyLight4 = 1.000F;//builder.getSkyLight(x, y, z-1,  x, y+1, z-1,  x+1, y+1, z-1,  x, y+1, z-1  ) / MAX_LIGHT_LEVEL;

        final float blockLight1 = builder.getBlockLight(x, y, z-1,  x, y+1, z-1,  x-1, y+1, z-1,  x, y+1, z-1  ) / MAX_LIGHT_LEVEL;
        final float blockLight2 = builder.getBlockLight(x, y, z-1,  x, y-1, z-1,  x-1, y-1, z-1,  x, y-1, z-1  ) / MAX_LIGHT_LEVEL;
        final float blockLight3 = builder.getBlockLight(x, y, z-1,  x, y-1, z-1,  x+1, y-1, z-1,  x, y-1, z-1  ) / MAX_LIGHT_LEVEL;
        final float blockLight4 = builder.getBlockLight(x, y, z-1,  x, y+1, z-1,  x+1, y+1, z-1,  x, y+1, z-1  ) / MAX_LIGHT_LEVEL;

        final BiomeMix biomes1 = builder.getBiomeMix(x, z-1,  x, z-1,  x-1, z-1,  x, z-1  );
        final BiomeMix biomes2 = builder.getBiomeMix(x, z-1,  x, z-1,  x-1, z-1,  x, z-1  );
        final BiomeMix biomes3 = builder.getBiomeMix(x, z-1,  x, z-1,  x+1, z-1,  x, z-1  );
        final BiomeMix biomes4 = builder.getBiomeMix(x, z-1,  x, z-1,  x+1, z-1,  x, z-1  );

        final float shadow = 0.7F;

        final float ao1 = builder.getAO(x-1, y, z-1,  x, y+1, z-1,  x-1, y+1, z-1,  x  , y  , z-1) * shadow;
        final float ao2 = builder.getAO(x-1, y, z-1,  x, y-1, z-1,  x-1, y-1, z-1,  x  , y  , z-1) * shadow;
        final float ao3 = builder.getAO(x+1, y, z-1,  x, y-1, z-1,  x+1, y-1, z-1,  x  , y  , z-1) * shadow;
        final float ao4 = builder.getAO(x+1, y, z-1,  x, y+1, z-1,  x+1, y+1, z-1,  x  , y  , z-1) * shadow;

        final Color col1 = pickFaceColor(face, biomes1);
        final Color col2 = pickFaceColor(face, biomes2);
        final Color col3 = pickFaceColor(face, biomes3);
        final Color col4 = pickFaceColor(face, biomes4);

        final ChunkMesh mesh =
            (meshType == ChunkMeshType.TRANSLUCENT) ?
                builder.translucentMesh :
                (meshType == ChunkMeshType.CUSTOM) ? builder.customMesh :
                    builder.solidMesh;
        face.putFacePacked(mesh,  x, y, z,  col1, col2, col3, col4,  ao1, ao2, ao3, ao4,  skyLight1, skyLight2, skyLight3, skyLight4,  blockLight1, blockLight2, blockLight3, blockLight4);
    }

    private void buildPzFace(ChunkBuilder builder, Face face, int x, int y, int z){
        final float skyLight1 = 1.000F;//builder.getSkyLight(x, y, z+1,  x, y+1, z+1,  x+1, y+1, z+1,  x+1, y, z+1  ) / MAX_LIGHT_LEVEL;
        final float skyLight2 = 1.000F;//builder.getSkyLight(x, y, z+1,  x, y-1, z+1,  x+1, y-1, z+1,  x+1, y, z+1  ) / MAX_LIGHT_LEVEL;
        final float skyLight3 = 1.000F;//builder.getSkyLight(x, y, z+1,  x, y-1, z+1,  x-1, y-1, z+1,  x-1, y, z+1  ) / MAX_LIGHT_LEVEL;
        final float skyLight4 = 1.000F;//builder.getSkyLight(x, y, z+1,  x, y+1, z+1,  x-1, y+1, z+1,  x-1, y, z+1  ) / MAX_LIGHT_LEVEL;

        final float blockLight1 = builder.getBlockLight(x, y, z+1,  x, y+1, z+1,  x+1, y+1, z+1,  x+1, y, z+1  ) / MAX_LIGHT_LEVEL;
        final float blockLight2 = builder.getBlockLight(x, y, z+1,  x, y-1, z+1,  x+1, y-1, z+1,  x+1, y, z+1  ) / MAX_LIGHT_LEVEL;
        final float blockLight3 = builder.getBlockLight(x, y, z+1,  x, y-1, z+1,  x-1, y-1, z+1,  x-1, y, z+1  ) / MAX_LIGHT_LEVEL;
        final float blockLight4 = builder.getBlockLight(x, y, z+1,  x, y+1, z+1,  x-1, y+1, z+1,  x-1, y, z+1  ) / MAX_LIGHT_LEVEL;

        final BiomeMix biomes1 = builder.getBiomeMix(x, z+1,  x, z+1,  x+1, z+1,  x+1, z+1  );
        final BiomeMix biomes2 = builder.getBiomeMix(x, z+1,  x, z+1,  x+1, z+1,  x+1, z+1  );
        final BiomeMix biomes3 = builder.getBiomeMix(x, z+1,  x, z+1,  x-1, z+1,  x-1, z+1  );
        final BiomeMix biomes4 = builder.getBiomeMix(x, z+1,  x, z+1,  x-1, z+1,  x-1, z+1  );

        final float shadow = 0.7F;

        final float ao1 = builder.getAO(x+1, y, z+1,  x, y+1, z+1,  x+1, y+1, z+1,  x  , y  , z+1) * shadow;
        final float ao2 = builder.getAO(x+1, y, z+1,  x, y-1, z+1,  x+1, y-1, z+1,  x  , y  , z+1) * shadow;
        final float ao3 = builder.getAO(x-1, y, z+1,  x, y-1, z+1,  x-1, y-1, z+1,  x  , y  , z+1) * shadow;
        final float ao4 = builder.getAO(x-1, y, z+1,  x, y+1, z+1,  x-1, y+1, z+1,  x  , y  , z+1) * shadow;

        final Color col1 = pickFaceColor(face, biomes1);
        final Color col2 = pickFaceColor(face, biomes2);
        final Color col3 = pickFaceColor(face, biomes3);
        final Color col4 = pickFaceColor(face, biomes4);

        final ChunkMesh mesh =
            (meshType == ChunkMeshType.TRANSLUCENT) ?
                builder.translucentMesh :
                (meshType == ChunkMeshType.CUSTOM) ? builder.customMesh :
                    builder.solidMesh;
        face.putFacePacked(mesh,  x, y, z,  col1, col2, col3, col4,  ao1, ao2, ao3, ao4,  skyLight1, skyLight2, skyLight3, skyLight4,  blockLight1, blockLight2, blockLight3, blockLight4);
    }


    public BlockModel rotated(BlockRotation rotation){
        final BlockModel model = new BlockModel(meshType);

        for(Face face: faces)
            model.face(face.rotated(rotation).setFaceData(face.faceData));

        for(Face face: nxFaces){
            switch(rotation){
                case X90 -> model.face(Dir.NEGATIVE_X, new Face(Quad.getNxQuad(),  face.t2, face.t3, face.t4, face.t1,  face.color).setFaceData(face.faceData));
                case Y90 -> model.face(Dir.NEGATIVE_Z, new Face(Quad.getNzQuad(),  face.t1, face.t2, face.t3, face.t4,  face.color).setFaceData(face.faceData));
                case Z90 -> model.face(Dir.POSITIVE_Y, new Face(Quad.getPyQuad(),  face.t4, face.t1, face.t2, face.t3,  face.color).setFaceData(face.faceData));
            }
        }
        for(Face face: pxFaces){
            switch(rotation){
                case X90 -> model.face(Dir.POSITIVE_X, new Face(Quad.getPxQuad(),  face.t4, face.t1, face.t2, face.t3,  face.color).setFaceData(face.faceData));
                case Y90 -> model.face(Dir.POSITIVE_Z, new Face(Quad.getPzQuad(),  face.t1, face.t2, face.t3, face.t4,  face.color).setFaceData(face.faceData));
                case Z90 -> model.face(Dir.NEGATIVE_Y, new Face(Quad.getNyQuad(),  face.t4, face.t1, face.t2, face.t3,  face.color).setFaceData(face.faceData));
            }
        }

        for(Face face: nyFaces){
            switch(rotation){
                case X90 -> model.face(Dir.POSITIVE_Z, new Face(Quad.getPzQuad(),  face.t1, face.t2, face.t3, face.t4,  face.color).setFaceData(face.faceData));
                case Y90 -> model.face(Dir.NEGATIVE_Y, new Face(Quad.getNyQuad(),  face.t2, face.t3, face.t4, face.t1,  face.color).setFaceData(face.faceData));
                case Z90 -> model.face(Dir.NEGATIVE_X, new Face(Quad.getNxQuad(),  face.t4, face.t1, face.t2, face.t3,  face.color).setFaceData(face.faceData));
            }
        }
        for(Face face: pyFaces){
            switch(rotation){
                case X90 -> model.face(Dir.NEGATIVE_Z, new Face(Quad.getNzQuad(),  face.t3, face.t4, face.t1, face.t2,  face.color).setFaceData(face.faceData));
                case Y90 -> model.face(Dir.POSITIVE_Y, new Face(Quad.getPyQuad(),  face.t4, face.t1, face.t2, face.t3,  face.color).setFaceData(face.faceData));
                case Z90 -> model.face(Dir.POSITIVE_X, new Face(Quad.getPxQuad(),  face.t4, face.t1, face.t2, face.t3,  face.color).setFaceData(face.faceData));
            }
        }

        for(Face face: nzFaces){
            switch(rotation){
                case X90 -> model.face(Dir.NEGATIVE_Y, new Face(Quad.getNyQuad(),  face.t3, face.t4, face.t1, face.t2,  face.color).setFaceData(face.faceData));
                case Y90 -> model.face(Dir.POSITIVE_X, new Face(Quad.getPxQuad(),  face.t1, face.t2, face.t3, face.t4,  face.color).setFaceData(face.faceData));
                case Z90 -> model.face(Dir.NEGATIVE_Z, new Face(Quad.getNzQuad(),  face.t2, face.t3, face.t4, face.t1,  face.color).setFaceData(face.faceData));
            }
        }
        for(Face face: pzFaces){
            switch(rotation){
                case X90 -> model.face(Dir.POSITIVE_Y, new Face(Quad.getPyQuad(),  face.t1, face.t2, face.t3, face.t4,  face.color).setFaceData(face.faceData));
                case Y90 -> model.face(Dir.NEGATIVE_X, new Face(Quad.getNxQuad(),  face.t1, face.t2, face.t3, face.t4,  face.color).setFaceData(face.faceData));
                case Z90 -> model.face(Dir.POSITIVE_Z, new Face(Quad.getPzQuad(),  face.t4, face.t1, face.t2, face.t3,  face.color).setFaceData(face.faceData));
            }
        }

        return model;
    }

}
