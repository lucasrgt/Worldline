package worldline.api;

/** Accepted personal-window transactions extended with bounded 2x2 crafting. */
public interface PersonalCraftingSession extends PersonalInventoryTransactionSession {
    RemotePersonalCraft craftPersonal2x2(int ingredientSlot);
}
