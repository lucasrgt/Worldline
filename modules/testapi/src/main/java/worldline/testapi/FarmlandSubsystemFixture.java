package worldline.testapi;

import java.util.Objects;
import worldline.api.WorldlineBehavior;
import worldline.api.WorldlineWorldBehaviors;

/** Executes and validates the reusable Beta 1.7.3 farmland fixture. */
public final class FarmlandSubsystemFixture {
    private static final String DOMAINS = "60=0..7,item-route=60x1->0,placed=60:0";
    private static final String LIFECYCLE =
            "break=60:0->0:0,strength=finite,drop=3x1";
    private static final String PERSISTENCE = "chunk-nbt=60:7";
    private static final String PHYSICS =
            "collision=full,visual-height=15/16,opaque=F,cube=F,light=255:0";
    private static final String TIMING = "random-enrolled=T,hydration=0->7,dry=7->0";
    private static final String NEIGHBORS =
            "air-above=stable-60:0,solid-cover=60:0->3:0";
    private FarmlandSubsystemFixture() { }
    public static FarmlandSubsystemEvidence execute(FarmlandSubsystemScenario scenario) {
        FarmlandSubsystemObservation actual = Objects.requireNonNull(
                Objects.requireNonNull(scenario, "scenario").observe(), "observation");
        expect(DOMAINS, actual.domains(), "state domains and placement");
        expect(LIFECYCLE, actual.lifecycle(), "lifecycle");
        expect(PERSISTENCE, actual.persistence(), "persistence");
        expect(PHYSICS, actual.physics(), "physics");
        expect(TIMING, actual.timing(), "timing");
        expect(NEIGHBORS, actual.neighbors(), "neighbor response");
        if (WorldlineBehavior.require("farmland-subsystem") != WorldlineWorldBehaviors.FARMLAND_SUBSYSTEM)
            throw new IllegalStateException("farmland-subsystem registration drifted");
        return new FarmlandSubsystemEvidence(actual);
    }
    private static void expect(String expected, String actual, String label) {
        if (!expected.equals(actual))
            throw new IllegalStateException(label + " drifted: " + actual);
    }
}
