package worldline.api;

/** Personal crafting extended with one bounded transfer into an active single chest. */
public interface ChestTransferSession extends PersonalCraftingSession {
    RemoteChestTransfer storeInOpenChest(int personalSlot, int chestSlot);
}
