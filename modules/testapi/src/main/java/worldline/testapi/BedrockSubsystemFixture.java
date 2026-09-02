package worldline.testapi;

import java.util.Objects;
import worldline.api.WorldlineBehavior;
import worldline.api.WorldlineWorldBehaviors;

/** Executes and validates the reusable Beta 1.7.3 bedrock fixture. */
public final class BedrockSubsystemFixture {
    private static final String DOMAINS = "7=0,item-route=7x1->0,placed=7:0";
    private static final String LIFECYCLE = "break-attempt=7:0->7:0,strength=0,drop=none";
    private static final String PERSISTENCE = "chunk-nbt=7:0";
    private static final String PHYSICS = "collision=full,light=255:0";
    private static final String TIMING = "scheduled=F,callback-stable=7:0";
    private static final String NEIGHBORS = "stone+lever=stable-7:0";
    private BedrockSubsystemFixture() { }
    public static BedrockSubsystemEvidence execute(BedrockSubsystemScenario scenario) {
        BedrockSubsystemObservation actual = Objects.requireNonNull(
                Objects.requireNonNull(scenario, "scenario").observe(), "observation");
        expect(DOMAINS, actual.domains(), "state domains and placement");
        expect(LIFECYCLE, actual.lifecycle(), "lifecycle");
        expect(PERSISTENCE, actual.persistence(), "persistence");
        expect(PHYSICS, actual.physics(), "physics");
        expect(TIMING, actual.timing(), "timing");
        expect(NEIGHBORS, actual.neighbors(), "neighbor response");
        if (WorldlineBehavior.require("bedrock-subsystem")
                != WorldlineWorldBehaviors.BEDROCK_SUBSYSTEM)
            throw new IllegalStateException("bedrock-subsystem registration drifted");
        return new BedrockSubsystemEvidence(actual);
    }
    private static void expect(String expected, String actual, String label) {
        if (!expected.equals(actual))
            throw new IllegalStateException(label + " drifted: " + actual);
    }
}
