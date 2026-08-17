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
import worldline.api.RemoteWorldMultiplayerSession;

/** Minimal original protocol-14 client for headless multiplayer qualification. */
public final class B173WireClient implements RemoteWorldMultiplayerSession {
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
            output.writeByte(2); writeString(output, username); output.flush();
            require(input.readUnsignedByte() == 2, "handshake response packet drift");
            require(readString(input, 32).equals("-"), "server did not use offline handshake");
            output.writeByte(1); output.writeInt(PROTOCOL); writeString(output, username);
            output.writeLong(0L); output.writeByte(0); output.flush();
            int packet = input.readUnsignedByte();
            if (packet == 255) throw new IllegalStateException("login rejected: " + readString(input, 256));
            require(packet == 1, "login response packet drift: " + packet);
            entityId = input.readInt();
            readString(input, 16); input.readLong(); input.readByte();
            require(entityId >= 0, "server returned invalid entity id");
            play = new B173PlayChannel(input, output);
            connection = MultiplayerConnection.CONNECTED;
        } catch (IOException error) { closeSocket(); throw new IllegalStateException("multiplayer login failed", error); }
    }

    @Override
    public MultiplayerState state() {
        return new MultiplayerState(connection, username, PROTOCOL, entityId);
    }

    @Override
    public PlayerPose synchronizePose() {
        require(connection == MultiplayerConnection.CONNECTED, "session is not connected");
        try { return play.synchronize(); }
        catch (IOException error) { throw new IllegalStateException("play synchronization failed", error); }
    }

    @Override
    public void look(float yaw, float pitch) {
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
        try { return play.awaitChunkSnapshot(); }
        catch (IOException error) { throw new IllegalStateException("chunk decode failed", error); }
    }

    @Override
    public void close() {
        closeSocket();
        if (connection != MultiplayerConnection.NEW) connection = MultiplayerConnection.DISCONNECTED;
    }

    private void closeSocket() {
        if (socket == null) return;
        try { socket.close(); }
        catch (IOException error) { throw new IllegalStateException("could not close multiplayer socket", error); }
        finally { socket = null; play = null; }
    }

    private static void writeString(DataOutputStream output, String value) throws IOException {
        output.writeShort(value.length());
        for (int index = 0; index < value.length(); index++) output.writeChar(value.charAt(index));
    }

    private static String readString(DataInputStream input, int maximum) throws IOException {
        int length = input.readShort();
        if (length < 0 || length > maximum) throw new IOException("invalid string length " + length);
        StringBuilder result = new StringBuilder(length);
        for (int index = 0; index < length; index++) result.append(input.readChar());
        return result.toString();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
