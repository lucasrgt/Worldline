package worldline.testkit;

import java.util.Objects;

/** Canonical public observation of the native mob-spawner lifecycle. */
public final class MobSpawnerSubsystemObservation {
    private final String registry, placement, lifecycle, persistence, timing, neighbors;
    public MobSpawnerSubsystemObservation(String registry, String placement, String lifecycle,
            String persistence, String timing, String neighbors) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.placement = Objects.requireNonNull(placement, "placement");
        this.lifecycle = Objects.requireNonNull(lifecycle, "lifecycle");
        this.persistence = Objects.requireNonNull(persistence, "persistence");
        this.timing = Objects.requireNonNull(timing, "timing");
        this.neighbors = Objects.requireNonNull(neighbors, "neighbors");
    }
    public String registry() { return registry; }
    public String placement() { return placement; }
    public String lifecycle() { return lifecycle; }
    public String persistence() { return persistence; }
    public String timing() { return timing; }
    public String neighbors() { return neighbors; }
    @Override public boolean equals(Object other) {
        if (!(other instanceof MobSpawnerSubsystemObservation))
            return false;
        MobSpawnerSubsystemObservation value = (MobSpawnerSubsystemObservation) other;
        return registry.equals(value.registry) && placement.equals(value.placement)
                && lifecycle.equals(value.lifecycle) && persistence.equals(value.persistence)
                && timing.equals(value.timing) && neighbors.equals(value.neighbors);
    }
    @Override public int hashCode() {
        return Objects.hash(registry, placement, lifecycle, persistence, timing, neighbors);
    }
}
