package worldline.api;

/** Chest-reading session extended with accepted personal-window take, place, and swap clicks. */
public interface PersonalInventoryTransactionSession extends ChestWindowMultiplayerSession {
    RemotePersonalTransaction clickPersonalSlot(int slot);
}
