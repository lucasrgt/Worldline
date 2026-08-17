package worldline.b173server;

import java.io.DataInputStream;
import java.io.IOException;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteHeldItem;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemCollection;
import worldline.api.RemoteItemStack;

/** Modular bounded coordinator for protocol-14 inventory and item-entity traffic. */
final class B173ItemInbound {
    private final B173EntityIdentityTracker identities = new B173EntityIdentityTracker();
    private final B173InventoryTracker inventory = new B173InventoryTracker();
    private final B173PeerEquipmentTracker equipment = new B173PeerEquipmentTracker(identities);
    private final B173DroppedItemTracker dropped = new B173DroppedItemTracker();

    B173ItemInbound(int localEntityId, String localUsername) throws IOException {
        identities.bind(localEntityId, localUsername); }

    boolean accept(int packet, DataInputStream input) throws IOException {
        if (packet == 5) equipment.equipment(input);
        else if (packet == 20) equipment.spawn(input);
        else if (packet == 21) dropped.spawn(input);
        else if (packet == 22) dropped.collect(input, identities);
        else if (packet == 29) dropped.destroy(input);
        else if (packet == 103 || packet == 104) inventory.accept(packet, input);
        else return false;
        return true;
    }

    RemoteInventoryView awaitInventory(Pump pump) throws IOException {
        if (inventory.snapshot() != null) return inventory.snapshot();
        for (int count = 0; count < 8192; count++) { pump.one();
            if (inventory.snapshot() != null) return inventory.snapshot(); }
        throw new IOException("inventory window absent from bounded inbound window");
    }

    RemoteInventoryView inventory() { if (inventory.snapshot() == null)
        throw new IllegalStateException("inventory window is not observed"); return inventory.snapshot(); }

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
