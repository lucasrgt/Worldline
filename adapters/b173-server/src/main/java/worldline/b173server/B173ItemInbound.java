package worldline.b173server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteHeldItem;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemCollection;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemotePersonalTransaction;
import worldline.api.RemoteRejectedTransaction;
import worldline.api.RemoteTransactionRejectedException;

/** Modular bounded coordinator for protocol-14 inventory and item-entity traffic. */
final class B173ItemInbound {
    private final B173EntityIdentityTracker identities = new B173EntityIdentityTracker();
    private final B173InventoryTracker inventory = new B173InventoryTracker();
    private final B173PeerEquipmentTracker equipment = new B173PeerEquipmentTracker(identities);
    private final B173DroppedItemTracker dropped = new B173DroppedItemTracker();
    private final B173WindowTracker windows = new B173WindowTracker();
    private final B173PersonalTransactionTracker transactions = new B173PersonalTransactionTracker();
    private final B173ContainerTransactionTracker containerTransactions = new B173ContainerTransactionTracker();
    private final B173FurnaceTracker furnace = new B173FurnaceTracker();
    private final DataOutputStream output;

    B173ItemInbound(int localEntityId, String localUsername, DataOutputStream output) throws IOException {
        this.output = output; identities.bind(localEntityId, localUsername); }

    boolean accept(int packet, DataInputStream input) throws IOException {
        if (packet == 5) equipment.equipment(input);
        else if (packet == 20) equipment.spawn(input);
        else if (packet == 21) dropped.spawn(input);
        else if (packet == 22) dropped.collect(input, identities);
        else if (packet == 29) dropped.destroy(input);
        else if (packet == 100) windows.open(input);
        else if (packet == 101) windows.close(input.readUnsignedByte());
        else if (packet == 103) { B173InventoryUpdate update = B173InventoryCodec.update(input);
            if (transactions.recovering()) transactions.resyncCursor(update, inventory);
            else if (update.cursor() || update.windowId == 0) inventory.apply(update);
            else { int personalSlot = windows.update(update); if (personalSlot >= 9)
                inventory.apply(new B173InventoryUpdate(0, personalSlot, update.item)); } }
        else if (packet == 104) { RemoteInventoryView view = B173InventoryCodec.window(input);
            if (view.windowId() == 0 && transactions.recovering()) transactions.resyncWindow(view);
            else if (view.windowId() == 0) inventory.window(view);
            else { RemoteInventoryView personal = inventory.snapshot();
                int owned = windows.pendingContainerSlots();
                if (personal == null || view.size() != owned + 36) throw new IOException("container tail base absent");
                for (int slot = 9; slot <= 44; slot++) {
                    worldline.api.RemoteInventorySlot own = personal.slot(slot), combined = view.slot(owned + slot - 9);
                    if (own.empty() != combined.empty() || !own.empty() && !own.item().equals(combined.item()))
                        throw new IOException("container tail differs from personal inventory"); }
                windows.contents(view); } }
        else if (packet == 105) furnace.progress(input, windows);
        else if (packet == 106) { int windowId = input.readByte(), action = input.readShort();
            boolean allowed = input.readBoolean();
            if (transactions.pending()) transactions.acknowledge(windowId, action, allowed, output, inventory);
            else if (containerTransactions.pending()) containerTransactions.acknowledge(
                    windowId, action, allowed, inventory, windows);
            else throw new IOException("unexpected transaction acknowledgement"); }
        else if (packet == 200) containerTransactions.statistic(input.readInt(), input.readUnsignedByte());
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
        if (containerTransactions.pending()) throw new IllegalStateException("container transaction pending");
        transactions.begin(action, slot, predicted, before, after, cursorBefore, cursorAfter); }

    RemotePersonalTransaction awaitPersonalTransaction(Pump pump) throws IOException {
        for (int count = 0; count < 8192; count++) { pump.one();
            RemoteRejectedTransaction rejected = transactions.takeRejected();
            if (rejected != null) throw new RemoteTransactionRejectedException(rejected);
            RemotePersonalTransaction result = transactions.take(); if (result != null) return result; }
        throw new IOException("accepted personal transaction absent from bounded inbound window");
    }

    B173PersonalStep awaitPersonalStep(Pump pump) throws IOException {
        for (int count = 0; count < 8192; count++) { pump.one();
            RemoteRejectedTransaction rejected = transactions.takeRejected();
            if (rejected != null) throw new RemoteTransactionRejectedException(rejected);
            B173PersonalStep result = transactions.takeStep(); if (result != null) return result; }
        throw new IOException("accepted personal crafting step absent from bounded inbound window");
    }

    void beginContainerTransaction(B173ContainerStep step) {
        if (transactions.pending()) throw new IllegalStateException("personal transaction pending");
        containerTransactions.begin(step); }
    B173ContainerStep awaitContainerTransaction(Pump pump) throws IOException {
        for (int count = 0; count < 8192; count++) { pump.one();
            B173ContainerStep result = containerTransactions.take(); if (result != null) return result; }
        throw new IOException("accepted container transaction absent from bounded inbound window");
    }

    RemoteContainerWindow awaitChest(Pump pump) throws IOException {
        if (windows.snapshot() != null) return windows.snapshot();
        for (int count = 0; count < 8192; count++) { pump.one();
            if (windows.snapshot() != null) return windows.snapshot(); }
        throw new IOException("chest window absent from bounded inbound window");
    }

    worldline.api.RemoteFurnaceSmelt awaitFurnaceSmelt(Pump pump) throws IOException {
        worldline.api.RemoteFurnaceSmelt ready = furnace.ready(windows); if (ready != null) return ready;
        for (int count = 0; count < 8192; count++) { pump.one(); ready = furnace.ready(windows);
            if (ready != null) return ready; }
        throw new IOException("expected furnace smelt absent from bounded inbound window");
    }

    void beginChest() { windows.begin(worldline.api.RemoteWindowKind.CHEST); }
    void beginFurnace() { windows.begin(worldline.api.RemoteWindowKind.FURNACE); }
    int activeWindowId() { return windows.activeId(); }
    RemoteContainerWindow activeWindow() { return windows.activeWindow(); }
    long activeWindowEpoch() { return windows.activeEpoch(); }
    boolean windowActive() { return windows.active(); }
    void closeWindow(int windowId) throws IOException { windows.close(windowId); }

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
