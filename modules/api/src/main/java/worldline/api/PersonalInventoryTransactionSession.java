package worldline.api;

/** Chest-reading session extended with accepted personal-window left clicks. */
public interface PersonalInventoryTransactionSession extends ChestWindowMultiplayerSession {
    RemotePersonalTransaction clickPersonalSlot(int slot);
}
