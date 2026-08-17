package worldline.api;

import java.util.Objects;

/** Immutable item-entity spawn observed from an authoritative remote server. */
public final class RemoteDroppedItem {
    private final int entityId;
    private final RemoteItemStack item;
    private final double x, y, z, velocityX, velocityY, velocityZ;

    public RemoteDroppedItem(int entityId, RemoteItemStack item, double x, double y, double z,
            double velocityX, double velocityY, double velocityZ) {
        if (entityId < 0) throw new IllegalArgumentException("invalid dropped-item entity ID");
        this.item = Objects.requireNonNull(item, "item");
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
                || !Double.isFinite(velocityX) || !Double.isFinite(velocityY)
                || !Double.isFinite(velocityZ)) throw new IllegalArgumentException("non-finite dropped item");
        if (Math.abs(velocityX) >= 1D || Math.abs(velocityY) >= 1D || Math.abs(velocityZ) >= 1D)
            throw new IllegalArgumentException("invalid dropped-item velocity");
        this.entityId = entityId; this.x = x; this.y = y; this.z = z;
        this.velocityX = velocityX; this.velocityY = velocityY; this.velocityZ = velocityZ;
    }

    public int entityId() { return entityId; }
    public RemoteItemStack item() { return item; }
    public double x() { return x; }
    public double y() { return y; }
    public double z() { return z; }
    public double velocityX() { return velocityX; }
    public double velocityY() { return velocityY; }
    public double velocityZ() { return velocityZ; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof RemoteDroppedItem)) return false;
        RemoteDroppedItem value = (RemoteDroppedItem) other;
        return entityId == value.entityId && item.equals(value.item)
                && Double.compare(x, value.x) == 0 && Double.compare(y, value.y) == 0
                && Double.compare(z, value.z) == 0 && Double.compare(velocityX, value.velocityX) == 0
                && Double.compare(velocityY, value.velocityY) == 0
                && Double.compare(velocityZ, value.velocityZ) == 0;
    }

    @Override public int hashCode() { return Objects.hash(entityId, item, x, y, z, velocityX, velocityY, velocityZ); }
    @Override public String toString() { return "RemoteDroppedItem[id=" + entityId + ",item=" + item
            + ",position=" + x + ":" + y + ":" + z + ",velocity="
            + velocityX + ":" + velocityY + ":" + velocityZ + "]"; }
}
