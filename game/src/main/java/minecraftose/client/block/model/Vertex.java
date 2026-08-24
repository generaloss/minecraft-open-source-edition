package minecraftose.client.block.model;

import jpize.util.color.Color;
import generaloss.spatialmath.vector.Vec2f;
import generaloss.spatialmath.vector.Vec3f;

import java.util.List;

public class Vertex{

    private final Vec3f position;
    private final Vec2f uv;
    private final Color color;

    public Vertex(Vec3f position, Vec2f uv, Color color){
        this.position = position;
        this.uv = uv;
        this.color = color;
    }

    public Vertex(Vec3f position, float u, float v, Color color){
        this(position, new Vec2f(u, v), color);
    }

    public void putFloats(List<Float> floatList, float x, float y, float z, float r, float g, float b, float a){
        floatList.add(position.x + x);
        floatList.add(position.y + y);
        floatList.add(position.z + z);

        floatList.add(color.red * r);
        floatList.add(color.green * g);
        floatList.add(color.blue * b);
        floatList.add(color.alpha * a);

        floatList.add(uv.x);
        floatList.add(uv.y);
    }

    public void putIntsPacked(List<Integer> intList, int x, int y, int z, int mulU, int mulV, float r, float g, float b, float a){
        // Packed position
        final int positionPacked = (
            ((int) (position.x + x)      ) | // 5 bit
            ((int) (position.y + y) << 5 ) | // 9 bit
            ((int) (position.z + z) << 14) | // 5 bit

            ((int) (uv.x * mulU) << 19) | // 4 bit
            ((int) (uv.y * mulV) << 23)   // 4 bit

            // 4 bit remaining
        );
        intList.add(positionPacked); // x, y, z, u, v

        // Packed color
        final int colorPacked = (
            ((int) (color.red * r * 255)      ) | // 8 bit
            ((int) (color.green * g * 255) << 8 ) | // 8 bit
            ((int) (color.blue * b * 255) << 16) | // 8 bit
            ((int) (color.alpha * a * 255) << 24)   // 8 bit
        );
        intList.add(colorPacked); // r, g, b, a
    }

}
