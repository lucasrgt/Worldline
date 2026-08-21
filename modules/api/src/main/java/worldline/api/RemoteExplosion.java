package worldline.api;

import java.util.*;

/** Exact bounded protocol-14 Packet60 explosion center, strength and destroyed cells. */
public final class RemoteExplosion {
    public static final int MAX_BLOCKS = 65536;
    private final double x, y, z; private final float strength;
    private final List<BlockPosition> destroyed;
    public RemoteExplosion(double x, double y, double z, float strength, List<BlockPosition> destroyed) {
        if (!finite(x) || !finite(y) || !finite(z)) throw new IllegalArgumentException("invalid explosion center");
        if (!finite(strength) || strength <= 0) throw new IllegalArgumentException("invalid explosion strength " + strength);
        if (destroyed == null || destroyed.size() > MAX_BLOCKS) throw new IllegalArgumentException("invalid explosion block list");
        ArrayList<BlockPosition> copy = new ArrayList<>(destroyed);
        for (BlockPosition position : copy) if (position == null) throw new IllegalArgumentException("invalid destroyed explosion cell");
        this.x = x; this.y = y; this.z = z; this.strength = strength; this.destroyed = Collections.unmodifiableList(copy);
    }
    public double x() { return x; } public double y() { return y; } public double z() { return z; }
    public float strength() { return strength; } public List<BlockPosition> destroyed() { return destroyed; }
    private static boolean finite(double value) { return !Double.isNaN(value) && !Double.isInfinite(value); }
    @Override public boolean equals(Object other) { if (!(other instanceof RemoteExplosion)) return false; RemoteExplosion value = (RemoteExplosion) other;
        return Double.doubleToLongBits(x) == Double.doubleToLongBits(value.x) && Double.doubleToLongBits(y) == Double.doubleToLongBits(value.y)
                && Double.doubleToLongBits(z) == Double.doubleToLongBits(value.z) && Float.floatToIntBits(strength) == Float.floatToIntBits(value.strength)
                && destroyed.equals(value.destroyed); }
    @Override public int hashCode() { return Objects.hash(x, y, z, strength, destroyed); }
}
