package worldline.api;

import worldline.api.BlockLifecycleDriver.ReloadBoundary;

/** Orthogonal gameplay driver for server-authoritative block collision probes. */
public interface BlockCollisionDriver extends AutoCloseable {
    RemoteInventoryView inventory();
    void selectHeldSlot(int slot);
    void look(float yaw, float pitch);
    void useHeldItemOnBlock(BlockPosition support, BlockFace face);
    RemoteWorldView awaitBlock(BlockPosition position, BlockState expected);
    RemoteWorldView sustainTicks(int ticks);
    PlayerPose origin();
    MovementOutcome moveAndObserve(double deltaX, double deltaY, double deltaZ, int ticks);
    void saveAndReload();
    ReloadBoundary reloadBoundary();

    @Override
    void close();
}
