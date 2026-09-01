package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemotePaintingSpawn;

/** Equatable evidence for Packet25 pose, opposed orientation and support-loss lifecycle. */
public final class PaintingLifecycleEvidence {
    private final List<EntityConformanceCase> claims;
    private final List<PaintingSpawnExpectation> expectations;
    private final List<RemotePaintingSpawn> spawns;
    private final RemoteDroppedItem drop;

    PaintingLifecycleEvidence(List<EntityConformanceCase> claims,
            List<PaintingSpawnExpectation> expectations,
            List<RemotePaintingSpawn> spawns, RemoteDroppedItem drop) {
        this.claims = Collections.unmodifiableList(new ArrayList<EntityConformanceCase>(claims));
        this.expectations = Collections.unmodifiableList(
                new ArrayList<PaintingSpawnExpectation>(expectations));
        this.spawns = Collections.unmodifiableList(new ArrayList<RemotePaintingSpawn>(spawns));
        this.drop = drop;
    }

    public List<EntityConformanceCase> claims() { return claims; }
    public List<RemotePaintingSpawn> spawns() { return spawns; }
    public RemoteDroppedItem drop() { return drop; }

    public String canonical() {
        StringBuilder value = new StringBuilder("schema=worldline.painting-lifecycle-evidence.v1\n");
        for (EntityConformanceCase claim : claims) {
            value.append("claim=").append(claim.claimId()).append('|')
                    .append(claim.layer()).append('\n');
        }
        value.append("spawn=packet:25,positive-id:true,known-title:true\n");
        for (int index = 0; index < expectations.size(); index++) {
            value.append("pose.").append(index + 1).append('=')
                    .append(expectations.get(index).canonical()).append('\n');
        }
        return value.append("orientation=distinct-direction:true,distinct-id:true\n")
                .append("support-loss=packet:29,same-id:true,drop:321x1:0\n").toString();
    }

    @Override public boolean equals(Object other) {
        return other instanceof PaintingLifecycleEvidence
                && canonical().equals(((PaintingLifecycleEvidence) other).canonical());
    }

    @Override public int hashCode() { return canonical().hashCode(); }
}
