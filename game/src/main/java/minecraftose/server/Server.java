package minecraftose.server;

import generaloss.networkforge.tcp.TCPServer;
import minecraftose.main.Tickable;
import minecraftose.main.text.Component;
import minecraftose.main.time.GameTime;
import minecraftose.server.command.ServerCommandDispatcher;
import minecraftose.server.level.LevelManager;
import minecraftose.server.level.LevelS;
import minecraftose.server.network.ServerConnectionManager;
import minecraftose.server.player.PlayerList;
import minecraftose.server.player.ServerPlayer;
import generaloss.chronokit.TickGenerator;
import minecraftose.server.level.gen.generator.ChunkGeneratorDefault;
import minecraftose.server.time.ServerGameTime;

import java.io.IOException;
import java.util.Collection;

public abstract class Server implements Tickable {
    
    private final ServerConfiguration configuration;
    private final TCPServer tcpServer;
    private final ServerConnectionManager connectionManager;
    private final PlayerList playerList;
    private final LevelManager levelManager;
    private final ServerCommandDispatcher commandDispatcher;
    private final ServerGameTime gameTime;
    
    public Server(){
        this.configuration = new ServerConfiguration();
        
        this.connectionManager = new ServerConnectionManager(this);
        this.tcpServer = new TCPServer()
            .registerOnReceive(connectionManager::onReceive)
            .registerOnConnect(connectionManager::onConnect)
            .registerOnDisconnect(connectionManager::onDisconnect);

        this.playerList = new PlayerList(this);
        this.levelManager = new LevelManager(this);
        this.commandDispatcher = new ServerCommandDispatcher(this);
        this.gameTime = new ServerGameTime(this);
    }
    

    public void run(){
        final long seed = 61128216; // Maths.randomSeed(8);
        getLevelManager().createLevel(getConfiguration().getDefaultLevelName(), String.valueOf(seed), ChunkGeneratorDefault.INSTANCE); // Create overworld level
        // getLevelManager().createLevel("flat-world", "FLAT", ChunkGeneratorFlat.INSTANCE); // Create flat-world level

        try {
            tcpServer.run(getConfiguration().getAddress(), getConfiguration().getPort()); // Run TCP server

            new TickGenerator(GameTime.TICKS_PER_SECOND).startAsync(this::tick);
        } catch(IOException e) {
            throw new RuntimeException("Cannot run server: ", e);
        }
    }

    public void stop(){
        final Collection<ServerPlayer> players = getPlayerList().getPlayers();
        for(ServerPlayer player: players)
            player.disconnect();
    }

    
    @Override
    public void tick(){
        connectionManager.handlePackets();
        gameTime.tick();
        
        for(LevelS level : getLevelManager().getLoadedLevels())
            level.tick();
    }


    public void broadcast(Component component){
        playerList.broadcastServerMessage(component);
    }
    
    
    public ServerConfiguration getConfiguration(){
        return configuration;
    }
    
    public PlayerList getPlayerList(){
        return playerList;
    }
    
    public LevelManager getLevelManager(){
        return levelManager;
    }
    
    public ServerConnectionManager getConnectionManager(){
        return connectionManager;
    }
    
    public ServerCommandDispatcher getCommandDispatcher(){
        return commandDispatcher;
    }
    
    public ServerGameTime getGameTime(){
        return gameTime;
    }
    
}
