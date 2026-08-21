package worldline.api;

import java.util.ArrayList;
import java.util.List;

/** Exact result/consumption/tail invariants for M64 evidence. */
final class RemoteWorkbenchOutputTest {
    private RemoteWorkbenchOutputTest() {}
    static void run() {
        RemoteItemStack slabs = item(44, 3, 2), plank = item(5, 1, 0);
        RemoteInventoryView before = set(set(set(set(empty(1, 46), 0, slabs), 1, plank), 2, plank), 3, plank);
        RemoteInventoryView consumed = empty(1, 46), after = set(consumed, 37, slabs);
        RemoteInventoryView personalBefore = empty(0, 45), personalAfter = set(personalBefore, 36, slabs);
        RemoteWorkbenchOutput value = new RemoteWorkbenchOutput(36, 5, 6, 3, slabs,
                before, consumed, after, personalBefore, personalAfter);
        if (value.takeAction() != 5 || value.storeAction() != 6 || value.craftedCount() != 3
                || !value.consumed().equals(consumed)) throw new AssertionError("workbench output accessors drifted");
        failure(() -> new RemoteWorkbenchOutput(36, 5, 6, 3, item(44, 3, 0),
                before, consumed, after, personalBefore, personalAfter));
        failure(() -> new RemoteWorkbenchOutput(36, 5, 6, 3, slabs,
                before, set(consumed, 38, item(1, 1, 0)), after, personalBefore, personalAfter));
    }
    private static RemoteInventoryView empty(int id, int size) { List<RemoteInventorySlot> slots = new ArrayList<>();
        for (int slot = 0; slot < size; slot++) slots.add(new RemoteInventorySlot(slot, null));
        return new RemoteInventoryView(id, slots); }
    private static RemoteInventoryView set(RemoteInventoryView source, int slot, RemoteItemStack item) {
        List<RemoteInventorySlot> slots = new ArrayList<>(source.slots()); slots.set(slot,
                new RemoteInventorySlot(slot, item)); return new RemoteInventoryView(source.windowId(), slots); }
    private static RemoteItemStack item(int id, int count, int damage) { return new RemoteItemStack(id, count, damage); }
    private static void failure(Runnable action) { try { action.run(); throw new AssertionError("expected failure"); }
        catch (IllegalArgumentException expected) { } }
}
