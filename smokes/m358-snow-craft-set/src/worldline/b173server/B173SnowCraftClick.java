package worldline.b173server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;

/** Window-0 2x2: four snowballs 332 become one snow block 80. */
public final class B173SnowCraftClick {
    public static final RemoteItemStack SNOWBALLS = new RemoteItemStack(332, 4, 0);
    public static final RemoteItemStack SNOWBALLS3 = new RemoteItemStack(332, 3, 0);
    public static final RemoteItemStack SNOWBALLS2 = new RemoteItemStack(332, 2, 0);
    public static final RemoteItemStack SNOWBALL = new RemoteItemStack(332, 1, 0);
    public static final RemoteItemStack SNOW_BLOCK = new RemoteItemStack(80, 1, 0);
    private B173SnowCraftClick() {}

    public static void apply(B173WireClient actor) {
        try {
            B173PlayChannel channel = actor.channel(); B173PlayInbound inbound = channel.inbound();
            RemoteInventoryView before = inbound.inventory();
            if (inbound.windowActive() || !inbound.cursorObserved() || inbound.cursor() != null
                    || before.windowId() != 0 || before.size() != 45 || !emptyCraft(before)
                    || !same(before, 37, SNOWBALLS) || SNOW_BLOCK.legacyId() != 80
                    || SNOWBALLS.legacyId() != 332)
                throw new IllegalStateException("snow-block 2x2 preflight failed");
            step(channel, inbound, 1, 37, 0, SNOWBALLS, replace(before, 37, null), SNOWBALLS);
            step(channel, inbound, 2, 1, 1, null, replace(inbound.inventory(), 1, SNOWBALL), SNOWBALLS3);
            step(channel, inbound, 3, 2, 1, null, replace(inbound.inventory(), 2, SNOWBALL), SNOWBALLS2);
            step(channel, inbound, 4, 3, 1, null, replace(inbound.inventory(), 3, SNOWBALL), SNOWBALL);
            step(channel, inbound, 5, 4, 1, null,
                    replace(replace(inbound.inventory(), 4, SNOWBALL), 0, SNOW_BLOCK), null);
            if (!same(inbound.inventory(), 0, SNOW_BLOCK))
                throw new IllegalStateException("snow-block result 80 absent from 2x2 snowballs");
            step(channel, inbound, 6, 0, 0, SNOW_BLOCK, clearCraft(inbound.inventory()), SNOW_BLOCK);
            step(channel, inbound, 7, 37, 0, null, replace(inbound.inventory(), 37, SNOW_BLOCK), null);
            if (!same(inbound.inventory(), 37, SNOW_BLOCK) || !emptyCraft(inbound.inventory())
                    || inbound.cursor() != null)
                throw new IllegalStateException("snow-block 80 2x2 store drifted");
        } catch (IOException error) {
            throw new IllegalStateException("snow-block 2x2 click failed", error);
        }
    }

    private static void step(B173PlayChannel channel, B173PlayInbound inbound, int action, int slot, int button,
            RemoteItemStack predicted, RemoteInventoryView after, RemoteItemStack cursorAfter) throws IOException {
        inbound.beginPersonalTransaction(action, slot, predicted, inbound.inventory(), after,
                inbound.cursor(), cursorAfter);
        B173ContainerPacket.write(channel.output, 0, slot, button, action, predicted);
        channel.output.flush(); inbound.awaitPersonalStep();
    }

    private static RemoteInventoryView clearCraft(RemoteInventoryView view) {
        RemoteInventoryView next = view;
        for (int slot = 0; slot < 5; slot++) next = replace(next, slot, null);
        return next;
    }

    private static RemoteInventoryView replace(RemoteInventoryView source, int slot, RemoteItemStack item) {
        List<RemoteInventorySlot> slots = new ArrayList<RemoteInventorySlot>(source.slots());
        slots.set(slot, new RemoteInventorySlot(slot, item));
        return new RemoteInventoryView(0, slots);
    }

    private static boolean same(RemoteInventoryView view, int slot, RemoteItemStack item) {
        return !view.slot(slot).empty() && view.slot(slot).item().equals(item);
    }

    public static boolean emptyCraft(RemoteInventoryView view) {
        for (int slot = 0; slot < 5; slot++) if (!view.slot(slot).empty()) return false;
        return true;
    }
}
