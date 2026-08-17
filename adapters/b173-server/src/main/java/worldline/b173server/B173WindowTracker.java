package worldline.b173server;

import java.io.DataInputStream;
import java.io.IOException;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteWindowDescriptor;
import worldline.api.RemoteWindowKind;

/** Correlates a strict Packet100 chest descriptor with its matching Packet104 view. */
final class B173WindowTracker {
    private RemoteWindowDescriptor pending;
    private RemoteContainerWindow ready;
    private boolean expected;
    private long epoch;

    void begin() {
        if (expected || pending != null || ready != null)
            throw new IllegalStateException("chest window is already observed or pending");
        expected = true;
    }

    void open(DataInputStream input) throws IOException {
        int windowId = input.readUnsignedByte(), type = input.readUnsignedByte();
        String title = input.readUTF(); int slots = input.readUnsignedByte();
        if (!expected || pending != null || ready != null || windowId < 1 || windowId > 100 || type != 0
                || !"Chest".equals(title) || slots != 27)
            throw new IOException("unsupported remote chest descriptor");
        pending = new RemoteWindowDescriptor(windowId, RemoteWindowKind.CHEST, title, slots); ready = null;
    }

    void contents(RemoteInventoryView inventory) throws IOException {
        if (pending == null || ready != null) return;
        if (inventory.windowId() == 0) return;
        if (inventory.windowId() != pending.windowId()) throw new IOException("remote chest window ID drift");
        try { ready = new RemoteContainerWindow(pending, inventory); pending = null; expected = false; epoch++; }
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

    boolean active() { return ready != null || pending != null || expected; }

    void close(int windowId) throws IOException {
        if (ready == null || ready.descriptor().windowId() != windowId)
            throw new IOException("remote window close identity drift");
        ready = null; pending = null; expected = false;
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
}
