package worldline.b173server;

import java.util.Objects;
import worldline.api.PlayerPose;

/** Immutable protocol-14 Packet28 entity-velocity observation. */
public final class B173EntityVelocity {
    private final int entityId, fixedX, fixedY, fixedZ;

    public B173EntityVelocity(int entityId, int fixedX, int fixedY, int fixedZ) {
        if (entityId < 0 || !valid(fixedX) || !valid(fixedY) || !valid(fixedZ))
            throw new IllegalArgumentException("invalid entity velocity");
        this.entityId = entityId; this.fixedX = fixedX; this.fixedY = fixedY; this.fixedZ = fixedZ;
    }

    public int entityId() { return entityId; }
    public int fixedX() { return fixedX; }
    public int fixedY() { return fixedY; }
    public int fixedZ() { return fixedZ; }
    public double x() { return fixedX / 8000D; }
    public double y() { return fixedY / 8000D; }
    public double z() { return fixedZ / 8000D; }

    public boolean awayFrom(PlayerPose victim, double sourceX, double sourceZ) {
        if (victim == null || !Double.isFinite(sourceX) || !Double.isFinite(sourceZ))
            throw new IllegalArgumentException("invalid knockback origin");
        double dx = victim.x() - sourceX, dz = victim.z() - sourceZ;
        return dx * x() + dz * z() > 0D && y() > 0D;
    }

    @Override public boolean equals(Object other) {
        if (!(other instanceof B173EntityVelocity)) return false;
        B173EntityVelocity value = (B173EntityVelocity) other;
        return entityId == value.entityId && fixedX == value.fixedX
                && fixedY == value.fixedY && fixedZ == value.fixedZ;
    }

    @Override public int hashCode() { return Objects.hash(entityId, fixedX, fixedY, fixedZ); }

    private static boolean valid(int value) { return value >= Short.MIN_VALUE && value <= Short.MAX_VALUE; }
}
