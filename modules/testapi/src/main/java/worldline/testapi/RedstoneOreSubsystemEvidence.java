package worldline.testapi;

import java.util.Objects;

/** Equatable canonical evidence for inactive and glowing redstone ore. */
public final class RedstoneOreSubsystemEvidence {
    private final RedstoneOreSubsystemObservation observation;
    public RedstoneOreSubsystemEvidence(RedstoneOreSubsystemObservation observation) {
        this.observation = Objects.requireNonNull(observation, "observation");
    }
    public RedstoneOreSubsystemObservation observation() { return observation; }
    public String canonical() {
        StringBuilder value = new StringBuilder("schema=worldline.redstone-ore-subsystem-evidence.v1\n");
        value.append("subjects=b1.7.3:block/073,b1.7.3:block/074\n");
        value.append("claims=13|unlit:state-domain+collision-shape")
                .append("+light-behavior+neighbor-response|glowing:state-domain")
                .append("+gameplay-placement+break-transition+drop-matrix+save-reload")
                .append("+collision-shape+light-behavior+tick-policy+neighbor-response\n");
        value.append("registry=").append(observation.registry()).append('\n');
        value.append("domains=").append(observation.domains()).append('\n');
        value.append("lifecycle=").append(observation.lifecycle()).append('\n');
        value.append("physics=").append(observation.physics()).append('\n');
        value.append("timing=").append(observation.timing()).append('\n');
        return value.append("neighbors=").append(observation.neighbors()).append('\n').toString();
    }
    @Override public boolean equals(Object other) {
        return other instanceof RedstoneOreSubsystemEvidence
                && observation.equals(((RedstoneOreSubsystemEvidence) other).observation);
    }
    @Override public int hashCode() { return observation.hashCode(); }
}
