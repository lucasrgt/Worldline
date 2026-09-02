package worldline.testapi;

import java.util.Objects;
import worldline.api.WorldlineBehavior;
import worldline.api.WorldlineWorldBehaviors;

/** Validates the reusable chest, wooden-door, and trapdoor conformance matrix. */
public final class OpenableAccessStructuresFixture {
    private static final String CHEST =
            "54:scheduled=F+callback-stable+neighbor-stable";
    private static final String WOODEN_DOOR =
            "64:collision=closed-x-3/16+open-z-3/16,light=0:0,"
                    + "scheduled=F+callback-stable";
    private static final String TRAPDOOR =
            "96:meta=0..7,collision=closed-floor-3/16+open-four-faces,light=0:0,"
                    + "scheduled=F+callback-stable,neighbor=support-stable+support-loss-air+96x1";

    private OpenableAccessStructuresFixture() { }

    public static OpenableAccessStructuresEvidence execute(
            OpenableAccessStructuresScenario scenario) {
        OpenableAccessStructuresObservation actual = Objects.requireNonNull(
                Objects.requireNonNull(scenario, "scenario").observe(), "observation");
        expect(CHEST, actual.chest(), "chest boundary");
        expect(WOODEN_DOOR, actual.woodenDoor(), "wooden-door boundary");
        expect(TRAPDOOR, actual.trapdoor(), "trapdoor boundary");
        if (WorldlineBehavior.require("openable-access-structures-subsystem")
                != WorldlineWorldBehaviors.OPENABLE_ACCESS_STRUCTURES_SUBSYSTEM)
            throw new IllegalStateException("openable access-structure registration drifted");
        return new OpenableAccessStructuresEvidence(actual);
    }

    private static void expect(String expected, String actual, String label) {
        if (!expected.equals(actual))
            throw new IllegalStateException(label + " drifted: " + actual);
    }
}
