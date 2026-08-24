package minecraftose.main.network.packet.c2s.game;

import generaloss.networkforge.packet.VisitorNetPacket;
import minecraftose.server.network.ServerPlayerGameConnection;
import generaloss.resourceflow.stream.BinaryInputStream;
import generaloss.resourceflow.stream.BinaryOutputStream;

import java.io.IOException;

public class C2SPacketPing extends VisitorNetPacket<ServerPlayerGameConnection> {

    public C2SPacketPing(){ }

    public long timeNanos;
    
    public C2SPacketPing(long timeNanos){
        this();
        this.timeNanos = timeNanos;
    }
    
    @Override
    public void write(BinaryOutputStream stream) throws IOException{
        stream.writeLong(timeNanos);
    }
    
    @Override
    public void read(BinaryInputStream stream) throws IOException{
        timeNanos = stream.readLong();
    }

    @Override
    public void handle(ServerPlayerGameConnection handler){
        handler.ping(this);
    }
    
}
