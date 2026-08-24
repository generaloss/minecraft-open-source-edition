package minecraftose.main.network.packet.s2c.game;

import generaloss.networkforge.packet.VisitorNetPacket;
import generaloss.resourceflow.stream.BinaryInputStream;
import generaloss.resourceflow.stream.BinaryOutputStream;
import minecraftose.client.network.ClientPacketHandler;
import minecraftose.main.entity.Player;
import java.io.IOException;
import java.util.UUID;

public class S2CPacketPlayerSneaking extends VisitorNetPacket<ClientPacketHandler> {
    
       
    public S2CPacketPlayerSneaking(){ }
    
    
    public UUID playerUUID;
    public boolean sneaking;
    
    public S2CPacketPlayerSneaking(Player player){
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
    public void handle(ClientPacketHandler handler){
        handler.playerSneaking(this);
    }
    
}