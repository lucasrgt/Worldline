package worldline.b173server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;

/** Window-0 2x2: gold 41, iron 42, diamond 57, and lapis 22 each uncraft to nine items. */
public final class B173OreBlockUncraftsClick {
    public static final RemoteItemStack GOLD_BLOCK = new RemoteItemStack(41, 1, 0);
    public static final RemoteItemStack IRON_BLOCK = new RemoteItemStack(42, 1, 0);
    public static final RemoteItemStack DIAMOND_BLOCK = new RemoteItemStack(57, 1, 0);
    public static final RemoteItemStack LAPIS_BLOCK = new RemoteItemStack(22, 1, 0);
    public static final RemoteItemStack GOLD_INGOTS = new RemoteItemStack(266, 9, 0);
    public static final RemoteItemStack IRON_INGOTS = new RemoteItemStack(265, 9, 0);
    public static final RemoteItemStack DIAMONDS = new RemoteItemStack(264, 9, 0);
    public static final RemoteItemStack LAPIS = new RemoteItemStack(351, 9, 4);
    private B173OreBlockUncraftsClick() {}

    public static void gold(B173WireClient actor) { uncraft(actor, 1, 36, GOLD_BLOCK, GOLD_INGOTS); }
    public static void iron(B173WireClient actor) { uncraft(actor, 5, 37, IRON_BLOCK, IRON_INGOTS); }
    public static void diamond(B173WireClient actor) { uncraft(actor, 9, 38, DIAMOND_BLOCK, DIAMONDS); }
    public static void lapis(B173WireClient actor) { uncraft(actor, 13, 39, LAPIS_BLOCK, LAPIS); }

    public static boolean stored(RemoteInventoryView view) {
        return view.occupiedSlots() == 4 && same(view, 36, GOLD_INGOTS) && same(view, 37, IRON_INGOTS)
                && same(view, 38, DIAMONDS) && same(view, 39, LAPIS) && emptyCraft(view);
    }

    public static boolean emptyCraft(RemoteInventoryView view) {
        for (int slot = 0; slot < 5; slot++) if (!view.slot(slot).empty()) return false;
        return true;
    }

    private static void uncraft(B173WireClient actor, int action, int hotbar, RemoteItemStack block,
            RemoteItemStack result) {
        try {
            B173PlayChannel channel = actor.channel(); B173PlayInbound inbound = channel.inbound();
            RemoteInventoryView before = inbound.inventory();
            if (inbound.windowActive() || !inbound.cursorObserved() || inbound.cursor() != null
                    || before.windowId() != 0 || before.size() != 45 || !emptyCraft(before)
                    || !same(before, hotbar, block) || result.count() != 9)
                throw new IllegalStateException("ore-block uncraft preflight failed");
            step(channel, inbound, action, hotbar, block, with(before, hotbar, null), block);
            step(channel, inbound, action + 1, 1, null, with(with(inbound.inventory(), 1, block), 0, result), null);
            if (!same(inbound.inventory(), 0, result))
                throw new IllegalStateException("ore-block uncraft result absent from 2x2");
            step(channel, inbound, action + 2, 0, result,
                    with(with(inbound.inventory(), 0, null), 1, null), result);
            step(channel, inbound, action + 3, hotbar, null, with(inbound.inventory(), hotbar, result), null);
            if (!same(inbound.inventory(), hotbar, result) || !emptyCraft(inbound.inventory())
                    || inbound.cursor() != null)
                throw new IllegalStateException("ore-block uncraft store drifted");
        } catch (IOException error) {
            throw new IllegalStateException("ore-block uncraft click failed", error);
        }
    }

    private static void step(B173PlayChannel channel, B173PlayInbound inbound, int action, int slot,
            RemoteItemStack predicted, RemoteInventoryView after, RemoteItemStack cursorAfter) throws IOException {
        inbound.beginPersonalTransaction(action, slot, predicted, inbound.inventory(), after,
                inbound.cursor(), cursorAfter);
        B173ContainerPacket.write(channel.output, 0, slot, 0, action, predicted);
        channel.output.flush(); inbound.awaitPersonalStep();
    }

    private static RemoteInventoryView with(RemoteInventoryView source, int slot, RemoteItemStack item) {
        List<RemoteInventorySlot> slots = new ArrayList<RemoteInventorySlot>(source.slots());
        slots.set(slot, new RemoteInventorySlot(slot, item));
        return new RemoteInventoryView(0, slots);
    }

    private static boolean same(RemoteInventoryView view, int slot, RemoteItemStack item) {
        return !view.slot(slot).empty() && view.slot(slot).item().equals(item);
    }
}
