package worldline.testkit;

import java.util.Objects;

/** Canonical observation of the Beta 1.7.3 built-environment material matrix. */
public final class BuiltEnvironmentMaterialsObservation {
    private final String states, shapes, light, ticks, neighbors;

    public BuiltEnvironmentMaterialsObservation(String states, String shapes,
            String light, String ticks, String neighbors) {
        this.states = Objects.requireNonNull(states, "states");
        this.shapes = Objects.requireNonNull(shapes, "shapes");
        this.light = Objects.requireNonNull(light, "light");
        this.ticks = Objects.requireNonNull(ticks, "ticks");
        this.neighbors = Objects.requireNonNull(neighbors, "neighbors");
    }

    public String states() { return states; }
    public String shapes() { return shapes; }
    public String light() { return light; }
    public String ticks() { return ticks; }
    public String neighbors() { return neighbors; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof BuiltEnvironmentMaterialsObservation))
            return false;
        BuiltEnvironmentMaterialsObservation value = (BuiltEnvironmentMaterialsObservation) other;
        return states.equals(value.states) && shapes.equals(value.shapes)
                && light.equals(value.light) && ticks.equals(value.ticks)
                && neighbors.equals(value.neighbors);
    }

    @Override public int hashCode() {
        return Objects.hash(states, shapes, light, ticks, neighbors);
    }
}
