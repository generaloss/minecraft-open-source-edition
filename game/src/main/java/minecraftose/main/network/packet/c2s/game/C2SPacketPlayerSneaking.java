package minecraftose.main.network.packet.c2s.game;

import generaloss.resourceflow.stream.BinaryInputStream;
import generaloss.resourceflow.stream.BinaryOutputStream;
import generaloss.networkforge.packet.VisitorNetPacket;
import minecraftose.main.entity.Player;
import minecraftose.server.network.ServerPlayerGameConnection;

import java.io.IOException;
import java.util.UUID;

public class C2SPacketPlayerSneaking extends VisitorNetPacket<ServerPlayerGameConnection> {

    public C2SPacketPlayerSneaking(){ }
    
    
    public UUID playerUUID;
    public boolean sneaking;
    
    public C2SPacketPlayerSneaking(Player player){
        this();
        this.playerUUID = player.getUUID();
        this.sneaking = player.isSneaking();
    }
    
    
    @Override
    public void write(BinaryOutputStream stream) throws IOException{
        stream.writeUUID(playerUUID);
        stream.writeBoolean(sneaking);
    }
    
    @Override
    public void read(BinaryInputStream stream) throws IOException{
        playerUUID = stream.readUUID();
        sneaking = stream.readBoolean();
    }
    
    @Override
    public void handle(ServerPlayerGameConnection handler){
        handler.sneaking(this);
    }
    
}