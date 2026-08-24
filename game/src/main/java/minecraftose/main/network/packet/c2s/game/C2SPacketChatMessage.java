package minecraftose.main.network.packet.c2s.game;

import generaloss.networkforge.packet.VisitorNetPacket;
import minecraftose.server.network.ServerPlayerGameConnection;
import generaloss.resourceflow.stream.BinaryInputStream;
import generaloss.resourceflow.stream.BinaryOutputStream;

import java.io.IOException;

public class C2SPacketChatMessage extends VisitorNetPacket<ServerPlayerGameConnection> {

    public C2SPacketChatMessage(){ }
    
    public String message;
    
    public C2SPacketChatMessage(String message){
        this();
        this.message = message;
    }
    
    
    @Override
    public void write(BinaryOutputStream stream) throws IOException{
        stream.writeUTF(message);
    }
    
    @Override
    public void read(BinaryInputStream stream) throws IOException{
        message = stream.readUTF();
    }
    
    @Override
    public void handle(ServerPlayerGameConnection handler){
        handler.chatMessage(this);
    }
    
}