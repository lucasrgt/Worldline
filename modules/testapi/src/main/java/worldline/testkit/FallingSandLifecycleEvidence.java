package worldline.testkit;

import java.util.Collections;
import java.util.List;

/** Equatable evidence for falling-sand materialization, landing and persistence. */
public final class FallingSandLifecycleEvidence {
    private final List<EntityConformanceCase> claims;
    private final ObjectMaterializationEvidence materialization;
    private final FallingSandLifecycleObservation observation;

    FallingSandLifecycleEvidence(List<EntityConformanceCase> claims,
            ObjectMaterializationEvidence materialization,
            FallingSandLifecycleObservation observation) {
        this.claims = Collections.unmodifiableList(claims);
        this.materialization = materialization;
        this.observation = observation;
    }

    public List<EntityConformanceCase> claims() { return claims; }
    public ObjectMaterializationEvidence materialization() { return materialization; }
    public FallingSandLifecycleObservation observation() { return observation; }

    public String canonical() {
        return "schema=worldline.falling-sand-lifecycle-evidence.v1\n"
                + "claims=" + claims.get(0).claimId() + '|' + claims.get(0).layer()
                + ',' + claims.get(1).claimId() + '|' + claims.get(1).layer() + "\n"
                + "spawn=packet:23,type:70,positive-distinct-id:true\n"
                + "cause=remove-support,state:1:0-to-0:0\n"
                + "effect=upper:12:0-to-0:0,lower:0:0-to-12:0,ticks:40+40\n"
                + "reload=lower:12:0,upper:0:0\n";
    }

    @Override public boolean equals(Object other) {
        return other instanceof FallingSandLifecycleEvidence
                && canonical().equals(((FallingSandLifecycleEvidence) other).canonical());
    }

    @Override public int hashCode() { return canonical().hashCode(); }
}
