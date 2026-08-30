package worldline.testkit;

import java.util.Objects;

/** Equatable evidence for the native redstone signal-consumer matrix. */
public final class RedstoneSignalConsumersEvidence {
    private final RedstoneSignalConsumersObservation observation;

    RedstoneSignalConsumersEvidence(RedstoneSignalConsumersObservation observation) {
        this.observation = Objects.requireNonNull(observation, "observation");
    }

    public RedstoneSignalConsumersObservation observation() { return observation; }

    public String canonical() {
        StringBuilder value = new StringBuilder(
                "schema=worldline.redstone-signal-consumers-evidence.v1\n");
        value.append("subjects=b1.7.3:block/023+025+046+050+055+084\n");
        value.append("claims=18|states=3+shapes=2+light=2+ticks=6+neighbors=5\n");
        value.append("states=").append(observation.states()).append('\n');
        value.append("shapes=").append(observation.shapes()).append('\n');
        value.append("light=").append(observation.light()).append('\n');
        value.append("ticks=").append(observation.ticks()).append('\n');
        return value.append("neighbors=").append(observation.neighbors()).append('\n').toString();
    }

    @Override public boolean equals(Object other) {
        return other instanceof RedstoneSignalConsumersEvidence
                && observation.equals(((RedstoneSignalConsumersEvidence) other).observation);
    }

    @Override public int hashCode() { return observation.hashCode(); }
}
