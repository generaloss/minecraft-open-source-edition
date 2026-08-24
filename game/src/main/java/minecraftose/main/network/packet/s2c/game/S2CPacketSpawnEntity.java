package minecraftose.main.network.packet.s2c.game;

import generaloss.spatialmath.EulerAngles;
import generaloss.spatialmath.vector.Vec3f;
import generaloss.networkforge.packet.VisitorNetPacket;
import minecraftose.client.network.ClientPacketHandler;
import minecraftose.main.entity.Entity;
import minecraftose.main.entity.EntityType;
import generaloss.resourceflow.stream.BinaryInputStream;
import generaloss.resourceflow.stream.BinaryOutputStream;

import java.io.IOException;
import java.util.UUID;

public class S2CPacketSpawnEntity extends VisitorNetPacket<ClientPacketHandler> {

    public S2CPacketSpawnEntity(){ }
    
    public EntityType<?> type;
    public UUID uuid;
    public Vec3f position;
    public EulerAngles rotation;

    public S2CPacketSpawnEntity(Entity entity){
        this();
        type = entity.getEntityType();
        uuid = entity.getUUID();
        position = entity.getPosition();
        rotation = entity.getRotation();
    }


    @Override
    public void write(BinaryOutputStream stream) throws IOException{
        stream.writeInt(type.getID());
        stream.writeUTF(uuid.toString());
        stream.writeVec3f(position);
        stream.writeEulerAngles(rotation);
    }

    @Override
    public void read(BinaryInputStream stream) throws IOException{
        type = EntityType.fromEntityID(stream.readInt());
        uuid = UUID.fromString(stream.readUTF());
        position = stream.readVec3f();
        rotation = stream.readEulerAngles();
    }

    @Override
    public void handle(ClientPacketHandler handler){
        handler.spawnEntity(this);
    }
    
}