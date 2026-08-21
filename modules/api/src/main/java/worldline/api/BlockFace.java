package worldline.api;

/** Neutral face of one integer block position. */
public enum BlockFace {
    DOWN(0, -1, 0),
    UP(0, 1, 0),
    NORTH(0, 0, -1),
    SOUTH(0, 0, 1),
    WEST(-1, 0, 0),
    EAST(1, 0, 0);

    private final int dx, dy, dz;

    BlockFace(int dx, int dy, int dz) { this.dx = dx; this.dy = dy; this.dz = dz; }

    public BlockPosition adjacent(BlockPosition support) {
        if (support == null) throw new IllegalArgumentException("null support position");
        return new BlockPosition(Math.addExact(support.x(), dx), Math.addExact(support.y(), dy),
                Math.addExact(support.z(), dz));
    }
}
