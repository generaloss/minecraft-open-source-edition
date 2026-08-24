package minecraftose.main.network.packet.s2c.game;

import generaloss.networkforge.packet.VisitorNetPacket;
import minecraftose.client.network.ClientPacketHandler;
import minecraftose.main.entity.Entity;
import generaloss.resourceflow.stream.BinaryInputStream;
import generaloss.resourceflow.stream.BinaryOutputStream;

import java.io.IOException;
import java.util.UUID;

public class S2CPacketRemoveEntity extends VisitorNetPacket<ClientPacketHandler> {
    
       
    public S2CPacketRemoveEntity(){ }
    
    
    public UUID uuid;
    
    public S2CPacketRemoveEntity(Entity entity){
        this();
        uuid = entity.getUUID();
    }
    
    
    @Override
    public void write(BinaryOutputStream stream) throws IOException{
        stream.writeUTF(uuid.toString());
    }
    
    @Override
    public void read(BinaryInputStream stream) throws IOException{
        uuid = UUID.fromString(stream.readUTF());
    }

    @Override
    public void handle(ClientPacketHandler handler){
        handler.removeEntity(this);
    }
    
}