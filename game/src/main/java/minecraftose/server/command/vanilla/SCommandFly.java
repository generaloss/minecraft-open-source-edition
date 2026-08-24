package minecraftose.server.command.vanilla;

import minecraftose.main.text.Component;
import minecraftose.main.command.CommandContext;
import minecraftose.server.command.ServerCommandDispatcher;
import minecraftose.main.command.builder.Commands;
import minecraftose.server.player.ServerPlayer;

public class SCommandFly{
    
    public static void registerTo(ServerCommandDispatcher dispatcher){
        dispatcher.newCommand(Commands.literal("fly")
            .requiresPlayer()
            .executes( SCommandFly::toggleFly )
        );
    }
    
    private static void toggleFly(CommandContext context){
        // Player
        final ServerPlayer sender = context.getSource().asServerPlayerSource().getPlayer();
        // Fly
        if(sender.isFlyEnabled()){
            sender.setFlyEnabled(false);
            sender.sendMessage(new Component().text("Fly disabled"));
        }else{
            sender.setFlyEnabled(true);
            sender.sendMessage(new Component().text("Fly enabled"));
        }
    }
    
}
