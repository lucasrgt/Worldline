package worldline.testapi;

import java.util.Objects;
import worldline.api.WorldlineBehavior;
import worldline.api.WorldlineWorldBehaviors;

/** Validates the reusable Beta 1.7.3 built-environment material matrix. */
public final class BuiltEnvironmentMaterialsFixture {
    private static final String STATES = "1:0+17:0-2+20:0+44:0-3+85:0+89:0";
    private static final String SHAPES =
            "17+20+86+89+91:full,65:wall-2/16,67:two-box-stair";
    private static final String LIGHT =
            "1+17+44+53+67+86:255/0,65+85:0/0";
    private static final String TICKS =
            "86+91:on-load-stable,17+20+30+43+44+53+65+67+85+88+89:manual-stable";
    private static final String NEIGHBORS =
            "65:support-drop,17+20+30+43+44+53+67+85+86+88+89+91:stable";

    private BuiltEnvironmentMaterialsFixture() { }

    public static BuiltEnvironmentMaterialsEvidence execute(
            BuiltEnvironmentMaterialsScenario scenario) {
        BuiltEnvironmentMaterialsObservation actual = Objects.requireNonNull(
                Objects.requireNonNull(scenario, "scenario").observe(), "observation");
        expect(STATES, actual.states(), "state domains");
        expect(SHAPES, actual.shapes(), "collision shapes");
        expect(LIGHT, actual.light(), "light table");
        expect(TICKS, actual.ticks(), "tick policies");
        expect(NEIGHBORS, actual.neighbors(), "neighbor responses");
        if (WorldlineBehavior.require("built-environment-materials-subsystem")
                != WorldlineWorldBehaviors.BUILT_ENVIRONMENT_MATERIALS_SUBSYSTEM)
            throw new IllegalStateException("built-environment registration drifted");
        return new BuiltEnvironmentMaterialsEvidence(actual);
    }

    private static void expect(String expected, String actual, String label) {
        if (!expected.equals(actual))
            throw new IllegalStateException(label + " drifted: " + actual);
    }
}
