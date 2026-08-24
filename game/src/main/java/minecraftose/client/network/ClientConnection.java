package minecraftose.client.network;

import generaloss.networkforge.packet.PacketReader;
import generaloss.networkforge.packet.VisitorNetPacket;
import generaloss.networkforge.tcp.TCPClient;
import generaloss.networkforge.tcp.TCPConnection;
import generaloss.networkforge.tcp.listener.CloseReason;
import generaloss.networkforge.tcp.options.TCPConnectionOptionsHolder;
import jpize.context.Jpize;
import minecraftose.client.Minecraft;
import minecraftose.client.network.pipeline.security.AESKey;
import minecraftose.main.network.packet.s2c.game.*;
import minecraftose.main.network.packet.s2c.login.S2CPacketEncryptStart;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;

public class ClientConnection {

    private final Minecraft minecraft;

    private final TCPClient tcpClient;
    private TCPConnection tcpConnection;

    private final PacketReader packetReader;
    private final ConcurrentLinkedQueue<Runnable> packetsToHandle;
    private final ClientPacketHandler handler;

    private int txCounter, rxCounter, tx, rx;

    public ClientConnection(Minecraft minecraft){
        this.minecraft = minecraft;
        this.packetReader = new PacketReader();
        this.packetsToHandle = new ConcurrentLinkedQueue<>();
        this.handler = new ClientPacketHandler(this);
        registerPackets();

        // Handle packets
        final Thread thread = new Thread(() -> {
            while(!Thread.interrupted()){
                Thread.yield();
                handlePackets();
            }
        });
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setDaemon(true);
        thread.start();

        this.tcpClient = new TCPClient()
            .registerOnReceive(this::onReceive)
            .registerOnConnect(this::onConnect)
            .registerOnDisconnect(this::onDisconnect);
    }
    
    public Minecraft getMinecraft(){
        return minecraft;
    }


    public void connect(String address, int port){
        final TCPConnectionOptionsHolder options = new TCPConnectionOptionsHolder();
        options.setTcpNoDelay(true);
        tcpClient.setInitialOptions(options);

        try {
            tcpClient.connect(address, port);
        } catch(IOException | TimeoutException e) {
            System.err.println("Failed to connect to " + address + ":" + port);
            Jpize.exit();
            throw new RuntimeException(e);
        }
    }

    public void disconnect(){
        tcpClient.close();
    }

    public void sendPacket(VisitorNetPacket<?> packet){
        tcpConnection.send(packet);
        txCounter++;
    }

    public void encode(AESKey encodeKey){
        // if(encodeKey == null) {
        //     tcpConnection.getCiphers().setCiphers(null, null);
        // } else {
        //     tcpConnection.getCiphers().setCiphers(
        //         encodeKey.getEncryptCipher(),
        //         encodeKey.getDecryptCipher()
        //     );
        // }
    }


    public void onReceive(TCPConnection sender, byte[] bytes){
        packetReader.tryRead(bytes).ifPresentOrElse(packet -> {
            final Runnable packetHandleRunnable = () -> {
                // noinspection rawtypes
                if(packet instanceof VisitorNetPacket visitorPacket) {
                    // noinspection unchecked
                    visitorPacket.handle(handler);
                } else {
                    System.err.println("Client cannot handle packet ID=" + packet.getClass());
                }
            };
            packetsToHandle.add(packetHandleRunnable);
        }, () -> System.err.println("Client cannot read packet"));
    }
    
    public void onConnect(TCPConnection connection){
        this.tcpConnection = connection;
    }
    
    public void onDisconnect(TCPConnection connection, CloseReason reason){
        Jpize.exit();
    }


    public void handlePackets(){
        while(!packetsToHandle.isEmpty()){
            packetsToHandle.poll().run();
            rxCounter++;
        }
    }


    public void countTxRx(){
        tx = txCounter;
        txCounter = 0;
        rx = rxCounter;
        rxCounter = 0;
    }

    public int getTX(){
        return tx;
    }

    public int getRX(){
        return rx;
    }


    private void registerPackets(){
        packetReader.register(S2CPacketAbilities.class);
        packetReader.register(S2CPacketBlockUpdate.class);
        packetReader.register(S2CPacketChatMessage.class);
        packetReader.register(S2CPacketChunk.class);
        packetReader.register(S2CPacketDisconnect.class);
        packetReader.register(S2CPacketEncryptStart.class);
        packetReader.register(S2CPacketEntityMove.class);
        packetReader.register(S2CPacketLightUpdate.class);
        packetReader.register(S2CPacketPlayerSneaking.class);
        packetReader.register(S2CPacketPlaySound.class);
        packetReader.register(S2CPacketPong.class);
        packetReader.register(S2CPacketRemoveEntity.class);
        packetReader.register(S2CPacketSpawnEntity.class);
        packetReader.register(S2CPacketSpawnInfo.class);
        packetReader.register(S2CPacketSpawnPlayer.class);
        packetReader.register(S2CPacketTeleportPlayer.class);
        packetReader.register(S2CPacketTime.class);
        packetReader.register(S2CPacketPlayerVelocity.class);
    }
    
}
