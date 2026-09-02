package worldline.testapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Equatable invariant evidence for ghast, slime, boat and minecart motion. */
public final class EntityDynamicsEvidence {
    private final List<EntityConformanceCase> claims;
    private final Map<EntityDynamicsScene, EntityDynamicsObservation> observations;

    public EntityDynamicsEvidence(List<EntityConformanceCase> claims,
            Map<EntityDynamicsScene, EntityDynamicsObservation> observations) {
        this.claims = Collections.unmodifiableList(new ArrayList<EntityConformanceCase>(claims));
        this.observations = Collections.unmodifiableMap(
                new EnumMap<EntityDynamicsScene, EntityDynamicsObservation>(observations));
    }

    public List<EntityConformanceCase> claims() { return claims; }
    public Map<EntityDynamicsScene, EntityDynamicsObservation> observations() {
        return observations;
    }

    public String canonical() {
        StringBuilder value = new StringBuilder("schema=worldline.entity-dynamics-evidence.v1\n");
        for (EntityConformanceCase claim : claims) value.append("claim=")
                .append(claim.claimId()).append('|').append(claim.layer()).append('\n');
        return value.append("ghast=open:span>200,roof:span<200\n")
                .append("slime=open:air+ground+span>100,roof:span<700\n")
                .append("boat=open:no-collision+x>9300,wall:collision\n")
                .append("minecart=short-rail:abs-motion-x<50,long-rail:abs-motion-x>50\n")
                .toString();
    }

    @Override public boolean equals(Object other) {
        return other instanceof EntityDynamicsEvidence
                && canonical().equals(((EntityDynamicsEvidence) other).canonical());
    }

    @Override public int hashCode() { return canonical().hashCode(); }
}
