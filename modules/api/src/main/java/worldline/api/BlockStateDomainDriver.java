package worldline.api;

/** Orthogonal gameplay driver for proving reachable block metadata states. */
public interface BlockStateDomainDriver extends AutoCloseable {
    RemoteInventoryView inventory();
    void selectHeldSlot(int slot);
    void look(float yaw, float pitch);
    void useHeldItemOnBlock(BlockPosition support, BlockFace face);
    void activateBlock(BlockPosition position, BlockFace face);
    RemoteWorldView awaitBlock(BlockPosition position, BlockState expected);
    RemoteWorldView sustainTicks(int ticks);
    void saveAndReload();
    BlockLifecycleDriver.ReloadBoundary reloadBoundary();

    @Override
    void close();
}
