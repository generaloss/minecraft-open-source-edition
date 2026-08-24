package minecraftose.main.network.packet.c2s.game.move;

import generaloss.spatialmath.vector.Vec3f;
import generaloss.networkforge.packet.VisitorNetPacket;
import minecraftose.client.entity.LocalPlayer;
import minecraftose.server.network.ServerPlayerGameConnection;
import generaloss.resourceflow.stream.BinaryInputStream;
import generaloss.resourceflow.stream.BinaryOutputStream;

import java.io.IOException;

public class C2SPacketMove extends VisitorNetPacket<ServerPlayerGameConnection> {
    
    public C2SPacketMove(){ }
    
    public Vec3f position;
    public Vec3f velocity;
    
    public C2SPacketMove(LocalPlayer localPlayer){
        this();
        position = localPlayer.getPosition();
        velocity = localPlayer.getVelocity();
    }
    
    
    @Override
    public void write(BinaryOutputStream stream) throws IOException{
        stream.writeVec3f(position);
        stream.writeVec3f(velocity);
    }
    
    @Override
    public void read(BinaryInputStream stream) throws IOException{
        position = stream.readVec3f();
        velocity = new Vec3f(stream.readVec3f());
    }
    
    @Override
    public void handle(ServerPlayerGameConnection handler){
        handler.move(this);
    }
    
}