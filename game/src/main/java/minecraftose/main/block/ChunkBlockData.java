package minecraftose.main.block;

import minecraftose.client.block.ClientBlock;
import minecraftose.client.block.BlockProps;
import minecraftose.client.block.ClientBlocks;

public class ChunkBlockData{
    
    public static BlockProps getProps(short data){
        return getBlock(getID(data)).getState(getState(data));
    }
    
    public static ClientBlock getBlock(byte ID){
        return ClientBlocks.get(ID);
    }

    public static ClientBlock getBlock(short data){
        return ClientBlocks.get(getID(data));
    }

    public static short getData(ClientBlock block, byte state){
        return getData(block.getID(), state);
    }

    public static short getData(byte id, byte state){
        return (short) (id & 0xFF | (state << 8));
    }

    public static short getData(int id, int state){
        return getData((byte) id, (byte) state);
    }

    public static short getData(ClientBlock block){
        return getData(block.getID());
    }

    public static short getData(byte id){
        return getData(id, (byte) 0);
    }

    public static short getData(int id){
        return getData((byte) id);
    }

    
    public static byte getID(short data){
        return (byte) (data & 0xFF);
    }
    
    public static byte getState(short data){
        return (byte) (data >> 8);
    }

}
