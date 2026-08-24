package minecraftose.main.nbt.io;

import generaloss.resourceflow.stream.BinaryInputStream;
import generaloss.resourceflow.stream.BinaryOutputStream;
import minecraftose.main.nbt.tag.NbtTag;
import minecraftose.main.nbt.tag.NbtTagID;
import minecraftose.main.nbt.tag.array.NbtByteArray;
import minecraftose.main.nbt.tag.array.NbtIntArray;
import minecraftose.main.nbt.tag.array.NbtLongArray;
import minecraftose.main.nbt.tag.list.NbtCompound;
import minecraftose.main.nbt.tag.list.NbtList;
import minecraftose.main.nbt.tag.type.*;

import java.io.IOException;
import java.util.Map;

public class NbtIO{

    public static void writeToStream(BinaryOutputStream out, NbtTag<?> nbt) throws IOException{
        out.writeByte(nbt.getID());

        switch(nbt.getID()){
            case NbtTagID.BYTE       -> out.writeByte     ((byte)    nbt.getValue());
            case NbtTagID.SHORT      -> out.writeShort    ((short)   nbt.getValue());
            case NbtTagID.INT        -> out.writeInt      ((int)     nbt.getValue());
            case NbtTagID.LONG       -> out.writeLong     ((long)    nbt.getValue());
            case NbtTagID.FLOAT      -> out.writeFloat    ((float)   nbt.getValue());
            case NbtTagID.DOUBLE     -> out.writeDouble   ((double)  nbt.getValue());
            case NbtTagID.STRING     -> out.writeUTF      ((String)  nbt.getValue());
            case NbtTagID.BOOL       -> out.writeBoolean  ((boolean) nbt.getValue());

            case NbtTagID.BYTE_ARRAY -> out.writeByteArray((byte[]) nbt.getValue());
            case NbtTagID.INT_ARRAY  -> out.writeIntArray ((int[])  nbt.getValue());
            case NbtTagID.LONG_ARRAY -> out.writeLongArray((long[]) nbt.getValue());

            case NbtTagID.LIST       -> {
                final NbtList<?> list = (NbtList<?>) nbt;
                out.writeInt(list.size());

                for(NbtTag<?> tag: list)
                    writeToStream(out, tag);
            }

            case NbtTagID.COMPOUND   -> {
                final NbtCompound compound = (NbtCompound) nbt;
                out.writeInt(compound.size());

                for(Map.Entry<String, NbtTag<?>> entry: compound){
                    out.writeUTF(entry.getKey());
                    writeToStream(out, entry.getValue());
                }
            }

        }
    }

    public static void tryWriteToStream(BinaryOutputStream out, NbtTag<?> nbt){
        try{
            writeToStream(out, nbt);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }


    public static NbtTag<?> readStream(BinaryInputStream in) throws IOException{
        final byte ID = in.readByte();

        return switch(ID){
            case NbtTagID.BYTE       -> new NbtByte  (in.readByte   ());
            case NbtTagID.SHORT      -> new NbtShort (in.readShort  ());
            case NbtTagID.INT        -> new NbtInt   (in.readInt    ());
            case NbtTagID.LONG       -> new NbtLong  (in.readLong   ());
            case NbtTagID.FLOAT      -> new NbtFloat (in.readFloat  ());
            case NbtTagID.DOUBLE     -> new NbtDouble(in.readDouble ());
            case NbtTagID.STRING     -> new NbtString(in.readUTF    ());
            case NbtTagID.BOOL       -> new NbtBool  (in.readBoolean());

            case NbtTagID.BYTE_ARRAY -> new NbtByteArray(in.readByteArray());
            case NbtTagID.INT_ARRAY  -> new NbtIntArray (in.readIntArray ());
            case NbtTagID.LONG_ARRAY -> new NbtLongArray(in.readLongArray());

            case NbtTagID.LIST       -> {
                final int size = in.readInt();
                final NbtList<NbtTag<?>> list = new NbtList<>(size);

                for(int i = 0; i < size; i++)
                    list.add(readStream(in));

                yield list;
            }

            case NbtTagID.COMPOUND   -> {
                final int size = in.readInt();
                final NbtCompound compound = new NbtCompound(size);

                for(int i = 0; i < size; i++){
                    final String key = in.readUTF();
                    final NbtTag<?> value = readStream(in);
                    compound.put(key, value);
                }

                yield compound;
            }

            default -> NbtTag.NULL_TAG;

        };
    }

    public static NbtTag<?> tryReadStream(BinaryInputStream in){
        try{
            return readStream(in);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

}
