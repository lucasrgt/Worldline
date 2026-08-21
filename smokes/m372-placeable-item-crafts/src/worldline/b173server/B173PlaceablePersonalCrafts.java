package worldline.b173server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;

/** Window-0 2x2: two vertical plank pairs become eight sticks 280 for painting. */
public final class B173PlaceablePersonalCrafts {
    public static final RemoteItemStack PLANKS13 = new RemoteItemStack(5, 13, 0);
    public static final RemoteItemStack PLANKS9 = new RemoteItemStack(5, 9, 0);
    public static final RemoteItemStack WOOL = new RemoteItemStack(35, 1, 0);
    public static final RemoteItemStack STICK = new RemoteItemStack(280, 1, 0);
    public static final RemoteItemStack STICKS8 = new RemoteItemStack(280, 8, 0);
    private static final RemoteItemStack STICKS4 = new RemoteItemStack(280, 4, 0);
    private B173PlaceablePersonalCrafts() {}

    public static void apply(B173WireClient actor) {
        try {
            B173PlayChannel channel = actor.channel(); B173PlayInbound inbound = channel.inbound();
            RemoteInventoryView before = inbound.inventory();
            if (inbound.windowActive() || !inbound.cursorObserved() || inbound.cursor() != null
                    || before.windowId() != 0 || before.size() != 45 || !emptyCraft(before)
                    || !same(before, 38, PLANKS13) || !same(before, 39, WOOL) || !same(before, 40, STICK)
                    || !before.slot(37).empty() || STICKS8.legacyId() != 280)
                throw new IllegalStateException("placeable 2x2 preflight failed");
            pair(channel, inbound, pair(channel, inbound, 1, 13, 0), 11, 4);
            if (!prepared(inbound.inventory()) || inbound.cursor() != null)
                throw new IllegalStateException("placeable 2x2 sticks drifted");
        } catch (IOException error) {
            throw new IllegalStateException("placeable 2x2 stick crafts failed", error);
        }
    }

    public static boolean prepared(RemoteInventoryView view) {
        return same(view, 37, STICKS8) && same(view, 38, PLANKS9) && same(view, 39, WOOL)
                && same(view, 40, STICK) && emptyCraft(view);
    }

    private static int pair(B173PlayChannel channel, B173PlayInbound inbound, int action, int planks,
            int sticks) throws IOException {
        RemoteItemStack all = item(5, planks), rest = item(5, planks - 2);
        action = step(channel, inbound, 38, 0, all, replace(inbound.inventory(), 38, null), all, action);
        action = step(channel, inbound, 1, 1, null, replace(inbound.inventory(), 1, item(5, 1)),
                item(5, planks - 1), action);
        action = step(channel, inbound, 3, 1, null,
                replace(replace(inbound.inventory(), 3, item(5, 1)), 0, STICKS4), rest, action);
        if (!same(inbound.inventory(), 0, STICKS4))
            throw new IllegalStateException("sticks 280 absent from vertical 2x2");
        action = step(channel, inbound, 38, 0, null, replace(inbound.inventory(), 38, rest), null, action);
        action = step(channel, inbound, 0, 0, STICKS4,
                replace(replace(replace(inbound.inventory(), 0, null), 1, null), 3, null), STICKS4, action);
        if (sticks == 0)
            return step(channel, inbound, 37, 0, null, replace(inbound.inventory(), 37, STICKS4), null, action);
        return step(channel, inbound, 37, 0, STICKS4, replace(inbound.inventory(), 37, STICKS8), null, action);
    }

    private static int step(B173PlayChannel channel, B173PlayInbound inbound, int slot, int button,
            RemoteItemStack predicted, RemoteInventoryView after, RemoteItemStack cursorAfter, int action)
            throws IOException {
        inbound.beginPersonalTransaction(action, slot, predicted, inbound.inventory(), after,
                inbound.cursor(), cursorAfter);
        B173ContainerPacket.write(channel.output, 0, slot, button, action, predicted);
        channel.output.flush(); inbound.awaitPersonalStep(); return action + 1;
    }

    private static RemoteInventoryView replace(RemoteInventoryView source, int slot, RemoteItemStack item) {
        List<RemoteInventorySlot> slots = new ArrayList<RemoteInventorySlot>(source.slots());
        slots.set(slot, new RemoteInventorySlot(slot, item));
        return new RemoteInventoryView(0, slots);
    }

    private static RemoteItemStack item(int id, int count) { return new RemoteItemStack(id, count, 0); }
    private static boolean same(RemoteInventoryView view, int slot, RemoteItemStack item) {
        return !view.slot(slot).empty() && view.slot(slot).item().equals(item); }
    static boolean emptyCraft(RemoteInventoryView view) {
        for (int slot = 0; slot < 5; slot++) if (!view.slot(slot).empty()) return false; return true; }
}
