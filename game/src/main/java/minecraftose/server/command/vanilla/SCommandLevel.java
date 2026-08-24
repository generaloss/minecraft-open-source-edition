package minecraftose.server.command.vanilla;

import minecraftose.main.audio.Sound;
import minecraftose.main.registry.Registry;
import minecraftose.main.text.Component;
import minecraftose.main.text.TextColor;
import minecraftose.server.Server;
import minecraftose.server.level.gen.ChunkGenerator;
import minecraftose.main.command.CommandContext;
import minecraftose.server.command.ServerCommandDispatcher;
import minecraftose.main.command.argument.CommandArg;
import minecraftose.main.command.builder.Commands;
import minecraftose.server.level.LevelManager;
import minecraftose.server.level.LevelS;
import minecraftose.server.player.ServerPlayer;

import java.util.Collection;
import java.util.StringJoiner;

public class SCommandLevel{
    
    public static void registerTo(ServerCommandDispatcher dispatcher){
        dispatcher.newCommand(Commands.literal("level")
            
            .then(Commands.literal("create")
                .then(Commands.argument("mame", CommandArg.word())
                    .then(Commands.argument("seed", CommandArg.word())
                        .then(Commands.argument("generator", CommandArg.numInt())
                            .executes( SCommandLevel::createLevel )
                        )
                    )
                )
            )

            .then(Commands.literal("list")
                .executes( SCommandLevel::sendLevelList )
            )

            .then(Commands.argument("name", CommandArg.word())
                .requiresPlayer()
                .executes( SCommandLevel::goToLevel )
            )
            
        );
    }
    
    private static void createLevel(CommandContext context){
        // Level name, seed, generator Type
        final Server server = context.getSource().tryToGetServer();

        final String levelName = context.getArg(0).asWord().getWord();
        final String seed = context.getArg(1).asWord().getWord();

        final byte generatorID = (byte) context.getArg(2).asNumInt().getInt();
        final ChunkGenerator generator = Registry.generator.get(generatorID);
        // Create level
        final ServerPlayer sender = context.getSource().asServerPlayerSource().getPlayer();
        final LevelManager levelManager = server.getLevelManager();
        
        if(levelManager.isLevelLoaded(levelName))
            sender.sendMessage(new Component().color(TextColor.DARK_RED).text("Level " + levelName + " already loaded"));
        else{
            // Parse seed

            levelManager.createLevel(levelName, seed, generator);
            server.getPlayerList().broadcastServerMessage(new Component().color(TextColor.YELLOW).text("Level '" + levelName + "' loaded"));
        }
    }
    
    public static void goToLevel(CommandContext context){
        // Level name
        final String levelName = context.getArg(0).asWord().getWord();
        // Go to level
        final ServerPlayer sender = context.getSource().asServerPlayerSource().getPlayer();
        final LevelManager levelManager = context.getSource().tryToGetServer().getLevelManager();
        
        if(!levelManager.isLevelLoaded(levelName))
            sender.sendMessage(new Component().color(TextColor.DARK_RED).text("Level " + levelName + " is not loaded"));
        else{
            final LevelS level = levelManager.getLevel(levelName);
            sender.teleport(level, level.getSpawnPosition());
            sender.sendMessage(new Component().text("You teleported to level " + levelName));
            sender.playSound(Sound.LEVEL_UP, 1, 1);
        }
    }
    
    private static void sendLevelList(CommandContext context){
        // Levels
        final LevelManager levelManager = context.getSource().tryToGetServer().getLevelManager();
        final Collection<LevelS> levels = levelManager.getLoadedLevels();
        
        // Create list
        final StringJoiner joiner = new StringJoiner(", ");
        for(LevelS level: levels)
            joiner.add(level.getConfiguration().getName());
        
        // Send levels
        context.getSource().sendMessage(new Component().color(TextColor.YELLOW).text("Levels: ").reset().text(joiner.toString()));
    }
    
    
}
