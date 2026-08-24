package minecraftose.client.network.pipeline.heartbeat;

import generaloss.networkforge.tcp.TCPConnection;
import generaloss.networkforge.tcp.listener.CloseReason;
import generaloss.networkforge.tcp.pipeline.EventHandler;
import generaloss.networkforge.tcp.pipeline.EventInvocationContext;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientHeartbeatHandler extends EventHandler {

    private final long timeoutMs;
    private final long pingIntervalMs;

    private volatile TCPConnection connection;
    private volatile long lastReceiveTime;
    private volatile long lastSendTime;

    public ClientHeartbeatHandler(long timeoutMs, long pingIntervalMs) {
        this.timeoutMs = timeoutMs;
        this.pingIntervalMs = pingIntervalMs;

        final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor((runnable) -> {
            final Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(this::tick, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public boolean handleConnect(EventInvocationContext context) {
        connection = context.getConnection();

        final long now = System.currentTimeMillis();
        lastReceiveTime = now;
        lastSendTime = now;

        return true;
    }

    @Override
    public boolean handleDisconnect(EventInvocationContext context, CloseReason reason) {
        connection = null;
        return true;
    }

    @Override
    public boolean handleReceive(EventInvocationContext context, byte[] data) {
        lastReceiveTime = System.currentTimeMillis();
        return (!(data.length == 1 && data[0] == 0x01));
    }

    @Override
    public boolean handleSend(EventInvocationContext context, byte[] data) {
        lastSendTime = System.currentTimeMillis();
        return true;
    }

    private void tick() {
        if(connection == null)
            return;

        final long now = System.currentTimeMillis();

        // timeout
        if(now - lastReceiveTime > timeoutMs) {
            System.out.println("[Debug] Client heartbeat timeout");
            connection.close();
            return;
        }

        // ping
        if(now - lastSendTime > pingIntervalMs) {
            connection.send(new byte[]{ 0x01 });
            lastSendTime = now;
        }
    }

}