package worldline.api;

import java.util.Objects;

/** Immutable legacy item stack observed from an authoritative remote server. */
public final class RemoteItemStack {
    private final int legacyId, count, damage;

    public RemoteItemStack(int legacyId, int count, int damage) {
        if (legacyId < 0 || legacyId > 32767) throw new IllegalArgumentException("invalid item ID");
        if (count < 1 || count > 127) throw new IllegalArgumentException("invalid item count");
        if (damage < 0 || damage > 32767) throw new IllegalArgumentException("invalid item damage");
        this.legacyId = legacyId; this.count = count; this.damage = damage;
    }

    public int legacyId() { return legacyId; }
    public int count() { return count; }
    public int damage() { return damage; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof RemoteItemStack)) return false;
        RemoteItemStack value = (RemoteItemStack) other;
        return legacyId == value.legacyId && count == value.count && damage == value.damage;
    }
    @Override public int hashCode() { return Objects.hash(legacyId, count, damage); }
    @Override public String toString() { return "RemoteItemStack[" + legacyId + "x" + count + ":" + damage + "]"; }
}
