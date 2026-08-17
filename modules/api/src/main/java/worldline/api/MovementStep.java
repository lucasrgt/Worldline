package worldline.api;

/** One relative movement and bounded response window in a neutral route. */
public final class MovementStep {
    private final double deltaX, deltaY, deltaZ;
    private final int ticks;

    public MovementStep(double deltaX, double deltaY, double deltaZ, int ticks) {
        if (!Double.isFinite(deltaX) || !Double.isFinite(deltaY) || !Double.isFinite(deltaZ)
                || (deltaX == 0D && deltaY == 0D && deltaZ == 0D))
            throw new IllegalArgumentException("invalid movement delta");
        if (ticks < 1 || ticks > 1200) throw new IllegalArgumentException("invalid response ticks");
        this.deltaX = deltaX; this.deltaY = deltaY; this.deltaZ = deltaZ; this.ticks = ticks;
    }

    public double deltaX() { return deltaX; }
    public double deltaY() { return deltaY; }
    public double deltaZ() { return deltaZ; }
    public int ticks() { return ticks; }
}
