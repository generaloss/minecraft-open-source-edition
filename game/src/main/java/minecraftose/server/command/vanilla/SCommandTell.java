package minecraftose.server.command.vanilla;

import minecraftose.main.command.source.CommandServerPlayerSource;
import minecraftose.main.text.Component;
import minecraftose.main.text.TextColor;
import minecraftose.main.command.CommandContext;
import minecraftose.server.command.ServerCommandDispatcher;
import minecraftose.main.command.argument.CommandArg;
import minecraftose.main.command.builder.Commands;
import minecraftose.server.player.ServerPlayer;

public class SCommandTell{
    
    public static void registerTo(ServerCommandDispatcher dispatcher){
        dispatcher.newCommand(Commands.literal("tell")
            
            .then(Commands.argument("player", CommandArg.player())
                .then(Commands.argument("text", CommandArg.text())
                    .requiresPlayer()
                    .executes( SCommandTell::tell)
                )
            )
            
        );
    }
    
    private static void tell(CommandContext context){
        // Players
        final CommandServerPlayerSource playerSrc = context.getSource().asServerPlayerSource();
        final ServerPlayer sender = playerSrc.getPlayer();
        final ServerPlayer targetPlayer = context.getArg(0).asPlayer().getPlayer(playerSrc.getServer());
        final String text = context.getArg(1).asText().getText();
        // Tell
        targetPlayer.sendMessage(new Component().color(TextColor.YELLOW).text("<" + sender.getName() + "> tells you: \"").reset().text(text).color(TextColor.YELLOW).text("\""));
    }
    
}
