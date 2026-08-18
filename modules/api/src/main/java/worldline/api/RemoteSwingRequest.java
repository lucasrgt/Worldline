package worldline.api;

import java.util.Objects;

/** Local Packet18 request identity; server acceptance is not implied. */
public final class RemoteSwingRequest {
    private final String username; private final int entityId;
    public RemoteSwingRequest(String username, int entityId) { if (!name(username) || entityId < 0)
        throw new IllegalArgumentException("invalid swing request"); this.username = username; this.entityId = entityId; }
    public String username() { return username; } public int entityId() { return entityId; }
    public int animation() { return 1; }
    private static boolean name(String value) { return value != null && value.matches("[A-Za-z0-9_]{1,16}"); }
    @Override public boolean equals(Object other) { if (!(other instanceof RemoteSwingRequest)) return false;
        RemoteSwingRequest value = (RemoteSwingRequest) other; return username.equals(value.username) && entityId == value.entityId; }
    @Override public int hashCode() { return Objects.hash(username, entityId); }
}
