package worldline.api;

/** Immutable integer coordinate in a Minecraft world. */
public final class BlockPosition {
    private final int x;
    private final int y;
    private final int z;

    public BlockPosition(int x, int y, int z) { this.x = x; this.y = y; this.z = z; }

    public int x() { return x; }

    public int y() { return y; }

    public int z() { return z; }

    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof BlockPosition)) return false;
        BlockPosition value = (BlockPosition) other;
        return x == value.x && y == value.y && z == value.z;
    }

    @Override public int hashCode() { return 31 * (31 * x + y) + z; }

    @Override public String toString() { return "BlockPosition[" + x + "," + y + "," + z + "]"; }
}
