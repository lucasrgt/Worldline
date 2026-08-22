package worldline.b173server;

import java.io.IOException;
import worldline.api.RemoteInventoryView;

/** Reusable protocol-14 vehicle interaction and Packet39 observation boundary. */
public final class B173VehicleAccess {
    private B173VehicleAccess() {}

    public static void useVehicle(B173WireClient client, int entity) {
        B173PlayChannel channel = checked(client, entity, "vehicle mount");
        if (!channel.selectedEmpty()) throw new IllegalStateException("vehicle mount requires empty hand");
        interact(channel, client.state().entityId(), entity, "vehicle mount failed");
    }

    public static void useSaddle(B173WireClient client, int entity) {
        B173PlayChannel channel = checked(client, entity, "saddle use");
        B173PlayInbound inbound = channel.inbound();
        int slot = find(inbound.inventory(), 329);
        if (inbound.windowActive() || !inbound.cursorObserved() || inbound.cursor() != null)
            throw new IllegalStateException("saddle use requires synchronized play");
        if (slot < 0) throw new IllegalStateException("saddle 329 absent from hotbar");
        try {
            synchronized (channel.output) {
                channel.output.writeByte(16); channel.output.writeShort(slot); channel.output.flush();
                channel.output.writeByte(7); channel.output.writeInt(client.state().entityId());
                channel.output.writeInt(entity); channel.output.writeByte(0); channel.output.flush();
            }
        } catch (IOException error) { throw new IllegalStateException("saddle Packet7 failed", error); }
    }

    public static void awaitSaddleConsumed(B173WireClient client) {
        until(client, inbound -> find(inbound.inventory(), 329) < 0 ? Boolean.TRUE : null,
                "saddle 329 still present before deadline");
    }

    public static B173VehicleAttach awaitAttach(B173WireClient client, int vehicle) {
        return until(client, inbound -> inbound.objects().takeAttach(vehicle),
                "vehicle attach absent before deadline");
    }

    public static B173VehicleAttach awaitDetach(B173WireClient client, int passenger) {
        return until(client, inbound -> inbound.objects().takeDetach(passenger),
                "vehicle detach absent before deadline");
    }

    private static B173PlayChannel checked(B173WireClient client, int entity, String action) {
        B173PlayChannel channel = client.channel(); int local = client.state().entityId();
        if (local < 0 || entity < 0 || local == entity)
            throw new IllegalArgumentException("invalid " + action + " target");
        return channel;
    }

    private static void interact(B173PlayChannel channel, int local, int entity, String failure) {
        try {
            synchronized (channel.output) {
                channel.output.writeByte(7); channel.output.writeInt(local);
                channel.output.writeInt(entity); channel.output.writeByte(0); channel.output.flush();
            }
        } catch (IOException error) { throw new IllegalStateException(failure, error); }
    }

    private static int find(RemoteInventoryView view, int id) {
        for (int slot = 0; slot <= 8; slot++)
            if (!view.slot(36 + slot).empty() && view.slot(36 + slot).item().legacyId() == id) return slot;
        return -1;
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
