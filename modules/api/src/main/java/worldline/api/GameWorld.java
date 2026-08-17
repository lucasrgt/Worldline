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
}
