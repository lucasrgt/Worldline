package worldline.testkit;

import java.util.Objects;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;

/** One gameplay-provisioned neighbor that must survive a complete lifecycle. */
public final class BlockLifecycleNeighbor {
    private final BlockFace face;
    private final BlockState state;
    private final BlockLifecycleSlot placementSlot;

    public BlockLifecycleNeighbor(BlockFace face, BlockState state,
            BlockLifecycleSlot placementSlot) {
        this.face = Objects.requireNonNull(face, "face");
        this.state = Objects.requireNonNull(state, "state");
        this.placementSlot = Objects.requireNonNull(placementSlot, "placementSlot");
        if (state.legacyId() == 0) throw new IllegalArgumentException("neighbor state is air");
    }

    public BlockFace face() { return face; }
    public BlockState state() { return state; }
    public BlockLifecycleSlot placementSlot() { return placementSlot; }
    public BlockPosition position(BlockPosition support) { return face.adjacent(support); }
}
