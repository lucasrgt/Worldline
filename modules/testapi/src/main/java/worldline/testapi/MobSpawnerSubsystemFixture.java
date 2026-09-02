package worldline.testapi;

import java.util.Objects;
import worldline.api.WorldlineBehavior;
import worldline.api.WorldlineWorldBehaviors;

/** Executes and validates the reusable Beta 1.7.3 mob-spawner fixture. */
public final class MobSpawnerSubsystemFixture {
    private static final String REGISTRY =
            "block=52:BlockMobSpawner,item=52:ItemBlock,tile=TileEntityMobSpawner";
    private static final String PLACEMENT = "item=52x1->0,placed=52:0,tile=Pig:20";
    private static final String LIFECYCLE =
            "break=52:0->0:0,strength=finite,drops=none";
    private static final String PERSISTENCE = "chunk-nbt=52:0+Zombie:37";
    private static final String TIMING = "scheduled=F,out-of-range=20,near-player=19";
    private static final String NEIGHBORS = "stone+lever=stable-52:0+Pig:20";
    private MobSpawnerSubsystemFixture() { }
    public static MobSpawnerSubsystemEvidence execute(MobSpawnerSubsystemScenario scenario) {
        MobSpawnerSubsystemObservation actual = Objects.requireNonNull(
                Objects.requireNonNull(scenario, "scenario").observe(), "observation");
        expect(REGISTRY, actual.registry(), "registry");
        expect(PLACEMENT, actual.placement(), "placement");
        expect(LIFECYCLE, actual.lifecycle(), "lifecycle");
        expect(PERSISTENCE, actual.persistence(), "persistence");
        expect(TIMING, actual.timing(), "timing");
        expect(NEIGHBORS, actual.neighbors(), "neighbor response");
        if (WorldlineBehavior.require("mob-spawner-subsystem")
                != WorldlineWorldBehaviors.MOB_SPAWNER_SUBSYSTEM)
            throw new IllegalStateException("mob-spawner-subsystem registration drifted");
        return new MobSpawnerSubsystemEvidence(actual);
    }
    private static void expect(String expected, String actual, String label) {
        if (!expected.equals(actual))
            throw new IllegalStateException(label + " drifted: " + actual);
    }
}
