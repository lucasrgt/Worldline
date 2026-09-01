package worldline.testkit;

import java.util.Objects;

/** Equatable canonical evidence for the native fire subsystem. */
public final class FireSubsystemEvidence {
    private final FireSubsystemObservation observation;
    FireSubsystemEvidence(FireSubsystemObservation observation) {
        this.observation = Objects.requireNonNull(observation, "observation");
    }
    public FireSubsystemObservation observation() {
        return observation;
    }
    public String canonical() {
        StringBuilder value = new StringBuilder(
                "schema=worldline.fire-subsystem-evidence.v1\n");
        value.append("subject=b1.7.3:block/051\n");
        value.append("claims=8|state-domain+gameplay-placement+break-transition+drop-matrix")
                .append("+save-reload+collision-shape+light-behavior+neighbor-response\n");
        value.append("domains=").append(observation.domains()).append('\n');
        value.append("lifecycle=").append(observation.lifecycle()).append('\n');
        value.append("persistence=").append(observation.persistence()).append('\n');
        value.append("physics=").append(observation.physics()).append('\n');
        value.append("timing=").append(observation.timing()).append('\n');
        return value.append("neighbors=").append(observation.neighbors()).append('\n').toString();
    }
    @Override public boolean equals(Object other) {
        return other instanceof FireSubsystemEvidence
                && observation.equals(((FireSubsystemEvidence) other).observation);
    }
    @Override public int hashCode() {
        return observation.hashCode();
    }
}
