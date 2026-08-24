package minecraftose.client.renderer;

import generaloss.rawlist.FloatList;
import jpize.opengl.buffer.GLVertexBuffer;
import jpize.util.color.Color;
import jpize.util.region.Region;

public class BufferBuilder {
    
    private final FloatList vertices;
    
    public BufferBuilder(){
        vertices = new FloatList();
    }
    
    public BufferBuilder array(float... array){
        vertices.add(array);
        return this;
    }
    
    public BufferBuilder array(double... array){
        for(double value: array)
            vertices.add((float) value);
        return this;
    }
    
    public BufferBuilder vertex(double x, double y){
        vertices.add((float) x, (float) y);
        return this;
    }
    
    public BufferBuilder vertex(double x, double y, double z){
        vertices.add((float) x, (float) y, (float) z);
        return this;
    }
    
    public BufferBuilder color(double r, double g, double b){
        vertices.add((float) r, (float) g, (float) b, 1F);
        return this;
    }
    
    public BufferBuilder color(double r, double g, double b, double a){
        vertices.add((float) r, (float) g, (float) b, (float) a);
        return this;
    }
    
    public BufferBuilder color(Color color){
        vertices.add(color.red, color.green, color.blue, color.alpha);
        return this;
    }
    
    public BufferBuilder color(){
        return color(1, 1, 1);
    }
    
    
    public BufferBuilder uv(float u, float v){
        vertices.add(u, v);
        return this;
    }
    
    public BufferBuilder uv(double u, double v){
        vertices.add((float) u, (float) v);
        return this;
    }
    
    
    public BufferBuilder quad(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4, double u1, double v1, double u2, double v2, Color color){
        vertex(x1, y1, z1).color(color).uv(u1, v1); // 1
        vertex(x2, y2, z2).color(color).uv(u1, v2); // 2
        vertex(x3, y3, z3).color(color).uv(u2, v2); // 3
        vertex(x3, y3, z3).color(color).uv(u2, v2); // 3
        vertex(x4, y4, z4).color(color).uv(u2, v1); // 4
        vertex(x1, y1, z1).color(color).uv(u1, v1); // 1
        
        return this;
    }

    public BufferBuilder quad(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4, Region region, Color color){
        return quad(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, region.u1(), region.v1(), region.u2(), region.v2(), color);
    }
    
    
    public void end(GLVertexBuffer buffer){
        vertices.trim();
        buffer.setData(vertices.array());
        vertices.clear();
    }
    
    public void end(FloatList list){
        vertices.trim();
        list.clear();
        list.addAll(vertices);
        vertices.clear();
    }
    
}
