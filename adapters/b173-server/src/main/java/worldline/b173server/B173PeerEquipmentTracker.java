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
        if (itemId > 0) held.put(username, item(username, itemId, 0));
    }

    void equipment(DataInputStream input) throws IOException {
        int entityId = input.readInt(), slot = input.readShort();
        int itemId = input.readShort(), damage = input.readShort(); String username = names.get(entityId);
        if (username != null && slot == 0 && itemId >= 0) held.put(username, item(username, itemId, damage));
    }

    boolean matches(RemoteHeldItem expected) { return expected.equals(held.get(expected.username())); }

    private static RemoteHeldItem item(String username, int id, int damage) throws IOException {
        try { return new RemoteHeldItem(username, id, damage); }
        catch (IllegalArgumentException error) { throw new IOException("invalid peer held item", error); }
    }
}
