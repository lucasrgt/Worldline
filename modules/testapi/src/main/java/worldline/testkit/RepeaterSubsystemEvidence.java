package worldline.testkit;

import java.util.Objects;

/** Equatable canonical evidence for the two-block native repeater subsystem. */
public final class RepeaterSubsystemEvidence {
    private final RepeaterSubsystemObservation observation;

    RepeaterSubsystemEvidence(RepeaterSubsystemObservation observation) {
        this.observation = Objects.requireNonNull(observation, "observation");
    }

    public RepeaterSubsystemObservation observation() { return observation; }

    public String canonical() {
        StringBuilder value = new StringBuilder("schema=worldline.repeater-subsystem-evidence.v1\n");
        value.append("subjects=b1.7.3:block/093,b1.7.3:block/094\n");
        value.append("claims=14|off:state-domain+collision-shape+light-behavior")
                .append("+tick-policy+neighbor-response|on:state-domain+gameplay-placement")
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
        return other instanceof RepeaterSubsystemEvidence
                && observation.equals(((RepeaterSubsystemEvidence) other).observation);
    }
    @Override public int hashCode() { return observation.hashCode(); }
}
