package minecraftose.main.network.packet.s2c.game;

import generaloss.networkforge.packet.VisitorNetPacket;
import minecraftose.client.network.ClientPacketHandler;
import generaloss.resourceflow.stream.BinaryInputStream;
import generaloss.resourceflow.stream.BinaryOutputStream;

import java.io.IOException;

public class S2CPacketTime extends VisitorNetPacket<ClientPacketHandler> {
    
    public S2CPacketTime(){ }

    public long gameTimeTicks;
    
    public S2CPacketTime(long gameTimeTicks){
        this();
        this.gameTimeTicks = gameTimeTicks;
    }
    
    
    @Override
    public void write(BinaryOutputStream stream) throws IOException{
        stream.writeLong(gameTimeTicks);
    }
    
    @Override
    public void read(BinaryInputStream stream) throws IOException{
        gameTimeTicks = stream.readLong();
    }

    @Override
    public void handle(ClientPacketHandler handler){
        handler.time(this);
    }
    
}