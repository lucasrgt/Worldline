package worldline.testapi;

import java.util.Objects;
import worldline.api.BlockPosition;
import worldline.api.BlockState;

/** One expected or observed state transition in a coupled block break. */
public final class BlockCellTransition {
    private final BlockPosition position;
    private final BlockState before, after;

    public BlockCellTransition(BlockPosition position, BlockState before, BlockState after) {
        this.position = Objects.requireNonNull(position, "position");
        this.before = Objects.requireNonNull(before, "before");
        this.after = Objects.requireNonNull(after, "after");
        if (before.equals(after)) throw new IllegalArgumentException("transition did not change");
    }

    public BlockPosition position() { return position; }
    public BlockState before() { return before; }
    public BlockState after() { return after; }

    public String canonical() {
        return position.x() + ":" + position.y() + ":" + position.z() + ":"
                + before.legacyId() + ":" + before.metadata() + "->"
                + after.legacyId() + ":" + after.metadata();
    }

    @Override public boolean equals(Object other) {
        if (!(other instanceof BlockCellTransition)) return false;
        BlockCellTransition value = (BlockCellTransition) other;
        return position.equals(value.position) && before.equals(value.before)
                && after.equals(value.after);
    }

    @Override public int hashCode() { return Objects.hash(position, before, after); }
    @Override public String toString() { return canonical(); }
}
