package worldline.testkit;

import java.util.Objects;

/** Equatable canonical evidence for the two-block native furnace subsystem. */
public final class FurnaceSubsystemEvidence {
    private final FurnaceSubsystemObservation observation;
    FurnaceSubsystemEvidence(FurnaceSubsystemObservation observation) {
        this.observation = Objects.requireNonNull(observation, "observation");
    }
    public FurnaceSubsystemObservation observation() { return observation; }
    public String canonical() {
        StringBuilder value = new StringBuilder("schema=worldline.furnace-subsystem-evidence.v1\n");
        value.append("subjects=b1.7.3:block/061,b1.7.3:block/062\n");
        value.append("claims=10|idle:neighbor-response|active:state-domain+gameplay-placement")
                .append("+break-transition+drop-matrix+save-reload+collision-shape")
                .append("+light-behavior+tick-policy+neighbor-response\n");
        value.append("domains=").append(observation.domains()).append('\n');
        value.append("materialization=").append(observation.materialization()).append('\n');
        value.append("lifecycle=").append(observation.lifecycle()).append('\n');
        value.append("physics=").append(observation.physics()).append('\n');
        value.append("timing=").append(observation.timing()).append('\n');
        return value.append("neighbors=").append(observation.neighbors()).append('\n').toString();
    }
    @Override public boolean equals(Object other) {
        return other instanceof FurnaceSubsystemEvidence
                && observation.equals(((FurnaceSubsystemEvidence) other).observation);
    }
    @Override public int hashCode() { return observation.hashCode(); }
}
