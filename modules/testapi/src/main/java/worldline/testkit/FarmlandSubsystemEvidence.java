package worldline.testkit;

import java.util.Objects;

/** Equatable canonical evidence for the native farmland subsystem. */
public final class FarmlandSubsystemEvidence {
    private final FarmlandSubsystemObservation observation;
    FarmlandSubsystemEvidence(FarmlandSubsystemObservation observation) {
        this.observation = Objects.requireNonNull(observation, "observation");
    }
    public FarmlandSubsystemObservation observation() {
        return observation;
    }
    public String canonical() {
        StringBuilder value = new StringBuilder(
                "schema=worldline.farmland-subsystem-evidence.v1\n");
        value.append("subject=b1.7.3:block/060\n");
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
        return other instanceof FarmlandSubsystemEvidence
                && observation.equals(((FarmlandSubsystemEvidence) other).observation);
    }
    @Override public int hashCode() {
        return observation.hashCode();
    }
}
