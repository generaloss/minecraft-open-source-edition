package minecraftose.main.network.packet.s2c.game;

import generaloss.networkforge.packet.VisitorNetPacket;
import minecraftose.client.network.ClientPacketHandler;
import generaloss.resourceflow.stream.BinaryInputStream;
import generaloss.resourceflow.stream.BinaryOutputStream;

import java.io.IOException;

public class S2CPacketAbilities extends VisitorNetPacket<ClientPacketHandler> {

    public S2CPacketAbilities(){ }
    
    public boolean flyEnabled;
    
    public S2CPacketAbilities(boolean flyEnabled){
        this();
        this.flyEnabled = flyEnabled;
    }
    
    
    @Override
    public void write(BinaryOutputStream stream) throws IOException{
        stream.writeBoolean(flyEnabled);
    }
    
    @Override
    public void read(BinaryInputStream stream) throws IOException{
        flyEnabled = stream.readBoolean();
    }

    @Override
    public void handle(ClientPacketHandler handler){
        handler.abilities(this);
    }
    
}
