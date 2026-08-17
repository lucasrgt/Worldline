package worldline.api;

import java.util.Objects;

/** Immutable observation from a controlled dedicated server. */
public final class ServerState {
    public static final long UNKNOWN_TIME = -1L;

    private final ServerLifecycle lifecycle;
    private final int port;
    private final boolean onlineMode;
    private final long worldTime;
    private final int completedSaves;

    public ServerState(ServerLifecycle lifecycle, int port, boolean onlineMode,
            long worldTime, int completedSaves) {
        this.lifecycle = Objects.requireNonNull(lifecycle, "lifecycle");
        if (port < 1 || port > 65535) throw new IllegalArgumentException("invalid server port");
        if (worldTime < UNKNOWN_TIME) throw new IllegalArgumentException("invalid world time");
        if (completedSaves < 0) throw new IllegalArgumentException("negative save count");
        this.port = port;
        this.onlineMode = onlineMode;
        this.worldTime = worldTime;
        this.completedSaves = completedSaves;
    }

    public ServerLifecycle lifecycle() { return lifecycle; }
    public int port() { return port; }
    public boolean onlineMode() { return onlineMode; }
    public long worldTime() { return worldTime; }
    public int completedSaves() { return completedSaves; }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ServerState)) return false;
        ServerState state = (ServerState) other;
        return lifecycle == state.lifecycle && port == state.port
                && onlineMode == state.onlineMode && worldTime == state.worldTime
                && completedSaves == state.completedSaves;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lifecycle, port, onlineMode, worldTime, completedSaves);
    }

    @Override
    public String toString() {
        return "ServerState{" + lifecycle + ",port=" + port + ",online=" + onlineMode
                + ",time=" + worldTime + ",saves=" + completedSaves + "}";
    }
}
