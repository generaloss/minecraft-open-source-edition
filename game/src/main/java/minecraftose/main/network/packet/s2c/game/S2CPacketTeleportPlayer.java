package minecraftose.main.network.packet.s2c.game;

import generaloss.spatialmath.EulerAngles;
import generaloss.spatialmath.vector.Vec3f;
import generaloss.networkforge.packet.VisitorNetPacket;
import minecraftose.client.network.ClientPacketHandler;
import generaloss.resourceflow.stream.BinaryInputStream;
import generaloss.resourceflow.stream.BinaryOutputStream;

import java.io.IOException;

public class S2CPacketTeleportPlayer extends VisitorNetPacket<ClientPacketHandler> {
    
    public S2CPacketTeleportPlayer(){ }
    
    public Vec3f position;
    public EulerAngles rotation;
    public String levelName;
    
    public S2CPacketTeleportPlayer(String levelName, Vec3f position, EulerAngles rotation){
        this();
        this.levelName = levelName;
        this.position = position;
        this.rotation = rotation;
    }
    
    
    @Override
    public void write(BinaryOutputStream stream) throws IOException{
        stream.writeUTF(levelName);
        stream.writeVec3f(position);
        stream.writeEulerAngles(rotation);
    }
    
    @Override
    public void read(BinaryInputStream stream) throws IOException{
        levelName = stream.readUTF();
        position = stream.readVec3f();
        rotation = stream.readEulerAngles();
    }

    @Override
    public void handle(ClientPacketHandler handler){
        handler.teleportPlayer(this);
    }
    
}