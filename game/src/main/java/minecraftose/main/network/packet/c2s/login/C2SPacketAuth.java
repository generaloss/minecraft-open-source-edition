package minecraftose.main.network.packet.c2s.login;

import generaloss.networkforge.packet.VisitorNetPacket;
import minecraftose.server.network.PlayerLoginConnection;
import generaloss.resourceflow.stream.BinaryInputStream;
import generaloss.resourceflow.stream.BinaryOutputStream;

import java.io.IOException;

public class C2SPacketAuth extends VisitorNetPacket<PlayerLoginConnection> {
    
       
    public C2SPacketAuth(){ }
    
    
    public String accountSessionToken;
    
    public C2SPacketAuth(String accountSessionToken){
        this();
        this.accountSessionToken = accountSessionToken;
    }
    
    
    @Override
    public void write(BinaryOutputStream stream) throws IOException{
        stream.writeUTF(accountSessionToken);
    }
    
    @Override
    public void read(BinaryInputStream stream) throws IOException{
        accountSessionToken = stream.readUTF();
    }
    
    @Override
    public void handle(PlayerLoginConnection handler){
        handler.auth(this);
    }
    
}
