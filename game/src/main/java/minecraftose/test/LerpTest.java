package minecraftose.test;

import jpize.context.Jpize;
import jpize.context.JpizeApplication;
import jpize.context.input.MouseBtn;
import jpize.opengl.gl.GL;
import jpize.util.mesh.TextureBatch;
import jpize.context.input.Key;
import generaloss.spatialmath.vector.Vec2f;
import generaloss.chronokit.TickGenerator;
import minecraftose.client.time.ClientGameTime;
import minecraftose.client.time.var.TickFloat;
import minecraftose.main.time.GameTime;

public class LerpTest extends JpizeApplication implements Runnable {

    public static final int SIZE = 70;

    TickGenerator tickGen = new TickGenerator(GameTime.TICKS_PER_SECOND);
    ClientGameTime time = new ClientGameTime();

    TextureBatch batch = new TextureBatch();

    TickFloat x = new TickFloat();
    TickFloat y = new TickFloat();
    Vec2f velocity = new Vec2f();

    public void init(){
        tickGen.startAsync(this);
    }

    @Override
    public void run(){
        time.tick();

        velocity.mul(0.95);

        float speed = 5F;
        if(Key.W.pressed()) velocity.y += speed;
        if(Key.A.pressed()) velocity.x -= speed;
        if(Key.S.pressed()) velocity.y -= speed;
        if(Key.D.pressed()) velocity.x += speed;
        if(MouseBtn.pressedAny(MouseBtn.values())){
            velocity.add(
                    (Jpize.getX() - x.value()) / 10,
                    (Jpize.getY() - y.value()) / 10
            );
        }


        if(x.value() + velocity.x < 0){
            x.set(0);
            velocity.x = 0;
        }else if(x.value() + velocity.x >= Jpize.getWidth() - SIZE){
            x.set(Jpize.getWidth() - 1 - SIZE);
            velocity.x = 0;
        }else{
            x.add(velocity.x);
        }

        if(y.value() + velocity.y < 0){
            y.set(0);
            velocity.y = 0;
        }else if(y.value() + velocity.y >= Jpize.getHeight() - SIZE){
            y.set(Jpize.getHeight() - 1 - SIZE);
            velocity.y = 0;
        }else{
            y.add(velocity.y);
        }
    }

    public void render(){
        GL.clearColorBuffer();
        batch.setup();

        float t = time.getTickLerpFactor();
        batch.drawRect(x.value(), y.value(), SIZE, SIZE,  0.4, 0, 0, 1);
        batch.drawRect(x.getLerp(t), y.getLerp(t), SIZE, SIZE,  0, 1, 0, 0.75);

        batch.render();
    }

    public void update(){
        if(Key.ESCAPE.down())
            Jpize.exit();
        System.out.println(Jpize.getX());
    }

}
