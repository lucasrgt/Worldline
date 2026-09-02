package worldline.testapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Canonical public evidence for universal EntityList registry presence. */
public final class EntityRegistryEvidence {
    private final List<EntityConformanceCase> claims;
    private final List<EntityRegistryObservation> observations;

    public EntityRegistryEvidence(List<EntityConformanceCase> claims,
            List<EntityRegistryObservation> observations) {
        this.claims = Collections.unmodifiableList(new ArrayList<EntityConformanceCase>(claims));
        this.observations = Collections.unmodifiableList(
                new ArrayList<EntityRegistryObservation>(observations));
    }

    public List<EntityConformanceCase> claims() { return claims; }
    public List<EntityRegistryObservation> observations() { return observations; }

    public String canonical() {
        StringBuilder value = new StringBuilder("schema=worldline.entity-registry-evidence.v1\n");
        value.append("claims=").append(claims.size()).append('\n');
        for (int index = 0; index < claims.size(); index++) {
            String key = String.format("%03d", index + 1);
            EntityConformanceCase claim = claims.get(index);
            EntityRegistryObservation observation = observations.get(index);
            value.append("claim.").append(key).append('=').append(claim.claimId())
                    .append('|').append(claim.layer()).append('\n');
            value.append("row.").append(key).append('=').append(observation.row()).append('\n');
        }
        return value.toString();
    }

    @Override public boolean equals(Object other) {
        return other instanceof EntityRegistryEvidence
                && canonical().equals(((EntityRegistryEvidence) other).canonical());
    }

    @Override public int hashCode() { return canonical().hashCode(); }
}
