package minecraftose.server.command.vanilla;

import generaloss.spatialmath.vector.Vec3f;
import minecraftose.main.text.Component;
import minecraftose.main.command.CommandContext;
import minecraftose.server.Server;
import minecraftose.server.command.ServerCommandDispatcher;
import minecraftose.main.command.argument.CommandArg;
import minecraftose.main.command.argument.CommandArgPlayer;
import minecraftose.main.command.argument.CommandArgPosition;
import minecraftose.main.command.builder.Commands;
import minecraftose.server.player.ServerPlayer;

public class SCommandTeleport{
    
    public static void registerTo(ServerCommandDispatcher dispatcher){
        final Server server = dispatcher.getServer();

        dispatcher.newCommand(Commands.literal("tp")
            .then(Commands.argument("position", CommandArg.position())
                .requiresPlayer()
                .executes( SCommandTeleport::teleportToPosition )
            )
            
            .then(Commands.argument("player", CommandArg.player())
                .requiresPlayer()
                .executes( (context) -> SCommandTeleport.teleportToPlayer(context, server) )
                
                .then(Commands.argument("targetPlayer", CommandArg.player())
                    .executes( (context) -> SCommandTeleport.teleportPlayerToPlayer(context, server) )
                )
                .then(Commands.argument("position", CommandArg.position())
                    .executes( (context) -> SCommandTeleport.teleportPlayerToPosition(context, server) )
                )
            )
            
        );
    }
    
    private static void teleportToPosition(CommandContext context){
        // Arguments
        final CommandArgPosition argPosition = context.getArg(0).asPosition();
        
        // Sender, Position
        final ServerPlayer sender = context.getSource().asServerPlayerSource().getPlayer();
        final Vec3f position = argPosition.getPosition();
        // Teleport
        sender.teleport(position);
        sender.sendMessage(new Component().text("You teleported to " + position));
    }
    
    private static void teleportToPlayer(CommandContext context, Server server){
        // Arguments
        final CommandArgPlayer argPlayer = context.getArg(0).asPlayer();
        
        // Players
        final ServerPlayer sender = context.getSource().asServerPlayerSource().getPlayer();
        final ServerPlayer targetPlayer = argPlayer.getPlayer(server);
        // Teleport
        sender.teleport(targetPlayer);
        sender.sendMessage(new Component().text("You teleported to " + targetPlayer.getName()));
        targetPlayer.sendMessage(new Component().text("Player " + sender.getName() + " teleported to you"));
    }
    
    private static void teleportPlayerToPlayer(CommandContext context, Server server){
        // Arguments
        final CommandArgPlayer argPlayer = context.getArg(0).asPlayer();
        final CommandArgPlayer argTargetPlayer = context.getArg(1).asPlayer();
        
        // Players
        final ServerPlayer player = argPlayer.getPlayer(server);
        final ServerPlayer targetPlayer = argTargetPlayer.getPlayer(server);
        
        // Teleport
        player.teleport(targetPlayer);
        player.sendMessage(new Component().text("You teleported to " + targetPlayer.getName()));
        targetPlayer.sendMessage(new Component().text("Player " + player.getName() + " teleported to you"));
    }
    
    private static void teleportPlayerToPosition(CommandContext context, Server server){
        // Arguments
        final CommandArgPlayer argPlayer = context.getArg(0).asPlayer();
        final CommandArgPosition argPosition = context.getArg(1).asPosition();
        
        // Player, Position
        final ServerPlayer player = argPlayer.getPlayer(server);
        final Vec3f position = argPosition.getPosition();
        // Teleport
        player.teleport(position);
        player.sendMessage(new Component().text("You teleported to " + position));
    }
    
}
