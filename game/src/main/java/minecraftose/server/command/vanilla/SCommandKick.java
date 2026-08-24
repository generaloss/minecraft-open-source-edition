package minecraftose.server.command.vanilla;

import minecraftose.main.command.source.CommandServerPlayerSource;
import minecraftose.main.network.packet.s2c.game.S2CPacketDisconnect;
import minecraftose.main.text.Component;
import minecraftose.main.text.TextColor;
import minecraftose.main.command.CommandContext;
import minecraftose.server.command.ServerCommandDispatcher;
import minecraftose.main.command.argument.CommandArg;
import minecraftose.main.command.builder.Commands;
import minecraftose.server.player.ServerPlayer;

public class SCommandKick{
    
    public static void registerTo(ServerCommandDispatcher dispatcher){
        dispatcher.newCommand(Commands.literal("kick")
            .then(Commands.argument("player", CommandArg.player())
                .then(Commands.argument("reason", CommandArg.text())
                    .requiresPlayer()
                    .executes( SCommandKick::kick )
                )
            )
        );
    }
    
    private static void kick(CommandContext context){
        // Players
        final CommandServerPlayerSource playerSrc = context.getSource().asServerPlayerSource();
        final ServerPlayer sender = playerSrc.getPlayer();
        final ServerPlayer targetPlayer = context.getArg(0).asPlayer().getPlayer(playerSrc.getServer());
        // Reason
        final String reason = context.getArg(1).asText().getText();
        // Kick
        targetPlayer.sendPacket(new S2CPacketDisconnect("You been kicked by " + sender.getName() + ", reason: " + reason));
        context.getSource().tryToGetServer().getPlayerList().broadcastServerMessage(new Component().color(TextColor.DARK_RED).text(targetPlayer.getName() + " was kicked by " + sender.getName() + ", reason: " + reason));
    }
    
}
