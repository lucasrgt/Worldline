package worldline.b173server;

import java.io.DataInputStream;
import java.io.IOException;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteHeldItem;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemCollection;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemotePersonalTransaction;

/** Modular bounded coordinator for protocol-14 inventory and item-entity traffic. */
final class B173ItemInbound {
    private final B173EntityIdentityTracker identities = new B173EntityIdentityTracker();
    private final B173InventoryTracker inventory = new B173InventoryTracker();
    private final B173PeerEquipmentTracker equipment = new B173PeerEquipmentTracker(identities);
    private final B173DroppedItemTracker dropped = new B173DroppedItemTracker();
    private final B173WindowTracker windows = new B173WindowTracker();
    private final B173PersonalTransactionTracker transactions = new B173PersonalTransactionTracker();

    B173ItemInbound(int localEntityId, String localUsername) throws IOException {
        identities.bind(localEntityId, localUsername); }

    boolean accept(int packet, DataInputStream input) throws IOException {
        if (packet == 5) equipment.equipment(input);
        else if (packet == 20) equipment.spawn(input);
        else if (packet == 21) dropped.spawn(input);
        else if (packet == 22) dropped.collect(input, identities);
        else if (packet == 29) dropped.destroy(input);
        else if (packet == 100) windows.open(input);
        else if (packet == 103) inventory.slot(input);
        else if (packet == 104) { RemoteInventoryView view = B173InventoryCodec.window(input);
            if (view.windowId() == 0) inventory.window(view); else windows.contents(view); }
        else if (packet == 106) transactions.acknowledge(input, inventory);
        else return false;
        return true;
    }

    RemoteInventoryView awaitInventory(Pump pump) throws IOException {
        if (inventory.snapshot() != null && inventory.cursorObserved()) return inventory.snapshot();
        for (int count = 0; count < 8192; count++) { pump.one();
            if (inventory.snapshot() != null && inventory.cursorObserved()) return inventory.snapshot(); }
        throw new IOException("inventory window absent from bounded inbound window");
    }

    RemoteInventoryView inventory() { if (inventory.snapshot() == null)
        throw new IllegalStateException("inventory window is not observed"); return inventory.snapshot(); }
    boolean cursorObserved() { return inventory.cursorObserved(); }
    RemoteItemStack cursor() { return inventory.cursor(); }

    void beginPersonalTransaction(int action, int slot, RemoteItemStack predicted,
            RemoteInventoryView before, RemoteInventoryView after,
            RemoteItemStack cursorBefore, RemoteItemStack cursorAfter) {
        transactions.begin(action, slot, predicted, before, after, cursorBefore, cursorAfter); }

    RemotePersonalTransaction awaitPersonalTransaction(Pump pump) throws IOException {
        for (int count = 0; count < 8192; count++) { pump.one();
            RemotePersonalTransaction result = transactions.take(); if (result != null) return result; }
        throw new IOException("accepted personal transaction absent from bounded inbound window");
    }

    RemoteContainerWindow awaitChest(Pump pump) throws IOException {
        if (windows.snapshot() != null) return windows.snapshot();
        for (int count = 0; count < 8192; count++) { pump.one();
            if (windows.snapshot() != null) return windows.snapshot(); }
        throw new IOException("chest window absent from bounded inbound window");
    }

    void beginChest() { windows.begin(); }

    RemoteHeldItem awaitPeerHeldItem(RemoteHeldItem expected, Pump pump) throws IOException {
        if (expected == null) throw new IllegalArgumentException("null expected peer held item");
        if (equipment.matches(expected)) return expected;
        for (int count = 0; count < 8192; count++) { pump.one();
            if (equipment.matches(expected)) return expected; }
        throw new IOException("expected peer held item absent from bounded inbound window");
    }

    RemoteDroppedItem awaitDroppedItem(RemoteItemStack expected, Pump pump) throws IOException {
        RemoteDroppedItem ready = dropped.matching(expected); if (ready != null) return ready;
        for (int count = 0; count < 8192; count++) { pump.one(); ready = dropped.matching(expected);
            if (ready != null) return ready; }
        throw new IOException("expected dropped-item spawn absent from bounded inbound window");
    }

    RemoteItemCollection awaitCollection(RemoteDroppedItem expected, String username, Pump pump)
            throws IOException {
        RemoteItemCollection ready = dropped.collection(expected, username, identities);
        if (ready != null) return ready;
        for (int count = 0; count < 8192; count++) { pump.one();
            ready = dropped.collection(expected, username, identities); if (ready != null) return ready; }
        throw new IOException("expected item collection absent from bounded inbound window");
    }

    interface Pump { void one() throws IOException; }
}
