package worldline.testapi;

import java.util.Objects;
import worldline.api.WorldlineBehavior;
import worldline.api.WorldlineRedstoneBehaviors;

/** Executes and validates the complete reusable Beta 1.7.3 redstone torch fixture. */
public final class RedstoneTorchSubsystemFixture {
    private static final String DOMAINS = "75=1..5,76=1..5";
    private static final String MATERIALIZATION = "item76=76:1..5,signal=76>75:1..5";
    private static final String LIFECYCLE = "off=75:5->0:0+drop=76x1:0,saved=75:1+76:5";
    private static final String PHYSICS = "collision=75:none+76:none,light=75:0:0+76:0:7";
    private static final String TIMING = "random=TT,delay=2,invert=76>75>76,"
            + "burnout=8@100,recovery=101+2";
    private static final String NEIGHBORS = "faces=1..5,"
            + "support=75:5->0:0+drop=76x1:0";
    private RedstoneTorchSubsystemFixture() { }

    public static RedstoneTorchSubsystemEvidence execute(RedstoneTorchSubsystemScenario scenario) {
        RedstoneTorchSubsystemObservation actual = Objects.requireNonNull(
                Objects.requireNonNull(scenario, "scenario").observe(), "observation");
        expect(DOMAINS, actual.domains(), "state domains");
        expect(MATERIALIZATION, actual.materialization(), "materialization");
        expect(LIFECYCLE, actual.lifecycle(), "lifecycle");
        expect(PHYSICS, actual.physics(), "physics");
        expect(TIMING, actual.timing(), "timing");
        expect(NEIGHBORS, actual.neighbors(), "neighbor response");
        if (WorldlineBehavior.require("redstone-torch-subsystem")
                != WorldlineRedstoneBehaviors.REDSTONE_TORCH_SUBSYSTEM)
            throw new IllegalStateException("redstone-torch-subsystem registration drifted");
        return new RedstoneTorchSubsystemEvidence(actual);
    }
    private static void expect(String expected, String actual, String label) {
        if (!expected.equals(actual))
            throw new IllegalStateException(label + " drifted: " + actual);
    }
}
