package worldline.api;

import worldline.api.BlockLifecycleDriver.ReloadBoundary;

/** Gameplay driver for bounded random-tick spread with physical and support probes. */
public interface BlockRandomTickSpreadDriver extends AutoCloseable {
    RemoteInventoryView inventory();
    void selectHeldSlot(int slot);
    void placeHeldBlock(BlockPosition support, BlockFace face);
    void beginBreak(BlockPosition position);
    void finishBreak(BlockPosition position);
    RemoteWorldView awaitBlock(BlockPosition position, BlockState expected);
    RemoteWorldView observe();
    RemoteWorldView sustainTicks(int ticks);
    PlayerPose origin();
    MovementOutcome moveAndObserve(double deltaX, double deltaY, double deltaZ, int ticks);
    void saveAndReload();
    ReloadBoundary reloadBoundary();

    @Override void close();
}
