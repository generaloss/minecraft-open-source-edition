package minecraftose.server;

import minecraftose.client.Minecraft;

public class IntegratedServer extends Server {
    
    private final Minecraft minecraft;
    
    public IntegratedServer(Minecraft minecraft){
        this.minecraft = minecraft;
        getConfiguration().loadDefaults(); // Load server configuration
    }
    
    public Minecraft getMinecraft(){
        return minecraft;
    }
    

    public void run(){
        super.run();
        System.out.println("[Server]: Integrated server listening on " + getConfiguration().getAddress() + ":" + getConfiguration().getPort());
    }
    
}
