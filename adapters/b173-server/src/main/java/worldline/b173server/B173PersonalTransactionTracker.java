package worldline.b173server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemotePersonalTransaction;
import worldline.api.RemoteRejectedTransaction;

/** Stages one personal-window prediction and commits it only on matching Packet106 true. */
final class B173PersonalTransactionTracker {
    private Pending pending;
    private RemotePersonalTransaction accepted;
    private RemoteRejectedTransaction rejected;
    private boolean recovering, fullResync;
    private RemoteInventoryView resyncView;

    void begin(int action, int slot, RemoteItemStack predicted, RemoteInventoryView before,
            RemoteInventoryView after, RemoteItemStack cursorBefore, RemoteItemStack cursorAfter) {
        if (pending != null || accepted != null || rejected != null)
            throw new IllegalStateException("personal transaction is pending");
        pending = new Pending(action, slot, predicted, before, after, cursorBefore, cursorAfter);
    }

    void acknowledge(DataInputStream input, DataOutputStream output,
            B173InventoryTracker inventory) throws IOException {
        int windowId = input.readByte(), action = input.readShort(); boolean allowed = input.readBoolean();
        if (pending == null || recovering || windowId != 0 || action != pending.action)
            throw new IOException("personal transaction acknowledgement drift");
        if (!allowed) { synchronized (output) { output.writeByte(106); output.writeByte(0);
            output.writeShort(action); output.writeBoolean(true); output.flush(); }
            recovering = true; return; }
        inventory.commit(pending.before, pending.after, pending.cursorBefore, pending.cursorAfter);
        accepted = new RemotePersonalTransaction(pending.action, pending.slot, pending.predicted,
                pending.before, pending.after, pending.cursorBefore, pending.cursorAfter); pending = null;
    }

    RemotePersonalTransaction take() {
        RemotePersonalTransaction result = accepted; accepted = null; return result; }

    boolean recovering() { return recovering; }

    void resyncWindow(RemoteInventoryView view) throws IOException {
        if (!recovering) return;
        if (fullResync || view.windowId() != 0 || view.size() != 45)
            throw new IOException("rejected transaction full resync drift");
        fullResync = true; resyncView = view;
    }

    void resyncCursor(B173InventoryUpdate update, B173InventoryTracker inventory) throws IOException {
        if (!recovering) return;
        if (!fullResync || !update.cursor()) throw new IOException("rejected transaction cursor resync drift");
        inventory.recover(resyncView, update.item);
        rejected = new RemoteRejectedTransaction(pending.action, pending.slot, pending.predicted,
                pending.before, resyncView, pending.cursorBefore, update.item);
        pending = null; recovering = false; fullResync = false; resyncView = null;
    }

    RemoteRejectedTransaction takeRejected() {
        RemoteRejectedTransaction result = rejected; rejected = null; return result; }

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
