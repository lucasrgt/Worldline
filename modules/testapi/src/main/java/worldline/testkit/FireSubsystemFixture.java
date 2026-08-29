package worldline.testkit;

import java.util.Objects;
import worldline.api.WorldlineBehavior;
import worldline.api.WorldlineWorldBehaviors;

/** Executes and validates the reusable Beta 1.7.3 fire fixture. */
public final class FireSubsystemFixture {
    private static final String DOMAINS = "51=0..15,item-route=51x1->0,placed=51:0";
    private static final String LIFECYCLE =
            "break=51:0->0:0,strength=infinite,drop=none";
    private static final String PERSISTENCE = "chunk-nbt=51:15";
    private static final String PHYSICS = "collision=none,collidable=F,light=0:15";
    private static final String TIMING = "random-enrolled=T,age=0->15,tick-rate=40";
    private static final String NEIGHBORS =
            "supported=stable-51:0,support-loss=51:0->0:0";
    private FireSubsystemFixture() { }
    public static FireSubsystemEvidence execute(FireSubsystemScenario scenario) {
        FireSubsystemObservation actual = Objects.requireNonNull(
                Objects.requireNonNull(scenario, "scenario").observe(), "observation");
        expect(DOMAINS, actual.domains(), "state domains and placement");
        expect(LIFECYCLE, actual.lifecycle(), "lifecycle");
        expect(PERSISTENCE, actual.persistence(), "persistence");
        expect(PHYSICS, actual.physics(), "physics");
        expect(TIMING, actual.timing(), "timing");
        expect(NEIGHBORS, actual.neighbors(), "neighbor response");
        if (WorldlineBehavior.require("fire-subsystem") != WorldlineWorldBehaviors.FIRE_SUBSYSTEM)
            throw new IllegalStateException("fire-subsystem registration drifted");
        return new FireSubsystemEvidence(actual);
    }
    private static void expect(String expected, String actual, String label) {
        if (!expected.equals(actual))
            throw new IllegalStateException(label + " drifted: " + actual);
    }
}
