package worldline.b173server;

import java.io.DataInputStream;
import java.io.IOException;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemotePersonalTransaction;

/** Stages one personal-window prediction and commits it only on matching Packet106 true. */
final class B173PersonalTransactionTracker {
    private Pending pending;
    private RemotePersonalTransaction accepted;

    void begin(int action, int slot, RemoteItemStack predicted, RemoteInventoryView before,
            RemoteInventoryView after, RemoteItemStack cursorBefore, RemoteItemStack cursorAfter) {
        if (pending != null || accepted != null) throw new IllegalStateException("personal transaction is pending");
        pending = new Pending(action, slot, predicted, before, after, cursorBefore, cursorAfter);
    }

    void acknowledge(DataInputStream input, B173InventoryTracker inventory) throws IOException {
        int windowId = input.readByte(), action = input.readShort(); boolean allowed = input.readBoolean();
        if (pending == null || windowId != 0 || action != pending.action)
            throw new IOException("personal transaction acknowledgement drift");
        if (!allowed) throw new IOException("personal transaction rejected; recovery is not qualified");
        inventory.commit(pending.before, pending.after, pending.cursorBefore, pending.cursorAfter);
        accepted = new RemotePersonalTransaction(pending.action, pending.slot, pending.predicted,
                pending.before, pending.after, pending.cursorBefore, pending.cursorAfter); pending = null;
    }

    RemotePersonalTransaction take() {
        RemotePersonalTransaction result = accepted; accepted = null; return result; }

    private static final class Pending {
        final int action, slot; final RemoteItemStack predicted, cursorBefore, cursorAfter;
        final RemoteInventoryView before, after;
        Pending(int action, int slot, RemoteItemStack predicted, RemoteInventoryView before,
                RemoteInventoryView after, RemoteItemStack cursorBefore, RemoteItemStack cursorAfter) {
            this.action = action; this.slot = slot; this.predicted = predicted;
            this.before = before; this.after = after;
            this.cursorBefore = cursorBefore; this.cursorAfter = cursorAfter;
        }
    }
}
