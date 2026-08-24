package minecraftose.main.network.packet.c2s.game;

import generaloss.networkforge.packet.VisitorNetPacket;
import minecraftose.server.network.ServerPlayerGameConnection;
import generaloss.resourceflow.stream.BinaryInputStream;
import generaloss.resourceflow.stream.BinaryOutputStream;

import java.io.IOException;

public class C2SPacketPlayerBlockSet extends VisitorNetPacket<ServerPlayerGameConnection> {

    public C2SPacketPlayerBlockSet(){ }
    
    public int x, y, z;
    public short blockData;
    
    public C2SPacketPlayerBlockSet(int x, int y, int z, short blockData){
        this();
        
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockData = blockData;
    }
    
    
    @Override
    public void write(BinaryOutputStream stream) throws IOException{
        stream.writeInt(x);
        stream.writeInt(y);
        stream.writeInt(z);
        stream.writeShort(blockData);
    }
    
    @Override
    public void read(BinaryInputStream stream) throws IOException{
        x = stream.readInt();
        y = stream.readInt();
        z = stream.readInt();
        blockData = stream.readShort();
    }
    
    @Override
    public void handle(ServerPlayerGameConnection handler){
        handler.playerBlockSet(this);
    }
    
}
