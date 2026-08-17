package worldline.b173server;

import java.io.IOException;
import worldline.api.RemoteHeldItem;
import worldline.api.RemoteInventoryView;

/** Unchecked public-client boundary for the bounded item channel. */
final class B173ItemAccess {
    private B173ItemAccess() {}

    static RemoteInventoryView awaitInventory(B173PlayChannel channel) {
        try { return channel.awaitInventory(); }
        catch (IOException error) { throw new IllegalStateException("inventory receive failed", error); }
    }

    static RemoteInventoryView inventory(B173PlayChannel channel) { return channel.inventory(); }

    static void selectHeldSlot(B173PlayChannel channel, int slot) {
        try { channel.selectHeldSlot(slot); }
        catch (IOException error) { throw new IllegalStateException("held-slot selection failed", error); }
    }

    static void dropHeldItem(B173PlayChannel channel) {
        try { channel.dropHeldItem(); }
        catch (IOException error) { throw new IllegalStateException("held-item drop failed", error); }
    }

    static RemoteHeldItem awaitPeerHeldItem(B173PlayChannel channel, RemoteHeldItem expected) {
        try { return channel.awaitPeerHeldItem(expected); }
        catch (IOException error) { throw new IllegalStateException("peer held-item receive failed", error); }
    }
}
