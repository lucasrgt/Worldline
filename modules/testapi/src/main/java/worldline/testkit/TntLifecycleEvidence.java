package worldline.testkit;

import java.util.Collections;
import java.util.List;

/** Equatable evidence for primed-TNT materialization and the exact 80-update fuse. */
public final class TntLifecycleEvidence {
    private final List<EntityConformanceCase> claims;
    private final ObjectMaterializationEvidence materialization;
    private final TntFuseLifecycleObservation observation;

    TntLifecycleEvidence(List<EntityConformanceCase> claims,
            ObjectMaterializationEvidence materialization,
            TntFuseLifecycleObservation observation) {
        this.claims = Collections.unmodifiableList(claims);
        this.materialization = materialization;
        this.observation = observation;
    }

    public List<EntityConformanceCase> claims() { return claims; }
    public ObjectMaterializationEvidence materialization() { return materialization; }
    public TntFuseLifecycleObservation observation() { return observation; }

    public String canonical() {
        return "schema=worldline.tnt-lifecycle-evidence.v1\n"
                + "claims=" + claims.get(0).claimId() + '|' + claims.get(0).layer()
                + ',' + claims.get(1).claimId() + '|' + claims.get(1).layer() + "\n"
                + "spawn=packet:23,type:50,thrower:zero,positive-distinct-id:true\n"
                + "fuse=seed:80,t1:79,t40:40,t79:1,t80:0,t81:-1\n"
                + "liveness=seed:present,mid:present-alive,terminal:removed-dead\n"
                + "controls=motion-zeroed,unprimed-block:46,no-unprimed-entity\n";
    }

    @Override public boolean equals(Object other) {
        return other instanceof TntLifecycleEvidence
                && canonical().equals(((TntLifecycleEvidence) other).canonical());
    }

    @Override public int hashCode() { return canonical().hashCode(); }
}
