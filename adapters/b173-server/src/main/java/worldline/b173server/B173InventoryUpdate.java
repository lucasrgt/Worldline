package worldline.b173server;

import java.io.IOException;
import worldline.api.RemoteItemStack;

/** Strict decoded Packet103 address and optional stack. */
final class B173InventoryUpdate {
    final int windowId, slot;
    final RemoteItemStack item;

    B173InventoryUpdate(int windowId, int slot, RemoteItemStack item) throws IOException {
        boolean cursor = windowId == -1 && slot == -1;
        if (!cursor && (windowId < 0 || slot < 0)) throw new IOException("invalid inventory slot address");
        this.windowId = windowId; this.slot = slot; this.item = item;
    }

    boolean cursor() { return windowId == -1; }
}
