package minecraftose.client.time;

import generaloss.chronokit.DeltaTimeCounter;
import minecraftose.main.time.GameTime;

public class ClientGameTime extends GameTime{

    private final DeltaTimeCounter dtCounter;
    private long prevTickPointMillis;

    public ClientGameTime(){
        this.dtCounter = new DeltaTimeCounter();
    }

    @Override
    public void tick(){
        super.tick();
        dtCounter.update();
        this.prevTickPointMillis = System.currentTimeMillis();
    }


    public float getDeltaTime(){
        return dtCounter.getTime();
    }

    private float getMillisLerpFactor(){
        return (float) (System.currentTimeMillis() - prevTickPointMillis);
    }

    private float getSecondLerpFactor(){
        return this.getMillisLerpFactor() / MILLIS_PER_SECOND;
    }

    public float getTickLerpFactor(){
        return this.getMillisLerpFactor() / MILLIS_IN_SECOND / this.getDeltaTime();
    }

    public float getLerpSeconds(){
        return super.getSeconds() + this.getSecondLerpFactor();
    }

    public float getLerpMinutes(){
        return this.getLerpSeconds() / SECONDS_IN_MINUTE;
    }

    public float getLerpDays(){
        return this.getLerpSeconds() / SECONDS_IN_DAY;
    }

}
