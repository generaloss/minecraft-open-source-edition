package minecraftose.client;

import generaloss.chronokit.Sync;
import generaloss.chronokit.TickGenerator;
import generaloss.chronokit.TimeUtils;
import generaloss.resourceflow.resource.Resource;
import generaloss.spatialmath.Mathc;
import jpize.audio.al.listener.AlListener;
import jpize.context.Jpize;
import jpize.context.JpizeApplication;
import jpize.context.SyncExecutor;
import generaloss.spatialmath.Maths;
import generaloss.spatialmath.vector.Vec3f;
import jpize.opengl.gl.GL;
import jpize.opengl.texture.Texture2D;
import minecraftose.Main;
import minecraftose.client.audio.MusicGroup;
import minecraftose.client.audio.MusicPlayer;
import minecraftose.client.audio.SoundPlayer;
import minecraftose.client.block.ClientBlocks;
import minecraftose.client.chat.Chat;
import minecraftose.client.command.ClientCommandDispatcher;
import minecraftose.client.control.BlockRayCast;
import minecraftose.client.control.GameInput;
import minecraftose.client.control.camera.PlayerCamera;
import minecraftose.client.entity.LocalPlayer;
import minecraftose.client.level.LevelC;
import minecraftose.client.network.ClientConnection;
import minecraftose.client.options.Options;
import minecraftose.client.renderer.GameRenderer;
import minecraftose.client.renderer.particle.Particle;
import minecraftose.client.renderer.particle.ParticleBatch;
import minecraftose.client.resources.GameResources;
import minecraftose.client.resources.VanillaBlocks;
import minecraftose.client.resources.VanillaMusic;
import minecraftose.client.resources.VanillaSound;
import minecraftose.client.time.ClientGameTime;
import minecraftose.main.SharedConstants;
import minecraftose.main.Tickable;
import minecraftose.main.Version;
import minecraftose.main.block.ChunkBlockData;
import minecraftose.main.modification.loader.ModEntryPointType;
import minecraftose.main.modification.loader.ModLoader;
import minecraftose.main.network.PlayerProfile;
import minecraftose.main.network.packet.c2s.game.move.C2SPacketMoveAndRot;
import minecraftose.main.network.packet.c2s.game.move.C2SPacketRot;
import minecraftose.main.network.packet.c2s.login.C2SPacketLogin;
import minecraftose.main.network.packet.c2s.game.move.C2SPacketMove;
import minecraftose.main.time.GameTime;
import minecraftose.server.IntegratedServer;

public class Minecraft extends JpizeApplication implements Tickable{

    public static final Minecraft INSTANCE = new Minecraft();

    private final PlayerProfile profile = Main.profile;
    private Options options;
    private Version version;
    private GameInput gameInput;
    private GameResources gameResources;
    private GameRenderer renderer;
    private IntegratedServer integratedServer;
    private SoundPlayer soundPlayer;
    private MusicPlayer musicPlayer;
    private ModLoader modLoader;
    private TickGenerator ticks;

    public static SyncExecutor syncExecutor;
    private Sync fpsSync;

    private ClientConnection connection;
    private ClientCommandDispatcher commandDispatcher;
    private Chat chat;

    private BlockRayCast blockRayCast;
    private ClientGameTime time;
    private LocalPlayer player;
    private PlayerCamera camera;


    private LevelC level;

    @Override
    public void init(){
        // Create Instances //
        Thread.currentThread().setName("Render-Thread");

        syncExecutor = Jpize.syncExecutor();

        // Resources //
        this.gameResources = new GameResources();
        VanillaBlocks.register(gameResources);
        VanillaSound.register(gameResources);
        VanillaMusic.register(gameResources);
        this.gameResources.load();

        // Other //
        this.version = new Version();

        this.options = new Options(this, SharedConstants.GAME_DIR_PATH);
        this.fpsSync = new Sync(0);
        this.fpsSync.disable();

        this.gameInput = new GameInput(this);
        this.renderer = new GameRenderer(this);
        this.soundPlayer = new SoundPlayer(this);
        this.musicPlayer = new MusicPlayer(this);

        this.renderer.init();
        Resource.file(SharedConstants.GAME_DIR_PATH).mkdirs();
        Resource.file(SharedConstants.MODS_PATH).mkdirs();

        this.options.load();

        this.ticks = new TickGenerator(GameTime.TICKS_PER_SECOND);
        this.ticks.startAsync(this::tick);

        this.connection = new ClientConnection(this);

        this.blockRayCast = new BlockRayCast(this, 16);
        this.chat = new Chat(this);
        this.time = new ClientGameTime();

        this.player = new LocalPlayer(this, null, profile.getUsername());
        this.commandDispatcher = new ClientCommandDispatcher(this);

        this.camera = new PlayerCamera(this, 0.1, 5000, options.getFieldOfView());
        this.camera.setDistance(options.getRenderDistance());

        // Mod Loader //
        this.modLoader = new ModLoader();
        this.modLoader.loadMods(SharedConstants.MODS_PATH);

        // Run local server //
        final String[] address = options.getHost().split(":");
        if(address[0].equals("0.0.0.0")){
            integratedServer = new IntegratedServer(this);
            integratedServer.run();
        }

        // Init mods //
        this.modLoader.initializeMods(ModEntryPointType.CLIENT);
        this.modLoader.initializeMods(ModEntryPointType.MAIN);

        // Load blocks
        ClientBlocks.register();
        ClientBlocks.loadBlocks(this);

        // Connect to server //
        TimeUtils.delayMillis(1000);
        this.connect(address[0], Integer.parseInt(address[1]));

        // Music
        this.musicPlayer.setGroup(MusicGroup.GAME);
    }

    
    public void connect(String address, int port){
        System.out.println("[Client]: Connect to " + address + ":" + port);
        connection.connect(address, port);
        connection.sendPacket( new C2SPacketLogin(version.getID(), profile.getUsername()) );
    }

    @Override
    public void tick(){
        if(level == null)
            return;

        // Send player position & rotation
        if(player.isPositionChanged() && player.isRotationChanged())
            connection.sendPacket(new C2SPacketMoveAndRot(player));
        else if(player.isPositionChanged())
            connection.sendPacket(new C2SPacketMove(player));
        else if(player.isRotationChanged())
            connection.sendPacket(new C2SPacketRot(player));

        time.tick();
        player.tick();
        level.tick();

        // Update audio listener
        if(player.isRotationChanged())
            AlListener.setOrientation(player.getRotation().getDirection(new Vec3f()).rotateY(180));
        if(player.isPositionChanged()){
            AlListener.setPosition(player.getLerpPosition().copy().add(0, player.getEyeHeight(), 0));
            AlListener.setVelocity(player.getVelocity());
        }

        if(time.getTicks() % GameTime.TICKS_IN_SECOND == 0)
            connection.countTxRx();
    }

    @Override
    public void update(){
        if(camera == null)
            return;

        player.getInput().update();
        player.updateInterpolation();
        blockRayCast.update();
        camera.update();
    }
    
    public void createClientLevel(String worldName){
        if(level != null){
            Jpize.syncExecutor().exec(() ->{
                level.getConfiguration().setName(worldName);
                level.getChunkProvider().removeAllChunks();
            });
        }else{
            level = new LevelC(this, worldName);
            blockRayCast.setLevel(level);
            player.setLevel(level);
        }
    }

    @Override
    public void render(){
        fpsSync.sync();
        gameInput.update();

        GL.clearColorDepthBuffers();
        renderer.render();

        modLoader.invokeMethod(ModEntryPointType.CLIENT, "render");
    }

    @Override
    public void resize(int width, int height){
        renderer.resize(width, height);
        camera.resize(width, height);
    }

    @Override
    public void dispose(){
        ticks.stop();

        // Save options
        options.save();

        // Stop server
        if(integratedServer != null)
            integratedServer.stop();
        else
            this.disconnect();

        // Free resources
        renderer.dispose();
        gameResources.dispose();
        soundPlayer.dispose();
        musicPlayer.dispose();
    }
    
    public void disconnect(){
        connection.disconnect();
        
        if(level != null){
            Jpize.syncExecutor().exec(() -> {
                System.out.println(10000);
                // level.getChunkProvider().dispose(); //: deprecated dispose chunk provider
            });
        }
    }
    
    public void spawnParticle(Particle particle, Vec3f position){
        final ParticleBatch particleBatch = renderer.getWorldRenderer().getParticleBatch();
        particleBatch.spawnParticle(particle, position);
    }
    
    public final LevelC getLevel(){
        return level;
    }
    
    public final LocalPlayer getPlayer(){
        return player;
    }
    
    public final PlayerCamera getCamera(){
        return camera;
    }
    
    public final BlockRayCast getBlockRayCast(){
        return blockRayCast;
    }
    
    public final Chat getChat(){
        return chat;
    }

    public final ClientCommandDispatcher getCommandDispatcher(){
        return commandDispatcher;
    }

    public final ClientGameTime getTime(){
        return time;
    }

    public final ClientConnection getConnection(){
        return connection;
    }

    public final Options getOptions(){
        return options;
    }

    public final Sync getFpsSync(){
        return fpsSync;
    }

    public final Version getVersion(){
        return version;
    }

    public final PlayerProfile getProfile(){
        return Main.profile;
    }

    public final GameRenderer getRenderer(){
        return renderer;
    }

    public final IntegratedServer getIntegratedServer(){
        return integratedServer;
    }

    public final GameInput getController(){
        return gameInput;
    }

    public final ModLoader getModLoader(){
        return modLoader;
    }

    public GameResources getResources(){
        return gameResources;
    }

    public SoundPlayer getSoundPlayer(){
        return soundPlayer;
    }

    public String getSessionToken(){
        return Main.sessionToken;
    }


    /**                 SOME HORRIBLE CODE                */

    public final Particle BREAK_PARTICLE = new Particle()
        .init(instance->{
            instance.size = Maths.random(0.02F, 0.15F);
            instance.region.set(Maths.random(0, 0.5), Maths.random(0, 0.5), Maths.random(0.5, 1), Maths.random(0.5, 1));
            instance.rotation = Maths.random(1, 360);
            instance.lifeTimeSeconds = Maths.random(0.5F, 2F);
            instance.velocity.set(Maths.random(-0.04F, 0.04F), Maths.random(-0.02F, 0.1F), Maths.random(-0.04F, 0.04F));
            instance.color.set(0.6, 0.6, 0.6);
        })
        .texture(new Texture2D("/texture/block/grass_block_side.png"))
        .animate(instance->{
            instance.velocity.y -= Jpize.getDeltaTime() * 0.35F;
            instance.velocity.mul(0.95);
            collide(instance.position, instance.velocity);
            instance.position.add(instance.velocity);
        });

    public void collide(Vec3f position, Vec3f velocity){
        double x = velocity.x;
        double y = velocity.y;
        if(ChunkBlockData.getID(level.getBlockState(position.xFloor(), position.yFloor() + Mathc.signum(x), position.zFloor())) != 0){
            double ny = Maths.frac(position.y) + y;
            if(ny < 0)
                y = 0;
        }
        double z = velocity.z;

        velocity.set(x, y, z);
    }

}
