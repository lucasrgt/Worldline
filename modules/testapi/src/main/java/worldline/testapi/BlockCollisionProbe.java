package worldline.testapi;

/** One relative player trajectory used to sample a block collision envelope. */
public final class BlockCollisionProbe {
    private final String id;
    private final double deltaX, deltaY, deltaZ;
    private final int ticks;
    private final BlockCollisionExpectation expected;

    public BlockCollisionProbe(String id, double deltaX, double deltaY, double deltaZ,
            int ticks, BlockCollisionExpectation expected) {
        if (id == null || !id.matches("[a-z0-9][a-z0-9._-]{0,63}")) {
            throw new IllegalArgumentException("invalid collision probe id");
        }
        if (!finite(deltaX) || !finite(deltaY) || !finite(deltaZ)
                || deltaX == 0D && deltaY == 0D && deltaZ == 0D) {
            throw new IllegalArgumentException("invalid collision trajectory");
        }
        if (ticks < 1 || ticks > 200) throw new IllegalArgumentException("invalid probe ticks");
        this.id = id; this.deltaX = deltaX; this.deltaY = deltaY; this.deltaZ = deltaZ;
        this.ticks = ticks;
        this.expected = java.util.Objects.requireNonNull(expected, "expected");
    }

    public String id() { return id; }
    public double deltaX() { return deltaX; }
    public double deltaY() { return deltaY; }
    public double deltaZ() { return deltaZ; }
    public int ticks() { return ticks; }
    public BlockCollisionExpectation expected() { return expected; }

    private static boolean finite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }
}
