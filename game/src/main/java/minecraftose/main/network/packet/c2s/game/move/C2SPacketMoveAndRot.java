package minecraftose.main.network.packet.c2s.game.move;

import generaloss.resourceflow.stream.BinaryInputStream;
import generaloss.resourceflow.stream.BinaryOutputStream;
import generaloss.spatialmath.EulerAngles;
import generaloss.spatialmath.vector.Vec3f;
import generaloss.networkforge.packet.VisitorNetPacket;
import minecraftose.client.entity.LocalPlayer;
import minecraftose.server.network.ServerPlayerGameConnection;

import java.io.IOException;

public class C2SPacketMoveAndRot extends VisitorNetPacket<ServerPlayerGameConnection> {

    public C2SPacketMoveAndRot(){ }

    public Vec3f position;
    public Vec3f velocity;
    public EulerAngles rotation;

    public C2SPacketMoveAndRot(LocalPlayer localPlayer){
        this();
        position = localPlayer.getPosition();
        velocity = localPlayer.getVelocity();
        rotation = localPlayer.getRotation();
    }


    @Override
    public void write(BinaryOutputStream stream) throws IOException{
        stream.writeVec3f(position);
        stream.writeVec3f(velocity);
        stream.writeEulerAngles(rotation);
    }

    @Override
    public void read(BinaryInputStream stream) throws IOException{
        position = stream.readVec3f();
        velocity = stream.readVec3f();
        rotation = stream.readEulerAngles();
    }

    @Override
    public void handle(ServerPlayerGameConnection handler){
        handler.moveAndRotate(this);
    }

}
