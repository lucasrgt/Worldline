package worldline.testapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Equatable semantic evidence for all five qualified pig lifecycle claims. */
public final class PigLifecycleEvidence {
    private final List<EntityConformanceCase> claims;
    private final EntityLifecycleEvidence lifecycle;
    private final PigSaddleMountObservation interaction;

    public PigLifecycleEvidence(List<EntityConformanceCase> claims,
            EntityLifecycleEvidence lifecycle, PigSaddleMountObservation interaction) {
        this.claims = Collections.unmodifiableList(new ArrayList<EntityConformanceCase>(claims));
        this.lifecycle = lifecycle;
        this.interaction = interaction;
    }

    public List<EntityConformanceCase> claims() { return claims; }
    public EntityLifecycleEvidence lifecycle() { return lifecycle; }
    public PigSaddleMountObservation interaction() { return interaction; }

    public String canonical() {
        StringBuilder value = new StringBuilder("schema=worldline.pig-lifecycle-evidence.v1\n");
        for (EntityConformanceCase claim : claims) value.append("claim=")
                .append(claim.claimId()).append('|').append(claim.layer()).append('\n');
        return value.append("lifecycle=type:90,movement:true,death:3+29,drop:319:0x1\n")
                .append("interaction=item:329,count:1>0,buttons:0+0,packet39:actor>pig,alive:true\n")
                .toString();
    }

    @Override public boolean equals(Object other) {
        return other instanceof PigLifecycleEvidence
                && canonical().equals(((PigLifecycleEvidence) other).canonical());
    }

    @Override public int hashCode() { return canonical().hashCode(); }
}
