package minecraftose.server.command.vanilla;

import generaloss.spatialmath.vector.Vec3f;
import minecraftose.main.text.Component;
import minecraftose.main.text.TextColor;
import minecraftose.main.command.CommandContext;
import minecraftose.server.command.ServerCommandDispatcher;
import minecraftose.main.command.builder.Commands;
import minecraftose.server.level.LevelS;
import minecraftose.server.player.ServerPlayer;

public class SCommandSetWorldSpawn{
    
    public static void registerTo(ServerCommandDispatcher dispatcher){
        dispatcher.newCommand(Commands.literal("setworldspawn")
            .requiresPlayer()
            .executes( SCommandSetWorldSpawn::setWorldSpawn)
        );
    }
    
    private static void setWorldSpawn(CommandContext context){
        // Player
        final ServerPlayer sender = context.getSource().asServerPlayerSource().getPlayer();
        // Spawn position
        final Vec3f position = sender.getPosition();
        final LevelS level = sender.getLevel();
        // Set world spawn
        level.getConfiguration().setWorldSpawn(position.x, position.z);
        context.getSource().tryToGetServer().getPlayerList().broadcastServerMessage(new Component().color(TextColor.GREEN).text("World spawn set in: " + position));
    }
    
}
