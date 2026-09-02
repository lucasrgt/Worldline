package worldline.testapi;

import java.util.Objects;

/** Structured observations spanning portal materialization, lifecycle, and physics. */
public final class PortalBlockSubsystemObservation {
    private final String domains, lifecycle, persistence, physics, timing, neighbors;
    public PortalBlockSubsystemObservation(String domains, String lifecycle, String persistence,
            String physics, String timing, String neighbors) {
        this.domains = required(domains, "domains");
        this.lifecycle = required(lifecycle, "lifecycle");
        this.persistence = required(persistence, "persistence");
        this.physics = required(physics, "physics");
        this.timing = required(timing, "timing");
        this.neighbors = required(neighbors, "neighbors");
    }
    public String domains() { return domains; }
    public String lifecycle() { return lifecycle; }
    public String persistence() { return persistence; }
    public String physics() { return physics; }
    public String timing() { return timing; }
    public String neighbors() { return neighbors; }
    @Override public boolean equals(Object other) {
        if (!(other instanceof PortalBlockSubsystemObservation)) return false;
        PortalBlockSubsystemObservation value = (PortalBlockSubsystemObservation) other;
        return domains.equals(value.domains) && lifecycle.equals(value.lifecycle)
                && persistence.equals(value.persistence) && physics.equals(value.physics)
                && timing.equals(value.timing) && neighbors.equals(value.neighbors);
    }
    @Override public int hashCode() {
        return Objects.hash(domains, lifecycle, persistence, physics, timing, neighbors);
    }
    private static String required(String value, String label) {
        if (value == null || value.trim().isEmpty())
            throw new IllegalArgumentException(label + " must not be blank");
        return value.trim();
    }
}
