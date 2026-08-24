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

public class S2CPacketSpawnPlayer extends VisitorNetPacket<ClientPacketHandler> {
    
    public S2CPacketSpawnPlayer(){ }
    
    public UUID uuid;
    public Vec3f position;
    public EulerAngles rotation;
    public String playerName;
    
    public S2CPacketSpawnPlayer(ServerPlayer player){
        this();
        this.uuid = player.getUUID();
        this.position = player.getPosition();
        this.rotation = player.getRotation();
        this.playerName = player.getName();
    }
    
    
    @Override
    public void write(BinaryOutputStream stream) throws IOException{
        stream.writeUTF(uuid.toString());
        stream.writeVec3f(position);
        stream.writeEulerAngles(rotation);
        stream.writeUTF(playerName);
    }
    
    @Override
    public void read(BinaryInputStream stream) throws IOException{
        uuid = UUID.fromString(stream.readUTF());
        position = stream.readVec3f();
        rotation = stream.readEulerAngles();
        playerName = stream.readUTF();
    }

    @Override
    public void handle(ClientPacketHandler handler){
        handler.spawnPlayer(this);
    }
    
}
