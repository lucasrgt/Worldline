package worldline.b173server;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import worldline.api.RemoteHeldItem;

/** Correlates named-player spawns with authoritative carried-item equipment updates. */
final class B173PeerEquipmentTracker {
    private final Map<Integer, String> names = new HashMap<>();
    private final Map<String, RemoteHeldItem> held = new HashMap<>();

    void spawn(DataInputStream input) throws IOException {
        int entityId = input.readInt(); String username = B173InboundPacket.string(input, 16);
        input.readInt(); input.readInt(); input.readInt(); input.readByte(); input.readByte();
        int itemId = input.readShort(); names.put(entityId, username);
        held.put(username, spawnItem(username, itemId));
    }

    void equipment(DataInputStream input) throws IOException {
        int entityId = input.readInt(), slot = input.readShort();
        int itemId = input.readShort(), damage = input.readShort(); String username = names.get(entityId);
        if (username != null && slot == 0) held.put(username, equipmentItem(username, itemId, damage));
    }

    boolean matches(RemoteHeldItem expected) { return expected.equals(held.get(expected.username())); }

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

    private static RemoteHeldItem item(String username, int id, int damage) throws IOException {
        try { return new RemoteHeldItem(username, id, damage); }
        catch (IllegalArgumentException error) { throw new IOException("invalid peer held item", error); }
    }
}
