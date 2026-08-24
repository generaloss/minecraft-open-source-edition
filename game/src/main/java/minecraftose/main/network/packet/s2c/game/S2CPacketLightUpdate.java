package minecraftose.main.network.packet.s2c.game;

import generaloss.networkforge.packet.VisitorNetPacket;
import minecraftose.client.network.ClientPacketHandler;
import minecraftose.main.chunk.ChunkBase;
import minecraftose.main.chunk.ChunkSection;
import minecraftose.main.chunk.storage.SectionPos;
import generaloss.resourceflow.stream.BinaryInputStream;
import generaloss.resourceflow.stream.BinaryOutputStream;

import java.io.IOException;

public class S2CPacketLightUpdate extends VisitorNetPacket<ClientPacketHandler> {

        public S2CPacketLightUpdate(){ }

    public SectionPos position;
    public byte[] light;

    public S2CPacketLightUpdate(ChunkSection section){
        this();
        position = section.getPosition();
        light = section.light;
    }


    @Override
    public void write(BinaryOutputStream stream) throws IOException{
        stream.writeVec3i(position);
        stream.write(light);
    }

    @Override
    public void read(BinaryInputStream stream) throws IOException{
        position = new SectionPos(stream.readVec3i());
        light = stream.readNBytes(ChunkBase.VOLUME);
    }

    @Override
    public void handle(ClientPacketHandler handler){
        handler.lightUpdate(this);
    }

}