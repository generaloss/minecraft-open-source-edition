package minecraftose.server.command.vanilla;

import generaloss.spatialmath.vector.Vec3f;
import minecraftose.client.block.ClientBlock;
import minecraftose.client.block.ClientBlocks;
import minecraftose.main.command.CommandContext;
import minecraftose.main.command.argument.CommandArg;
import minecraftose.main.command.builder.Commands;
import minecraftose.main.command.source.CommandSource;
import minecraftose.main.network.packet.s2c.game.S2CPacketBlockUpdate;
import minecraftose.main.text.Component;
import minecraftose.main.text.TextColor;
import minecraftose.server.command.ServerCommandDispatcher;
import minecraftose.server.level.LevelS;

public class SCommandSetBlock{

    public static void registerTo(ServerCommandDispatcher dispatcher){
        dispatcher.newCommand(Commands.literal("setblock")
            .then(Commands.argument("position", CommandArg.position())
                .then(Commands.argument("blockID", CommandArg.numInt())
                    .executes(SCommandSetBlock::setBlock)
                )
            )
        );
    }

    private static void setBlock(CommandContext context){
        // Args
        final Vec3f position = context.getArg(0).asPosition().getPosition();
        final int x = position.xFloor();
        final int y = position.yRound();
        final int z = position.zFloor();

        final byte blockID = (byte) context.getArg(1).asNumInt().getInt();
        final ClientBlock block = ClientBlocks.get(blockID);
        if(block == null){
            context.getSource().sendMessage(new Component().color(TextColor.RED).text("Block ID is invalid."));
            return;
        }

        // Set block
        final CommandSource source = context.getSource();
        final LevelS level = source.getLevel();
        level.setBlock(x, y, z, block);

        context.getSource().tryToGetServer().getPlayerList().broadcastPacket(new S2CPacketBlockUpdate(x, y, z, block.getDefaultData()));
        source.sendMessage(new Component().text("Block " + block.getID() + " set in: " + x + ", " + y + ", " + z));
    }

}
