package minecraftose.server.player;

import generaloss.spatialmath.EulerAngles;
import generaloss.spatialmath.vector.Vec3f;
import generaloss.networkforge.tcp.TCPConnection;
import generaloss.networkforge.packet.VisitorNetPacket;
import minecraftose.main.audio.Sound;
import minecraftose.main.entity.Entity;
import minecraftose.main.entity.Player;
import minecraftose.main.inventory.PlayerInventory;
import minecraftose.main.level.Level;
import minecraftose.main.network.packet.s2c.game.S2CPacketAbilities;
import minecraftose.main.network.packet.s2c.game.S2CPacketChatMessage;
import minecraftose.main.network.packet.s2c.game.S2CPacketPlaySound;
import minecraftose.main.network.packet.s2c.game.S2CPacketTeleportPlayer;
import minecraftose.main.text.Component;
import minecraftose.server.Server;
import minecraftose.server.level.LevelS;
import minecraftose.server.network.ServerPlayerGameConnection;

public class ServerPlayer extends Player{
    
    private final Server server;
    private final ServerPlayerGameConnection connection;
    private final PlayerInventory inventory;
    
    private int renderDistance;

    public ServerPlayer(LevelS level, TCPConnection connection, String name){
        super(level, name);
        this.server = level.getServer();
        this.connection = new ServerPlayerGameConnection(this, connection);
        this.renderDistance = server.getConfiguration().getMaxRenderDistance(); //: 0
        this.inventory = new PlayerInventory();
    }
    
    public Server getServer(){
        return server;
    }

    @Override
    public LevelS getLevel(){
        return (LevelS) super.getLevel();
    }

    public void playSound(Sound sound, float volume, float pitch){
        sendPacket(new S2CPacketPlaySound(sound, volume, pitch, this.getPosition()));
    }
    
    
    public void teleport(Level level, Vec3f position, EulerAngles rotation){
        sendPacket(new S2CPacketTeleportPlayer(level.getConfiguration().getName(), position, rotation));
        
        final Level oldLevel = getLevel();
        if(level != oldLevel){
            setLevel(level);
            oldLevel.removeEntity(this);
            level.addEntity(this);
        }
        getPosition().set(position);
        getRotation().set(rotation);
    }
    
    public void teleport(Entity entity){
        teleport(entity.getLevel(), entity.getPosition(), entity.getRotation());
    }
    
    public void teleport(Vec3f position, EulerAngles rotation){
        teleport(getLevel(), position, rotation);
    }
    
    public void teleport(Level level, Vec3f position){
        teleport(level, position, getRotation());
    }
    
    public void teleport(Vec3f position){
        teleport(getLevel(), position, getRotation());
    }
    
    
    public void setFlyEnabled(boolean flyEnabled){
        sendPacket(new S2CPacketAbilities(flyEnabled));
        super.setFlyEnabled(flyEnabled);
    }


    public void disconnect(){
        getConnection().disconnect();
    }
    
    public void sendToChat(Component message){
        server.getPlayerList().broadcastPlayerMessage(this, message);
    }
    
    public void sendMessage(Component message){
        sendPacket(new S2CPacketChatMessage(message.toFlatList()));
    }
    
    
    public ServerPlayerGameConnection getConnection(){
        return connection;
    }
    
    public void sendPacket(VisitorNetPacket<?> packet){
        connection.sendPacket(packet);
    }
    
    public int getRenderDistance(){
        return renderDistance;
    }
    
    public void setRenderDistance(int renderDistance){
        this.renderDistance = renderDistance;
    }


    public PlayerInventory getInventory(){
        return inventory;
    }
    
}
