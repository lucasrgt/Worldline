package worldline.api;

import java.util.Objects;

/** One accepted protocol-14 mob death after Packet38 status 3 and Packet29 destroy. */
public final class RemoteMobDeath {
    private final int entityId, deathStatus;
    private final boolean hurtObserved;
    public RemoteMobDeath(int entityId, int deathStatus, boolean hurtObserved) {
        if (entityId < 0) throw new IllegalArgumentException("invalid mob entity id");
        if (deathStatus != 3) throw new IllegalArgumentException("invalid mob death status");
        this.entityId = entityId; this.deathStatus = deathStatus; this.hurtObserved = hurtObserved;
    }
    public int entityId() { return entityId; }
    public int deathStatus() { return deathStatus; }
    public boolean hurtObserved() { return hurtObserved; }
    public int destroyPacket() { return 29; }
    @Override public boolean equals(Object other) {
        if (!(other instanceof RemoteMobDeath)) return false;
        RemoteMobDeath v = (RemoteMobDeath) other;
        return entityId == v.entityId && deathStatus == v.deathStatus && hurtObserved == v.hurtObserved;
    }
    @Override public int hashCode() { return Objects.hash(entityId, deathStatus, hurtObserved); }
}
