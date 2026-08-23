package worldline.stationapi;

import java.util.Collections;
import java.util.List;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.GameEntity;
import worldline.api.GameWorld;
import worldline.api.ItemCensus;

/** Minimal read-only world surface deliberately bounded to the M620 boot contract. */
final class StationApiWorld implements GameWorld {
    private final StationApiRuntime runtime;
    StationApiWorld(StationApiRuntime runtime) { this.runtime = runtime; }
    @Override public long time() { return runtime.snapshot().time; }
    @Override public BlockState block(BlockPosition position) {
        throw new UnsupportedOperationException("StationAPI block reads are not qualified by M620");
    }
    @Override public boolean setBlock(BlockPosition position, BlockState state) {
        throw new UnsupportedOperationException("StationAPI block writes are not qualified by M620");
    }
    @Override public List<GameEntity> entities() {
        return Collections.<GameEntity>singletonList(runtime.player());
    }
    @Override public ItemCensus items() { return ItemCensus.empty(); }
    @Override public ItemCensus blocks() { return ItemCensus.empty(); }
}
