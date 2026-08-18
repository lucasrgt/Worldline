package worldline.api;

import java.util.Objects;

/** Named peer Packet18 observation; target and damage are not encoded. */
public final class RemotePeerSwing {
    private final String username; private final int entityId;
    public RemotePeerSwing(String username, int entityId) { if (!name(username) || entityId < 0)
        throw new IllegalArgumentException("invalid peer swing"); this.username = username; this.entityId = entityId; }
    public String username() { return username; } public int entityId() { return entityId; }
    public int animation() { return 1; }
    private static boolean name(String value) { return value != null && value.matches("[A-Za-z0-9_]{1,16}"); }
    @Override public boolean equals(Object other) { if (!(other instanceof RemotePeerSwing)) return false;
        RemotePeerSwing value = (RemotePeerSwing) other; return username.equals(value.username) && entityId == value.entityId; }
    @Override public int hashCode() { return Objects.hash(username, entityId); }
}
