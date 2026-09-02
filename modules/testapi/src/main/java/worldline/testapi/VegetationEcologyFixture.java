package worldline.testapi;

import java.util.Objects;
import worldline.api.WorldlineBehavior;
import worldline.api.WorldlineWorldBehaviors;

/** Validates the reusable Beta 1.7.3 vegetation ecology matrix. */
public final class VegetationEcologyFixture {
    private static final String STATES =
            "2:0,6:0+1+2+8+9+10,18:0+1+2+4+5+6+8+9+10+12+13+14,31:0+1+2,59:0-7,83:0-15";
    private static final String SHAPES = "2+18:full,6+59+83:passable";
    private static final String LIGHT = "2:255/0,6+59+83:0/0";
    private static final String NEIGHBORS =
            "2:stable,6+59:support-drop,18:decay-mark";

    private VegetationEcologyFixture() { }

    public static VegetationEcologyEvidence execute(VegetationEcologyScenario scenario) {
        VegetationEcologyObservation actual = Objects.requireNonNull(
                Objects.requireNonNull(scenario, "scenario").observe(), "observation");
        expect(STATES, actual.states(), "state domains");
        expect(SHAPES, actual.shapes(), "collision shapes");
        expect(LIGHT, actual.light(), "light table");
        expect(NEIGHBORS, actual.neighbors(), "neighbor responses");
        if (WorldlineBehavior.require("vegetation-ecology-subsystem")
                != WorldlineWorldBehaviors.VEGETATION_ECOLOGY_SUBSYSTEM)
            throw new IllegalStateException("vegetation ecology registration drifted");
        return new VegetationEcologyEvidence(actual);
    }

    private static void expect(String expected, String actual, String label) {
        if (!expected.equals(actual))
            throw new IllegalStateException(label + " drifted: " + actual);
    }
}
