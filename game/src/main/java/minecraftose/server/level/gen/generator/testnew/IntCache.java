package minecraftose.server.level.gen.generator.testnew;

import generaloss.rawlist.IntList;

public class IntCache{

    private static int a = 256;
    private static final IntList b = new IntList();
    private static final IntList c = new IntList();
    private static final IntList d = new IntList();
    private static final IntList e = new IntList();

    public static synchronized int[] getIntCache(int i){
        int[] aint;

        if(i <= 256){
            if(b.isEmpty()){
                aint = new int[256];
                c.add(aint);
                return aint;
            }else{
                aint = new int[] { b.remove(b.size() - 1) };
                c.add(aint);
                return aint;
            }
        }else if(i > a){
            a = i;
            d.clear();
            e.clear();
            aint = new int[a];
            e.add(aint);
            return aint;
        }else if(d.isEmpty()){
            aint = new int[a];
            e.add(aint);
            return aint;
        }else{
            aint = new int[] { d.remove(d.size() - 1) };
            e.add(aint);
            return aint;
        }
    }

    public static synchronized void getIntCache(){
        if(!d.isEmpty()){
            d.remove(d.size() - 1);
        }

        if(!b.isEmpty()){
            b.remove(b.size() - 1);
        }

        d.addAll(e);
        b.addAll(c);
        e.clear();
        c.clear();
    }

    public static synchronized String getString(){
        return "cache: " + d.size() + ", tcache: " + b.size() + ", allocated: " + e.size() + ", tallocated: " + c.size();
    }

}