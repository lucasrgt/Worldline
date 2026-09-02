package worldline.testapi;

import java.util.Objects;

/** Equatable canonical evidence for the native mob-spawner subsystem. */
public final class MobSpawnerSubsystemEvidence {
    private final MobSpawnerSubsystemObservation observation;
    public MobSpawnerSubsystemEvidence(MobSpawnerSubsystemObservation observation) {
        this.observation = Objects.requireNonNull(observation, "observation");
    }
    public MobSpawnerSubsystemObservation observation() { return observation; }
    public String canonical() {
        StringBuilder value = new StringBuilder(
                "schema=worldline.mob-spawner-subsystem-evidence.v1\n");
        value.append("subject=b1.7.3:block/052\n");
        value.append("claims=7|registry-presence+gameplay-placement+break-transition")
                .append("+drop-matrix+save-reload+tick-policy+neighbor-response\n");
        value.append("registry=").append(observation.registry()).append('\n');
        value.append("placement=").append(observation.placement()).append('\n');
        value.append("lifecycle=").append(observation.lifecycle()).append('\n');
        value.append("persistence=").append(observation.persistence()).append('\n');
        value.append("timing=").append(observation.timing()).append('\n');
        return value.append("neighbors=").append(observation.neighbors()).append('\n').toString();
    }
    @Override public boolean equals(Object other) {
        return other instanceof MobSpawnerSubsystemEvidence
                && observation.equals(((MobSpawnerSubsystemEvidence) other).observation);
    }
    @Override public int hashCode() { return observation.hashCode(); }
}
