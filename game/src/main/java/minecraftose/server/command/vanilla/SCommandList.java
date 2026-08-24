package minecraftose.server.command.vanilla;

import minecraftose.main.text.Component;
import minecraftose.main.text.TextColor;
import minecraftose.main.command.CommandContext;
import minecraftose.server.command.ServerCommandDispatcher;
import minecraftose.main.command.builder.Commands;
import minecraftose.main.command.source.CommandSource;
import minecraftose.server.player.ServerPlayer;

import java.util.Collection;
import java.util.StringJoiner;

public class SCommandList{
    
    public static void registerTo(ServerCommandDispatcher dispatcher){
        dispatcher.newCommand(Commands.literal("list")
            .executes(SCommandList::sendList)
        );
    }
    
    private static void sendList(CommandContext context){
        final CommandSource source = context.getSource();
        
        final StringJoiner joiner = new StringJoiner(", ");
        
        final Collection<ServerPlayer> players = context.getSource().tryToGetServer().getPlayerList().getPlayers();
        for(ServerPlayer player: players)
            joiner.add(player.getName());
        
        source.sendMessage(new Component().color(TextColor.YELLOW).text("Players: ").reset().text(joiner.toString()));
    }
    
}
