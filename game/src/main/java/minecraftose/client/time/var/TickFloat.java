package minecraftose.client.time.var;

import generaloss.spatialmath.Maths;

public class TickFloat{

    private float lastValue, value;

    public TickFloat(){ }


    public float value(){
        return value;
    }


    public void set(float newValue){
        updateLast();
        value = newValue;
    }

    public void add(float addValue){
        updateLast();
        value += addValue;
    }

    public void sub(float addValue){
        updateLast();
        value -= addValue;
    }

    public void mul(float addValue){
        updateLast();
        value *= addValue;
    }

    public void div(float addValue){
        updateLast();
        value /= addValue;
    }


    private void updateLast(){
        lastValue = value;
    }


    public float getLerp(float t){
        return Maths.lerp(lastValue, value, t);
    }

}
