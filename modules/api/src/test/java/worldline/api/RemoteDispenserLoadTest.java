package worldline.api;

import java.util.ArrayList;
import java.util.List;

/** Exact two-click dispenser load identity and fail-closed unrelated-slot rules. */
final class RemoteDispenserLoadTest {
    private RemoteDispenserLoadTest() {}

    static void run() {
        RemoteItemStack cobble = new RemoteItemStack(4, 1, 0);
        RemoteInventoryView personalBefore = view(0, 45, 39, cobble);
        RemoteInventoryView personalAfter = view(0, 45, -1, null);
        RemoteInventoryView before = view(1, 45, 39, cobble);
        RemoteInventoryView after = view(1, 45, 0, cobble);
        RemoteDispenserLoad value = new RemoteDispenserLoad(39, 0, 1, 2, cobble,
                before, after, personalBefore, personalAfter);
        if (value.personalSlot() != 39 || value.dispenserSlot() != 0 || value.takeAction() != 1
                || value.storeAction() != 2 || !value.stack().equals(cobble) || value.after() != after
                || value.personalAfter() != personalAfter)
            throw new AssertionError("dispenser load accessors drifted");
        RemoteItemStack arrow = new RemoteItemStack(262, 1, 0);
        RemoteDispenserLoad arrows = new RemoteDispenserLoad(39, 0, 1, 2, arrow,
                view(1, 45, 39, arrow), view(1, 45, 0, arrow), view(0, 45, 39, arrow), view(0, 45, -1, null));
        if (arrows.stack().legacyId() != 262) throw new AssertionError("dispenser arrow load drifted");
        failure(() -> new RemoteDispenserLoad(39, 0, 1, 2, cobble,
                before, view(2, 45, 0, cobble), personalBefore, personalAfter));
        failure(() -> new RemoteDispenserLoad(39, 0, 1, 2, cobble,
                before, after, personalBefore, view(0, 45, 38, cobble)));
        failure(() -> new RemoteDispenserLoad(39, 0, 1, 2, new RemoteItemStack(1, 1, 0),
                view(1, 45, 39, new RemoteItemStack(1, 1, 0)), view(1, 45, 0, new RemoteItemStack(1, 1, 0)),
                view(0, 45, 39, new RemoteItemStack(1, 1, 0)), view(0, 45, -1, null)));
        failure(() -> new RemoteWindowDescriptor(1, RemoteWindowKind.DISPENSER, "Dispenser", 9));
        failure(() -> new RemoteWindowDescriptor(1, RemoteWindowKind.DISPENSER, "Trap", 27));
    }

    private static RemoteInventoryView view(int window, int size, int occupied, RemoteItemStack item) {
        List<RemoteInventorySlot> slots = new ArrayList<>();
        for (int index = 0; index < size; index++)
            slots.add(new RemoteInventorySlot(index, index == occupied ? item : null));
        return new RemoteInventoryView(window, slots);
    }
    private static void failure(Runnable action) {
        try { action.run(); throw new AssertionError("expected dispenser load failure"); }
        catch (IllegalArgumentException expected) { }
    }
}
