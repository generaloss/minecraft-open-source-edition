package minecraftose.client;

import jpize.Jpize;
import jpize.math.vecmath.vector.Vec3f;
import jpize.net.tcp.TcpClient;
import jpize.util.time.DeltaTimeCounter;
import jpize.util.time.FpsCounter;
import minecraftose.client.chat.Chat;
import minecraftose.client.control.BlockRayCast;
import minecraftose.client.control.camera.GameCamera;
import minecraftose.client.entity.LocalPlayer;
import minecraftose.client.level.ClientLevel;
import minecraftose.client.net.ClientConnectionHandler;
import minecraftose.client.renderer.particle.Particle;
import minecraftose.client.renderer.particle.ParticleBatch;
import minecraftose.client.time.ClientGameTime;
import minecraftose.main.Tickable;
import minecraftose.main.net.packet.serverbound.SBPacketLogin;
import minecraftose.main.net.packet.serverbound.SBPacketMove;
import minecraftose.main.time.GameTime;

public class ClientGame implements Tickable{
    
    private final Minecraft session;
    private final TcpClient client;
    private final ClientConnectionHandler connectionHandler;
    private final Chat chat;

    private final BlockRayCast blockRayCast;
    private final ClientGameTime time;
    private final LocalPlayer player;
    private final GameCamera camera;

    private final DeltaTimeCounter tickDtCounter;


    private ClientLevel level;
    
    public ClientGame(Minecraft session){
        this.session = session;

        this.connectionHandler = new ClientConnectionHandler(this);
        this.client = new TcpClient(connectionHandler);
        
        this.blockRayCast = new BlockRayCast(session, 10);
        this.chat = new Chat(this);
        this.time = new ClientGameTime(this);

        this.player = new LocalPlayer(null, session.getProfile().getUsername());

        this.camera = new GameCamera(this, 0.1, 5000, session.getOptions().getFieldOfView());
        this.camera.setDistance(session.getOptions().getRenderDistance());

        this.tickDtCounter = new DeltaTimeCounter();
    }
    
    public Minecraft getSession(){
        return session;
    }

    
    public void connect(String address, int port){
        System.out.println("[Client]: Connect to " + address + ":" + port);
        client.connect(address, port);
        client.getConnection().setTcpNoDelay(true);
        connectionHandler.sendPacket( new SBPacketLogin(session.getVersion().getID(), session.getProfile().getUsername()) );
    }

    @Override
    public void tick(){
        tickDtCounter.count();

        if(level == null)
            return;

        time.tick();
        player.tick();
        level.tick();

        // Send player position
        if(player.isPositionChanged() || player.isRotationChanged())
            connectionHandler.sendPacket(new SBPacketMove(player));

        if(time.getTicks() % GameTime.TICKS_IN_SECOND == 0)
            connectionHandler.countTxRx();
    }
    
    public void update(){
        if(camera == null)
            return;

        session.getGame().getPlayer().getController().update();
        player.updateInterpolation();
        blockRayCast.update();
        camera.update();
    }
    
    public void createClientLevel(String worldName){
        if(level != null){
            Jpize.execSync(() ->{
                level.getConfiguration().setName(worldName);
                level.getChunkManager().reset();
            });
        }else{
            level = new ClientLevel(this, worldName);
            blockRayCast.setLevel(level);
            player.setLevel(level);
        }
    }
    
    public void disconnect(){
        client.disconnect();
        
        if(level != null){
            Jpize.execSync(() -> {
                System.out.println(10000);
                level.getChunkManager().dispose();
            });
        }
    }
    
    
    public void spawnParticle(Particle particle, Vec3f position){
        final ParticleBatch particleBatch = session.getRenderer().getWorldRenderer().getParticleBatch();
        particleBatch.spawnParticle(particle, position);
    }
    
    public final ClientLevel getLevel(){
        return level;
    }
    
    public final LocalPlayer getPlayer(){
        return player;
    }
    
    public final GameCamera getCamera(){
        return camera;
    }
    
    public final BlockRayCast getBlockRayCast(){
        return blockRayCast;
    }
    
    public final Chat getChat(){
        return chat;
    }
    
    public final ClientGameTime getTime(){
        return time;
    }

    public final ClientConnectionHandler getConnectionHandler(){
        return connectionHandler;
    }

    public float getTickDt(){
        return tickDtCounter.get();
    }

}