package worldline.api;

import java.util.Objects;

/** One accepted protocol-14 Packet17 sleep plus the last Packet70Bed reason. */
public final class RemoteBedUse {
    public static final int NO_PACKET70 = -1;
    private final int entityId, unused, x, y, z, packet70;

    public RemoteBedUse(int entityId, int unused, int x, int y, int z, int packet70) {
        if (entityId < 0) throw new IllegalArgumentException("invalid sleep entity id");
        if (unused != 0) throw new IllegalArgumentException("invalid Packet17 unused field");
        if (y < 0 || y > 127) throw new IllegalArgumentException("invalid sleep bed y");
        if (packet70 < NO_PACKET70 || packet70 > 2)
            throw new IllegalArgumentException("invalid Packet70 reason");
        this.entityId = entityId; this.unused = unused; this.x = x; this.y = y; this.z = z;
        this.packet70 = packet70;
    }

    public int entityId() { return entityId; }
    public int unused() { return unused; }
    public int x() { return x; }
    public int y() { return y; }
    public int z() { return z; }
    public int packet70() { return packet70; }
    public int sleepPacket() { return 17; }
    public int bedPacket() { return 70; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof RemoteBedUse)) return false;
        RemoteBedUse value = (RemoteBedUse) other;
        return entityId == value.entityId && unused == value.unused && x == value.x && y == value.y
                && z == value.z && packet70 == value.packet70;
    }

    @Override public int hashCode() { return Objects.hash(entityId, unused, x, y, z, packet70); }
}
