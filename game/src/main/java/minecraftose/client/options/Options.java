package minecraftose.client.options;

import generaloss.resourceflow.resource.FileResource;
import generaloss.resourceflow.stream.FastReader;
import jpize.context.Jpize;
import jpize.context.input.Key;
import generaloss.resourceflow.resource.Resource;
import jpize.lwjgl.glfw.Glfw;
import minecraftose.client.Minecraft;
import minecraftose.client.control.camera.PlayerCamera;
import minecraftose.main.SharedConstants;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class Options{

    public static int UNLIMITED_FPS_THRESHOLD = 256;

    public static float DEFAULT_FIELD_OF_VIEW = 85F;
    public static int DEFAULT_RENDER_DISTANCE = 14;
    public static float DEFAULT_MOUSE_SENSITIVITY = 1F;
    public static float DEFAULT_BRIGHTNESS = 0.5F;
    public static boolean DEFAULT_FOG = true;

    public static int DEFAULT_MAX_FRAME_RATE = 0; // VSync
    public static boolean DEFAULT_FULLSCREEN = false;
    public static boolean DEFAULT_SHOW_FPS = false;
    public static boolean DEFAULT_FIRST_PERSON_MODEL = false;
    public static String DEFAULT_HOST = "0.0.0.0:" + SharedConstants.DEFAULT_PORT;


    private final Minecraft minecraft;
    private final FileResource fileResource;
    
    private String host = DEFAULT_HOST;
    private final Map<KeyMapping, Key> keyMappings;
    private float fieldOfView = DEFAULT_FIELD_OF_VIEW;
    private int renderDistance = DEFAULT_RENDER_DISTANCE;
    private int maxFrameRate = DEFAULT_MAX_FRAME_RATE; // 0 - VSync; [1, 255]; 256 - Unlimited
    private boolean fullscreen = DEFAULT_FULLSCREEN;
    private boolean showFps = DEFAULT_SHOW_FPS;
    private float mouseSensitivity = DEFAULT_MOUSE_SENSITIVITY;
    private boolean fog = DEFAULT_FOG;
    private float brightness = DEFAULT_BRIGHTNESS;
    private boolean firstPersonModel = DEFAULT_FIRST_PERSON_MODEL;
    

    public Options(Minecraft minecraft, String gameDirPath){
        this.minecraft = minecraft;

        this.keyMappings = new HashMap<>();
        this.fileResource = Resource.file(gameDirPath + "options.txt");
        this.fileResource.create();
    }
    

    public void load(){
        try(final FastReader reader = fileResource.reader()){
            while(reader.hasNext()){
                final String[] parts = reader.nextLine().split(" : ");
                if(parts.length != 2)
                    continue;

                final String value = parts[1].trim();

                final String[] keyParts = parts[0].split("\\.");
                if(keyParts.length != 2)
                    continue;

                final String category = keyParts[0];
                final String key = keyParts[1];

                // category.key : value
                switch(category){
                    case "remote" -> {
                        switch(key){
                            case "host" -> setRemoteHost(value);
                        }
                    }
                    case "graphics" -> {
                        switch(key){
                            case "fieldOfView" -> setFieldOfView(Float.parseFloat(value));
                            case "renderDistance" -> setRenderDistance(Integer.parseInt(value));
                            case "maxFramerate" -> setMaxFrameRate(Integer.parseInt(value));
                            case "fullscreen" -> setFullscreen(Boolean.parseBoolean(value));
                            case "showFps" -> setShowFPS(Boolean.parseBoolean(value));
                            case "brightness" -> setBrightness(Float.parseFloat(value));
                            case "fog" -> setFog(Boolean.parseBoolean(value));
                            case "firstPersonModel" -> setFirstPersonModel(Boolean.parseBoolean(value));
                        }
                    }
                    case "key" -> setKey(KeyMapping.valueOf(key.toUpperCase()), Key.valueOf(value.toUpperCase()));
                    case "control" -> {
                        switch(key){
                            case "mouseSensitivity" -> setMouseSensitivity(Float.parseFloat(value));
                        }
                    }
                }
            }

        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public void save(){
        final PrintWriter out = fileResource.writer();
        
        out.println("remote.host : " + host);
        out.println("graphics.fieldOfView : " + fieldOfView);
        out.println("graphics.renderDistance : " + renderDistance);
        out.println("graphics.maxFramerate : " + maxFrameRate);
        out.println("graphics.fullscreen : " + fullscreen);
        out.println("graphics.showFps : " + showFps);
        out.println("graphics.brightness : " + brightness);
        out.println("graphics.fog : " + fog);
        out.println("graphics.firstPersonModel : " + firstPersonModel);

        out.println("control.mouseSensitivity : " + mouseSensitivity);

        for(KeyMapping keyType: KeyMapping.values())
            out.println("key." + keyType.toString().toLowerCase() + " : " + keyMappings.getOrDefault(keyType, keyType.getDefault()).toString().toLowerCase());
        
        out.close();
    }
    
    public String getHost(){
        return host;
    }
    
    public void setRemoteHost(String host){
        this.host = host;
    }
    

    public Key getKey(KeyMapping keyType){
        return keyMappings.getOrDefault(keyType, keyType.getDefault());
    }

    public void setKey(KeyMapping keyType, Key key){
        keyMappings.put(keyType, key);
    }


    public float getFieldOfView(){
        return fieldOfView;
    }

    public void setFieldOfView(float fieldOfView){
        this.fieldOfView = fieldOfView;
        
        final PlayerCamera camera = minecraft.getCamera();
        if(camera != null)
            camera.setFov(fieldOfView);
    }


    public int getRenderDistance(){
        return renderDistance;
    }

    public void setRenderDistance(int renderDistance){
        this.renderDistance = renderDistance;
        
        final PlayerCamera camera = minecraft.getCamera();
        if(camera != null)
            camera.setDistance(renderDistance);
    }


    public int getMaxFrameRate(){
        return maxFrameRate;
    }

    public void setMaxFrameRate(int maxFrameRate){
        this.maxFrameRate = maxFrameRate;
        minecraft.getFpsSync().setRate(maxFrameRate);

        minecraft.getFpsSync().setEnabled(maxFrameRate > 0 && maxFrameRate < UNLIMITED_FPS_THRESHOLD);
        Glfw.enableVSync(maxFrameRate == 0);
    }


    public boolean isFullscreen(){
        return fullscreen;
    }

    public void setFullscreen(boolean fullscreen){
        this.fullscreen = fullscreen;
        Jpize.window.setFullscreen(fullscreen);
    }


    public boolean isShowFPS(){
        return showFps;
    }

    public void setShowFPS(boolean showFps){
        this.showFps = showFps;
    }
    
    
    public float getBrightness(){
        return brightness;
    }
    
    public void setBrightness(float brightness){
        this.brightness = brightness;
    }


    public boolean isFogEnabled(){
        return fog;
    }

    public void setFog(boolean fog){
        this.fog = fog;
    }


    public float getMouseSensitivity(){
        return mouseSensitivity;
    }

    public void setMouseSensitivity(float mouseSensitivity){
        this.mouseSensitivity = mouseSensitivity;
    }
    
    
    public boolean isFirstPersonModel(){
        return firstPersonModel;
    }
    
    public void setFirstPersonModel(boolean firstPersonModel){
        this.firstPersonModel = firstPersonModel;
    }

}
