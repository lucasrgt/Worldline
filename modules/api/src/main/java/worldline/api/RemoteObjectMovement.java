package worldline.api;

import java.util.Objects;

/** One accepted protocol-14 object position transition after its Packet23 spawn. */
public final class RemoteObjectMovement {
    private final int entityId, packetId, fromX, fromY, fromZ, toX, toY, toZ, yaw, pitch;

    public RemoteObjectMovement(int entityId, int packetId, int fromX, int fromY, int fromZ,
            int toX, int toY, int toZ, int yaw, int pitch) {
        if (entityId < 0) throw new IllegalArgumentException("invalid object entity id");
        if (packetId != 31 && packetId != 33 && packetId != 34)
            throw new IllegalArgumentException("invalid movement packet");
        if (fromX == toX && fromY == toY && fromZ == toZ)
            throw new IllegalArgumentException("stationary object transition");
        if (yaw < 0 || yaw > 255 || pitch < 0 || pitch > 255)
            throw new IllegalArgumentException("invalid object rotation");
        this.entityId = entityId; this.packetId = packetId; this.fromX = fromX;
        this.fromY = fromY; this.fromZ = fromZ; this.toX = toX; this.toY = toY;
        this.toZ = toZ; this.yaw = yaw; this.pitch = pitch;
    }

    public int entityId() { return entityId; }
    public int packetId() { return packetId; }
    public int fromFixedX() { return fromX; }
    public int fromFixedY() { return fromY; }
    public int fromFixedZ() { return fromZ; }
    public int toFixedX() { return toX; }
    public int toFixedY() { return toY; }
    public int toFixedZ() { return toZ; }
    public double fromX() { return fromX / 32D; }
    public double fromY() { return fromY / 32D; }
    public double fromZ() { return fromZ / 32D; }
    public double toX() { return toX / 32D; }
    public double toY() { return toY / 32D; }
    public double toZ() { return toZ / 32D; }
    public int yaw() { return yaw; }
    public int pitch() { return pitch; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof RemoteObjectMovement)) return false;
        RemoteObjectMovement value = (RemoteObjectMovement) other;
        return entityId == value.entityId && packetId == value.packetId && fromX == value.fromX
                && fromY == value.fromY && fromZ == value.fromZ && toX == value.toX
                && toY == value.toY && toZ == value.toZ && yaw == value.yaw
                && pitch == value.pitch;
    }

    @Override public int hashCode() {
        return Objects.hash(entityId, packetId, fromX, fromY, fromZ, toX, toY, toZ, yaw, pitch);
    }
}
