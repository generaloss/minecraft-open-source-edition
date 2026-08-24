package minecraftose.main.network.packet.c2s.game;

import generaloss.resourceflow.stream.BinaryInputStream;
import generaloss.resourceflow.stream.BinaryOutputStream;
import generaloss.networkforge.packet.VisitorNetPacket;
import minecraftose.server.network.ServerPlayerGameConnection;

import java.io.IOException;
import java.util.UUID;

public class C2SPacketHitEntity extends VisitorNetPacket<ServerPlayerGameConnection> {

    public C2SPacketHitEntity(){ }

    public UUID entityUUID;

    public C2SPacketHitEntity(UUID entityUUID){
        this();
        this.entityUUID = entityUUID;
    }

    @Override
    public void write(BinaryOutputStream stream) throws IOException{
        stream.writeUUID(entityUUID);
    }

    @Override
    public void read(BinaryInputStream stream) throws IOException{
        entityUUID = stream.readUUID();
    }

    @Override
    public void handle(ServerPlayerGameConnection handler){
        handler.hitEntity(this);
    }

}