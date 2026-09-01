package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteMobDeath;
import worldline.api.RemoteMobMovement;
import worldline.api.RemoteMobSpawn;

/** Equatable semantic evidence from one selected persistent-entity lifecycle. */
public final class EntityLifecycleEvidence {
    private final List<EntityConformanceCase> claims;
    private final RemoteMobSpawn spawn;
    private final RemoteMobMovement movement;
    private final RemoteMobDeath death;
    private final RemoteDroppedItem drop;
    private final EntityDropExpectation expectedDrop;
    private final int attempts;
    private final int maximumAttempts;

    EntityLifecycleEvidence(List<EntityConformanceCase> claims, RemoteMobSpawn spawn,
            RemoteMobMovement movement, RemoteMobDeath death, RemoteDroppedItem drop,
            EntityDropExpectation expectedDrop, int attempts, int maximumAttempts) {
        this.claims = Collections.unmodifiableList(
                new ArrayList<EntityConformanceCase>(claims));
        this.spawn = spawn;
        this.movement = movement;
        this.death = death;
        this.drop = drop;
        this.expectedDrop = expectedDrop;
        this.attempts = attempts;
        this.maximumAttempts = maximumAttempts;
    }

    public List<EntityConformanceCase> claims() { return claims; }
    public RemoteMobSpawn spawn() { return spawn; }
    public RemoteMobMovement movement() { return movement; }
    public RemoteMobDeath death() { return death; }
    public RemoteDroppedItem drop() { return drop; }
    public int attempts() { return attempts; }
    public int maximumAttempts() { return maximumAttempts; }

    public String canonical() {
        StringBuilder value = new StringBuilder("schema=worldline.entity-lifecycle-evidence.v2\n");
        value.append("claims=").append(claims.size()).append('\n');
        for (int index = 0; index < claims.size(); index++) {
            EntityConformanceCase claim = claims.get(index);
            value.append("claim.").append(String.format("%03d", index + 1)).append('=')
                    .append(claim.claimId()).append('|').append(claim.layer()).append('\n');
        }
        value.append("spawn=type:").append(spawn.legacyType())
                .append(",positive-id:true,packet:24\n");
        if (movement != null) value.append("movement=entity-consistent:true,transition:true,packet-family:31-34\n");
        if (death != null) value.append("death=status:").append(death.deathStatus())
                .append(",destroy:").append(death.destroyPacket())
                .append(",hurt:").append(death.hurtObserved()).append('\n');
        if (drop != null) value.append("drop=item:").append(expectedDrop.legacyId())
                .append(",count:").append(expectedDrop.minimumCount()).append('-')
                .append(expectedDrop.maximumCount())
                .append(",damage:").append(expectedDrop.damage()).append('\n');
        value.append("attempts=bounded,max:").append(maximumAttempts).append('\n');
        return value.toString();
    }

    @Override public boolean equals(Object other) {
        return other instanceof EntityLifecycleEvidence
                && canonical().equals(((EntityLifecycleEvidence) other).canonical());
    }

    @Override public int hashCode() {
        return canonical().hashCode();
    }
}
