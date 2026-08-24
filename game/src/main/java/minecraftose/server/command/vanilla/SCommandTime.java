package minecraftose.server.command.vanilla;

import generaloss.spatialmath.Maths;
import minecraftose.main.time.GameTime;
import minecraftose.main.command.CommandContext;
import minecraftose.server.command.ServerCommandDispatcher;
import minecraftose.main.command.argument.CommandArg;
import minecraftose.main.command.argument.CommandArgTime;
import minecraftose.main.command.builder.Commands;

public class SCommandTime{
    
    public static void registerTo(ServerCommandDispatcher dispatcher){
        dispatcher.newCommand(Commands.literal("time")
        
            .then(Commands.literal("set")
                .then(Commands.argument("time", CommandArg.time()).executes( SCommandTime::setTime ))
        
                .then(Commands.literal("day")
                    .executes( SCommandTime::setDay )
                    
                    .then(Commands.argument("time", CommandArg.time()).executes( SCommandTime::setDayTime ))
                )
                .then(Commands.literal("midnight").executes( SCommandTime::setMidnight ))
                .then(Commands.literal("night").executes( SCommandTime::setNight ))
                .then(Commands.literal("noon").executes( SCommandTime::setNoon ))
            )
        
            .then(Commands.literal("get")
                .executes( SCommandTime::getTicks )
                .then(Commands.literal("seconds").executes( SCommandTime::getSeconds ))
                .then(Commands.literal("minutes").executes( SCommandTime::getMinutes ))
                .then(Commands.literal("day")
                    .executes( SCommandTime::getDays )

                    .then(Commands.literal("ticks").executes( SCommandTime::getDayTicks ))
                    .then(Commands.literal("seconds").executes( SCommandTime::getDaySeconds ))
                    .then(Commands.literal("minutes").executes( SCommandTime::getDayMinutes ))
                )
            )
        
        );
    }
    
    
    private static void setTime(CommandContext context){
        final CommandArgTime time = context.getArg(0).asTime();
        // Set time
        context.getSource().tryToGetServer().getGameTime()
            .setTime(time.getTime(), time.getUnit());
        // Send msg
        context.getSource().sendMessage("Set the gametime to " + time.getTime() + " " + time.getUnit().getLiteral());
    }
    
    
    private static void setDayTime(CommandContext context){
        final CommandArgTime time = context.getArg(0).asTime();
        // Set day time
        context.getSource().tryToGetServer().getGameTime()
            .setDayTime(time.getTime(), time.getUnit());
        // Send msg
        context.getSource().sendMessage("Set the daytime to " + time.getTime() + " " + time.getUnit().getLiteral());
    }
    
    
    private static void setDay(CommandContext context){
        context.getSource().tryToGetServer().getGameTime().setDay();
        // Send msg
        context.getSource().sendMessage("Set the time to Day (" + GameTime.TIME_DAY + ")");
    }
    
    private static void setMidnight(CommandContext context){
        context.getSource().tryToGetServer().getGameTime().setMidnight();
        // Send msg
        context.getSource().sendMessage("Set the time to Midnight (" + GameTime.TIME_MIDNIGHT + ")");
    }
    
    private static void setNight(CommandContext context){
        context.getSource().tryToGetServer().getGameTime().setNight();
        // Send msg
        context.getSource().sendMessage("Set the time to Night (" + GameTime.TIME_NIGHT + ")");
    }
    
    private static void setNoon(CommandContext context){
        context.getSource().tryToGetServer().getGameTime().setNoon();
        // Send msg
        context.getSource().sendMessage("Set the time to Noon (" + GameTime.TIME_NOON + ")");
    }
    
    
    private static void getTicks(CommandContext context){
        context.getSource().sendMessage("Gametime is " +
            context.getSource().tryToGetServer().getGameTime().getTicks()
        );
    }
    
    private static void getSeconds(CommandContext context){
        context.getSource().sendMessage("Gametime is " +
            context.getSource().tryToGetServer().getGameTime().getSeconds() + " seconds"
        );
    }
    
    private static void getMinutes(CommandContext context){
        context.getSource().sendMessage("Gametime is " +
            context.getSource().tryToGetServer().getGameTime().getMinutes() + " minutes"
        );
    }
    
    private static void getDays(CommandContext context){
        context.getSource().sendMessage("Day is " +
            Maths.floor( context.getSource().tryToGetServer().getGameTime().getDays() + 1 )
        );
    }
    
    
    private static void getDayTicks(CommandContext context){
        context.getSource().sendMessage("Daytime is " +
            context.getSource().tryToGetServer().getGameTime().getDayTicks()
        );
    }
    
    private static void getDaySeconds(CommandContext context){
        context.getSource().sendMessage("Daytime is " +
            context.getSource().tryToGetServer().getGameTime().getDaySeconds() + " seconds"
        );
    }
    
    private static void getDayMinutes(CommandContext context){
        context.getSource().sendMessage("Daytime is " +
            context.getSource().tryToGetServer().getGameTime().getDayMinutes() + " minutes"
        );
    }
    
}
