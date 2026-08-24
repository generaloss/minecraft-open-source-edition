package minecraftose.main.network.packet.s2c.game;

import generaloss.networkforge.packet.VisitorNetPacket;
import minecraftose.client.network.ClientPacketHandler;
import generaloss.resourceflow.stream.BinaryInputStream;
import generaloss.resourceflow.stream.BinaryOutputStream;

import java.io.IOException;

public class S2CPacketPong extends VisitorNetPacket<ClientPacketHandler> {
    
       
    public S2CPacketPong(){ }
    
    
    public long timeNanos;
    
    public S2CPacketPong(long timeNanos){
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
    public void handle(ClientPacketHandler handler){
        handler.pong(this);
    }
    
}

