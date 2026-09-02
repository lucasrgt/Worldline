package worldline.testapi;

import java.util.Objects;

/** Equatable canonical evidence for the two-block native redstone torch subsystem. */
public final class RedstoneTorchSubsystemEvidence {
    private final RedstoneTorchSubsystemObservation observation;
    public RedstoneTorchSubsystemEvidence(RedstoneTorchSubsystemObservation observation) {
        this.observation = Objects.requireNonNull(observation, "observation");
    }
    public RedstoneTorchSubsystemObservation observation() { return observation; }
    public String canonical() {
        StringBuilder value = new StringBuilder("schema=worldline.redstone-torch-subsystem-evidence.v1\n");
        value.append("subjects=b1.7.3:block/075,b1.7.3:block/076\n");
        value.append("claims=13|off:state-domain+gameplay-placement+break-transition")
                .append("+drop-matrix+save-reload+collision-shape+light-behavior+tick-policy")
                .append("+neighbor-response|on:state-domain+collision-shape+tick-policy")
                .append("+neighbor-response\n");
        value.append("domains=").append(observation.domains()).append('\n');
        value.append("materialization=").append(observation.materialization()).append('\n');
        value.append("lifecycle=").append(observation.lifecycle()).append('\n');
        value.append("physics=").append(observation.physics()).append('\n');
        value.append("timing=").append(observation.timing()).append('\n');
        return value.append("neighbors=").append(observation.neighbors()).append('\n').toString();
    }
    @Override public boolean equals(Object other) {
        return other instanceof RedstoneTorchSubsystemEvidence
                && observation.equals(((RedstoneTorchSubsystemEvidence) other).observation);
    }
    @Override public int hashCode() { return observation.hashCode(); }
}
