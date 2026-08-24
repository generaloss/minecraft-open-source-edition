package minecraftose.server.command.vanilla;

import generaloss.spatialmath.vector.Vec3f;
import minecraftose.main.chunk.ChunkSection;
import minecraftose.main.command.CommandContext;
import minecraftose.main.command.builder.Commands;
import minecraftose.main.command.source.CommandSource;
import minecraftose.main.network.packet.s2c.game.S2CPacketChunk;
import minecraftose.main.text.Component;
import minecraftose.main.text.TextColor;
import minecraftose.server.level.chunk.ChunkS;
import minecraftose.server.command.ServerCommandDispatcher;

import java.util.Arrays;

public class SCommandChunk{

    public static void registerTo(ServerCommandDispatcher dispatcher){
        dispatcher.newCommand(Commands.literal("chunk")
                .then(Commands.literal("fillair")
                    .executes( SCommandChunk::fillChunkWithAir)
                )
        );
    }

    private static void fillChunkWithAir(CommandContext context){
        final CommandSource source = context.getSource();

        final Vec3f srcPos = source.getPosition();
        final ChunkS chunk = context.getSource().asServerPlayerSource().getLevel().getBlockChunk(srcPos.xFloor(), srcPos.zFloor());
        if(chunk == null){
            source.sendMessage(new Component().color(TextColor.RED).text("Unable to find chunk"));
            return;
        }

        for(ChunkSection section: chunk.getSections())
            if(section != null)
                Arrays.fill(section.blocks, (short) 0);

        context.getSource().tryToGetServer().getPlayerList().broadcastPacket(new S2CPacketChunk(chunk));

        source.sendMessage("Chunk successfully filled with air");
    }

}
