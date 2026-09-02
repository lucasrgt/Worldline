package worldline.testapi;

import java.util.Objects;

/** Equatable evidence for the native openable access-structure matrix. */
public final class OpenableAccessStructuresEvidence {
    private final OpenableAccessStructuresObservation observation;

    public OpenableAccessStructuresEvidence(OpenableAccessStructuresObservation observation) {
        this.observation = Objects.requireNonNull(observation, "observation");
    }

    public OpenableAccessStructuresObservation observation() { return observation; }

    public String canonical() {
        StringBuilder value = new StringBuilder(
                "schema=worldline.openable-access-structures-evidence.v1\n");
        value.append("subjects=b1.7.3:block/054+064+096\n");
        value.append("claims=10|chest=2+wooden-door=3+trapdoor=5\n");
        value.append("chest=").append(observation.chest()).append('\n');
        value.append("wooden-door=").append(observation.woodenDoor()).append('\n');
        return value.append("trapdoor=").append(observation.trapdoor()).append('\n').toString();
    }

    @Override public boolean equals(Object other) {
        return other instanceof OpenableAccessStructuresEvidence
                && observation.equals(((OpenableAccessStructuresEvidence) other).observation);
    }

    @Override public int hashCode() { return observation.hashCode(); }
}
