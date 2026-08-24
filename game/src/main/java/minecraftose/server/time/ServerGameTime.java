package minecraftose.server.time;

import minecraftose.main.time.GameTime;
import minecraftose.main.network.packet.s2c.game.S2CPacketTime;
import minecraftose.server.Server;

public class ServerGameTime extends GameTime{
    
    private final Server server;
    
    public ServerGameTime(Server server){
        this.server = server;
    }
    
    public Server getServer(){
        return server;
    }
    
    
    @Override
    public void setTicks(long ticks){
        super.setTicks(ticks);
        server.getPlayerList().broadcastPacket(new S2CPacketTime(ticks));
    }
    
}
