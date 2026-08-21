package worldline.api;

import java.util.Objects;

/** Immutable observation of one multiplayer protocol session. */
public final class MultiplayerState {
    public static final int UNKNOWN_ENTITY = -1;
    private final MultiplayerConnection connection;
    private final String username;
    private final int protocolVersion;
    private final int entityId;

    public MultiplayerState(MultiplayerConnection connection, String username,
            int protocolVersion, int entityId) {
        this.connection = Objects.requireNonNull(connection, "connection");
        if (username == null || username.isEmpty() || username.length() > 16)
            throw new IllegalArgumentException("invalid multiplayer username");
        if (protocolVersion < 0) throw new IllegalArgumentException("negative protocol version");
        if (entityId < UNKNOWN_ENTITY) throw new IllegalArgumentException("invalid entity id");
        this.username = username;
        this.protocolVersion = protocolVersion;
        this.entityId = entityId;
    }

    public MultiplayerConnection connection() { return connection; }
    public String username() { return username; }
    public int protocolVersion() { return protocolVersion; }
    public int entityId() { return entityId; }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof MultiplayerState)) return false;
        MultiplayerState state = (MultiplayerState) other;
        return connection == state.connection && username.equals(state.username)
                && protocolVersion == state.protocolVersion && entityId == state.entityId;
    }

    @Override public int hashCode() { return Objects.hash(connection, username, protocolVersion, entityId); }
}
