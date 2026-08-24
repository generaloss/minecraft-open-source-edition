package minecraftose.main.level.structure;

import generaloss.spatialmath.Maths;
import generaloss.spatialmath.vector.Vec2f;
import generaloss.spatialmath.vector.Vec3i;
import generaloss.resourceflow.resource.Resource;
import minecraftose.client.block.ClientBlock;
import minecraftose.server.level.LevelS;
import generaloss.resourceflow.stream.BinaryInputStream;

import java.io.IOException;

public class Structure{

    public static void circleFilledXZ(LevelS level, int x, int y, int z, float radius, ClientBlock block){
        final int intRadius = Maths.ceil(radius);
        for(int i = 0; i < intRadius; i++){
            for(int j = 0; j < intRadius; j++){
                if(Vec2f.len(i, j) >= radius)
                    continue;

                level.genBlock(x + i, y, z + j, block);
                level.genBlock(x - i, y, z + j, block);
                level.genBlock(x - i, y, z - j, block);
                level.genBlock(x + i, y, z - j, block);
            }
        }
    }

    public static void circleXZ(LevelS level, int x, int y, int z, float radius, ClientBlock block){
        final int intRadius = Maths.ceil(radius);
        for(int i = 0; i < intRadius; i++){
            for(int j = 0; j < intRadius; j++){
                final float len = Vec2f.len(i, j);
                if( !(len < radius && len >= radius - 1) )
                    continue;

                level.genBlock(x + i, y, z + j, block);
                level.genBlock(x - i, y, z + j, block);
                level.genBlock(x - i, y, z - j, block);
                level.genBlock(x + i, y, z - j, block);
            }
        }
    }

    public static void loadTo(LevelS level, String name, int x, int y, int z){
        // File
        final Resource file = Resource.internal("/struct/" + name + ".struct");
        try(final BinaryInputStream inStream = file.inStreamBin()){
            // Read
            final Vec3i size = inStream.readVec3i();

            for(int i = 0; i < size.x; i++)
                for(int j = 0; j < size.y; j++)
                    for(int k = 0; k < size.z; k++){

                        final short block = inStream.readShort();
                        level.setBlockState(x + i, y + j, z + k, block);
                    }

        }catch(IOException e){
            throw new Error(e);
        }
    }

}
