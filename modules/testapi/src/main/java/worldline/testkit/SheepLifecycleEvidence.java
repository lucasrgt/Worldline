package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Equatable evidence for all five qualified sheep lifecycle claims. */
public final class SheepLifecycleEvidence {
    private final List<EntityConformanceCase> claims;
    private final EntityLifecycleEvidence lethal;
    private final SheepDyeShearObservation interaction;
    private final SheepPersistenceObservation persistence;
    private final int maximumAttempts;

    SheepLifecycleEvidence(List<EntityConformanceCase> claims,
            EntityLifecycleEvidence lethal, SheepDyeShearObservation interaction,
            SheepPersistenceObservation persistence, int maximumAttempts) {
        this.claims = Collections.unmodifiableList(
                new ArrayList<EntityConformanceCase>(claims));
        this.lethal = lethal;
        this.interaction = interaction;
        this.persistence = persistence;
        this.maximumAttempts = maximumAttempts;
    }

    public List<EntityConformanceCase> claims() { return claims; }
    public EntityLifecycleEvidence lethal() { return lethal; }
    public SheepDyeShearObservation interaction() { return interaction; }
    public SheepPersistenceObservation persistence() { return persistence; }
    public int maximumAttempts() { return maximumAttempts; }

    public String canonical() {
        StringBuilder value = new StringBuilder("schema=worldline.sheep-lifecycle-evidence.v1\n");
        for (EntityConformanceCase claim : claims) value.append("claim=")
                .append(claim.claimId()).append('|').append(claim.layer()).append('\n');
        return value.append("lethal=type:91,death:3+29,drop:35:0x1,bounded:")
                .append(maximumAttempts).append('\n')
                .append("interaction=dyes:351:1+351:11,wool:35:14+35:4,alive:true\n")
                .append("persistence=metadata:14>30>30,control:0,repeat-wool:false,")
                .append("nbt:true>false,mutated:14>30,changed:1,restarts:3\n")
                .toString();
    }

    @Override public boolean equals(Object other) {
        return other instanceof SheepLifecycleEvidence
                && canonical().equals(((SheepLifecycleEvidence) other).canonical());
    }

    @Override public int hashCode() { return canonical().hashCode(); }
}
