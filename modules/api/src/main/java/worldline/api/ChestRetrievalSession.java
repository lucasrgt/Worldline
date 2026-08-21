package worldline.api;

/** Bounded combat extended with one exact retrieval from an active single chest. */
public interface ChestRetrievalSession extends CombatHealthSession {
    RemoteChestRetrieval retrieveFromOpenChest(int chestSlot, int personalSlot);
}
