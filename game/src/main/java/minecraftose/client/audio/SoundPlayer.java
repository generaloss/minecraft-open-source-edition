package minecraftose.client.audio;

import jpize.audio.al.buffer.AlBuffer;
import jpize.audio.al.listener.AlListener;
import jpize.audio.al.source.AlSource;
import jpize.audio.util.AlSourcePool;
import generaloss.resourceflow.Disposable;
import minecraftose.client.Minecraft;
import minecraftose.main.audio.Sound;

public class SoundPlayer implements Disposable{

    public static final int MAX_SOUND_SOURCES = 64;

    private final Minecraft minecraft;
    private final AlSourcePool sources;

    public SoundPlayer(Minecraft minecraft){
        this.minecraft = minecraft;

        System.out.println("[Client]: Initialize audio player");

        this.sources = new AlSourcePool(MAX_SOUND_SOURCES);
        AlListener.setOrientation(0, 0, 1, 0, 1, 0); //: check it
    }


    public void play(Sound sound, float volume, float pitch, float x, float y, float z){
        final AlBuffer buffer = minecraft.getResources().getSound(sound.getID());
        if(buffer == null){
            System.err.println("Sound " + sound.name() + " is not found");
            return;
        }
        play(buffer, volume, pitch * sound.pitch(), x, y, z);
    }

    public void play(AlBuffer buffer, float volume, float pitch, float x, float y, float z){
        if(buffer == null)
            return;
        final AlSource source = sources.findFreeSource();
        if(source == null)
            return;

        source.setBuffer(buffer);
        source.setGain(volume);
        source.setPitch(pitch);
        source.setPosition(x, y, z);
        source.play();
    }

    public int getPlayingSoundsNum(){
        int playing = 0;
        for(AlSource source: sources.getSources())
            if(source.isPlaying())
                playing++;
        return playing;
    }


    @Override
    public void dispose(){
        sources.dispose();
    }

}
