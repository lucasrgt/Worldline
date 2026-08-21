package worldline.api;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/** Stable neutral automation surface for a loaded world. */
public interface GameWorld {
    long time();

    BlockState block(BlockPosition position);

    boolean setBlock(BlockPosition position, BlockState state);

    List<GameEntity> entities();

    /** Read-only dropped-item and container totals. Not inventory manipulation. */
    ItemCensus items();

    /** Read-only loaded non-air block ID totals. Not world mutation. */
    ItemCensus blocks();

    /** Packed {@code (chunkX << 32) | (chunkZ & 0xffffffffL)} keys for loaded chunks. */
    default Set<Long> loadedChunks() {
        return Collections.emptySet();
    }

    /** Item totals whose entities or tile entities sit in {@code chunks}. */
    default ItemCensus itemsInChunks(Set<Long> chunks) {
        if (chunks == null) throw new NullPointerException("chunks");
        return ItemCensus.empty();
    }

    default ItemCensus blocksInChunks(Set<Long> chunks) {
        if (chunks == null) throw new NullPointerException("chunks");
        return ItemCensus.empty();
    }

    default WearCensus wear() {
        return WearCensus.empty();
    }

    /** True when hostile mobs do not spawn. Defaults to the controlled-client difficulty. */
    default boolean peaceful() {
        return true;
    }

    /**
     * Spawns one entity of a registered semantic type at {@code position} and
     * returns its live handle. Unsupported types fail closed.
     */
    default GameEntity spawn(String type, GamePosition position) {
        throw new UnsupportedOperationException("spawn is not supported by this runtime");
    }

    /** Removes a live entity from the world; returns false when it is already gone. */
    default boolean remove(GameEntity entity) {
        throw new UnsupportedOperationException("remove is not supported by this runtime");
    }

    /** Read-only item totals inside the container tile entity at {@code position}. */
    default ItemCensus itemsAt(BlockPosition position) {
        throw new UnsupportedOperationException("container reads are not supported by this runtime");
    }
}
