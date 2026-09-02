package worldline.testapi;

import java.util.Objects;
import worldline.api.BlockPosition;
import worldline.api.BlockState;

/** One exact block cell observed after a state-domain gameplay action. */
public final class BlockStateObservation {
    private final BlockPosition position;
    private final BlockState state;

    public BlockStateObservation(BlockPosition position, BlockState state) {
        this.position = Objects.requireNonNull(position, "position");
        this.state = Objects.requireNonNull(state, "state");
        if (state.legacyId() == 0) throw new IllegalArgumentException(
                "state-domain observation cannot be air");
    }

    public BlockPosition position() { return position; }
    public BlockState state() { return state; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof BlockStateObservation)) return false;
        BlockStateObservation value = (BlockStateObservation) other;
        return position.equals(value.position) && state.equals(value.state);
    }

    @Override public int hashCode() { return Objects.hash(position, state); }
}
