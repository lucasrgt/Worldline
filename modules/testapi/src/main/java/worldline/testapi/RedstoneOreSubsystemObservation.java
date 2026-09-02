package worldline.testapi;

import java.util.Objects;

/** Structured observations spanning both redstone-ore block states. */
public final class RedstoneOreSubsystemObservation {
    private final String registry, domains, lifecycle, physics, timing, neighbors;

    public RedstoneOreSubsystemObservation(String registry, String domains, String lifecycle,
            String physics, String timing, String neighbors) {
        this.registry = required(registry, "registry");
        this.domains = required(domains, "domains");
        this.lifecycle = required(lifecycle, "lifecycle");
        this.physics = required(physics, "physics");
        this.timing = required(timing, "timing");
        this.neighbors = required(neighbors, "neighbors");
    }
    public String registry() { return registry; }
    public String domains() { return domains; }
    public String lifecycle() { return lifecycle; }
    public String physics() { return physics; }
    public String timing() { return timing; }
    public String neighbors() { return neighbors; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof RedstoneOreSubsystemObservation)) return false;
        RedstoneOreSubsystemObservation value = (RedstoneOreSubsystemObservation) other;
        return registry.equals(value.registry) && domains.equals(value.domains)
                && lifecycle.equals(value.lifecycle) && physics.equals(value.physics)
                && timing.equals(value.timing) && neighbors.equals(value.neighbors);
    }
    @Override public int hashCode() {
        return Objects.hash(registry, domains, lifecycle, physics, timing, neighbors);
    }
    private static String required(String value, String label) {
        if (value == null || value.trim().isEmpty())
            throw new IllegalArgumentException(label + " must not be blank");
        return value.trim();
    }
}
