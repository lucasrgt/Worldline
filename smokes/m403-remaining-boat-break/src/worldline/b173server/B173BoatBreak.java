package worldline.b173server;

import java.io.IOException;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;

/** Packet7 button-1 attack against a Packet23 type-1 boat until Packet21 wreckage. */
public final class B173BoatBreak {
    public static final RemoteItemStack BOAT = new RemoteItemStack(333, 1, 0);
    public static final RemoteItemStack PLANK = new RemoteItemStack(5, 1, 0);
    public static final RemoteItemStack STICK = new RemoteItemStack(280, 1, 0);

    private B173BoatBreak() {}

    public static void attack(B173WireClient client, int entity) {
        try {
            B173PlayChannel channel = client.channel();
            B173PlayInbound inbound = channel.inbound();
            int local = client.state().entityId();
            if (entity < 0 || entity == local) throw new IllegalArgumentException("invalid boat target");
            if (inbound.windowActive() || !inbound.cursorObserved() || inbound.cursor() != null)
                throw new IllegalStateException("boat attack requires no active window and empty cursor");
            synchronized (channel.output) {
                channel.output.writeByte(7);
                channel.output.writeInt(local);
                channel.output.writeInt(entity);
                channel.output.writeByte(1);
                channel.output.flush();
            }
        } catch (IOException error) {
            throw new IllegalStateException("boat Packet7 button 1 failed", error);
        }
    }

    public static int[] snapshot(B173WireClient client) {
        return new int[] {id(client.peekDroppedItem(BOAT)), id(client.peekDroppedItem(PLANK)),
                id(client.peekDroppedItem(STICK))};
    }

    public static RemoteDroppedItem wreckage(B173WireClient client, int[] before) {
        RemoteDroppedItem boat = client.peekDroppedItem(BOAT);
        if (newer(boat, before[0])) return boat;
        RemoteDroppedItem plank = client.peekDroppedItem(PLANK);
        if (newer(plank, before[1])) return plank;
        RemoteDroppedItem stick = client.peekDroppedItem(STICK);
        return newer(stick, before[2]) ? stick : null;
    }

    public static RemoteDroppedItem attackUntilDrop(B173WireClient actor, B173WireClient observer, int boat) {
        int[] before = snapshot(actor);
        RemoteDroppedItem found = null;
        for (int hit = 0; hit < 16; hit++) {
            attack(actor, boat);
            actor.sustainTicks(1);
            observer.sustainTicks(1);
            found = wreckage(actor, before);
            if (found != null) break;
        }
        if (found == null) {
            actor.sustainTicks(20);
            observer.sustainTicks(20);
            found = wreckage(actor, before);
        }
        if (found == null) throw new IllegalStateException("boat Packet21 wreckage absent after Packet7 attacks");
        RemoteDroppedItem peer = wreckage(observer, before);
        if (peer == null) peer = observer.awaitDroppedItem(found.item());
        if (!found.item().equals(peer.item()) || found.entityId() != peer.entityId())
            throw new IllegalStateException("peer boat wreckage drift");
        return found;
    }

    private static int id(RemoteDroppedItem item) { return item == null ? -1 : item.entityId(); }
    private static boolean newer(RemoteDroppedItem item, int before) {
        return item != null && item.entityId() != before;
    }
}
