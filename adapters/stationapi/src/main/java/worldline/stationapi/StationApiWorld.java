package worldline.stationapi;

import java.util.Collections;
import java.util.List;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.GameEntity;
import worldline.api.GameWorld;
import worldline.api.ItemCensus;

/** Acknowledged local-world block controls and the existing lifecycle surface. */
final class StationApiWorld implements GameWorld {
    private final StationApiRuntime runtime;
    StationApiWorld(StationApiRuntime runtime) { this.runtime = runtime; }
    @Override public long time() { return runtime.snapshot().time; }
    @Override public BlockState block(BlockPosition position) {
        String[] values = runtime.control("BLOCK " + coordinates(position)).split(",", -1);
        if (values.length != 2) throw new IllegalStateException("invalid StationAPI block reply");
        return new BlockState(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
    }
    @Override public boolean setBlock(BlockPosition position, BlockState state) {
        if (state == null) throw new NullPointerException("state");
        String reply = runtime.control("SET_BLOCK " + coordinates(position) + " " + state.legacyId() + " " + state.metadata());
        if (!reply.equals("true") && !reply.equals("false")) throw new IllegalStateException("invalid block write reply");
        return Boolean.parseBoolean(reply);
    }
    private static String coordinates(BlockPosition position) {
        if (position == null) throw new NullPointerException("position");
        if (position.y() < 0 || position.y() > 127 || Math.abs((long) position.x()) >= 30000000
                || Math.abs((long) position.z()) >= 30000000) throw new IllegalArgumentException("position outside Beta world");
        return position.x() + " " + position.y() + " " + position.z();
    }
    @Override public List<GameEntity> entities() {
        return Collections.<GameEntity>singletonList(runtime.player());
    }
    @Override public ItemCensus items() { return ItemCensus.empty(); }
    @Override public ItemCensus blocks() { return ItemCensus.empty(); }
}
