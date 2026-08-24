package minecraftose.server.command.vanilla;

import generaloss.spatialmath.vector.Vec3f;
import minecraftose.main.text.Component;
import minecraftose.main.command.CommandContext;
import minecraftose.server.command.ServerCommandDispatcher;
import minecraftose.main.command.builder.Commands;
import minecraftose.server.level.LevelS;
import minecraftose.server.player.ServerPlayer;

public class SCommandSpawn{
    
    public static void registerTo(ServerCommandDispatcher dispatcher){
        dispatcher.newCommand(Commands.literal("spawn")
            .requiresPlayer()
            .executes( SCommandSpawn::teleportToSpawn )
        );
    }
    
    private static void teleportToSpawn(CommandContext context){
        // Player
        final ServerPlayer sender = context.getSource().asServerPlayerSource().getPlayer();
        // Spawn position
        final LevelS level = sender.getLevel();
        final Vec3f spawnPosition = level.getSpawnPosition();
        // Teleport
        sender.teleport(spawnPosition);
        sender.sendMessage(new Component().text("You teleported to spawn"));
    }
    
}
