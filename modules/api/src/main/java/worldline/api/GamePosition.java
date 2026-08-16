package worldline.api;

/** Immutable finite three-dimensional game position. */
public final class GamePosition {
    private final double x;
    private final double y;
    private final double z;

    public GamePosition(double x, double y, double z) {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
            throw new IllegalArgumentException("position components must be finite");
        }
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double x() { return x; }

    public double y() { return y; }

    public double z() { return z; }

    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof GamePosition)) return false;
        GamePosition value = (GamePosition) other;
        return Double.doubleToLongBits(x) == Double.doubleToLongBits(value.x)
                && Double.doubleToLongBits(y) == Double.doubleToLongBits(value.y)
                && Double.doubleToLongBits(z) == Double.doubleToLongBits(value.z);
    }

    @Override public int hashCode() {
        long bits = Double.doubleToLongBits(x);
        bits = 31 * bits + Double.doubleToLongBits(y);
        bits = 31 * bits + Double.doubleToLongBits(z);
        return (int) (bits ^ bits >>> 32);
    }

    @Override public String toString() { return "GamePosition[" + x + "," + y + "," + z + "]"; }
}
