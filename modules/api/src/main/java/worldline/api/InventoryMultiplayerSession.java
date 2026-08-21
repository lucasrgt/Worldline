package worldline.api;

/** Recovering session extended with bounded server-authoritative inventory observation. */
public interface InventoryMultiplayerSession extends RecoveringMovementMultiplayerSession {
    RemoteInventoryView awaitInventory();
    RemoteInventoryView inventory();
}
