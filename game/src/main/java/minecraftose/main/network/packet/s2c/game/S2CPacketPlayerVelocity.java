package minecraftose.main.network.packet.s2c.game;

import generaloss.resourceflow.stream.BinaryInputStream;
import generaloss.resourceflow.stream.BinaryOutputStream;
import generaloss.spatialmath.vector.Vec3f;
import generaloss.networkforge.packet.VisitorNetPacket;
import minecraftose.client.network.ClientPacketHandler;

import java.io.IOException;

public class S2CPacketPlayerVelocity extends VisitorNetPacket<ClientPacketHandler> {

    public S2CPacketPlayerVelocity(){ }

    public Vec3f velocity;

    public S2CPacketPlayerVelocity(Vec3f velocity){
        this();
        this.velocity = velocity;
    }


    @Override
    public void write(BinaryOutputStream stream) throws IOException{
        stream.writeVec3f(velocity);
    }

    @Override
    public void read(BinaryInputStream stream) throws IOException{
        velocity = stream.readVec3f();
    }

    @Override
    public void handle(ClientPacketHandler handler){
        handler.updateVelocity(this);
    }

}