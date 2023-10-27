package minecraftose.server.command.vanilla;

import minecraftose.main.text.Component;
import minecraftose.main.text.TextColor;
import minecraftose.main.command.CommandContext;
import minecraftose.server.command.CommandDispatcher;
import minecraftose.main.command.builder.Commands;
import jpize.util.time.JpizeRunnable;

import java.util.concurrent.atomic.AtomicInteger;

public class CommandShutdown{

    public static void registerTo(CommandDispatcher dispatcher){
        dispatcher.newCommand(Commands.literal("shutdown")
            .requiresPlayer()
            .requires(source -> source.asPlayer().getName().equals("GeneralPashon") || source.asPlayer().getName().equals("Herobrine"))

            .executes( CommandShutdown::shutdown)
            .then(Commands.literal("now").executes( CommandShutdown::shutdownNow ) )
        );
    }

    private static void shutdown(CommandContext context){
        context.getServer().broadcast(new Component().color(TextColor.DARK_RED).text("Shutting down the server..."));

        final AtomicInteger counterInt = new AtomicInteger(3);
        new JpizeRunnable(() -> {
            if(counterInt.get() == 0)
                context.getServer().stop();
            else{
                context.getServer().broadcast(new Component().color(TextColor.DARK_RED).text(counterInt));
                counterInt.set(counterInt.get() - 1);
            }
        }).runTimerAsync(1000, 1000);
    }

    private static void shutdownNow(CommandContext context){
        context.getServer().stop();
    }

}