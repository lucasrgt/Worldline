package worldline.b173server;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import worldline.api.RemoteObjectSpawn;

/** Bounded Packet23 spawn queue plus Packet39 attach on the same object tracker. */
final class B173ObjectTracker {
    private static final int MAX = 64;
    private final ArrayList<RemoteObjectSpawn> pending = new ArrayList<>();
    private final ArrayList<B173VehicleAttach> attaches = new ArrayList<>();

    void spawn(DataInputStream input) throws IOException {
        int entity = input.readInt(), type = input.readUnsignedByte();
        int x = input.readInt(), y = input.readInt(), z = input.readInt();
        int thrower = input.readInt(), vx = 0, vy = 0, vz = 0;
        if (thrower > 0) { vx = input.readShort(); vy = input.readShort(); vz = input.readShort(); }
        if (pending.size() == MAX) pending.remove(0);
        try { pending.add(new RemoteObjectSpawn(entity, type, x, y, z, thrower, vx, vy, vz)); }
        catch (IllegalArgumentException error) { throw new IOException("invalid object spawn", error); }
    }

    void attach(DataInputStream input) throws IOException {
        int passenger = input.readInt(), vehicle = input.readInt();
        if (passenger < 0 || vehicle < -1) return;
        if (attaches.size() == MAX) attaches.remove(0);
        try { attaches.add(new B173VehicleAttach(passenger, vehicle)); }
        catch (IllegalArgumentException error) { throw new IOException("invalid vehicle attach", error); }
    }

    RemoteObjectSpawn take(int type) {
        if (type < 1 || type > 127) throw new IllegalArgumentException("invalid expected object type");
        for (int index = 0; index < pending.size(); index++)
            if (pending.get(index).type() == type) return pending.remove(index);
        return null;
    }

    B173VehicleAttach takeAttach(int vehicle) {
        if (vehicle < 0) throw new IllegalArgumentException("invalid expected vehicle entity");
        for (int index = 0; index < attaches.size(); index++)
            if (attaches.get(index).vehicleId() == vehicle) return attaches.remove(index);
        return null;
    }

    B173VehicleAttach takeDetach(int passenger) {
        if (passenger < 0) throw new IllegalArgumentException("invalid expected passenger entity");
        for (int index = 0; index < attaches.size(); index++)
            if (attaches.get(index).passengerId() == passenger && attaches.get(index).vehicleId() == -1)
                return attaches.remove(index);
        return null;
    }
}
