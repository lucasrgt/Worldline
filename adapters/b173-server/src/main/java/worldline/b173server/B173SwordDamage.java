package worldline.b173server;

import java.io.IOException;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteMobDeath;

/** Packet7 attack with wood, iron, or diamond swords plus a non-blocking death peek. */
public final class B173SwordDamage {
    private B173SwordDamage() {}

    public static void attack(B173WireClient client, int entity, int sword) {
        if (sword != 268 && sword != 267 && sword != 276)
            throw new IllegalArgumentException("unsupported sword " + sword);
        try {
            B173PlayChannel channel = client.channel();
            B173PlayInbound inbound = channel.inbound();
            int local = client.state().entityId();
            if (entity < 0 || entity == local) throw new IllegalArgumentException("invalid sword target");
            if (inbound.windowActive() || !inbound.cursorObserved() || inbound.cursor() != null)
                throw new IllegalStateException("sword attack requires no active window and empty cursor");
            int slot = find(inbound.inventory(), sword);
            if (slot < 0) throw new IllegalStateException("sword " + sword + " absent from hotbar");
            channel.output.writeByte(16); channel.output.writeShort(slot); channel.output.flush();
            channel.output.writeByte(7); channel.output.writeInt(local);
            channel.output.writeInt(entity); channel.output.writeByte(1); channel.output.flush();
        } catch (IOException error) {
            throw new IllegalStateException("sword Packet7 failed", error);
        }
    }

    public static RemoteMobDeath peekDeath(B173WireClient client, int entity) {
        return client.channel().inbound().mobs().peekDeath(entity);
    }

    private static int find(RemoteInventoryView view, int id) {
        for (int slot = 0; slot <= 8; slot++)
            if (!view.slot(36 + slot).empty() && view.slot(36 + slot).item().legacyId() == id)
                return slot;
        return -1;
    }
}
