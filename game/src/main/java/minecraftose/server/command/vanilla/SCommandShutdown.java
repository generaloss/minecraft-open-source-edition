package minecraftose.server.command.vanilla;

import generaloss.chronokit.AsyncRunnable;
import minecraftose.main.text.Component;
import minecraftose.main.text.TextColor;
import minecraftose.main.command.CommandContext;
import minecraftose.server.Server;
import minecraftose.server.command.ServerCommandDispatcher;
import minecraftose.main.command.builder.Commands;

import java.util.concurrent.atomic.AtomicInteger;

public class SCommandShutdown{

    public static void registerTo(ServerCommandDispatcher dispatcher){
        dispatcher.newCommand(Commands.literal("shutdown")
            .requiresPlayer()
            .requires(source -> source.asServerPlayerSource().getPlayer().getName().equals("GeneralPashon") || source.asServerPlayerSource().getPlayer().getName().equals("Herobrine"))

            .executes( SCommandShutdown::shutdown)
            .then(Commands.literal("now").executes( SCommandShutdown::shutdownNow ) )
        );
    }

    private static void shutdown(CommandContext context){
        final Server server = context.getSource().tryToGetServer();
        server.broadcast(new Component().color(TextColor.DARK_RED).text("Shutting down the server..."));

        final AtomicInteger counterInt = new AtomicInteger(3);
        new AsyncRunnable(() -> {
            if(counterInt.get() == 0)
                server.stop();
            else{
                server.broadcast(new Component().color(TextColor.DARK_RED).text(counterInt));
                counterInt.set(counterInt.get() - 1);
            }
        }).runInterval(1000, 1000);
    }

    private static void shutdownNow(CommandContext context){
        context.getSource().tryToGetServer().stop();
    }

}
