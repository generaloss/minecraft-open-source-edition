package minecraftose.main.network.packet.s2c.login;

import generaloss.networkforge.packet.VisitorNetPacket;
import minecraftose.client.network.ClientPacketHandler;
import generaloss.resourceflow.stream.BinaryInputStream;
import generaloss.resourceflow.stream.BinaryOutputStream;
import minecraftose.client.network.pipeline.security.PublicRSA;

import java.io.IOException;

public class S2CPacketEncryptStart extends VisitorNetPacket<ClientPacketHandler> {
    
       
    public S2CPacketEncryptStart(){ }
    
    
    public PublicRSA publicServerKey;
    
    public S2CPacketEncryptStart(PublicRSA publicServerKey){
        this();
        this.publicServerKey = publicServerKey;
    }
    
    
    @Override
    public void write(BinaryOutputStream stream) throws IOException{
        stream.write(publicServerKey.getKey().getEncoded());
    }
    
    @Override
    public void read(BinaryInputStream stream) throws IOException{
        publicServerKey = new PublicRSA(stream.readAllBytes());
    }

    @Override
    public void handle(ClientPacketHandler handler){
        handler.encryptStart(this);
    }
    
}

