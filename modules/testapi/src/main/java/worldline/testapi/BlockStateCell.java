package worldline.testapi;

import java.util.Objects;
import worldline.api.BlockPosition;
import worldline.api.BlockState;

/** One absolute block cell in a public conformance observation. */
public final class BlockStateCell {
    private final BlockPosition position;
    private final BlockState state;

    public BlockStateCell(BlockPosition position, BlockState state) {
        this.position = Objects.requireNonNull(position, "position");
        this.state = Objects.requireNonNull(state, "state");
    }

    public BlockPosition position() { return position; }
    public BlockState state() { return state; }

    public String canonical() {
        return position.x() + ":" + position.y() + ":" + position.z() + ":"
                + state.legacyId() + ":" + state.metadata();
    }

    @Override public boolean equals(Object other) {
        if (!(other instanceof BlockStateCell)) return false;
        BlockStateCell value = (BlockStateCell) other;
        return position.equals(value.position) && state.equals(value.state);
    }

    @Override public int hashCode() { return Objects.hash(position, state); }
    @Override public String toString() { return canonical(); }
}
