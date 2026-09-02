package worldline.testapi;

import java.util.Objects;
import worldline.api.WorldlineBehavior;
import worldline.api.WorldlineWorldBehaviors;

/** Executes and validates the reusable Beta 1.7.3 iron-door fixture. */
public final class IronDoorSubsystemFixture {
    private static final String DOMAINS = "71=lower:0..7,upper:8..15,open-bit=4";
    private static final String LIFECYCLE =
            "break=lower+upper->air,drops=lower:330x1+upper:none,strength=finite";
    private static final String PHYSICS =
            "collision=closed-x-3/16+open-z-3/16,opaque=F,cube=F,light=0:0";
    private static final String TIMING = "scheduled=F,callback-stable=71:0+71:8";
    private static final String NEIGHBORS =
            "paired=stable,orphan-lower=air+330x1,orphan-upper=air+none,"
                    + "support-loss=both-air+330x1";
    private IronDoorSubsystemFixture() { }
    public static IronDoorSubsystemEvidence execute(IronDoorSubsystemScenario scenario) {
        IronDoorSubsystemObservation actual = Objects.requireNonNull(
                Objects.requireNonNull(scenario, "scenario").observe(), "observation");
        expect(DOMAINS, actual.domains(), "state domains");
        expect(LIFECYCLE, actual.lifecycle(), "lifecycle");
        expect(PHYSICS, actual.physics(), "physics");
        expect(TIMING, actual.timing(), "timing");
        expect(NEIGHBORS, actual.neighbors(), "neighbor response");
        if (WorldlineBehavior.require("iron-door-subsystem")
                != WorldlineWorldBehaviors.IRON_DOOR_SUBSYSTEM)
            throw new IllegalStateException("iron-door-subsystem registration drifted");
        return new IronDoorSubsystemEvidence(actual);
    }
    private static void expect(String expected, String actual, String label) {
        if (!expected.equals(actual))
            throw new IllegalStateException(label + " drifted: " + actual);
    }
}
