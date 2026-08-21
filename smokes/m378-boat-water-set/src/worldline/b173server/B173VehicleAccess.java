package worldline.b173server;

import java.io.IOException;

/** Public smoke boundary for empty-hand Packet7 boat mount/unmount and Packet39 attach/detach. */
public final class B173VehicleAccess {
    private B173VehicleAccess() {}

    public static void useVehicle(B173WireClient client, int entity) {
        try {
            B173PlayChannel channel = client.channel();
            int user = client.state().entityId();
            if (user < 0 || entity < 0 || user == entity)
                throw new IllegalArgumentException("invalid use-entity");
            if (!channel.selectedEmpty())
                throw new IllegalStateException("vehicle mount requires empty hand");
            synchronized (channel.output) {
                channel.output.writeByte(7);
                channel.output.writeInt(user);
                channel.output.writeInt(entity);
                channel.output.writeByte(0);
                channel.output.flush();
            }
        } catch (IOException error) {
            throw new IllegalStateException("vehicle mount failed", error);
        }
    }

    public static B173VehicleAttach awaitAttach(B173WireClient client, int vehicle) {
        return until(client, inbound -> inbound.objects().takeAttach(vehicle),
                "vehicle attach absent before deadline");
    }

    public static B173VehicleAttach awaitDetach(B173WireClient client, int passenger) {
        return until(client, inbound -> inbound.objects().takeDetach(passenger),
                "vehicle detach absent before deadline");
    }

    private static <T> T until(B173WireClient client, Take<T> take, String absent) {
        B173PlayInbound inbound = client.channel().inbound();
        Thread pulse = inbound.pulse();
        long deadline = System.nanoTime() + inbound.timeoutNanos();
        try {
            for (int count = 0; count < 8192 && System.nanoTime() < deadline; count++) {
                try {
                    T value = take.read(inbound);
                    if (value != null) return value;
                    inbound.pumpOne();
                    value = take.read(inbound);
                    if (value != null) return value;
                } catch (IOException error) { throw new IllegalStateException(absent, error); }
            }
            throw new IllegalStateException(absent);
        } finally { pulse.interrupt(); }
    }

    private interface Take<T> { T read(B173PlayInbound inbound) throws IOException; }
}
