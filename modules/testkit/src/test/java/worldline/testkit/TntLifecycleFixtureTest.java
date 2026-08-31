package worldline.testkit;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import worldline.api.RemoteObjectSpawn;

/** Proves primed TNT Packet23 identity and exact isolated internal fuse checkpoints. */
public final class TntLifecycleFixtureTest {
    private static final String SUBJECT = "b1.7.3:entity/020";

    private TntLifecycleFixtureTest() { }

    public static void main(String[] arguments) {
        execute();
        System.out.println("TntLifecycleFixtureTest passed");
    }

    static void execute() {
        TntLifecycleEvidence first = TntLifecycleFixture.execute(plan(),
                () -> observation(11, 10, 80, 79, 40, 1, 0, -1, true, false));
        TntLifecycleEvidence second = TntLifecycleFixture.execute(plan(),
                () -> observation(101, 100, 80, 79, 40, 1, 0, -1, true, false));
        require(first.claims().size() == 2
                && first.claims().get(0).layer() == ConformanceLayer.UNIVERSAL
                && first.claims().get(1).layer() == ConformanceLayer.SINGULAR,
                "TNT claim routing drifted");
        require(first.equals(second) && first.hashCode() == second.hashCode(),
                "TNT runtime identities leaked into evidence");
        reject(() -> TntLifecycleFixture.execute(plan(),
                () -> observation(11, 10, 79, 78, 39, 0, -1, -2, true, false)));
        reject(() -> TntLifecycleFixture.execute(plan(),
                () -> observation(11, 10, 80, 79, 40, 1, 0, -1, false, false)));
        reject(() -> TntLifecycleFixture.execute(plan(),
                () -> observation(11, 10, 80, 79, 40, 1, 0, -1, true, true)));
        reject(() -> TntLifecycleFixture.execute(plan(ConformanceLayer.ARCHETYPE),
                () -> observation(11, 10, 80, 79, 40, 1, 0, -1, true, false)));
    }

    private static TntFuseLifecycleObservation observation(int entity, int actor,
            int seed, int one, int forty, int seventyNine, int eighty, int terminal,
            boolean motionZeroed, boolean unprimedEntity) {
        return new TntFuseLifecycleObservation(spawn(entity), actor, seed, one, forty,
                seventyNine, eighty, terminal, true, true, false, false, true,
                motionZeroed, 46, unprimedEntity);
    }

    private static RemoteObjectSpawn spawn(int entity) {
        return new RemoteObjectSpawn(entity, 50, 32, 2048, 32, 0, 0, 0, 0);
    }

    private static EntityConformancePlan plan() { return plan(ConformanceLayer.SINGULAR); }
    private static EntityConformancePlan plan(ConformanceLayer fuse) {
        Map<String, ConformanceLayer> overrides = new LinkedHashMap<String, ConformanceLayer>();
        overrides.put("spawn-materialization", ConformanceLayer.UNIVERSAL);
        overrides.put("tick-lifecycle", fuse);
        EntityConformanceProfile tnt = new EntityConformanceProfile(SUBJECT,
                Collections.singletonList("primed-explosive"), true, overrides);
        return new EntityConformancePlan(Collections.singletonList(tnt), Arrays.asList(
                new EntityConformanceTemplate("spawn-materialization", ConformanceLayer.UNIVERSAL),
                new EntityConformanceTemplate("tick-lifecycle", ConformanceLayer.ARCHETYPE)));
    }

    private static void reject(Runnable action) {
        try { action.run(); throw new AssertionError("invalid TNT lifecycle accepted"); }
        catch (IllegalArgumentException | IllegalStateException expected) { }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
