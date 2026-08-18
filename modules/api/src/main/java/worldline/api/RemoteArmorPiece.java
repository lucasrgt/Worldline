package worldline.api;

import java.util.Objects;

/** Immutable armor item observed for one named remote player through Packet5. */
public final class RemoteArmorPiece {
    private final String username; private final RemoteArmorSlot slot;
    private final int legacyId, damage;
    public RemoteArmorPiece(String username, RemoteArmorSlot slot, int legacyId, int damage) {
        this(username, slot, legacyId, damage, false);
    }
    private RemoteArmorPiece(String username, RemoteArmorSlot slot, int legacyId, int damage, boolean empty) {
        if (username == null || !username.matches("[A-Za-z0-9_]{1,16}"))
            throw new IllegalArgumentException("invalid player username");
        if (slot == null || !empty && legacyId < 1 || legacyId > 32767 || damage < 0 || damage > 32767)
            throw new IllegalArgumentException("invalid remote armor piece");
        this.username = username; this.slot = slot; this.legacyId = legacyId; this.damage = damage;
    }
    public static RemoteArmorPiece empty(String username, RemoteArmorSlot slot) {
        return new RemoteArmorPiece(username, slot, -1, 0, true); }
    public String username() { return username; } public RemoteArmorSlot slot() { return slot; }
    public boolean empty() { return legacyId < 0; }
    public int legacyId() { if (empty()) throw new IllegalStateException("remote armor slot is empty"); return legacyId; }
    public int damage() { if (empty()) throw new IllegalStateException("remote armor slot is empty"); return damage; }
    @Override public boolean equals(Object other) { if (!(other instanceof RemoteArmorPiece)) return false;
        RemoteArmorPiece value = (RemoteArmorPiece) other; return username.equals(value.username)
                && slot == value.slot && legacyId == value.legacyId && damage == value.damage; }
    @Override public int hashCode() { return Objects.hash(username, slot, legacyId, damage); }
    @Override public String toString() { return "RemoteArmorPiece[" + username + "=" + slot
            + ":" + (empty() ? "empty" : legacyId + ":" + damage) + "]"; }
}
