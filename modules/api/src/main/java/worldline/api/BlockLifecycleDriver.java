package worldline.api;

import java.util.List;

/** Orthogonal gameplay driver for block placement, persistence, break, and drop evidence. */
public interface BlockLifecycleDriver extends AutoCloseable {
    enum ReloadBoundary {
        FRESH_LOGIN,
        CHUNK_RELOAD,
        PROCESS_RESTART
    }

    RemoteInventoryView inventory();
    void selectHeldSlot(int slot);
    void placeHeldBlock(BlockPosition support, BlockFace face);
    void beginBreak(BlockPosition position);
    void finishBreak(BlockPosition position);
    RemoteWorldView awaitBlock(BlockPosition position, BlockState expected);
    RemoteWorldView sustainTicks(int ticks);
    List<RemoteDroppedItem> droppedItems();
    void saveAndReload();
    ReloadBoundary reloadBoundary();

    @Override
    void close();
}
