package worldline.b173server;

import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;

/** Adapter-private accepted container transition. */
final class B173ContainerStep {
    final int windowId, action, slot; final RemoteItemStack predicted, cursorBefore, cursorAfter;
    final RemoteInventoryView before, after, personalBefore, personalAfter;
    B173ContainerStep(int windowId, int action, int slot, RemoteItemStack predicted,
            RemoteInventoryView before, RemoteInventoryView after,
            RemoteInventoryView personalBefore, RemoteInventoryView personalAfter,
            RemoteItemStack cursorBefore, RemoteItemStack cursorAfter) {
        this.windowId = windowId; this.action = action; this.slot = slot; this.predicted = predicted;
        this.before = before; this.after = after; this.personalBefore = personalBefore;
        this.personalAfter = personalAfter; this.cursorBefore = cursorBefore; this.cursorAfter = cursorAfter;
    }
}
