package worldline.testkit;

import java.util.Objects;

/** Equatable canonical evidence for the native portal block subsystem. */
public final class PortalBlockSubsystemEvidence {
    private final PortalBlockSubsystemObservation observation;
    PortalBlockSubsystemEvidence(PortalBlockSubsystemObservation observation) {
        this.observation = Objects.requireNonNull(observation, "observation");
    }
    public PortalBlockSubsystemObservation observation() { return observation; }
    public String canonical() {
        StringBuilder value = new StringBuilder("schema=worldline.portal-block-subsystem-evidence.v1\n");
        value.append("subject=b1.7.3:block/090\n");
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
        return other instanceof PortalBlockSubsystemEvidence
                && observation.equals(((PortalBlockSubsystemEvidence) other).observation);
    }
    @Override public int hashCode() { return observation.hashCode(); }
}
