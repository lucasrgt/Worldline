package worldline.b173server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import worldline.api.MultiplayerConnection;
import worldline.api.MultiplayerState;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkObservation;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.MovementOutcome;
import worldline.api.InventoryMultiplayerSession;
import worldline.api.RemoteInventoryView;

/** Minimal original protocol-14 client for headless multiplayer qualification. */
public final class B173WireClient implements InventoryMultiplayerSession {
    public static final int PROTOCOL = 14;
    private final String host, username;
    private final int port, timeoutMillis;
    private MultiplayerConnection connection = MultiplayerConnection.NEW;
    private int entityId = MultiplayerState.UNKNOWN_ENTITY;
    private Socket socket;
    private B173PlayChannel play;

    public B173WireClient(String host, int port, String username, Duration timeout) {
        if (host == null || host.isEmpty()) throw new IllegalArgumentException("empty host");
        if (port < 1 || port > 65535) throw new IllegalArgumentException("invalid port");
        if (username == null || username.isEmpty() || username.length() > 16)
            throw new IllegalArgumentException("invalid username");
        this.host = host; this.port = port; this.username = username;
        this.timeoutMillis = Math.toIntExact(timeout.toMillis());
    }

    @Override
    public void connect() {
        require(connection == MultiplayerConnection.NEW, "session was already used");
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), timeoutMillis);
            socket.setSoTimeout(timeoutMillis);
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            output.writeByte(2); B173InboundPacket.string(output, username); output.flush();
            require(input.readUnsignedByte() == 2, "handshake response packet drift");
            require(B173InboundPacket.string(input, 32).equals("-"), "server did not use offline handshake");
            output.writeByte(1); output.writeInt(PROTOCOL); B173InboundPacket.string(output, username);
            output.writeLong(0L); output.writeByte(0); output.flush();
            int packet = input.readUnsignedByte();
            if (packet == 255) throw new IllegalStateException("login rejected: " + B173InboundPacket.string(input, 256));
            require(packet == 1, "login response packet drift: " + packet);
            entityId = input.readInt();
            B173InboundPacket.string(input, 16); input.readLong(); input.readByte();
            require(entityId >= 0, "server returned invalid entity id");
            play = new B173PlayChannel(input, output, timeoutMillis);
            connection = MultiplayerConnection.CONNECTED;
        } catch (IOException error) { closeSocket(); throw new IllegalStateException("multiplayer login failed", error); }
    }

    @Override public MultiplayerState state() { return new MultiplayerState(connection, username, PROTOCOL, entityId); }

    @Override public PlayerPose synchronizePose() {
        require(connection == MultiplayerConnection.CONNECTED, "session is not connected");
        try { return play.synchronize(); }
        catch (IOException error) { throw new IllegalStateException("play synchronization failed", error); }
    }

    @Override public void look(float yaw, float pitch) {
        require(connection == MultiplayerConnection.CONNECTED, "session is not connected");
        try { play.look(yaw, pitch); }
        catch (IOException error) { throw new IllegalStateException("play look failed", error); }
    }

    @Override
    public PlayerPose moveBy(double deltaX, double deltaY, double deltaZ) {
        require(connection == MultiplayerConnection.CONNECTED, "session is not connected");
        try { return play.moveBy(deltaX, deltaY, deltaZ); }
        catch (IOException error) { throw new IllegalStateException("play movement failed", error); }
    }

    @Override
    public void sendChat(String message) {
        require(connection == MultiplayerConnection.CONNECTED, "session is not connected");
        try { play.sendChat(message); }
        catch (IOException error) { throw new IllegalStateException("chat send failed", error); }
    }

    @Override
    public String awaitChat() {
        require(connection == MultiplayerConnection.CONNECTED, "session is not connected");
        try { return play.awaitChat(); }
        catch (IOException error) { throw new IllegalStateException("chat receive failed", error); }
    }

    @Override
    public RemoteChunkObservation awaitChunk() {
        require(connection == MultiplayerConnection.CONNECTED, "session is not connected");
        try { return play.awaitChunk(); }
        catch (IOException error) { throw new IllegalStateException("chunk receive failed", error); }
    }

    @Override
    public RemoteChunkSnapshot awaitChunkSnapshot() {
        require(connection == MultiplayerConnection.CONNECTED, "session is not connected");
        try { return play.awaitChunkSnapshot(); } catch (IOException error) { throw new IllegalStateException("chunk decode failed", error); }
    }

    @Override
    public RemoteWorldView awaitRemoteWorld(int minimumChunks) {
        require(connection == MultiplayerConnection.CONNECTED, "session is not connected");
        try { return play.awaitRemoteWorld(minimumChunks); } catch (IOException error) { throw new IllegalStateException("remote world receive failed", error); }
    }

    @Override public RemoteWorldView awaitRemoteChunk(int chunkX, int chunkZ) {
        require(connection == MultiplayerConnection.CONNECTED, "session is not connected");
        try { return play.awaitRemoteChunk(chunkX, chunkZ); } catch (IOException error) { throw new IllegalStateException("remote chunk receive failed", error); }
    }

    @Override public void beginBreak(BlockPosition position) {
        require(connection == MultiplayerConnection.CONNECTED, "session is not connected");
        try { play.beginBreak(position); } catch (IOException error) { throw new IllegalStateException("begin break failed", error); }
    }

    @Override public void finishBreak(BlockPosition position) {
        require(connection == MultiplayerConnection.CONNECTED, "session is not connected");
        try { play.finishBreak(position); } catch (IOException error) { throw new IllegalStateException("finish break failed", error); }
    }

    @Override public RemoteWorldView awaitBlock(BlockPosition position, BlockState expected) {
        require(connection == MultiplayerConnection.CONNECTED, "session is not connected");
        try { return play.awaitBlock(position, expected); } catch (IOException error) {
            throw new IllegalStateException("expected block receive failed", error); }
    }

    @Override public RemoteWorldView sustainTicks(int ticks) {
        require(connection == MultiplayerConnection.CONNECTED, "session is not connected");
        try { return play.sustainTicks(ticks); }
        catch (IOException error) { throw new IllegalStateException("play heartbeat failed", error); }
        catch (InterruptedException error) { Thread.currentThread().interrupt();
            throw new IllegalStateException("play heartbeat interrupted", error); }
    }

    @Override public MovementOutcome moveAndObserve(double dx, double dy, double dz, int ticks) {
        require(connection == MultiplayerConnection.CONNECTED, "session is not connected");
        try { return play.moveAndObserve(dx, dy, dz, ticks); }
        catch (IOException error) { throw new IllegalStateException("movement observation failed", error); }
        catch (InterruptedException error) { Thread.currentThread().interrupt();
            throw new IllegalStateException("movement observation interrupted", error); }
    }

    @Override public RemoteInventoryView awaitInventory() { require(connection == MultiplayerConnection.CONNECTED, "session is not connected");
        try { return play.awaitInventory(); } catch (IOException error) {
            throw new IllegalStateException("inventory receive failed", error); }
    }

    @Override public RemoteInventoryView inventory() {
        require(connection == MultiplayerConnection.CONNECTED, "session is not connected"); return play.inventory(); }

    @Override public void close() { closeSocket();
        if (connection != MultiplayerConnection.NEW) connection = MultiplayerConnection.DISCONNECTED; }

    private void closeSocket() { if (socket == null) return;
        try { socket.close(); } catch (IOException error) {
            throw new IllegalStateException("could not close multiplayer socket", error); }
        finally { socket = null; play = null; }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message); }
}
