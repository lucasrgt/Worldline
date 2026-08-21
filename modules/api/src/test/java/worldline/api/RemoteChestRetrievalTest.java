package worldline.api;

import java.util.ArrayList;
import java.util.List;

final class RemoteChestRetrievalTest {
    private RemoteChestRetrievalTest() {}
    static void run() {
        RemoteItemStack stone = new RemoteItemStack(1, 1, 0);
        RemoteInventoryView personalBefore = view(0, 45, -1, null);
        RemoteInventoryView personalAfter = view(0, 45, 36, stone);
        RemoteInventoryView before = view(1, 63, 0, stone);
        RemoteInventoryView after = view(1, 63, 54, stone);
        RemoteChestRetrieval value = new RemoteChestRetrieval(0, 36, 1, 2, stone,
                before, after, personalBefore, personalAfter);
        if (value.chestSlot() != 0 || value.personalSlot() != 36 || value.takeAction() != 1
                || value.storeAction() != 2 || value.after() != after || value.personalAfter() != personalAfter)
            throw new AssertionError("chest retrieval accessors drifted");
        failure(() -> new RemoteChestRetrieval(0, 36, 1, 2, stone,
                before, view(2, 63, 54, stone), personalBefore, personalAfter));
        failure(() -> new RemoteChestRetrieval(0, 36, 1, 2, stone,
                before, after, personalBefore, view(0, 45, 37, stone)));
    }
    private static RemoteInventoryView view(int window, int size, int occupied, RemoteItemStack item) {
        List<RemoteInventorySlot> slots = new ArrayList<>();
        for (int index = 0; index < size; index++)
            slots.add(new RemoteInventorySlot(index, index == occupied ? item : null));
        return new RemoteInventoryView(window, slots);
    }
    private static void failure(Runnable action) { try { action.run(); throw new AssertionError("expected failure"); }
        catch (IllegalArgumentException expected) { } }
}
