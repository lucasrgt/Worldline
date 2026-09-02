package worldline.testapi;

import java.util.Arrays;
import java.util.List;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteObjectSpawn;

/** Executes Packet23 materialization, landing and fresh-login persistence as one sand lifecycle. */
public final class FallingSandLifecycleFixture {
    private static final String SUBJECT = "b1.7.3:entity/021";
    private static final BlockState AIR = new BlockState(0, 0);
    private static final BlockState STONE = new BlockState(1, 0);
    private static final BlockState SAND = new BlockState(12, 0);

    private FallingSandLifecycleFixture() { }

    public static FallingSandLifecycleEvidence execute(EntityConformancePlan plan,
            FallingSandLifecycleScenario scenario) {
        if (plan == null || scenario == null) throw new NullPointerException("falling sand lifecycle");
        FallingSandLifecycleObservation value = scenario.observe();
        if (value == null || value.actorEntityId() <= 0) {
            throw new IllegalStateException("falling sand lifecycle scene absent");
        }
        ObjectMaterializationEvidence spawn = ObjectMaterializationFixture.execute(plan,
                SUBJECT, ObjectSpawnExpectation.typeOnly(70), ignored -> value.falling());
        EntityConformanceCase lifecycle = plan.caseFor(SUBJECT, "tick-lifecycle");
        if (lifecycle.layer() != ConformanceLayer.SINGULAR) {
            throw new IllegalArgumentException("falling sand lifecycle must be singular");
        }
        RemoteObjectSpawn falling = value.falling();
        BlockPosition lower = value.lower(), upper = value.upper();
        if (falling.entityId() == value.actorEntityId() || lower == null || upper == null
                || lower.x() != upper.x() || lower.z() != upper.z()
                || lower.y() + 1 != upper.y() || !STONE.equals(value.stableLower())
                || !SAND.equals(value.stableUpper()) || !AIR.equals(value.openedLower())
                || !SAND.equals(value.landedLower()) || !AIR.equals(value.clearedUpper())
                || !SAND.equals(value.persistedLower()) || !AIR.equals(value.persistedUpper())
                || value.fixtureTicks() != 40 || value.gravityTicks() != 40) {
            throw new IllegalStateException("falling sand landing or reload boundary drifted");
        }
        List<EntityConformanceCase> claims = Arrays.asList(spawn.claim(), lifecycle);
        return new FallingSandLifecycleEvidence(claims, spawn, value);
    }
}
