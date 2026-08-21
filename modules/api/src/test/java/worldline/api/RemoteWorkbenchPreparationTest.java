package worldline.api;

import java.util.ArrayList;
import java.util.List;

/** Fail-closed identity, cursor, recipe, and personal-tail checks for M63 evidence. */
final class RemoteWorkbenchPreparationTest {
    private RemoteWorkbenchPreparationTest() {}
    static void run() {
        RemoteItemStack planks = item(5, 3, 0), one = item(5, 1, 0);
        RemoteInventoryView personalBefore = set(empty(0, 45), 36, planks), personalAfter = empty(0, 45);
        RemoteInventoryView before = set(empty(1, 46), 37, planks);
        RemoteInventoryView oneWide = set(empty(1, 46), 1, one);
        RemoteInventoryView twoWide = set(set(set(empty(1, 46), 0, item(72, 1, 0)), 1, one), 2, one);
        RemoteInventoryView prepared = set(set(set(set(empty(1, 46), 0, item(44, 3, 2)), 1, one), 2, one), 3, one);
        RemoteWorkbenchPreparation value = value(before, oneWide, twoWide, prepared, personalBefore, personalAfter);
        if (value.takeAction() != 1 || value.thirdAction() != 4 || !value.cursorEmptyAfterThird())
            throw new AssertionError("workbench preparation accessors drifted");
        failure(() -> value(before, oneWide, set(twoWide, 38, item(1, 1, 0)), prepared,
                personalBefore, personalAfter));
        failure(() -> value(before, oneWide, window(twoWide, 2), prepared, personalBefore, personalAfter));
    }
    private static RemoteWorkbenchPreparation value(RemoteInventoryView before, RemoteInventoryView oneWide,
            RemoteInventoryView twoWide, RemoteInventoryView prepared, RemoteInventoryView personalBefore,
            RemoteInventoryView personalAfter) { return new RemoteWorkbenchPreparation(36, 1, 2, 3, 4,
                item(5, 3, 0), item(72, 1, 0), item(44, 3, 2), item(5, 3, 0), item(5, 2, 0),
                item(5, 1, 0), true, before, oneWide, twoWide, prepared, personalBefore, personalAfter); }
    private static RemoteInventoryView empty(int id, int size) { List<RemoteInventorySlot> slots = new ArrayList<>();
        for (int slot = 0; slot < size; slot++) slots.add(new RemoteInventorySlot(slot, null));
        return new RemoteInventoryView(id, slots); }
    private static RemoteInventoryView set(RemoteInventoryView source, int slot, RemoteItemStack item) {
        List<RemoteInventorySlot> slots = new ArrayList<>(source.slots());
        slots.set(slot, new RemoteInventorySlot(slot, item)); return new RemoteInventoryView(source.windowId(), slots); }
    private static RemoteInventoryView window(RemoteInventoryView source, int id) {
        List<RemoteInventorySlot> slots = new ArrayList<>(); for (RemoteInventorySlot slot : source.slots())
            slots.add(new RemoteInventorySlot(slot.index(), slot.empty() ? null : slot.item()));
        return new RemoteInventoryView(id, slots); }
    private static RemoteItemStack item(int id, int count, int damage) { return new RemoteItemStack(id, count, damage); }
    private static void failure(Runnable action) { try { action.run(); throw new AssertionError("expected failure"); }
        catch (IllegalArgumentException expected) { } }
}
