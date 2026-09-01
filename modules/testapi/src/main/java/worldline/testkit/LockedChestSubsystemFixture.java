package worldline.testkit;

import java.util.Objects;
import worldline.api.WorldlineBehavior;
import worldline.api.WorldlineWorldBehaviors;

/** Executes and validates the reusable Beta 1.7.3 locked-chest fixture. */
public final class LockedChestSubsystemFixture {
    private static final String DOMAINS = "95=0,item-route=95x1->0,placed=95:0";
    private static final String LIFECYCLE =
            "break=95:0->0:0,strength=infinite,drop=95x1";
    private static final String PERSISTENCE = "chunk-nbt=95:0";
    private static final String PHYSICS = "collision=full,light=255:15";
    private static final String TIMING = "random-enrolled=T,callback=95:0->0:0";
    private static final String NEIGHBORS = "stone+lever=stable-95:0";
    private LockedChestSubsystemFixture() { }
    public static LockedChestSubsystemEvidence execute(LockedChestSubsystemScenario scenario) {
        LockedChestSubsystemObservation actual = Objects.requireNonNull(
                Objects.requireNonNull(scenario, "scenario").observe(), "observation");
        expect(DOMAINS, actual.domains(), "state domains and placement");
        expect(LIFECYCLE, actual.lifecycle(), "lifecycle");
        expect(PERSISTENCE, actual.persistence(), "persistence");
        expect(PHYSICS, actual.physics(), "physics");
        expect(TIMING, actual.timing(), "timing");
        expect(NEIGHBORS, actual.neighbors(), "neighbor response");
        if (WorldlineBehavior.require("locked-chest-subsystem")
                != WorldlineWorldBehaviors.LOCKED_CHEST_SUBSYSTEM)
            throw new IllegalStateException("locked-chest-subsystem registration drifted");
        return new LockedChestSubsystemEvidence(actual);
    }
    private static void expect(String expected, String actual, String label) {
        if (!expected.equals(actual))
            throw new IllegalStateException(label + " drifted: " + actual);
    }
}
