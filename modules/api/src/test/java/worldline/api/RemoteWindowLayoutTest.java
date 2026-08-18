package worldline.api;

import java.util.ArrayList;
import java.util.List;

/** Exact declared-slot versus combined-tail matrix for typed remote windows. */
final class RemoteWindowLayoutTest {
    private RemoteWindowLayoutTest() {}

    static void run() {
        verify(RemoteWindowKind.CHEST, "Chest", 27, 27, 63);
        verify(RemoteWindowKind.FURNACE, "Furnace", 3, 3, 39);
        verify(RemoteWindowKind.WORKBENCH, "Crafting", 9, 10, 46);
        if (!FurnaceOutputSession.class.isAssignableFrom(WorkbenchSession.class))
            throw new AssertionError("workbench session hierarchy drifted");
        failure(() -> new RemoteWindowDescriptor(1, RemoteWindowKind.WORKBENCH, "Crafting", 10));
        failure(() -> new RemoteWindowDescriptor(1, RemoteWindowKind.WORKBENCH, "Workbench", 9));
        RemoteWindowDescriptor workbench = new RemoteWindowDescriptor(
                1, RemoteWindowKind.WORKBENCH, "Crafting", 9);
        failure(() -> new RemoteContainerWindow(workbench, inventory(1, 45)));
        failure(() -> new RemoteContainerWindow(workbench, inventory(1, 47)));
    }

    private static void verify(RemoteWindowKind kind, String title, int declared, int offset, int total) {
        RemoteWindowDescriptor descriptor = new RemoteWindowDescriptor(1, kind, title, declared);
        RemoteContainerWindow window = new RemoteContainerWindow(descriptor, inventory(1, total));
        if (descriptor.containerSlots() != declared || descriptor.playerTailOffset() != offset
                || descriptor.totalSlots() != total || window.inventory().size() != total)
            throw new AssertionError("remote window layout drifted for " + kind);
    }

    private static RemoteInventoryView inventory(int windowId, int size) {
        List<RemoteInventorySlot> slots = new ArrayList<>();
        for (int slot = 0; slot < size; slot++) slots.add(new RemoteInventorySlot(slot, null));
        return new RemoteInventoryView(windowId, slots);
    }
    private static void failure(Runnable action) {
        try { action.run(); throw new AssertionError("expected remote window layout failure"); }
        catch (IllegalArgumentException expected) { }
    }
}
