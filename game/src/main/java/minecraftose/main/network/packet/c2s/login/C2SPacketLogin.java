package minecraftose.main.network.packet.c2s.login;

import generaloss.networkforge.packet.VisitorNetPacket;
import minecraftose.server.network.PlayerLoginConnection;
import generaloss.resourceflow.stream.BinaryInputStream;
import generaloss.resourceflow.stream.BinaryOutputStream;

import java.io.IOException;

public class C2SPacketLogin extends VisitorNetPacket<PlayerLoginConnection> {
    
       
    public C2SPacketLogin(){ }
    
    
    public int clientVersionID;
    public String profileName;
    
    public C2SPacketLogin(int clientVersionID, String profileName){
        this();
        this.clientVersionID = clientVersionID;
        this.profileName = profileName;
    }
    
    
    @Override
    public void write(BinaryOutputStream stream) throws IOException{
        stream.writeInt(clientVersionID);
        stream.writeUTF(profileName);
    }
    
    @Override
    public void read(BinaryInputStream stream) throws IOException{
        clientVersionID = stream.readInt();
        profileName = stream.readUTF();
    }
    
    @Override
    public void handle(PlayerLoginConnection handler){
        handler.login(this);
    }
    
}
