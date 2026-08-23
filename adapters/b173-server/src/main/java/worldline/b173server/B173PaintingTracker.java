package worldline.b173server;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import worldline.api.RemotePaintingSpawn;

/** Bounded Packet25 queue: entityId int, title UTF-16, x/y/z/direction ints. */
final class B173PaintingTracker {
    private static final int MAX = 64;
    private final ArrayList<RemotePaintingSpawn> pending = new ArrayList<RemotePaintingSpawn>();
    private final ArrayList<Integer> gone = new ArrayList<Integer>();

    void spawn(DataInputStream input) throws IOException {
        int entity = input.readInt();
        String title = B173InboundPacket.string(input, 13);
        int x = input.readInt(), y = input.readInt(), z = input.readInt(), direction = input.readInt();
        if (pending.size() == MAX) pending.remove(0);
        try { pending.add(new RemotePaintingSpawn(entity, title, x, y, z, direction)); }
        catch (IllegalArgumentException error) { throw new IOException("invalid painting spawn", error); }
    }

    RemotePaintingSpawn take() {
        return pending.isEmpty() ? null : pending.remove(0);
    }

    void destroy(int entity) {
        if (entity < 0) return;
        if (gone.size() == MAX) gone.remove(0);
        gone.add(Integer.valueOf(entity));
    }

    Integer takeDestroy(int entity) {
        return gone.contains(Integer.valueOf(entity)) ? Integer.valueOf(entity) : null;
    }
}
