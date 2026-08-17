package worldline.b173server;

import java.io.IOException;
import worldline.api.RemoteHeldItem;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteItemCollection;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.RemoteContainerWindow;

/** Unchecked public-client boundary for the bounded item channel. */
final class B173ItemAccess {
    private B173ItemAccess() {}

    static RemoteInventoryView awaitInventory(B173PlayChannel channel) {
        try { return channel.inbound().awaitInventory(); }
        catch (IOException error) { throw new IllegalStateException("inventory receive failed", error); }
    }

    static RemoteInventoryView inventory(B173PlayChannel channel) { return channel.inbound().inventory(); }

    static void selectHeldSlot(B173PlayChannel channel, int slot) {
        try { channel.selectHeldSlot(slot); }
        catch (IOException error) { throw new IllegalStateException("held-slot selection failed", error); }
    }

    static void dropHeldItem(B173PlayChannel channel) {
        try { channel.dropHeldItem(); }
        catch (IOException error) { throw new IllegalStateException("held-item drop failed", error); }
    }

    static void placeHeldBlock(B173PlayChannel channel, BlockPosition support, BlockFace face) {
        try { channel.placeHeldBlock(support, face); }
        catch (IOException error) { throw new IllegalStateException("held-block placement failed", error); }
    }

    static RemoteContainerWindow openChest(B173PlayChannel channel, BlockPosition position, BlockFace face) {
        try { return channel.openChest(position, face); }
        catch (IOException error) { throw new IllegalStateException("chest window receive failed", error); }
    }

    static RemoteHeldItem awaitPeerHeldItem(B173PlayChannel channel, RemoteHeldItem expected) {
        try { return channel.inbound().awaitPeerHeldItem(expected); }
        catch (IOException error) { throw new IllegalStateException("peer held-item receive failed", error); }
    }

    static RemoteDroppedItem awaitDroppedItem(B173PlayChannel channel, RemoteItemStack expected) {
        try { return channel.inbound().awaitDroppedItem(expected); }
        catch (IOException error) { throw new IllegalStateException("dropped-item receive failed", error); }
    }

    static RemoteItemCollection awaitItemCollection(B173PlayChannel channel, RemoteDroppedItem expected,
            String username) { try { return channel.inbound().awaitItemCollection(expected, username); }
        catch (IOException error) { throw new IllegalStateException("item-collection receive failed", error); }
    }
}
