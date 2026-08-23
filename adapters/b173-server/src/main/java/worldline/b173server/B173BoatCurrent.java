package worldline.b173server;

import java.io.IOException;
import worldline.api.RemoteObjectMovement;
import worldline.api.RemoteObjectSpawn;

/** Packet23 object spawn plus Packet31/33/34 downstream displacement. */
public final class B173BoatCurrent {
    private B173BoatCurrent() {}

    public static RemoteObjectSpawn awaitSpawn(B173WireClient client) {
        return until(client, new Take<RemoteObjectSpawn>() {
            public RemoteObjectSpawn read(B173PlayInbound inbound) {
                RemoteObjectSpawn spawn = inbound.objects().takeAny();
                if (spawn == null) return null;
                if (spawn.type() == 70 || spawn.type() == 71) return null;
                return spawn;
            }
        }, "object spawn absent before deadline");
    }

    public static RemoteObjectMovement awaitDownstream(B173WireClient client, RemoteObjectSpawn spawn,
            int dirX, int dirZ) {
        if (client == null || spawn == null || (dirX == 0 && dirZ == 0)
                || Math.abs(dirX) > 1 || Math.abs(dirZ) > 1)
            throw new IllegalArgumentException("invalid downstream wait");
        final int entity = spawn.entityId(), originX = spawn.fixedX(), originZ = spawn.fixedZ();
        final int axisX = dirX, axisZ = dirZ;
        return until(client, new Take<RemoteObjectMovement>() {
            public RemoteObjectMovement read(B173PlayInbound inbound) {
                RemoteObjectMovement move = inbound.objects().takeMovement(entity);
                if (move == null) return null;
                int dx = move.toFixedX() - originX, dz = move.toFixedZ() - originZ;
                return dx * axisX + dz * axisZ > 0 ? move : null;
            }
        }, "downstream object movement absent before deadline");
    }

    private static <T> T until(B173WireClient client, Take<T> take, String absent) {
        B173PlayInbound inbound = client.channel().inbound(); Thread pulse = inbound.pulse();
        long deadline = System.nanoTime() + inbound.timeoutNanos();
        try {
            for (int count = 0; count < 8192 && System.nanoTime() < deadline; count++) {
                try {
                    T value = take.read(inbound); if (value != null) return value;
                    inbound.pumpOne(); value = take.read(inbound); if (value != null) return value;
                } catch (IOException error) { throw new IllegalStateException(absent, error); }
            }
            throw new IllegalStateException(absent);
        } finally { pulse.interrupt(); }
    }

    private interface Take<T> { T read(B173PlayInbound inbound) throws IOException; }
}
