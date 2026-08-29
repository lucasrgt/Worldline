package worldline.testkit;

import java.util.Objects;
import worldline.api.WorldlineBehavior;
import worldline.api.WorldlineWorldBehaviors;

/** Executes and validates the complete reusable Beta 1.7.3 furnace fixture. */
public final class FurnaceSubsystemFixture {
    private static final String DOMAINS = "61=2..5,62=2..5";
    private static final String MATERIALIZATION = "item61=61:2..5,smelt=61:4>62:4>61:3";
    private static final String LIFECYCLE =
            "active=62:4->0:0,drops=61+12+263+20,saved=62:5+burn777+cook88";
    private static final String PHYSICS =
            "collision=61:full+62:full,light=61:255:0+62:255:13";
    private static final String TIMING = "random=FF,tile=burn1600+cook200,output=20x1:0";
    private static final String NEIGHBORS = "stable=61:3+62:2,orientation=2..5";
    private FurnaceSubsystemFixture() {
    }

    public static FurnaceSubsystemEvidence execute(FurnaceSubsystemScenario scenario) {
        FurnaceSubsystemObservation actual = Objects.requireNonNull(
                Objects.requireNonNull(scenario, "scenario").observe(), "observation");
        expect(DOMAINS, actual.domains(), "state domains");
        expect(MATERIALIZATION, actual.materialization(), "materialization");
        expect(LIFECYCLE, actual.lifecycle(), "lifecycle");
        expect(PHYSICS, actual.physics(), "physics");
        expect(TIMING, actual.timing(), "timing");
        expect(NEIGHBORS, actual.neighbors(), "neighbor response");
        if (WorldlineBehavior.require("furnace-subsystem")
                != WorldlineWorldBehaviors.FURNACE_SUBSYSTEM)
            throw new IllegalStateException("furnace-subsystem registration drifted");
        return new FurnaceSubsystemEvidence(actual);
    }
    private static void expect(String expected, String actual, String label) {
        if (!expected.equals(actual))
            throw new IllegalStateException(label + " drifted: " + actual);
    }
}
