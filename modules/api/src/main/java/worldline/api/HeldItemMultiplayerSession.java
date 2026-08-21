package worldline.api;

/** Inventory session extended with held-slot selection and exact peer observation. */
public interface HeldItemMultiplayerSession extends InventoryMultiplayerSession {
    void selectHeldSlot(int hotbarSlot);
    RemoteHeldItem awaitPeerHeldItem(RemoteHeldItem expected);
}
