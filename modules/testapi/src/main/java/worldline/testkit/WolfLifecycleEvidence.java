package worldline.testkit;

import java.util.Collections;
import java.util.List;

/** Equatable evidence for controlled wolf materialization and owner interaction state. */
public final class WolfLifecycleEvidence {
    private final List<EntityConformanceCase> claims;
    private final WolfOwnerStateObservation observation;

    WolfLifecycleEvidence(List<EntityConformanceCase> claims,
            WolfOwnerStateObservation observation) {
        this.claims = Collections.unmodifiableList(claims);
        this.observation = observation;
    }

    public List<EntityConformanceCase> claims() { return claims; }
    public WolfOwnerStateObservation observation() { return observation; }

    public String canonical() {
        return "schema=worldline.wolf-lifecycle-evidence.v1\n"
                + "claims=" + claims.get(0).claimId() + '|' + claims.get(0).layer()
                + ',' + claims.get(1).claimId() + '|' + claims.get(1).layer() + "\n"
                + "spawn=packet:24,type:95,positive-distinct-id:true\n"
                + "tame=item:352,status:7,collar:red,dye-damage:4,alive:true\n"
                + "owner-state=item:280,button:0,sequence:sitting-standing-sitting-standing"
                + ",packet40:index16,alive:true\n";
    }

    @Override public boolean equals(Object other) {
        return other instanceof WolfLifecycleEvidence
                && canonical().equals(((WolfLifecycleEvidence) other).canonical());
    }

    @Override public int hashCode() { return canonical().hashCode(); }
}
