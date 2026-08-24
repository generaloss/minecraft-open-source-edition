package minecraftose.main.network.packet.s2c.game;

import generaloss.spatialmath.vector.Vec3f;
import generaloss.networkforge.packet.VisitorNetPacket;
import minecraftose.client.network.ClientPacketHandler;
import generaloss.resourceflow.stream.BinaryInputStream;
import generaloss.resourceflow.stream.BinaryOutputStream;

import java.io.IOException;

public class S2CPacketSpawnInfo extends VisitorNetPacket<ClientPacketHandler> {

    public S2CPacketSpawnInfo(){ }
    
    public String levelName;
    public Vec3f position;
    public long gameTime;
    
    public S2CPacketSpawnInfo(String levelName, Vec3f position, long gameTime){
        this();
        
        this.levelName = levelName;
        this.position = position;
        this.gameTime = gameTime;
    }
    
    
    @Override
    public void write(BinaryOutputStream stream) throws IOException{
        stream.writeUTF(levelName);
        stream.writeVec3f(position);
        stream.writeLong(gameTime);
    }
    
    @Override
    public void read(BinaryInputStream stream) throws IOException{
        levelName = stream.readUTF();
        position = stream.readVec3f();
        gameTime = stream.readLong();
    }

    @Override
    public void handle(ClientPacketHandler handler){
        handler.spawnInfo(this);
    }
    
}
