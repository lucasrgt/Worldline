package worldline.testkit;

import java.util.Objects;

/** Structured observations spanning piston state, physics, lifecycle, and persistence. */
public final class PistonSubsystemObservation {
    private final String domains;
    private final String materialization;
    private final String breakAndDrops;
    private final String persistence;
    private final String collision;
    private final String light;
    private final String ticks;
    private final String neighbors;

    public PistonSubsystemObservation(String domains, String materialization,
            String breakAndDrops, String persistence, String collision, String light,
            String ticks, String neighbors) {
        this.domains = required(domains, "domains");
        this.materialization = required(materialization, "materialization");
        this.breakAndDrops = required(breakAndDrops, "breakAndDrops");
        this.persistence = required(persistence, "persistence");
        this.collision = required(collision, "collision");
        this.light = required(light, "light");
        this.ticks = required(ticks, "ticks");
        this.neighbors = required(neighbors, "neighbors");
    }

    public String domains() { return domains; }
    public String materialization() { return materialization; }
    public String breakAndDrops() { return breakAndDrops; }
    public String persistence() { return persistence; }
    public String collision() { return collision; }
    public String light() { return light; }
    public String ticks() { return ticks; }
    public String neighbors() { return neighbors; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof PistonSubsystemObservation)) return false;
        PistonSubsystemObservation value = (PistonSubsystemObservation) other;
        return domains.equals(value.domains)
                && materialization.equals(value.materialization)
                && breakAndDrops.equals(value.breakAndDrops)
                && persistence.equals(value.persistence)
                && collision.equals(value.collision)
                && light.equals(value.light)
                && ticks.equals(value.ticks)
                && neighbors.equals(value.neighbors);
    }

    @Override public int hashCode() {
        return Objects.hash(domains, materialization, breakAndDrops, persistence,
                collision, light, ticks, neighbors);
    }

    private static String required(String value, String label) {
        if (value == null || value.trim().isEmpty())
            throw new IllegalArgumentException(label + " must not be blank");
        return value.trim();
    }
}
