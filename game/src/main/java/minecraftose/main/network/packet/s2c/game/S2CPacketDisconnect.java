package minecraftose.main.network.packet.s2c.game;

import generaloss.networkforge.packet.VisitorNetPacket;
import minecraftose.client.network.ClientPacketHandler;
import generaloss.resourceflow.stream.BinaryInputStream;
import generaloss.resourceflow.stream.BinaryOutputStream;

import java.io.IOException;

public class S2CPacketDisconnect extends VisitorNetPacket<ClientPacketHandler> {
    
       
    public S2CPacketDisconnect(){ }
    
    
    public String reasonComponent;
    
    public S2CPacketDisconnect(String reasonComponent){
        this();
        this.reasonComponent = reasonComponent;
    }
    
    
    @Override
    public void write(BinaryOutputStream stream) throws IOException{
        stream.writeUTF(reasonComponent);
    }
    
    @Override
    public void read(BinaryInputStream stream) throws IOException{
        reasonComponent = stream.readUTF();
    }

    @Override
    public void handle(ClientPacketHandler handler){
        handler.disconnect(this);
    }
    
}
