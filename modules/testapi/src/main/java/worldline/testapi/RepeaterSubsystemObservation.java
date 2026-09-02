package worldline.testapi;

import java.util.Objects;

/** Structured observations spanning repeater state, lifecycle, timing, and physics. */
public final class RepeaterSubsystemObservation {
    private final String domains;
    private final String materialization;
    private final String lifecycle;
    private final String physics;
    private final String timing;
    private final String neighbors;

    public RepeaterSubsystemObservation(String domains, String materialization,
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
        if (!(other instanceof RepeaterSubsystemObservation)) return false;
        RepeaterSubsystemObservation value = (RepeaterSubsystemObservation) other;
        return domains.equals(value.domains)
                && materialization.equals(value.materialization)
                && lifecycle.equals(value.lifecycle)
                && physics.equals(value.physics)
                && timing.equals(value.timing)
                && neighbors.equals(value.neighbors);
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
