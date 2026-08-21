package worldline.api;

/** Drop-capable session extended with authoritative item-entity spawn observation. */
public interface DroppedItemMultiplayerSession extends DropItemMultiplayerSession {
    RemoteDroppedItem awaitDroppedItem(RemoteItemStack expected);
}
