package mymod;

import jpize.context.Jpize;
import jpize.opengl.texture.Texture2D;
import jpize.util.mesh.TextureBatch;
import jpize.context.input.Key;
import jpize.util.font.Font;
import generaloss.resourceflow.resource.Resource;
import minecraftose.client.Minecraft;
import minecraftose.main.modification.api.ClientModInitializer;
import minecraftose.main.text.Component;
import minecraftose.main.text.StyleFormatting;
import minecraftose.main.text.TextColor;
import minecraftose.server.player.ServerPlayer;

import java.util.Collection;

public class ModClient implements ClientModInitializer{

    private TextureBatch batch;
    private Texture2D texture;
    private Font font;
    
    @Override
    public void onInitializeClient(){
        System.out.println("[Test-Modification]: Mod initialized (Client)");
        
        batch = new TextureBatch();
        //font = FontLoader.getDefault();
        texture = new Texture2D(Resource.internal(ModClient.class, "icon.png"));
    }
    
    
    public void render(){
        batch.setup();
        batch.draw(texture, Jpize.getWidth() - 100, Jpize.getHeight() - 30, 100, 30);
        //font.drawText(batch, "Mod text", Jpize.getWidth() - 100, Jpize.getHeight() - 30);
        batch.render();
        
        if(Key.F10.down()){
            Collection<ServerPlayer> players = Minecraft.INSTANCE.getIntegratedServer().getPlayerList().getPlayers();
            for(ServerPlayer player: players)
                player.sendMessage(new Component()
                        .color(TextColor.DARK_RED)
                        .text("<" + player.getName() + "> ")
                        .color(1, 0.5, 0.2)
                        .style(StyleFormatting.ITALIC)
                        .text("Mod text")
                );
        }
    }

}
