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
import worldline.api.RemotePersonalTransaction;
import worldline.api.RemotePersonalCraft;
import worldline.api.RemoteRejectedTransaction;
import worldline.api.RemoteTransactionRejectedException;

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

    static worldline.api.RemoteWindowClosure closeWindow(B173PlayChannel channel) {
        try { return channel.closeWindow(); }
        catch (IOException error) { throw new IllegalStateException("remote window close failed", error); }
    }

    static worldline.api.RemoteChestTransfer storeInOpenChest(B173PlayChannel channel,
            int personalSlot, int chestSlot) {
        try { return channel.storeInOpenChest(personalSlot, chestSlot); }
        catch (IOException error) { throw new IllegalStateException("chest transfer failed", error); }
    }

    static worldline.api.RemoteContainerWindow openFurnace(B173PlayChannel channel,
            BlockPosition position, BlockFace face) {
        try { return channel.openFurnace(position, face); }
        catch (IOException error) { throw new IllegalStateException("furnace window receive failed", error); }
    }

    static worldline.api.RemoteContainerWindow openWorkbench(B173PlayChannel channel,
            BlockPosition position, BlockFace face) {
        try { return channel.openWorkbench(position, face); }
        catch (IOException error) { throw new IllegalStateException("workbench window receive failed", error); }
    }

    static worldline.api.RemoteWorkbenchPreparation prepareWorkbenchSlabs(
            B173PlayChannel channel, int personalSlot) {
        try { return channel.prepareWorkbenchSlabs(personalSlot); }
        catch (IOException error) { throw new IllegalStateException("workbench preparation failed", error); }
    }

    static worldline.api.RemoteWorkbenchOutput takeWorkbenchSlabs(B173PlayChannel channel, int personalSlot) {
        try { return channel.takeWorkbenchSlabs(personalSlot); }
        catch (IOException error) { throw new IllegalStateException("workbench output failed", error); }
    }

    static worldline.api.RemoteFurnaceLoad loadFurnace(B173PlayChannel channel, int inputSlot, int fuelSlot) {
        try { return channel.loadFurnace(inputSlot, fuelSlot); }
        catch (IOException error) { throw new IllegalStateException("furnace load failed", error); }
    }

    static worldline.api.RemoteFurnaceSmelt awaitFurnaceSmelt(B173PlayChannel channel) {
        try { return channel.awaitFurnaceSmelt(); }
        catch (IOException error) { throw new IllegalStateException("furnace smelt receive failed", error); }
    }

    static worldline.api.RemoteFurnaceExtraction takeFurnaceOutput(B173PlayChannel channel, int personalSlot) {
        try { return channel.takeFurnaceOutput(personalSlot); }
        catch (IOException error) { throw new IllegalStateException("furnace output extraction failed", error); }
    }

    static RemotePersonalTransaction clickPersonalSlot(B173PlayChannel channel, int slot) {
        try { return channel.clickPersonalSlot(slot); }
        catch (IOException error) { throw new IllegalStateException("personal transaction failed", error); }
    }

    static RemotePersonalCraft craftPersonal2x2(B173PlayChannel channel, int slot) {
        try { return channel.craftPersonal2x2(slot); }
        catch (IOException error) { throw new IllegalStateException("personal crafting failed", error); }
    }

    static RemoteRejectedTransaction rejectedTakeProbe(B173PlayChannel channel, int slot) {
        try { channel.rejectedTakeProbe(slot);
            throw new IllegalStateException("rejected-take probe was unexpectedly accepted"); }
        catch (RemoteTransactionRejectedException expected) { return expected.recovery(); }
        catch (IOException error) { throw new IllegalStateException("rejected-take probe failed", error); }
    }

    static RemoteHeldItem awaitPeerHeldItem(B173PlayChannel channel, RemoteHeldItem expected) {
        try { return channel.inbound().awaitPeerHeldItem(expected); }
        catch (IOException error) { throw new IllegalStateException("peer held-item receive failed", error); }
    }

    static worldline.api.RemoteArmorEquip equipLeatherArmor(B173PlayChannel channel, int personalSlot,
            worldline.api.RemoteArmorSlot slot) { try { return channel.equipLeatherArmor(personalSlot, slot); }
        catch (IOException error) { throw new IllegalStateException("leather armor equip failed", error); } }
    static worldline.api.RemoteArmorPiece awaitPeerArmor(B173PlayChannel channel,
            worldline.api.RemoteArmorPiece expected) { try { return channel.inbound().awaitPeerArmor(expected); }
        catch (IOException error) { throw new IllegalStateException("peer armor receive failed", error); } }
    static worldline.api.RemoteCombatStrike attackPlayer(B173PlayChannel channel, String target) { try {
        return channel.attackPlayer(target); } catch (IOException error) { throw new IllegalStateException("combat attack failed", error); } }
    static worldline.api.RemoteIncomingHit awaitIncomingHit(B173PlayChannel channel, int health) { try {
        return channel.awaitIncomingHit(health); } catch (IOException error) { throw new IllegalStateException("incoming combat receive failed", error); } }

    static RemoteDroppedItem awaitDroppedItem(B173PlayChannel channel, RemoteItemStack expected) {
        try { return channel.inbound().awaitDroppedItem(expected); }
        catch (IOException error) { throw new IllegalStateException("dropped-item receive failed", error); }
    }

    static RemoteItemCollection awaitItemCollection(B173PlayChannel channel, RemoteDroppedItem expected,
            String username) { try { return channel.inbound().awaitItemCollection(expected, username); }
        catch (IOException error) { throw new IllegalStateException("item-collection receive failed", error); }
    }
}
