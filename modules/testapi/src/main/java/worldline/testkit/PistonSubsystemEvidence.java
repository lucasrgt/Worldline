package worldline.testkit;

import java.util.Objects;

/** Equatable canonical evidence for the four-block native piston subsystem. */
public final class PistonSubsystemEvidence {
    private final PistonSubsystemObservation observation;

    PistonSubsystemEvidence(PistonSubsystemObservation observation) {
        this.observation = Objects.requireNonNull(observation, "observation");
    }

    public PistonSubsystemObservation observation() { return observation; }

    public String canonical() {
        StringBuilder value = new StringBuilder("schema=worldline.piston-subsystem-evidence.v1\n");
        value.append("subjects=b1.7.3:block/029,b1.7.3:block/033,")
                .append("b1.7.3:block/034,b1.7.3:block/036\n");
        value.append("claims=28|base:state-domain+collision-shape+light-behavior")
                .append("+tick-policy+neighbor-response|internal:state-domain")
                .append("+gameplay-placement+break-transition+drop-matrix+save-reload")
                .append("+collision-shape+light-behavior+tick-policy+neighbor-response\n");
        value.append("domains=").append(observation.domains()).append('\n');
        value.append("materialization=").append(observation.materialization()).append('\n');
        value.append("break-drops=").append(observation.breakAndDrops()).append('\n');
        value.append("persistence=").append(observation.persistence()).append('\n');
        value.append("collision=").append(observation.collision()).append('\n');
        value.append("light=").append(observation.light()).append('\n');
        value.append("ticks=").append(observation.ticks()).append('\n');
        return value.append("neighbors=").append(observation.neighbors()).append('\n').toString();
    }

    @Override public boolean equals(Object other) {
        return other instanceof PistonSubsystemEvidence
                && observation.equals(((PistonSubsystemEvidence) other).observation);
    }
    @Override public int hashCode() { return observation.hashCode(); }
}
