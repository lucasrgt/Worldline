package worldline.testkit;

import java.util.Objects;

/** Structured observations spanning furnace state, lifecycle, timing, and physics. */
public final class FurnaceSubsystemObservation {
    private final String domains, materialization, lifecycle, physics, timing, neighbors;

    public FurnaceSubsystemObservation(String domains, String materialization,
            String lifecycle, String physics, String timing, String neighbors) {
        this.domains = required(domains, "domains");
        this.materialization = required(materialization, "materialization");
        this.lifecycle = required(lifecycle, "lifecycle");
        this.physics = required(physics, "physics");
        this.timing = required(timing, "timing");
        this.neighbors = required(neighbors, "neighbors");
    }
    public String domains() { return domains; }
    public String materialization() { return materialization; }
    public String lifecycle() { return lifecycle; }
    public String physics() { return physics; }
    public String timing() { return timing; }
    public String neighbors() { return neighbors; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof FurnaceSubsystemObservation)) return false;
        FurnaceSubsystemObservation value = (FurnaceSubsystemObservation) other;
        return domains.equals(value.domains) && materialization.equals(value.materialization)
                && lifecycle.equals(value.lifecycle) && physics.equals(value.physics)
                && timing.equals(value.timing) && neighbors.equals(value.neighbors);
    }
    @Override public int hashCode() {
        return Objects.hash(domains, materialization, lifecycle, physics, timing, neighbors);
    }
    private static String required(String value, String label) {
        if (value == null || value.trim().isEmpty())
            throw new IllegalArgumentException(label + " must not be blank");
        return value.trim();
    }
}
