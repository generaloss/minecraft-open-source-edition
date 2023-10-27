package minecraftose.client.block;

import minecraftose.client.resources.GameResources;
import minecraftose.main.block.BlockData;
import minecraftose.main.item.Item;
import minecraftose.main.item.Items;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class Block{
    
    private final byte ID;
    private final short defaultState;
    protected Map<Byte, BlockProps> states;
    protected Item item;

    public Block(int ID){
        this.ID = (byte) ID;
        this.defaultState = BlockData.getData(ID);
        this.states = new HashMap<>(1);
        this.item = Items.AIR;
    }

    public final byte getID(){
        return ID;
    }

    public short getDefaultData(){
        return defaultState;
    }

    public short getData(int state){
        return BlockData.getData(ID, state);
    }


    public Item getItem(){
        return item;
    }


    /** Возвращает True если это воздух */
    public final boolean isEmpty(){
        return ID == Blocks.AIR.getID();
    }


    public Collection<BlockProps> getStates(){
        return states.values();
    }

    public BlockProps getState(byte state){
        return states.get(state);
    }

    public BlockProps getState(int index){
        return states.get((byte) index);
    }

    protected BlockProps newState(){
        final byte state = (byte) states.size();
        final BlockProps blockProps = new BlockProps(this, state);

        states.put(state, blockProps);
        return blockProps;
    }


    public abstract void load(GameResources resources);


    @Override
    public boolean equals(Object object){
        if(this == object)
            return true;

        if(object == null || getClass() != object.getClass())
            return false;

        final Block block = (Block) object;
        return ID == block.ID;
    }

    @Override
    public int hashCode(){
        return ID;
    }

}
