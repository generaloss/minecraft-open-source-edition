package minecraftose.main.network.packet.c2s.game.move;

import generaloss.resourceflow.stream.BinaryInputStream;
import generaloss.resourceflow.stream.BinaryOutputStream;
import generaloss.spatialmath.EulerAngles;
import generaloss.networkforge.packet.VisitorNetPacket;
import minecraftose.client.entity.LocalPlayer;
import minecraftose.server.network.ServerPlayerGameConnection;

import java.io.IOException;

public class C2SPacketRot extends VisitorNetPacket<ServerPlayerGameConnection> {

    public C2SPacketRot(){ }

    public EulerAngles rotation;

    public C2SPacketRot(LocalPlayer localPlayer){
        this();
        rotation = localPlayer.getRotation();
    }

    @Override
    public void write(BinaryOutputStream stream) throws IOException{
        stream.writeEulerAngles(rotation);
    }

    @Override
    public void read(BinaryInputStream stream) throws IOException{
        rotation = stream.readEulerAngles();
    }

    @Override
    public void handle(ServerPlayerGameConnection handler){
        handler.rotate(this);
    }

}
