package worldline.testkit;

import java.util.Collections;
import java.util.List;

/** Equatable evidence for the complete qualified hostile behavior matrix. */
public final class HostileBehaviorEvidence {
    private final List<EntityConformanceCase> claims;
    private final HostileBehaviorObservation observation;

    HostileBehaviorEvidence(List<EntityConformanceCase> claims,
            HostileBehaviorObservation observation) {
        this.claims = Collections.unmodifiableList(claims);
        this.observation = observation;
    }

    public List<EntityConformanceCase> claims() { return claims; }
    public HostileBehaviorObservation observation() { return observation; }

    public String canonical() {
        return "schema=worldline.hostile-behavior-evidence.v1\n"
                + "claims=" + claim(0) + ',' + claim(1) + ',' + claim(2) + ',' + claim(3) + "\n"
                + "identity=night:14000,zombie:54,skeleton:51,distinct:true\n"
                + "skeleton=two-packet23-type60,thrower:skeleton,diamond-armor:true\n"
                + "spider=positive-y-adjacent-to:4+5\n"
                + "creeper=proximity-fuse:true,packet60-strength:3,destroys:3+35,reload:air\n";
    }

    private String claim(int index) {
        EntityConformanceCase value = claims.get(index);
        return value.claimId() + '|' + value.layer();
    }

    @Override public boolean equals(Object other) {
        return other instanceof HostileBehaviorEvidence
                && canonical().equals(((HostileBehaviorEvidence) other).canonical());
    }

    @Override public int hashCode() { return canonical().hashCode(); }
}
