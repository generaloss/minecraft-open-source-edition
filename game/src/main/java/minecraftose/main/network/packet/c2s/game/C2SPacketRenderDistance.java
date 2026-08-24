package minecraftose.main.network.packet.c2s.game;

import generaloss.networkforge.packet.VisitorNetPacket;
import minecraftose.server.network.ServerPlayerGameConnection;
import generaloss.resourceflow.stream.BinaryInputStream;
import generaloss.resourceflow.stream.BinaryOutputStream;

import java.io.IOException;

public class C2SPacketRenderDistance extends VisitorNetPacket<ServerPlayerGameConnection> {
    
       
    public C2SPacketRenderDistance(){ }
    
    
    public int renderDistance;
    
    public C2SPacketRenderDistance(int renderDistance){
        this();
        this.renderDistance = renderDistance;
    }
    
    
    @Override
    public void write(BinaryOutputStream stream) throws IOException{
        stream.writeInt(renderDistance);
    }
    
    @Override
    public void read(BinaryInputStream stream) throws IOException{
        renderDistance = stream.readInt();
    }
    
    @Override
    public void handle(ServerPlayerGameConnection handler){
        handler.renderDistance(this);
    }
    
}
