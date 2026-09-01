package worldline.testkit;

import java.util.Objects;
import worldline.api.WorldlineBehavior;
import worldline.api.WorldlineWorldBehaviors;

/** Executes and validates the reusable Beta 1.7.3 portal-block fixture. */
public final class PortalBlockSubsystemFixture {
    private static final String DOMAINS = "90=0,frames=X+Z,cells=6+6";
    private static final String LIFECYCLE = "break=90:0->0:0,drop=none";
    private static final String PERSISTENCE = "chunk-nbt=6x90:0";
    private static final String PHYSICS = "collision=none,light=0:11";
    private static final String TIMING = "scheduled=F,callback-stable=90:0,entities=0";
    private static final String NEIGHBORS = "frame-loss=6x90:0->air";
    private PortalBlockSubsystemFixture() { }
    public static PortalBlockSubsystemEvidence execute(PortalBlockSubsystemScenario scenario) {
        PortalBlockSubsystemObservation actual = Objects.requireNonNull(
                Objects.requireNonNull(scenario, "scenario").observe(), "observation");
        expect(DOMAINS, actual.domains(), "state domains");
        expect(LIFECYCLE, actual.lifecycle(), "lifecycle");
        expect(PERSISTENCE, actual.persistence(), "persistence");
        expect(PHYSICS, actual.physics(), "physics");
        expect(TIMING, actual.timing(), "timing");
        expect(NEIGHBORS, actual.neighbors(), "neighbor response");
        if (WorldlineBehavior.require("portal-block-subsystem")
                != WorldlineWorldBehaviors.PORTAL_BLOCK_SUBSYSTEM)
            throw new IllegalStateException("portal-block-subsystem registration drifted");
        return new PortalBlockSubsystemEvidence(actual);
    }
    private static void expect(String expected, String actual, String label) {
        if (!expected.equals(actual))
            throw new IllegalStateException(label + " drifted: " + actual);
    }
}
