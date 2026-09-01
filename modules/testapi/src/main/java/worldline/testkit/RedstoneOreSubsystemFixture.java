package worldline.testkit;

import java.util.Objects;
import worldline.api.WorldlineBehavior;
import worldline.api.WorldlineRedstoneBehaviors;

/** Executes and validates the complete reusable Beta 1.7.3 redstone-ore fixture. */
public final class RedstoneOreSubsystemFixture {
    private static final String REGISTRY = "73+74=same-BlockRedstoneOre";
    private static final String DOMAINS = "73=0,74=0,activate=73:0->74:0";
    private static final String LIFECYCLE =
            "break=74:0->0:0,drop=331x4..5:0,saved=73:0+74:0";
    private static final String PHYSICS =
            "collision=73:full+74:full,light=73:255:0+74:255:9";
    private static final String TIMING = "random=FT,activate=click,fade=74:0->73:0";
    private static final String NEIGHBORS = "73:0+74:0=stable@1+69";
    private RedstoneOreSubsystemFixture() { }

    public static RedstoneOreSubsystemEvidence execute(RedstoneOreSubsystemScenario scenario) {
        RedstoneOreSubsystemObservation actual = Objects.requireNonNull(
                Objects.requireNonNull(scenario, "scenario").observe(), "observation");
        expect(REGISTRY, actual.registry(), "registry");
        expect(DOMAINS, actual.domains(), "state domains");
        expect(LIFECYCLE, actual.lifecycle(), "lifecycle");
        expect(PHYSICS, actual.physics(), "physics");
        expect(TIMING, actual.timing(), "timing");
        expect(NEIGHBORS, actual.neighbors(), "neighbor response");
        if (WorldlineBehavior.require("redstone-ore-subsystem")
                != WorldlineRedstoneBehaviors.REDSTONE_ORE_SUBSYSTEM)
            throw new IllegalStateException("redstone-ore-subsystem registration drifted");
        return new RedstoneOreSubsystemEvidence(actual);
    }
    private static void expect(String expected, String actual, String label) {
        if (!expected.equals(actual))
            throw new IllegalStateException(label + " drifted: " + actual);
    }
}
