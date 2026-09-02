package worldline.testapi;

import java.util.Objects;

/** Equatable evidence for the native vegetation ecology matrix. */
public final class VegetationEcologyEvidence {
    private final VegetationEcologyObservation observation;

    public VegetationEcologyEvidence(VegetationEcologyObservation observation) {
        this.observation = Objects.requireNonNull(observation, "observation");
    }

    public VegetationEcologyObservation observation() { return observation; }

    public String canonical() {
        StringBuilder value = new StringBuilder(
                "schema=worldline.vegetation-ecology-evidence.v1\n");
        value.append("subjects=b1.7.3:block/002+006+018+031+059+083\n");
        value.append("claims=19|states=6+shapes=5+light=4+neighbors=4\n");
        value.append("states=").append(observation.states()).append('\n');
        value.append("shapes=").append(observation.shapes()).append('\n');
        value.append("light=").append(observation.light()).append('\n');
        return value.append("neighbors=").append(observation.neighbors()).append('\n').toString();
    }

    @Override public boolean equals(Object other) {
        return other instanceof VegetationEcologyEvidence
                && observation.equals(((VegetationEcologyEvidence) other).observation);
    }

    @Override public int hashCode() { return observation.hashCode(); }
}
