package minecraftose.main.command.builder;

import minecraftose.main.command.argument.CommandArg;
import minecraftose.main.command.Command;
import minecraftose.main.command.node.CommandNodeArg;
import minecraftose.main.command.node.CommandNodeLiteral;

public class CommandBuilderLiteral{
    
    private final CommandNodeLiteral commandRoot;
    
    public CommandBuilderLiteral(String command){
        commandRoot = new CommandNodeLiteral(command);
    }
    
    
    public CommandBuilderLiteral then(String literal, CommandArg argumentType){
        commandRoot.addChild(new CommandNodeArg(literal, argumentType));
        return this;
    }
    
    public CommandBuilderLiteral executes(Command command){
        return this;
    }
    
    
    public CommandNodeLiteral buildNode(){
        return commandRoot;
    }

}
