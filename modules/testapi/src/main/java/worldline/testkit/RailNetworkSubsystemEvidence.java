package worldline.testkit;

import java.util.Objects;

/** Equatable evidence for the native rail-network subsystem. */
public final class RailNetworkSubsystemEvidence {
    private final RailNetworkSubsystemObservation observation;

    RailNetworkSubsystemEvidence(RailNetworkSubsystemObservation observation) {
        this.observation = Objects.requireNonNull(observation, "observation");
    }

    public RailNetworkSubsystemObservation observation() { return observation; }

    public String canonical() {
        StringBuilder value = new StringBuilder(
                "schema=worldline.rail-network-subsystem-evidence.v1\n");
        value.append("subjects=b1.7.3:block/027+028+066\n");
        value.append("claims=14|powered=4+detector=5+normal=5\n");
        value.append("normal-rail=").append(observation.normalRail()).append('\n');
        value.append("powered-rail=").append(observation.poweredRail()).append('\n');
        value.append("detector-rail=").append(observation.detectorRail()).append('\n');
        return value.append("support=").append(observation.support()).append('\n').toString();
    }

    @Override public boolean equals(Object other) {
        return other instanceof RailNetworkSubsystemEvidence
                && observation.equals(((RailNetworkSubsystemEvidence) other).observation);
    }

    @Override public int hashCode() { return observation.hashCode(); }
}
