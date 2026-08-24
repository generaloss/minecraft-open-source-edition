package minecraftose.main.network.packet.c2s.game;

import generaloss.networkforge.packet.VisitorNetPacket;
import minecraftose.server.network.ServerPlayerGameConnection;
import generaloss.resourceflow.stream.BinaryInputStream;
import generaloss.resourceflow.stream.BinaryOutputStream;

import java.io.IOException;

public class C2SPacketChunkRequest extends VisitorNetPacket<ServerPlayerGameConnection> {
    
       
    public C2SPacketChunkRequest(){ }
    
    public long packedChunkPos;
    
    public C2SPacketChunkRequest(long packedChunkPos){
        this();
        
        this.packedChunkPos = packedChunkPos;
    }
    
    
    @Override
    public void write(BinaryOutputStream stream) throws IOException{
        stream.writeLong(packedChunkPos);
    }
    
    @Override
    public void read(BinaryInputStream stream) throws IOException{
        packedChunkPos = stream.readLong();
    }
    
    @Override
    public void handle(ServerPlayerGameConnection handler){
        handler.chunkRequest(this);
    }
    
}
