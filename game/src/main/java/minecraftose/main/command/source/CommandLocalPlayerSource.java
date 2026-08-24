package minecraftose.main.command.source;

import generaloss.spatialmath.vector.Vec3f;
import minecraftose.client.entity.LocalPlayer;
import minecraftose.client.level.LevelC;
import minecraftose.main.chat.source.MessageSourceClient;
import minecraftose.main.text.Component;

public class CommandLocalPlayerSource extends CommandSource{

    private final LocalPlayer player;

    public CommandLocalPlayerSource(LocalPlayer player){
        this.player = player;
    }


    @Override
    public Vec3f getPosition(){
        return player.getPosition();
    }

    public LocalPlayer getPlayer(){
        return player;
    }

    @Override
    @SuppressWarnings("unchecked")
    public LevelC getLevel(){
        return player.getLevel();
    }

    @Override
    public void sendMessage(Component message){
        player.getMinecraft().getChat().putMessage(new MessageSourceClient(), message);
    }

}
