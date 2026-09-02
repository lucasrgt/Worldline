package worldline.testapi;

import java.util.Objects;

/** Equatable canonical evidence for the native bed subsystem. */
public final class BedSubsystemEvidence {
    private final BedSubsystemObservation observation;
    public BedSubsystemEvidence(BedSubsystemObservation observation) {
        this.observation = Objects.requireNonNull(observation, "observation");
    }
    public BedSubsystemObservation observation() {
        return observation;
    }
    public String canonical() {
        StringBuilder value = new StringBuilder("schema=worldline.bed-subsystem-evidence.v1\n");
        value.append("subject=b1.7.3:block/026\n");
        value.append("claims=7|state-domain+break-transition+drop-matrix")
                .append("+collision-shape+light-behavior+tick-policy")
                .append("+neighbor-response\n");
        value.append("domains=").append(observation.domains()).append('\n');
        value.append("lifecycle=").append(observation.lifecycle()).append('\n');
        value.append("physics=").append(observation.physics()).append('\n');
        value.append("timing=").append(observation.timing()).append('\n');
        return value.append("neighbors=").append(observation.neighbors()).append('\n').toString();
    }
    @Override public boolean equals(Object other) {
        return other instanceof BedSubsystemEvidence
                && observation.equals(((BedSubsystemEvidence) other).observation);
    }
    @Override public int hashCode() {
        return observation.hashCode();
    }
}
