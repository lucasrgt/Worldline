package worldline.testapi;

import java.util.Objects;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;

/** One gameplay placement that establishes a collision treatment scene. */
public final class BlockCollisionPlacement {
    private final BlockPosition support;
    private final BlockFace face;
    private final BlockState expected;

    public BlockCollisionPlacement(BlockPosition support, BlockFace face, BlockState expected) {
        this.support = Objects.requireNonNull(support, "support");
        this.face = Objects.requireNonNull(face, "face");
        this.expected = Objects.requireNonNull(expected, "expected");
        if (expected.legacyId() == 0) throw new IllegalArgumentException("air collision placement");
    }

    public BlockPosition support() { return support; }
    public BlockFace face() { return face; }
    public BlockPosition position() { return face.adjacent(support); }
    public BlockState expected() { return expected; }
}
