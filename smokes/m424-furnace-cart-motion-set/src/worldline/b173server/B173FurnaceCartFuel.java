package worldline.b173server;

import java.io.IOException;
import worldline.api.RemoteInventoryView;

/** Packet7 coal-263 fuel use against a furnace minecart. Vanilla opens no Packet100. */
public final class B173FurnaceCartFuel {
    private B173FurnaceCartFuel() {}

    public static void use(B173WireClient client, int entity) {
        try {
            B173PlayChannel channel = client.channel();
            B173PlayInbound inbound = channel.inbound();
            int local = client.state().entityId(), slot = find(inbound.inventory(), 263);
            if (entity < 1 || entity == local) throw new IllegalArgumentException("invalid furnace-cart entity");
            if (inbound.windowActive() || !inbound.cursorObserved() || inbound.cursor() != null)
                throw new IllegalStateException("furnace-cart fuel requires synchronized play");
            if (slot < 0) throw new IllegalStateException("coal 263 absent from hotbar");
            synchronized (channel.output) {
                channel.output.writeByte(16); channel.output.writeShort(slot); channel.output.flush();
                channel.output.writeByte(7); channel.output.writeInt(local);
                channel.output.writeInt(entity); channel.output.writeByte(0); channel.output.flush();
            }
        } catch (IOException error) { throw new IllegalStateException("furnace-cart coal Packet7 failed", error); }
    }

    public static void awaitConsumed(B173WireClient client) {
        B173PlayInbound inbound = client.channel().inbound();
        Thread pulse = inbound.pulse();
        long deadline = System.nanoTime() + inbound.timeoutNanos();
        try {
            for (int count = 0; count < 8192 && System.nanoTime() < deadline; count++) {
                if (find(inbound.inventory(), 263) < 0) return;
                inbound.pumpOne();
                if (find(inbound.inventory(), 263) < 0) return;
            }
            throw new IllegalStateException("coal 263 still present before deadline");
        } catch (IOException error) {
            throw new IllegalStateException("coal 263 still present before deadline", error);
        } finally { pulse.interrupt(); }
    }

    private static int find(RemoteInventoryView view, int id) {
        for (int slot = 0; slot <= 8; slot++)
            if (!view.slot(36 + slot).empty() && view.slot(36 + slot).item().legacyId() == id)
                return slot;
        return -1;
    }
}
