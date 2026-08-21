package worldline.api;

/** Item-entity session extended with named collection and terminal removal. */
public interface ItemCollectionMultiplayerSession extends DroppedItemMultiplayerSession {
    RemoteItemCollection awaitItemCollection(RemoteDroppedItem expected, String collectorUsername);
}
