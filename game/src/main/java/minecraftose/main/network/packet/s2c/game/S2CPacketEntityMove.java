package minecraftose.main.network.packet.s2c.game;

import generaloss.spatialmath.EulerAngles;
import generaloss.spatialmath.vector.Vec3f;
import generaloss.networkforge.packet.VisitorNetPacket;
import minecraftose.client.network.ClientPacketHandler;
import minecraftose.server.player.ServerPlayer;
import generaloss.resourceflow.stream.BinaryInputStream;
import generaloss.resourceflow.stream.BinaryOutputStream;

import java.io.IOException;
import java.util.UUID;

public class S2CPacketEntityMove extends VisitorNetPacket<ClientPacketHandler> {

        public S2CPacketEntityMove(){ }


    public UUID uuid;
    public Vec3f position;
    public EulerAngles rotation;
    public Vec3f velocity;

    public S2CPacketEntityMove(ServerPlayer serverPlayer){
        this();
        uuid = serverPlayer.getUUID();
        position = serverPlayer.getPosition();
        rotation = serverPlayer.getRotation();
        velocity = serverPlayer.getVelocity();
    }


    @Override
    public void write(BinaryOutputStream stream) throws IOException{
        stream.writeUUID(uuid);
        stream.writeVec3f(position);
        stream.writeEulerAngles(rotation);
        stream.writeVec3f(velocity);
    }

    @Override
    public void read(BinaryInputStream stream) throws IOException{
        uuid = stream.readUUID();
        position = stream.readVec3f();
        rotation = stream.readEulerAngles();
        velocity = new Vec3f(stream.readVec3f());
    }

    @Override
    public void handle(ClientPacketHandler handler){
        handler.entityMove(this);
    }

}