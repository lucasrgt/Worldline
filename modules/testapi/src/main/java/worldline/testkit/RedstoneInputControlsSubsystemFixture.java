package worldline.testkit;

import java.util.Objects;
import worldline.api.WorldlineBehavior;
import worldline.api.WorldlineWorldBehaviors;

/** Validates the reusable Beta 1.7.3 redstone input-control fixture. */
public final class RedstoneInputControlsSubsystemFixture {
    private static final String LEVER =
            "state=5>13>5,bounds=40.0.40.120.96.120,collision=none,light=0/0,tick=latch";
    private static final String BUTTON =
            "state=1>9>1,bounds=0.60.50.20.100.110>0.60.50.10.100.110,"
                    + "collision=none,light=0/0,tick=20";
    private static final String STONE_PLATE =
            "state=0>1>0,item=ignored,bounds=10.0.10.150.10.150>10.0.10.150.5.150,"
                    + "collision=none,light=0/0,tick=20";
    private static final String WOODEN_PLATE =
            "state=0>1>0,item=accepted,bounds=10.0.10.150.10.150>10.0.10.150.5.150,"
                    + "collision=none,light=0/0,tick=20";
    private static final String SUPPORT = "69+70+72+77=air+single-drop";

    private RedstoneInputControlsSubsystemFixture() { }

    public static RedstoneInputControlsSubsystemEvidence execute(
            RedstoneInputControlsSubsystemScenario scenario) {
        RedstoneInputControlsSubsystemObservation actual = Objects.requireNonNull(
                Objects.requireNonNull(scenario, "scenario").observe(), "observation");
        expect(LEVER, actual.lever(), "lever");
        expect(BUTTON, actual.button(), "button");
        expect(STONE_PLATE, actual.stonePlate(), "stone pressure plate");
        expect(WOODEN_PLATE, actual.woodenPlate(), "wooden pressure plate");
        expect(SUPPORT, actual.support(), "support response");
        if (WorldlineBehavior.require("redstone-input-controls-subsystem")
                != WorldlineWorldBehaviors.REDSTONE_INPUT_CONTROLS_SUBSYSTEM)
            throw new IllegalStateException("redstone-input-controls registration drifted");
        return new RedstoneInputControlsSubsystemEvidence(actual);
    }

    private static void expect(String expected, String actual, String label) {
        if (!expected.equals(actual))
            throw new IllegalStateException(label + " drifted: " + actual);
    }
}
