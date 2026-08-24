package minecraftose.client.renderer.level;

import generaloss.resourceflow.Disposable;
import minecraftose.client.control.camera.PlayerCamera;
import minecraftose.client.entity.model.PlayerModel;
import minecraftose.client.options.Options;

public class PlayerRenderer implements Disposable{
    
    final LevelRenderer levelRenderer;
    
    public PlayerRenderer(LevelRenderer levelRenderer){
        this.levelRenderer = levelRenderer;
    }
    
    
    public void render(PlayerCamera camera){
        final Options options = levelRenderer.getGameRenderer().getMinecraft().getOptions();
        final PlayerModel playerModel = levelRenderer.getGameRenderer().getMinecraft().getPlayer().getModel();
        
        // Render player
        if(playerModel != null){
            playerModel.animate();
            if(!camera.getPerspective().isFirstPerson() || options.isFirstPersonModel())
                playerModel.render(camera);
        }
    }
    
    @Override
    public void dispose(){ }
    
}