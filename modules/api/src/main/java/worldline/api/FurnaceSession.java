package worldline.api;

/** Chest transfer extended with one bounded furnace load and smelt observation. */
public interface FurnaceSession extends ChestTransferSession {
    RemoteContainerWindow openFurnace(BlockPosition position, BlockFace face);
    RemoteFurnaceLoad loadFurnace(int inputPersonalSlot, int fuelPersonalSlot);
    RemoteFurnaceSmelt awaitFurnaceSmelt();
}
