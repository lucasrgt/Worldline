package worldline.api;

import java.util.Objects;

/** Immutable server-authoritative item carried by one named remote player. */
public final class RemoteHeldItem {
    private final String username;
    private final int legacyId, damage;

    public RemoteHeldItem(String username, int legacyId, int damage) {
        if (username == null || !username.matches("[A-Za-z0-9_]{1,16}"))
            throw new IllegalArgumentException("invalid player username");
        if (legacyId < 0 || legacyId > 32767) throw new IllegalArgumentException("invalid held item ID");
        if (damage < 0 || damage > 32767) throw new IllegalArgumentException("invalid held item damage");
        this.username = username; this.legacyId = legacyId; this.damage = damage;
    }

    public String username() { return username; }
    public int legacyId() { return legacyId; }
    public int damage() { return damage; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof RemoteHeldItem)) return false;
        RemoteHeldItem value = (RemoteHeldItem) other;
        return username.equals(value.username) && legacyId == value.legacyId && damage == value.damage;
    }
    @Override public int hashCode() { return Objects.hash(username, legacyId, damage); }
    @Override public String toString() { return "RemoteHeldItem[" + username + "=" + legacyId + ":" + damage + "]"; }
}
