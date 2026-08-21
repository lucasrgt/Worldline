package worldline.b173server;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteInventoryView;

/** Replaces full windows and applies matching authoritative slot deltas. */
final class B173InventoryTracker {
    private RemoteInventoryView view;
    private RemoteItemStack cursor;
    private boolean cursorObserved;

    void window(RemoteInventoryView replacement) {
        if (replacement.windowId() == 0) view = replacement;
    }

    B173InventoryUpdate slot(DataInputStream input) throws IOException {
        B173InventoryUpdate update = B173InventoryCodec.update(input); apply(update); return update;
    }

    void apply(B173InventoryUpdate update) {
        if (update.cursor()) { cursor = update.item; cursorObserved = true; return; }
        if (view == null || update.windowId != view.windowId()
                || update.slot < 0 || update.slot >= view.size()) return;
        List<RemoteInventorySlot> slots = new ArrayList<>(view.slots());
        slots.set(update.slot, new RemoteInventorySlot(update.slot, update.item));
        view = new RemoteInventoryView(view.windowId(), slots);
    }

    RemoteInventoryView snapshot() { return view; }
    boolean cursorObserved() { return cursorObserved; }
    RemoteItemStack cursor() { if (!cursorObserved) throw new IllegalStateException("cursor is not observed");
        return cursor; }

    void commit(RemoteInventoryView before, RemoteInventoryView after,
            RemoteItemStack expectedCursor, RemoteItemStack nextCursor) throws IOException {
        if (!cursorObserved || !before.equals(view) || !Objects.equals(cursor, expectedCursor))
            throw new IOException("personal transaction base state drift");
        view = after; cursor = nextCursor;
    }

    boolean matches(RemoteInventoryView expectedView, RemoteItemStack expectedCursor) {
        return cursorObserved && expectedView.equals(view) && Objects.equals(cursor, expectedCursor); }
    void adopt(RemoteInventoryView nextView, RemoteItemStack nextCursor) {
        view = nextView; cursor = nextCursor; cursorObserved = true; }

    void recover(RemoteInventoryView authoritative, RemoteItemStack authoritativeCursor) {
        view = authoritative; cursor = authoritativeCursor; cursorObserved = true;
    }
}
