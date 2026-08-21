package worldline.b173server;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import worldline.api.RemoteHeldItem;
import worldline.api.RemoteArmorPiece;
import worldline.api.RemoteArmorSlot;

/** Correlates named-player spawns with authoritative carried-item equipment updates. */
final class B173PeerEquipmentTracker {
    private final B173EntityIdentityTracker identities;
    private final Map<String, RemoteHeldItem> held = new HashMap<>();
    private final Map<String, RemoteArmorPiece> armor = new HashMap<>();

    B173PeerEquipmentTracker(B173EntityIdentityTracker identities) { this.identities = identities; }

    void spawn(DataInputStream input) throws IOException {
        int entityId = input.readInt(); String username = B173InboundPacket.string(input, 16);
        input.readInt(); input.readInt(); input.readInt(); input.readByte(); input.readByte();
        int itemId = input.readShort(); identities.bind(entityId, username);
        held.put(username, spawnItem(username, itemId));
    }

    void equipment(DataInputStream input) throws IOException {
        int entityId = input.readInt(), slot = input.readShort();
        int itemId = input.readShort(), damage = input.readShort(); String username = identities.username(entityId);
        if (username == null) return;
        if (slot == 0) held.put(username, equipmentItem(username, itemId, damage));
        else if (slot >= 1 && slot <= 4) { RemoteArmorSlot armorSlot = RemoteArmorSlot.fromEquipmentSlot(slot);
            armor.put(key(username, armorSlot), armorItem(username, armorSlot, itemId, damage)); }
        else return;
    }

    boolean matches(RemoteHeldItem expected) { return expected.equals(held.get(expected.username())); }
    boolean matches(RemoteArmorPiece expected) { return expected.equals(armor.get(key(expected.username(), expected.slot()))); }

    private static RemoteHeldItem spawnItem(String username, int id) throws IOException {
        if (id == 0) return RemoteHeldItem.empty(username);
        if (id < 0) throw new IOException("invalid peer spawn held item");
        return item(username, id, 0);
    }

    private static RemoteHeldItem equipmentItem(String username, int id, int damage) throws IOException {
        if (id == -1 && damage == 0) return RemoteHeldItem.empty(username);
        if (id <= 0) throw new IOException("invalid peer equipment held item");
        return item(username, id, damage);
    }

    private static RemoteArmorPiece armorItem(String username, RemoteArmorSlot slot, int id, int damage)
            throws IOException {
        if (id == -1 && damage == 0) return RemoteArmorPiece.empty(username, slot);
        if (id <= 0) throw new IOException("invalid peer armor item");
        try { return new RemoteArmorPiece(username, slot, id, damage); }
        catch (IllegalArgumentException error) { throw new IOException("invalid peer armor item", error); }
    }

    private static RemoteHeldItem item(String username, int id, int damage) throws IOException {
        try { return new RemoteHeldItem(username, id, damage); }
        catch (IllegalArgumentException error) { throw new IOException("invalid peer held item", error); }
    }
    private static String key(String username, RemoteArmorSlot slot) { return username + "#" + slot; }
}
