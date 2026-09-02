package worldline.testapi;

import java.util.Objects;
import worldline.api.WorldlineBehavior;
import worldline.api.WorldlineRedstoneBehaviors;

/** Executes and validates the complete reusable Beta 1.7.3 repeater fixture. */
public final class RepeaterSubsystemFixture {
    private static final String DOMAINS = "93=0..15,94=0..15";
    private static final String MATERIALIZATION = "item356=93:0..15,signal=93>94:0..15";
    private static final String LIFECYCLE = "on=94:15->0:0+drop=356x1:0,"
            + "saved=93:0+94:15";
    private static final String PHYSICS = "collision=93:1/8+94:1/8,"
            + "light=93:0:0+94:0:9";
    private static final String TIMING = "random=FF,delays=2+4+6+8,"
            + "power=93>94,release=94>93,stable=93:15+94:15@20-window";
    private static final String NEIGHBORS = "signal=all-directions,"
            + "support=94:15->0:0+drop=356x1:0";

    private RepeaterSubsystemFixture() { }

    public static RepeaterSubsystemEvidence execute(RepeaterSubsystemScenario scenario) {
        RepeaterSubsystemObservation actual = Objects.requireNonNull(
                Objects.requireNonNull(scenario, "scenario").observe(), "observation");
        expect(DOMAINS, actual.domains(), "state domains");
        expect(MATERIALIZATION, actual.materialization(), "materialization");
        expect(LIFECYCLE, actual.lifecycle(), "lifecycle");
        expect(PHYSICS, actual.physics(), "physics");
        expect(TIMING, actual.timing(), "timing");
        expect(NEIGHBORS, actual.neighbors(), "neighbor response");
        if (WorldlineBehavior.require("repeater-subsystem")
                != WorldlineRedstoneBehaviors.REPEATER_SUBSYSTEM)
            throw new IllegalStateException("repeater-subsystem behavior registration drifted");
        return new RepeaterSubsystemEvidence(actual);
    }

    private static void expect(String expected, String actual, String label) {
        if (!expected.equals(actual))
            throw new IllegalStateException(label + " drifted: " + actual);
    }
}
