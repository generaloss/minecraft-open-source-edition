package minecraftose.main.command.source;

import generaloss.spatialmath.vector.Vec3f;
import minecraftose.main.level.Level;
import minecraftose.main.text.Component;
import minecraftose.server.Server;

public abstract class CommandSource{
    
    public abstract Vec3f getPosition();

    public abstract <L extends Level> L getLevel();

    public abstract void sendMessage(Component message);

    public final void sendMessage(String unformattedText){
        sendMessage(new Component().text(unformattedText));
    }

    
    public CommandLocalPlayerSource asClientSource(){
        return ((CommandLocalPlayerSource) this);
    }

    public CommandServerPlayerSource asServerPlayerSource(){
        return ((CommandServerPlayerSource) this);
    }

    public Server tryToGetServer(){
        return ((CommandServerSource) this).getServer();
    }

}
