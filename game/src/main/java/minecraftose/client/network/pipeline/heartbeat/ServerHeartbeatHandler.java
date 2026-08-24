package minecraftose.client.network.pipeline.heartbeat;

import generaloss.networkforge.tcp.TCPConnection;
import generaloss.networkforge.tcp.listener.CloseReason;
import generaloss.networkforge.tcp.pipeline.EventHandler;
import generaloss.networkforge.tcp.pipeline.EventInvocationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerHeartbeatHandler extends EventHandler {

    private static class State {
        volatile long lastReceiveTime;
        volatile long lastSendTime;
    }

    private final Map<TCPConnection, State> states = new ConcurrentHashMap<>();

    private final long timeoutMs;
    private final long pingIntervalMs;

    public ServerHeartbeatHandler(long timeoutMs, long pingIntervalMs) {
        this.timeoutMs = timeoutMs;
        this.pingIntervalMs = pingIntervalMs;

        final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor((runnable) -> {
            final Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(this::checkTimeouts, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public boolean handleConnect(EventInvocationContext ctx) {
        final State state = new State();
        state.lastReceiveTime = System.currentTimeMillis();

        states.put(ctx.getConnection(), state);
        return true;
    }

    @Override
    public boolean handleDisconnect(EventInvocationContext ctx, CloseReason reason) {
        states.remove(ctx.getConnection());
        return true;
    }

    @Override
    public boolean handleReceive(EventInvocationContext ctx, byte[] data) {
        final State state = states.get(ctx.getConnection());
        if(state != null)
            state.lastReceiveTime = System.currentTimeMillis();

        return (!(data.length == 1 && data[0] == 0x01));
    }

    private void checkTimeouts() {
        final long now = System.currentTimeMillis();

        states.forEach((connection, state) -> {
            if(now - state.lastReceiveTime > timeoutMs) {
                System.out.println("[Debug] Server heartbeat timedout");
                connection.close();
            }
            if(now - state.lastSendTime > pingIntervalMs) {
                connection.send(new byte[]{ 0x01 });
                state.lastSendTime = now;
            }
        });
    }

}