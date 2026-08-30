package worldline.modloader.testkit;

import java.util.Collections;
import java.util.List;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.GameEntity;
import worldline.api.GameWorld;
import worldline.api.ItemCensus;

/** Minimal read-only world surface qualified by the legacy provider milestone. */
final class LegacyWorld implements GameWorld {
    private final LegacyRuntime runtime;
    LegacyWorld(LegacyRuntime runtime) { this.runtime = runtime; }
    @Override public long time() { return runtime.snapshot().time; }
    @Override public BlockState block(BlockPosition position) {
        throw new UnsupportedOperationException("legacy block reads are not qualified by M767");
    }
    @Override public boolean setBlock(BlockPosition position, BlockState state) {
        throw new UnsupportedOperationException("legacy block writes are not qualified by M767");
    }
    @Override public List<GameEntity> entities() {
        return Collections.<GameEntity>singletonList(runtime.player());
    }
    @Override public ItemCensus items() { return ItemCensus.empty(); }
    @Override public ItemCensus blocks() { return ItemCensus.empty(); }
}
