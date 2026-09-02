package worldline.testkit;
import worldline.testapi.ConformanceLayer;
import worldline.testapi.EntityConformancePlan;
import worldline.testapi.EntityConformanceProfile;
import worldline.testapi.EntityConformanceTemplate;
import worldline.testapi.FallingSandLifecycleEvidence;
import worldline.testapi.FallingSandLifecycleFixture;
import worldline.testapi.FallingSandLifecycleObservation;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteObjectSpawn;

/** Proves the exact unsupported-sand Packet23, landing and reload boundary. */
public final class FallingSandLifecycleFixtureTest {
    private FallingSandLifecycleFixtureTest() { }

    public static void main(String[] arguments) {
        execute();
        System.out.println("FallingSandLifecycleFixtureTest passed");
    }

    static void execute() {
        FallingSandLifecycleEvidence first = FallingSandLifecycleFixture.execute(plan(),
                () -> observation(11, 10, new BlockPosition(4, 64, 4), 70, 40, 40));
        FallingSandLifecycleEvidence second = FallingSandLifecycleFixture.execute(plan(),
                () -> observation(101, 100, new BlockPosition(-20, 88, 31), 70, 40, 40));
        require(first.claims().size() == 2
                && first.claims().get(0).layer() == ConformanceLayer.UNIVERSAL
                && first.claims().get(1).layer() == ConformanceLayer.SINGULAR,
                "falling sand claim routing drifted");
        require(first.equals(second) && first.hashCode() == second.hashCode(),
                "falling sand runtime identity or coordinates leaked into evidence");
        reject(() -> FallingSandLifecycleFixture.execute(plan(),
                () -> observation(11, 10, new BlockPosition(4, 64, 4), 71, 40, 40)));
        reject(() -> FallingSandLifecycleFixture.execute(plan(),
                () -> invalidUpper(new BlockPosition(4, 64, 4))));
        reject(() -> FallingSandLifecycleFixture.execute(plan(),
                () -> invalidLanding(new BlockPosition(4, 64, 4))));
        reject(() -> FallingSandLifecycleFixture.execute(plan(),
                () -> observation(11, 10, new BlockPosition(4, 64, 4), 70, 39, 40)));
        reject(() -> FallingSandLifecycleFixture.execute(plan(ConformanceLayer.ARCHETYPE),
                () -> observation(11, 10, new BlockPosition(4, 64, 4), 70, 40, 40)));
    }

    private static FallingSandLifecycleObservation observation(int entity, int actor,
            BlockPosition lower, int type, int fixtureTicks, int gravityTicks) {
        BlockPosition upper = new BlockPosition(lower.x(), lower.y() + 1, lower.z());
        return new FallingSandLifecycleObservation(spawn(entity, type), actor, lower, upper,
                state(1), state(12), state(0), state(12), state(0), state(12), state(0),
                fixtureTicks, gravityTicks);
    }

    private static FallingSandLifecycleObservation invalidUpper(BlockPosition lower) {
        return new FallingSandLifecycleObservation(spawn(11, 70), 10, lower,
                new BlockPosition(lower.x() + 1, lower.y() + 1, lower.z()), state(1),
                state(12), state(0), state(12), state(0), state(12), state(0), 40, 40);
    }

    private static FallingSandLifecycleObservation invalidLanding(BlockPosition lower) {
        BlockPosition upper = new BlockPosition(lower.x(), lower.y() + 1, lower.z());
        return new FallingSandLifecycleObservation(spawn(11, 70), 10, lower, upper,
                state(1), state(12), state(0), state(13), state(0), state(12), state(0), 40, 40);
    }

    private static BlockState state(int id) { return new BlockState(id, 0); }
    private static RemoteObjectSpawn spawn(int entity, int type) {
        return new RemoteObjectSpawn(entity, type, 32, 2048, 32, 0, 0, 0, 0);
    }

    private static EntityConformancePlan plan() { return plan(ConformanceLayer.SINGULAR); }
    private static EntityConformancePlan plan(ConformanceLayer lifecycle) {
        Map<String, ConformanceLayer> overrides = new LinkedHashMap<String, ConformanceLayer>();
        overrides.put("spawn-materialization", ConformanceLayer.UNIVERSAL);
        overrides.put("tick-lifecycle", lifecycle);
        EntityConformanceProfile sand = new EntityConformanceProfile(SUBJECT,
                Collections.singletonList("gravity-block"), true, overrides);
        return new EntityConformancePlan(Collections.singletonList(sand), Arrays.asList(
                new EntityConformanceTemplate("spawn-materialization", ConformanceLayer.UNIVERSAL),
                new EntityConformanceTemplate("tick-lifecycle", ConformanceLayer.ARCHETYPE)));
    }

    private static final String SUBJECT = "b1.7.3:entity/021";

    private static void reject(Runnable action) {
        try { action.run(); throw new AssertionError("invalid falling sand lifecycle accepted"); }
        catch (IllegalArgumentException | IllegalStateException expected) { }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
