package worldline.api;

import java.util.Objects;

/** Immutable protocol-14 Packet23 identity, type, quantized pose and thrower. */
public final class RemoteObjectSpawn {
    private final int entityId, type, fixedX, fixedY, fixedZ, throwerId, velocityX, velocityY, velocityZ;

    public RemoteObjectSpawn(int entityId, int type, int fixedX, int fixedY, int fixedZ,
            int throwerId, int velocityX, int velocityY, int velocityZ) {
        if (entityId < 0) throw new IllegalArgumentException("invalid object entity id");
        if (type < 1 || type > 127) throw new IllegalArgumentException("invalid object type");
        if (throwerId < 0) throw new IllegalArgumentException("invalid object thrower");
        if (throwerId == 0 && (velocityX != 0 || velocityY != 0 || velocityZ != 0))
            throw new IllegalArgumentException("object velocity requires a thrower");
        this.entityId = entityId; this.type = type; this.fixedX = fixedX; this.fixedY = fixedY;
        this.fixedZ = fixedZ; this.throwerId = throwerId; this.velocityX = velocityX;
        this.velocityY = velocityY; this.velocityZ = velocityZ;
    }

    public int entityId() { return entityId; }
    public int type() { return type; }
    public int fixedX() { return fixedX; }
    public int fixedY() { return fixedY; }
    public int fixedZ() { return fixedZ; }
    public double x() { return fixedX / 32.0D; }
    public double y() { return fixedY / 32.0D; }
    public double z() { return fixedZ / 32.0D; }
    public int throwerId() { return throwerId; }
    public int velocityX() { return velocityX; }
    public int velocityY() { return velocityY; }
    public int velocityZ() { return velocityZ; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof RemoteObjectSpawn)) return false;
        RemoteObjectSpawn value = (RemoteObjectSpawn) other;
        return entityId == value.entityId && type == value.type && fixedX == value.fixedX
                && fixedY == value.fixedY && fixedZ == value.fixedZ && throwerId == value.throwerId
                && velocityX == value.velocityX && velocityY == value.velocityY
                && velocityZ == value.velocityZ;
    }

    @Override public int hashCode() {
        return Objects.hash(entityId, type, fixedX, fixedY, fixedZ, throwerId, velocityX, velocityY, velocityZ);
    }
}
