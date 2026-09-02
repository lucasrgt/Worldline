package worldline.testapi;

import java.util.Objects;

/** Equatable canonical evidence for the native locked-chest subsystem. */
public final class LockedChestSubsystemEvidence {
    private final LockedChestSubsystemObservation observation;
    public LockedChestSubsystemEvidence(LockedChestSubsystemObservation observation) {
        this.observation = Objects.requireNonNull(observation, "observation");
    }
    public LockedChestSubsystemObservation observation() {
        return observation;
    }
    public String canonical() {
        StringBuilder value = new StringBuilder(
                "schema=worldline.locked-chest-subsystem-evidence.v1\n");
        value.append("subject=b1.7.3:block/095\n");
        value.append("claims=9|state-domain+gameplay-placement+break-transition+drop-matrix")
                .append("+save-reload+collision-shape+light-behavior+tick-policy")
                .append("+neighbor-response\n");
        value.append("domains=").append(observation.domains()).append('\n');
        value.append("lifecycle=").append(observation.lifecycle()).append('\n');
        value.append("persistence=").append(observation.persistence()).append('\n');
        value.append("physics=").append(observation.physics()).append('\n');
        value.append("timing=").append(observation.timing()).append('\n');
        return value.append("neighbors=").append(observation.neighbors()).append('\n').toString();
    }
    @Override public boolean equals(Object other) {
        return other instanceof LockedChestSubsystemEvidence
                && observation.equals(((LockedChestSubsystemEvidence) other).observation);
    }
    @Override public int hashCode() {
        return observation.hashCode();
    }
}
