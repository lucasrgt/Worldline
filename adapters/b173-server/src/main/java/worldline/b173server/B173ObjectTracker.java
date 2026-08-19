package worldline.b173server;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import worldline.api.RemoteObjectSpawn;

/** Bounded Packet23 queue with the official 21-byte payload plus optional velocity. */
final class B173ObjectTracker {
    private static final int MAX = 64;
    private final ArrayList<RemoteObjectSpawn> pending = new ArrayList<>();

    void spawn(DataInputStream input) throws IOException {
        int entity = input.readInt(), type = input.readUnsignedByte();
        int x = input.readInt(), y = input.readInt(), z = input.readInt();
        int thrower = input.readInt(), vx = 0, vy = 0, vz = 0;
        if (thrower > 0) { vx = input.readShort(); vy = input.readShort(); vz = input.readShort(); }
        if (pending.size() == MAX) pending.remove(0);
        try { pending.add(new RemoteObjectSpawn(entity, type, x, y, z, thrower, vx, vy, vz)); }
        catch (IllegalArgumentException error) { throw new IOException("invalid object spawn", error); }
    }

    RemoteObjectSpawn take(int type) {
        if (type < 1 || type > 127) throw new IllegalArgumentException("invalid expected object type");
        for (int index = 0; index < pending.size(); index++)
            if (pending.get(index).type() == type) return pending.remove(index);
        return null;
    }
}
