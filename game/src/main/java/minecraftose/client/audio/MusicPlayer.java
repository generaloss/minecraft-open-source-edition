package minecraftose.client.audio;

import generaloss.resourceflow.Disposable;
import generaloss.spatialmath.Maths;
import jpize.audio.util.AlMusic;
import minecraftose.client.Minecraft;

public class MusicPlayer implements Disposable {

    private final Minecraft minecraft;

    private AlMusic music;
    private MusicGroup group;
    private int index;

    public MusicPlayer(Minecraft minecraft){
        this.minecraft = minecraft;
    }

    public Minecraft getMinecraft(){
        return minecraft;
    }


    public void setGroup(MusicGroup group){
        if(music != null)
            music.stop();
        if(group.getList().length == 0)
            return;

        this.group = group;
        this.index = Maths.random(0, group.getList().length - 1);
        play();
    }

    private void play(){
        final String musicID = group.getList()[index];
        final AlMusic almusic = minecraft.getResources().getMusic(musicID);
        if(almusic == null){
            System.err.println("Music " + musicID + " is not found");
            return;
        }

        music = almusic;
        music.play();
        music.setOnComplete(this::playNext);

        System.out.println("Playing: " + musicID + "(" + index + ")");
    }

    private void playNext(){
        index++;
        if(index == group.getList().length)
            index = 0;

        play();
    }

    @Override
    public void dispose(){
        if(music != null)
            music.dispose();
    }

}
