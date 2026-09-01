package worldline.testkit;

import java.util.Objects;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;

/** One gameplay placement that establishes a light-transport treatment. */
public final class BlockLightPlacement {
    private final BlockPosition support;
    private final BlockFace face;
    private final BlockState expected;

    public BlockLightPlacement(BlockPosition support, BlockFace face, BlockState expected) {
        this.support = Objects.requireNonNull(support, "support");
        this.face = Objects.requireNonNull(face, "face");
        this.expected = Objects.requireNonNull(expected, "expected");
        if (expected.legacyId() == 0) throw new IllegalArgumentException("air light placement");
    }

    public BlockPosition support() { return support; }
    public BlockFace face() { return face; }
    public BlockPosition position() { return face.adjacent(support); }
    public BlockState expected() { return expected; }
}
