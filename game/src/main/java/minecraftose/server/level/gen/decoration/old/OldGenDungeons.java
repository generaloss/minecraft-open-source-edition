package minecraftose.server.level.gen.decoration.old;

import generaloss.spatialmath.Maths;
import minecraftose.client.block.ClientBlocks;
import minecraftose.server.level.LevelS;

import java.util.Random;

public class OldGenDungeons{

    public static boolean generate(LevelS par1World, Random par2Random, int par3, int par4, int par5){
        byte byte0 = 3;
        int i = par2Random.nextInt(2) + 2;
        int j = par2Random.nextInt(2) + 2;
        int k = 0;
        /*
        unavailable code
        for(int l = par3 - i - 1; l <= par3 + i + 1; l++){
            for(int k1 = par4 - 1; k1 <= par4 + byte0 + 1; k1++){
                for(int j2 = par5 - j - 1; j2 <= par5 + j + 1; j2++){
                    final BlockProps material = par1World.getBlock(l, k1, j2).getState(0);

                    if(k1 == par4 - 1 && !material.isSolid())
                        return false;

                    if(k1 == par4 + byte0 + 1 && !material.isSolid())
                        return false;

                    if((l == par3 - i - 1 || l == par3 + i + 1 || j2 == par5 - j - 1 || j2 == par5 + j + 1) && k1 == par4 && par1World.getBlock(l, k1, j2).isEmpty() && par1World.getBlock(l, k1 + 1, j2).isEmpty())
                        k++;
                }
            }
        }

        if(k < 1 || k > 5)
            return false;
        */

        for(int i1 = par3 - i - 1; i1 <= par3 + i + 1; i1++){
            for(int l1 = par4 + byte0; l1 >= par4 - 1; l1--){
                for(int k2 = par5 - j - 1; k2 <= par5 + j + 1; k2++){
                    if(i1 == par3 - i - 1 || l1 == par4 - 1 || k2 == par5 - j - 1 || i1 == par3 + i + 1 || l1 == par4 + byte0 + 1 || k2 == par5 + j + 1){
                        if(l1 >= 0 && !par1World.getBlock(i1, l1 - 1, k2).getState(0).isSolid()){
                            par1World.setBlock(i1, l1, k2, ClientBlocks.AIR);
                            continue;
                        }

                        if(!par1World.getBlock(i1, l1, k2).getState(0).isSolid()){
                            continue;
                        }

                        if(l1 == par4 - 1 && par2Random.nextInt(4) != 0){
                            par1World.setBlock(i1, l1, k2, ClientBlocks.MOSSY_COBBLESTONE);
                        }else{
                            par1World.setBlock(i1, l1, k2, ClientBlocks.COBBLESTONE);
                        }
                    }else{
                        par1World.setBlock(i1, l1, k2, ClientBlocks.AIR);
                    }
                }
            }
        }

        for(int j1 = 0; j1 < 2; j1++){
            for(int i2 = 0; i2 < 3; i2++){
                int l2 = (par3 + par2Random.nextInt(i * 2 + 1)) - i;
                int i3 = Maths.floor(par4);
                int j3 = (par5 + par2Random.nextInt(j * 2 + 1)) - j;

                if(!par1World.getBlock(l2, i3, j3).isEmpty())
                    continue;

                int k3 = 0;
                if(par1World.getBlock(l2 - 1, i3, j3).getState(0).isSolid())
                    k3++;
                if(par1World.getBlock(l2 + 1, i3, j3).getState(0).isSolid())
                    k3++;
                if(par1World.getBlock(l2, i3, j3 - 1).getState(0).isSolid())
                    k3++;
                if(par1World.getBlock(l2, i3, j3 + 1).getState(0).isSolid())
                    k3++;
                if(k3 != 1)
                    continue;

                par1World.setBlock(l2, i3, j3, ClientBlocks.LAMP); // chest
            }
        }

        par1World.setBlock(par3, par4, par5, ClientBlocks.BIRCH_LEAVES); // mob spawner

        return true;
    }

}
