package worldline.testkit;

import java.util.Objects;

/** Canonical observation of the Beta 1.7.3 vegetation ecology matrix. */
public final class VegetationEcologyObservation {
    private final String states, shapes, light, neighbors;

    public VegetationEcologyObservation(String states, String shapes,
            String light, String neighbors) {
        this.states = Objects.requireNonNull(states, "states");
        this.shapes = Objects.requireNonNull(shapes, "shapes");
        this.light = Objects.requireNonNull(light, "light");
        this.neighbors = Objects.requireNonNull(neighbors, "neighbors");
    }

    public String states() { return states; }
    public String shapes() { return shapes; }
    public String light() { return light; }
    public String neighbors() { return neighbors; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof VegetationEcologyObservation))
            return false;
        VegetationEcologyObservation value = (VegetationEcologyObservation) other;
        return states.equals(value.states) && shapes.equals(value.shapes)
                && light.equals(value.light) && neighbors.equals(value.neighbors);
    }

    @Override public int hashCode() {
        return Objects.hash(states, shapes, light, neighbors);
    }
}
