package worldline.testapi;

import java.util.Objects;

/** Equatable evidence for the native redstone input-control subsystem. */
public final class RedstoneInputControlsSubsystemEvidence {
    private final RedstoneInputControlsSubsystemObservation observation;

    public RedstoneInputControlsSubsystemEvidence(RedstoneInputControlsSubsystemObservation observation) {
        this.observation = Objects.requireNonNull(observation, "observation");
    }

    public RedstoneInputControlsSubsystemObservation observation() { return observation; }

    public String canonical() {
        StringBuilder value = new StringBuilder(
                "schema=worldline.redstone-input-controls-subsystem-evidence.v1\n");
        value.append("subjects=b1.7.3:block/069+070+072+077\n");
        value.append("claims=20|state-domain+collision-shape+light-behavior")
                .append("+tick-policy+neighbor-response x4\n");
        value.append("lever=").append(observation.lever()).append('\n');
        value.append("button=").append(observation.button()).append('\n');
        value.append("stone-plate=").append(observation.stonePlate()).append('\n');
        value.append("wooden-plate=").append(observation.woodenPlate()).append('\n');
        return value.append("support=").append(observation.support()).append('\n').toString();
    }

    @Override public boolean equals(Object other) {
        return other instanceof RedstoneInputControlsSubsystemEvidence
                && observation.equals(
                        ((RedstoneInputControlsSubsystemEvidence) other).observation);
    }

    @Override public int hashCode() { return observation.hashCode(); }
}
