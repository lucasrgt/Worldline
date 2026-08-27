package worldline.api;

import worldline.api.BlockLifecycleDriver.ReloadBoundary;

/** Orthogonal gameplay driver for exact server-authored light-plane observations. */
public interface BlockLightDriver extends AutoCloseable {
    RemoteInventoryView inventory();
    void selectHeldSlot(int slot);
    void look(float yaw, float pitch);
    void useHeldItemOnBlock(BlockPosition support, BlockFace face);
    RemoteWorldView awaitBlock(BlockPosition position, BlockState expected);
    RemoteWorldView observe();
    void saveAndReload();
    ReloadBoundary reloadBoundary();

    @Override void close();
}
