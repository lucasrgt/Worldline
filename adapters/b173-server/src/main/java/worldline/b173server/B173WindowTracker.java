package worldline.b173server;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteWindowDescriptor;
import worldline.api.RemoteWindowKind;

/** Correlates a strict Packet100 chest descriptor with its matching Packet104 view. */
final class B173WindowTracker {
    private RemoteWindowDescriptor pending;
    private RemoteContainerWindow ready;
    private RemoteWindowKind expectedKind;
    private long epoch;

    void begin(RemoteWindowKind kind) {
        if (kind == null || expectedKind != null || pending != null || ready != null)
            throw new IllegalStateException("remote window is already observed or pending");
        expectedKind = kind;
    }

    void open(DataInputStream input) throws IOException {
        int windowId = input.readUnsignedByte(), type = input.readUnsignedByte();
        String title = input.readUTF(); int slots = input.readUnsignedByte();
        RemoteWindowKind kind = type == 0 ? RemoteWindowKind.CHEST
                : type == 1 ? RemoteWindowKind.WORKBENCH
                : type == 2 ? RemoteWindowKind.FURNACE : null;
        if (expectedKind == null || kind != expectedKind || pending != null || ready != null
                || windowId < 1 || windowId > 100)
            throw new IOException("unsupported remote window descriptor");
        try { pending = new RemoteWindowDescriptor(windowId, kind, title, slots); ready = null; }
        catch (IllegalArgumentException error) { throw new IOException("invalid remote window descriptor", error); }
    }

    void contents(RemoteInventoryView inventory) throws IOException {
        if (pending == null || ready != null) return;
        if (inventory.windowId() == 0) return;
        if (inventory.windowId() != pending.windowId()) throw new IOException("remote chest window ID drift");
        try { ready = new RemoteContainerWindow(pending, inventory); pending = null; expectedKind = null; epoch++; }
        catch (IllegalArgumentException error) { throw new IOException("invalid remote chest contents", error); }
    }

    RemoteContainerWindow snapshot() { return ready; }

    int activeId() {
        if (ready == null) throw new IllegalStateException("remote window is not open");
        return ready.descriptor().windowId();
    }

    RemoteContainerWindow activeWindow() {
        if (ready == null) throw new IllegalStateException("remote window is not open"); return ready; }
    long activeEpoch() { if (ready == null) throw new IllegalStateException("remote window is not open"); return epoch; }

    boolean active() { return ready != null || pending != null || expectedKind != null; }
    int pendingPlayerTailOffset() {
        if (pending == null) throw new IllegalStateException("remote window descriptor is absent");
        return pending.playerTailOffset(); }

    void close(int windowId) throws IOException {
        if (ready == null || ready.descriptor().windowId() != windowId)
            throw new IOException("remote window close identity drift");
        ready = null; pending = null; expectedKind = null;
    }

    void commit(RemoteInventoryView before, RemoteInventoryView after) throws IOException {
        if (ready == null || !ready.inventory().equals(before)
                || before.windowId() != after.windowId())
            throw new IOException("remote window transaction base drift");
        try { ready = new RemoteContainerWindow(ready.descriptor(), after); }
        catch (IllegalArgumentException error) { throw new IOException("invalid remote window commit", error); }
    }

    boolean matches(RemoteInventoryView expected) {
        return ready != null && ready.inventory().equals(expected); }
    void adopt(RemoteInventoryView after) { ready = new RemoteContainerWindow(ready.descriptor(), after); }

    int update(B173InventoryUpdate update) throws IOException {
        if (ready == null || update.windowId != ready.descriptor().windowId()
                || update.slot < 0 || update.slot >= ready.inventory().size())
            throw new IOException("active remote window slot drift");
        List<RemoteInventorySlot> slots = new ArrayList<>(ready.inventory().slots());
        slots.set(update.slot, new RemoteInventorySlot(update.slot, update.item));
        ready = new RemoteContainerWindow(ready.descriptor(),
                new RemoteInventoryView(update.windowId, slots));
        int offset = ready.descriptor().playerTailOffset();
        return update.slot < offset ? -1 : update.slot - offset + 9;
    }
}
