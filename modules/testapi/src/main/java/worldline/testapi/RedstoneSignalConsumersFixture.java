package worldline.testapi;

import java.util.Objects;
import worldline.api.WorldlineBehavior;
import worldline.api.WorldlineWorldBehaviors;

/** Validates the reusable Beta 1.7.3 redstone signal-consumer matrix. */
public final class RedstoneSignalConsumersFixture {
    private static final String STATES =
            "46:0+1,55:0-15,84:0+1";
    private static final String SHAPES = "46:full,55:passable";
    private static final String LIGHT = "46:255/0,55:0/0";
    private static final String TICKS =
            "23:rate4+unpowered-stable,25+46+50+55+84:rate10+noop";
    private static final String NEIGHBORS =
            "23:powered-schedule,25:rising-edge,46:powered-prime,55:support-loss,84:noop";

    private RedstoneSignalConsumersFixture() { }

    public static RedstoneSignalConsumersEvidence execute(RedstoneSignalConsumersScenario scenario) {
        RedstoneSignalConsumersObservation actual = Objects.requireNonNull(
                Objects.requireNonNull(scenario, "scenario").observe(), "observation");
        expect(STATES, actual.states(), "state domains");
        expect(SHAPES, actual.shapes(), "collision shapes");
        expect(LIGHT, actual.light(), "light table");
        expect(TICKS, actual.ticks(), "tick behavior");
        expect(NEIGHBORS, actual.neighbors(), "neighbor responses");
        if (WorldlineBehavior.require("redstone-signal-consumers-subsystem")
                != WorldlineWorldBehaviors.REDSTONE_SIGNAL_CONSUMERS_SUBSYSTEM)
            throw new IllegalStateException("redstone signal-consumer registration drifted");
        return new RedstoneSignalConsumersEvidence(actual);
    }

    private static void expect(String expected, String actual, String label) {
        if (!expected.equals(actual))
            throw new IllegalStateException(label + " drifted: " + actual);
    }
}
