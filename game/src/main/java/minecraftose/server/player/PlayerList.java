package minecraftose.server.player;

import generaloss.spatialmath.vector.Vec3f;
import generaloss.networkforge.tcp.TCPConnection;
import generaloss.networkforge.packet.VisitorNetPacket;
import minecraftose.main.network.packet.s2c.game.*;
import minecraftose.main.text.Component;
import minecraftose.main.text.TextColor;
import minecraftose.server.Server;
import minecraftose.server.level.LevelS;
import minecraftose.server.network.ServerPlayerGameConnection;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerList{
    
    private final Server server;
    private final Map<String, ServerPlayer> playerMap;
    
    public PlayerList(Server server){
        this.server = server;
        playerMap = new ConcurrentHashMap<>();
    }
    
    public Server getServer(){
        return server;
    }
    
    
    public Collection<ServerPlayer> getPlayers(){
        return playerMap.values();
    }
    
    public boolean isPlayerOnline(String name){
        return playerMap.containsKey(name);
    }
    
    public ServerPlayer getPlayer(String name){
        return playerMap.get(name);
    }
    
    
    public void addNewPlayer(String name, TCPConnection connection){
        // Get level & Spawn position
        final LevelS level;
        final Vec3f spawnPosition;
        
        final OfflinePlayer offlinePlayer = getOfflinePlayer(name);
        if(offlinePlayer != null){
            
            final String levelName = offlinePlayer.getLevelName();
            server.getLevelManager().loadLevel(levelName);
            level = server.getLevelManager().getLevel(levelName);
            spawnPosition = offlinePlayer.getPosition();
        }else{
            
            final String levelName = server.getConfiguration().getDefaultLevelName();
            server.getLevelManager().loadLevel(levelName);
            level = server.getLevelManager().getLevel(levelName);
            spawnPosition = level.getSpawnPosition();
        }
        
        // Add ServerPlayer to list
        final ServerPlayer serverPlayer = new ServerPlayer(level, connection, name);
        server.getConnectionManager().setHandlerForConnection(connection, serverPlayer.getConnection());
        serverPlayer.teleport(level, spawnPosition);
        
        playerMap.put(name, serverPlayer);
        
        // Send packets to player
        final ServerPlayerGameConnection connectionAdapter = serverPlayer.getConnection();
        
        connection.send(new S2CPacketSpawnInfo(level.getConfiguration().getName(), spawnPosition, server.getGameTime().getTicks())); // spawn init info
        connection.send(new S2CPacketAbilities(false)); // abilities
        
        for(ServerPlayer anotherPlayer: playerMap.values())
            if(anotherPlayer != serverPlayer)
                connectionAdapter.sendPacket(new S2CPacketSpawnPlayer(anotherPlayer)); // all players info
        
        // Load chunks for player
        level.addEntity(serverPlayer);
        level.getChunkProvider().getLoader().loadChunkForPlayer(serverPlayer);

        // Send to all player-connection-event packet
        broadcastPacketExcept(new S2CPacketSpawnPlayer(serverPlayer), serverPlayer);
        
        serverPlayer.sendToChat(new Component().color(TextColor.YELLOW).text("Player " + name + " joined the game"));
    }
    
    
    public void disconnectPlayer(ServerPlayer player){
        broadcastPacketExcept(new S2CPacketRemoveEntity(player), player); // Remove player entity on client
        player.getLevel().removeEntity(player); // Remove entity on server
        PlayerIO.save(player);                  // Save
        
        playerMap.remove(player.getName());
    }
    
    
    public OfflinePlayer getOfflinePlayer(String name){ //: NICHE TAK, HARAM
        return null;
    }
    
    
    public void broadcastPacket(VisitorNetPacket<?> packet){
        for(ServerPlayer player: playerMap.values())
            player.sendPacket(packet);
    }

    public void broadcastPacketExcept(VisitorNetPacket<?> packet, ServerPlayer except){
        for(ServerPlayer player: playerMap.values())
            if(player != except)
                player.sendPacket(packet);
    }

    public void broadcastPacketLevel(LevelS level, VisitorNetPacket<?> packet){
        for(ServerPlayer player: playerMap.values())
            if(player.getLevel() == level)
                player.sendPacket(packet);
    }

    public void broadcastPacketLevelExcept(LevelS level, VisitorNetPacket<?> packet, ServerPlayer except){
        for(ServerPlayer player: playerMap.values())
            if(player.getLevel() == level && player != except)
                player.sendPacket(packet);
    }
    
    
    public void broadcastServerMessage(Component message){
        broadcastPacket(new S2CPacketChatMessage(message.toFlatList()));
        System.out.println(message);
    }
    
    public void broadcastPlayerMessage(ServerPlayer player, Component message){
        broadcastPacket(new S2CPacketChatMessage(player.getName(), message.toFlatList()));
        System.out.println(message);
    }
    
}
