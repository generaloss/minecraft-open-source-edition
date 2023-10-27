package minecraftose.main.net.packet.serverbound;

import jpize.net.tcp.packet.IPacket;
import minecraftose.server.net.PlayerLoginConnection;
import jpize.util.io.JpizeInputStream;
import jpize.util.io.JpizeOutputStream;

import java.io.IOException;

public class SBPacketAuth extends IPacket<PlayerLoginConnection>{
    
    public static final int PACKET_ID = 1;
    
    public SBPacketAuth(){
        super(PACKET_ID);
    }
    
    
    public String accountSessionToken;
    
    public SBPacketAuth(String accountSessionToken){
        this();
        this.accountSessionToken = accountSessionToken;
    }
    
    
    @Override
    public void write(JpizeOutputStream stream) throws IOException{
        stream.writeUTF(accountSessionToken);
    }
    
    @Override
    public void read(JpizeInputStream stream) throws IOException{
        accountSessionToken = stream.readUTF();
    }
    
    @Override
    public void handle(PlayerLoginConnection handler){
        handler.auth(this);
    }
    
}