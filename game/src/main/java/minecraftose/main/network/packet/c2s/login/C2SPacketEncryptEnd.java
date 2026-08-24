package minecraftose.main.network.packet.c2s.login;

import generaloss.networkforge.packet.VisitorNetPacket;
import minecraftose.server.network.PlayerLoginConnection;
import generaloss.resourceflow.stream.BinaryInputStream;
import generaloss.resourceflow.stream.BinaryOutputStream;

import java.io.IOException;

public class C2SPacketEncryptEnd extends VisitorNetPacket<PlayerLoginConnection> {
    
       
    public C2SPacketEncryptEnd(){ }
    
    
    public byte[] encryptedClientKey;
    
    public C2SPacketEncryptEnd(byte[] encryptedClientKey){
        this();
        this.encryptedClientKey = encryptedClientKey;
    }
    
    
    @Override
    public void write(BinaryOutputStream stream) throws IOException{
        stream.writeByteArray(encryptedClientKey);
    }
    
    @Override
    public void read(BinaryInputStream stream) throws IOException{
        encryptedClientKey = stream.readByteArray();
    }
    
    @Override
    public void handle(PlayerLoginConnection handler){
        handler.encryptEnd(this);
    }
    
}
