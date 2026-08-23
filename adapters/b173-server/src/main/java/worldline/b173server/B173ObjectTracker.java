package worldline.b173server;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import worldline.api.RemoteObjectMovement;
import worldline.api.RemoteObjectSpawn;

/** Bounded Packet23 spawn queue plus Packet31/33/34 motion and Packet39 attach. */
final class B173ObjectTracker {
    private static final int MAX = 64;
    private final ArrayList<RemoteObjectSpawn> pending = new ArrayList<RemoteObjectSpawn>();
    private final ArrayList<B173VehicleAttach> attaches = new ArrayList<B173VehicleAttach>();
    private final ArrayList<RemoteObjectMovement> moves = new ArrayList<RemoteObjectMovement>();
    private final HashMap<Integer, State> states = new HashMap<Integer, State>();
    private final HashSet<Integer> armed = new HashSet<Integer>();

    void spawn(DataInputStream input) throws IOException {
        int entity = input.readInt(), type = input.readUnsignedByte();
        int x = input.readInt(), y = input.readInt(), z = input.readInt();
        int thrower = input.readInt(), vx = 0, vy = 0, vz = 0;
        if (thrower > 0) { vx = input.readShort(); vy = input.readShort(); vz = input.readShort(); }
        if (pending.size() == MAX) pending.remove(0);
        try { pending.add(new RemoteObjectSpawn(entity, type, x, y, z, thrower, vx, vy, vz)); }
        catch (IllegalArgumentException error) { throw new IOException("invalid object spawn", error); }
        states.put(Integer.valueOf(entity), new State(x, y, z, 0, 0));
    }

    void attach(DataInputStream input) throws IOException {
        int passenger = input.readInt(), vehicle = input.readInt();
        if (passenger < 0 || vehicle < -1) return;
        if (attaches.size() == MAX) attaches.remove(0);
        try { attaches.add(new B173VehicleAttach(passenger, vehicle)); }
        catch (IllegalArgumentException error) { throw new IOException("invalid vehicle attach", error); }
    }

    void apply(B173EntityMove move) throws IOException {
        State state = states.get(Integer.valueOf(move.entity));
        if (state == null || move.packet == 30) return;
        int nx = move.packet == 34 ? move.x : state.x + move.x;
        int ny = move.packet == 34 ? move.y : state.y + move.y;
        int nz = move.packet == 34 ? move.z : state.z + move.z;
        int yaw = move.yaw < 0 ? state.yaw : move.yaw, pitch = move.pitch < 0 ? state.pitch : move.pitch;
        if (armed.contains(Integer.valueOf(move.entity)) && (nx != state.x || ny != state.y || nz != state.z)
                && (move.packet == 31 || move.packet == 33 || move.packet == 34)) {
            if (moves.size() == MAX) moves.remove(0);
            try { moves.add(new RemoteObjectMovement(move.entity, move.packet, state.x, state.y, state.z,
                    nx, ny, nz, yaw, pitch)); }
            catch (IllegalArgumentException error) { throw new IOException("invalid object movement", error); }
        }
        state.x = nx; state.y = ny; state.z = nz; state.yaw = yaw; state.pitch = pitch;
    }

    RemoteObjectSpawn take(int type) {
        if (type < 1 || type > 127) throw new IllegalArgumentException("invalid expected object type");
        for (int index = 0; index < pending.size(); index++)
            if (pending.get(index).type() == type) return arm(pending.remove(index));
        return null;
    }

    RemoteObjectSpawn takeAny() {
        if (pending.isEmpty()) return null;
        return arm(pending.remove(0));
    }

    RemoteObjectSpawn takeFrom(int type, int thrower) {
        if (type < 1 || type > 127) throw new IllegalArgumentException("invalid expected object type");
        if (thrower < 1) throw new IllegalArgumentException("invalid expected object thrower");
        for (int index = 0; index < pending.size(); index++)
            if (pending.get(index).type() == type && pending.get(index).throwerId() == thrower)
                return arm(pending.remove(index));
        return null;
    }

    RemoteObjectMovement takeMovement(int entity) {
        if (entity < 0) throw new IllegalArgumentException("invalid expected object entity");
        for (int index = 0; index < moves.size(); index++)
            if (moves.get(index).entityId() == entity) return moves.remove(index);
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

    private RemoteObjectSpawn arm(RemoteObjectSpawn spawn) {
        armed.add(Integer.valueOf(spawn.entityId()));
        return spawn;
    }

    private static final class State {
        int x, y, z, yaw, pitch;
        State(int x, int y, int z, int yaw, int pitch) {
            this.x = x; this.y = y; this.z = z; this.yaw = yaw; this.pitch = pitch;
        }
    }
}
