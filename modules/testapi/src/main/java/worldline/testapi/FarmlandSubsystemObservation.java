package worldline.testapi;

import java.util.Objects;

/** Canonical public observation of the native farmland lifecycle. */
public final class FarmlandSubsystemObservation {
    private final String domains, lifecycle, persistence, physics, timing, neighbors;
    public FarmlandSubsystemObservation(String domains, String lifecycle, String persistence,
            String physics, String timing, String neighbors) {
        this.domains = Objects.requireNonNull(domains, "domains");
        this.lifecycle = Objects.requireNonNull(lifecycle, "lifecycle");
        this.persistence = Objects.requireNonNull(persistence, "persistence");
        this.physics = Objects.requireNonNull(physics, "physics");
        this.timing = Objects.requireNonNull(timing, "timing");
        this.neighbors = Objects.requireNonNull(neighbors, "neighbors");
    }
    public String domains() {
        return domains;
    }
    public String lifecycle() {
        return lifecycle;
    }
    public String persistence() {
        return persistence;
    }
    public String physics() {
        return physics;
    }
    public String timing() {
        return timing;
    }
    public String neighbors() {
        return neighbors;
    }
    @Override public boolean equals(Object other) {
        if (!(other instanceof FarmlandSubsystemObservation))
            return false;
        FarmlandSubsystemObservation value = (FarmlandSubsystemObservation) other;
        return domains.equals(value.domains) && lifecycle.equals(value.lifecycle)
                && persistence.equals(value.persistence) && physics.equals(value.physics)
                && timing.equals(value.timing) && neighbors.equals(value.neighbors);
    }
    @Override public int hashCode() {
        return Objects.hash(domains, lifecycle, persistence, physics, timing, neighbors);
    }
}
