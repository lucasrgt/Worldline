package worldline.b173server;

import java.io.IOException;
import worldline.api.RemoteInventoryView;

/** Public smoke boundary for saddle Packet7 interact and Packet39 pig attach. */
public final class B173VehicleAccess {
    private B173VehicleAccess() {}

    public static void useSaddle(B173WireClient client, int entity) {
        try {
            B173PlayChannel channel = client.channel();
            B173PlayInbound inbound = channel.inbound();
            int local = client.state().entityId(), slot = find(inbound.inventory(), 329);
            if (entity < 0 || entity == local) throw new IllegalArgumentException("invalid saddle target");
            if (inbound.windowActive() || !inbound.cursorObserved() || inbound.cursor() != null)
                throw new IllegalStateException("saddle use requires synchronized play");
            if (slot < 0) throw new IllegalStateException("saddle 329 absent from hotbar");
            synchronized (channel.output) {
                channel.output.writeByte(16); channel.output.writeShort(slot); channel.output.flush();
                channel.output.writeByte(7); channel.output.writeInt(local);
                channel.output.writeInt(entity); channel.output.writeByte(0); channel.output.flush();
            }
        } catch (IOException error) { throw new IllegalStateException("saddle Packet7 failed", error); }
    }

    public static void useVehicle(B173WireClient client, int entity) {
        try {
            B173PlayChannel channel = client.channel();
            int user = client.state().entityId();
            if (user < 0 || entity < 0 || user == entity)
                throw new IllegalArgumentException("invalid use-entity");
            if (!channel.selectedEmpty())
                throw new IllegalStateException("pig mount requires empty hand");
            synchronized (channel.output) {
                channel.output.writeByte(7);
                channel.output.writeInt(user);
                channel.output.writeInt(entity);
                channel.output.writeByte(0);
                channel.output.flush();
            }
        } catch (IOException error) { throw new IllegalStateException("pig mount failed", error); }
    }

    public static void awaitSaddleConsumed(B173WireClient client) {
        until(client, inbound -> find(inbound.inventory(), 329) < 0 ? Boolean.TRUE : null,
                "saddle 329 still present before deadline");
    }

    public static B173VehicleAttach awaitAttach(B173WireClient client, int vehicle) {
        return until(client, inbound -> inbound.objects().takeAttach(vehicle),
                "pig attach absent before deadline");
    }

    private static int find(RemoteInventoryView view, int id) {
        for (int slot = 0; slot <= 8; slot++)
            if (!view.slot(36 + slot).empty() && view.slot(36 + slot).item().legacyId() == id)
                return slot;
        return -1;
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
