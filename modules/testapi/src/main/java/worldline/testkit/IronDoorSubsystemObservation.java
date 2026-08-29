package worldline.testkit;

import java.util.Objects;

/** Canonical public observation of the native iron-door lifecycle. */
public final class IronDoorSubsystemObservation {
    private final String domains, lifecycle, physics, timing, neighbors;
    public IronDoorSubsystemObservation(String domains, String lifecycle, String physics,
            String timing, String neighbors) {
        this.domains = Objects.requireNonNull(domains, "domains");
        this.lifecycle = Objects.requireNonNull(lifecycle, "lifecycle");
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
        if (!(other instanceof IronDoorSubsystemObservation))
            return false;
        IronDoorSubsystemObservation value = (IronDoorSubsystemObservation) other;
        return domains.equals(value.domains) && lifecycle.equals(value.lifecycle)
                && physics.equals(value.physics)
                && timing.equals(value.timing) && neighbors.equals(value.neighbors);
    }
    @Override public int hashCode() {
        return Objects.hash(domains, lifecycle, physics, timing, neighbors);
    }
}
