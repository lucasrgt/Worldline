package worldline.b173server;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemCollection;
import worldline.api.RemoteItemStack;

/** Decodes the latest strict protocol-14 dropped-item spawn. */
final class B173DroppedItemTracker {
    private static final int MAX_ITEMS = 256;
    private final Map<Integer, RemoteDroppedItem> spawned = new LinkedHashMap<>();
    private final Map<Integer, Integer> collectors = new HashMap<>();
    private final Set<Integer> destroyed = new HashSet<>();

    void spawn(DataInputStream input) throws IOException {
        int entityId = input.readInt(), itemId = input.readShort(), count = input.readUnsignedByte();
        int damage = input.readShort(); double x = input.readInt() / 32D;
        double y = input.readInt() / 32D, z = input.readInt() / 32D;
        double velocityX = input.readByte() / 128D, velocityY = input.readByte() / 128D;
        double velocityZ = input.readByte() / 128D;
        if (spawned.containsKey(entityId) && !destroyed.contains(entityId))
            throw new IOException("duplicate live dropped-item entity ID");
        if (destroyed.remove(entityId)) { spawned.remove(entityId); collectors.remove(entityId); }
        if (spawned.size() >= MAX_ITEMS) throw new IOException("dropped-item bound exceeded");
        try { spawned.put(entityId, new RemoteDroppedItem(entityId, new RemoteItemStack(itemId, count, damage),
                x, y, z, velocityX, velocityY, velocityZ)); }
        catch (IllegalArgumentException | NullPointerException error) {
            throw new IOException("invalid dropped-item spawn", error); }
    }

    void collect(DataInputStream input, B173EntityIdentityTracker identities) throws IOException {
        int itemId = input.readInt(), collectorId = input.readInt();
        if (!spawned.containsKey(itemId)) return;
        if (collectors.containsKey(itemId)) throw new IOException("duplicate dropped-item collection");
        if (destroyed.contains(itemId)) throw new IOException("dropped-item collection after destruction");
        if (identities.username(collectorId) == null) throw new IOException("unknown dropped-item collector");
        collectors.put(itemId, collectorId);
    }

    void destroy(int itemId) throws IOException {
        if (!spawned.containsKey(itemId)) return;
        if (!destroyed.add(itemId)) throw new IOException("duplicate dropped-item destruction");
    }

    RemoteDroppedItem matching(RemoteItemStack expected) {
        if (expected == null) throw new IllegalArgumentException("null expected dropped item");
        RemoteDroppedItem result = null; for (RemoteDroppedItem item : spawned.values())
            if (item.item().equals(expected)) result = item; return result;
    }

    boolean despawned(RemoteDroppedItem expected) {
        if (expected == null) throw new IllegalArgumentException("null expected despawned item");
        return expected.equals(spawned.get(expected.entityId()))
                && destroyed.contains(expected.entityId())
                && !collectors.containsKey(expected.entityId());
    }

    boolean collected(RemoteDroppedItem expected) {
        if (expected == null) throw new IllegalArgumentException("null expected collected item");
        return collectors.containsKey(expected.entityId());
    }

    RemoteItemCollection collection(RemoteDroppedItem expected, String username,
            B173EntityIdentityTracker identities) {
        if (expected == null) throw new IllegalArgumentException("null expected collected item");
        if (username == null || !username.matches("[A-Za-z0-9_]{1,16}"))
            throw new IllegalArgumentException("invalid collector username");
        RemoteDroppedItem observed = spawned.get(expected.entityId());
        Integer collectorId = collectors.get(expected.entityId());
        if (!expected.equals(observed) || collectorId == null || !destroyed.contains(expected.entityId())) return null;
        String observedName = identities.username(collectorId);
        return username.equals(observedName) ? new RemoteItemCollection(observed, collectorId, observedName) : null;
    }
}
