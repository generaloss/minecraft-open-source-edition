package minecraftose.server.network;

import generaloss.networkforge.packet.PacketReader;
import generaloss.networkforge.packet.VisitorNetPacket;
import generaloss.networkforge.tcp.TCPConnection;
import generaloss.networkforge.tcp.listener.CloseReason;
import minecraftose.client.network.pipeline.security.RSAKey;
import minecraftose.main.network.packet.c2s.game.*;
import minecraftose.main.network.packet.c2s.game.move.C2SPacketMove;
import minecraftose.main.network.packet.c2s.game.move.C2SPacketMoveAndRot;
import minecraftose.main.network.packet.c2s.game.move.C2SPacketRot;
import minecraftose.main.network.packet.c2s.login.C2SPacketAuth;
import minecraftose.main.network.packet.c2s.login.C2SPacketEncryptEnd;
import minecraftose.main.network.packet.c2s.login.C2SPacketLogin;
import minecraftose.main.text.Component;
import minecraftose.main.text.TextColor;
import minecraftose.server.Server;
import minecraftose.server.player.ServerPlayer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerConnectionManager {
    
    private final Server server;
    private final Map<TCPConnection, ServerPacketHandler> handlerMap;
    private final PacketReader packetReader;
    private final ConcurrentLinkedQueue<Runnable> packetsToHandle;
    private final RSAKey rsaKey;
    
    public ServerConnectionManager(Server server){
        this.server = server;
        this.handlerMap = new HashMap<>();
        this.packetReader = new PacketReader();
        this.packetsToHandle = new ConcurrentLinkedQueue<>();
        this.rsaKey = new RSAKey(1024);
        registerPackets();
    }
    
    public Server getServer(){
        return server;
    }
    
    public RSAKey getRsaKey(){
        return rsaKey;
    }


    private void registerPackets(){
        packetReader.register(C2SPacketAuth.class);
        packetReader.register(C2SPacketChatMessage.class);
        packetReader.register(C2SPacketChunkRequest.class);
        packetReader.register(C2SPacketEncryptEnd.class);
        packetReader.register(C2SPacketLogin.class);
        packetReader.register(C2SPacketMoveAndRot.class);
        packetReader.register(C2SPacketMove.class);
        packetReader.register(C2SPacketRot.class);
        packetReader.register(C2SPacketPing.class);
        packetReader.register(C2SPacketPlayerBlockSet.class);
        packetReader.register(C2SPacketPlayerSneaking.class);
        packetReader.register(C2SPacketRenderDistance.class);
        packetReader.register(C2SPacketHitEntity.class);
    }
    
    
    public synchronized void onReceive(TCPConnection sender, byte[] bytes){
        final ServerPacketHandler packetHandler = handlerMap.get(sender);
        if(packetHandler == null)
            return;

        packetReader.tryRead(bytes).ifPresentOrElse(packet -> {
            final Runnable runnable = () -> {
                //noinspection rawtypes
                if (packet instanceof VisitorNetPacket visitorPacket) {
                    //noinspection unchecked
                    visitorPacket.handle(packetHandler);
                } else {
                    System.err.println("Server cannot handle packet");
                }
            };
            packetsToHandle.add(runnable);
        }, () -> System.err.println("Server cannot read packet " + Arrays.toString(bytes)));
    }
    
    public void onConnect(TCPConnection connection){
        this.setHandlerForConnection(connection, new PlayerLoginConnection(server, connection));
    }

    public void setHandlerForConnection(TCPConnection connection, ServerPacketHandler handler){
        handlerMap.put(connection, handler);
    }
    
    public void onDisconnect(TCPConnection connection, CloseReason reason){
        final ServerPacketHandler packetHandler = handlerMap.get(connection);

        if(packetHandler instanceof ServerPlayerGameConnection connectionAdapter){
            final ServerPlayer player = connectionAdapter.getPlayer();
            server.getPlayerList().disconnectPlayer(player);

            player.sendToChat(new Component().color(TextColor.YELLOW).text("Player " + player.getName() + " leave the game"));
            try {
                connection.awaitWriteDrain(500);
            } catch(InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        handlerMap.remove(connection);
    }

    public void handlePackets() {
        while(!packetsToHandle.isEmpty()){
            packetsToHandle.poll().run();
        }
    }

}
