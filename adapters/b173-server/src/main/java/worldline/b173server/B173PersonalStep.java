package worldline.b173server;

import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;

/** Adapter-private predicted transition committed by an accepted Packet106. */
final class B173PersonalStep {
    final int action, slot; final RemoteItemStack predicted, cursorBefore, cursorAfter;
    final RemoteInventoryView before, after;
    B173PersonalStep(int action, int slot, RemoteItemStack predicted, RemoteInventoryView before,
            RemoteInventoryView after, RemoteItemStack cursorBefore, RemoteItemStack cursorAfter) {
        this.action = action; this.slot = slot; this.predicted = predicted;
        this.before = before; this.after = after;
        this.cursorBefore = cursorBefore; this.cursorAfter = cursorAfter;
    }
}
