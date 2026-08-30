package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Canonical public evidence for the universal registry-presence dimension. */
public final class BlockRegistryEvidence {
    private final List<BlockConformanceCase> claims;
    private final List<BlockRegistryObservation> observations;

    BlockRegistryEvidence(List<BlockConformanceCase> claims,
            List<BlockRegistryObservation> observations) {
        this.claims = Collections.unmodifiableList(new ArrayList<BlockConformanceCase>(claims));
        this.observations = Collections.unmodifiableList(
                new ArrayList<BlockRegistryObservation>(observations));
    }

    public List<BlockConformanceCase> claims() { return claims; }
    public List<BlockRegistryObservation> observations() { return observations; }

    public String canonical() {
        StringBuilder value = new StringBuilder("schema=worldline.block-registry-evidence.v1\n");
        value.append("claims=").append(claims.size()).append('\n');
        for (int index = 0; index < claims.size(); index++) {
            String key = String.format("%03d", index + 1);
            BlockConformanceCase claim = claims.get(index);
            BlockRegistryObservation observation = observations.get(index);
            value.append("claim.").append(key).append('=').append(claim.claimId())
                    .append('|').append(claim.layer()).append('\n');
            value.append("row.").append(key).append('=').append(observation.row()).append('\n');
        }
        return value.toString();
    }

    @Override public boolean equals(Object other) {
        if (!(other instanceof BlockRegistryEvidence)) return false;
        BlockRegistryEvidence value = (BlockRegistryEvidence) other;
        return canonical().equals(value.canonical());
    }

    @Override public int hashCode() { return canonical().hashCode(); }
}
