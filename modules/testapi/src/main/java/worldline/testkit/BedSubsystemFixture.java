package worldline.testkit;

import java.util.Objects;
import worldline.api.WorldlineBehavior;
import worldline.api.WorldlineWorldBehaviors;

/** Executes and validates the reusable Beta 1.7.3 bed fixture. */
public final class BedSubsystemFixture {
    private static final String DOMAINS = "26=foot:0..3,head:8..15,occupied-head:12..15";
    private static final String LIFECYCLE =
            "break=foot+head->air,drops=foot:355x1+head:none,strength=finite";
    private static final String PHYSICS =
            "collision=1x9/16x1,opaque=F,cube=F,light=0:0";
    private static final String TIMING = "scheduled=F,callback-stable=26:0+26:8";
    private static final String NEIGHBORS =
            "paired=stable,orphan-foot=air+355x1,orphan-head=air+none";
    private BedSubsystemFixture() { }
    public static BedSubsystemEvidence execute(BedSubsystemScenario scenario) {
        BedSubsystemObservation actual = Objects.requireNonNull(
                Objects.requireNonNull(scenario, "scenario").observe(), "observation");
        expect(DOMAINS, actual.domains(), "state domains");
        expect(LIFECYCLE, actual.lifecycle(), "lifecycle");
        expect(PHYSICS, actual.physics(), "physics");
        expect(TIMING, actual.timing(), "timing");
        expect(NEIGHBORS, actual.neighbors(), "neighbor response");
        if (WorldlineBehavior.require("bed-subsystem")
                != WorldlineWorldBehaviors.BED_SUBSYSTEM)
            throw new IllegalStateException("bed-subsystem registration drifted");
        return new BedSubsystemEvidence(actual);
    }
    private static void expect(String expected, String actual, String label) {
        if (!expected.equals(actual))
            throw new IllegalStateException(label + " drifted: " + actual);
    }
}
