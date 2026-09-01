package worldline.testkit;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import worldline.api.RemoteMobSpawn;

/** Proves strict wolf materialization, tame and owner sit/stand boundaries. */
public final class WolfLifecycleFixtureTest {
    private WolfLifecycleFixtureTest() { }

    public static void main(String[] arguments) {
        execute();
        System.out.println("WolfLifecycleFixtureTest passed");
    }

    static void execute() {
        WolfLifecycleEvidence first = WolfLifecycleFixture.execute(plan(),
                () -> observation(11, 10, 5, 4, 5, 4, false));
        WolfLifecycleEvidence second = WolfLifecycleFixture.execute(plan(),
                () -> observation(101, 100, 13, 12, 13, 12, false));
        require(first.claims().size() == 2
                && first.claims().get(0).layer() == ConformanceLayer.UNIVERSAL
                && first.claims().get(1).layer() == ConformanceLayer.SINGULAR,
                "wolf claim routing drifted");
        require(first.equals(second) && first.hashCode() == second.hashCode(),
                "wolf runtime identities or legal Packet40 bits leaked into evidence");
        reject(() -> WolfLifecycleFixture.execute(plan(),
                () -> with(94, 7, 352, 4, true, 280, 0, 5, 4, 5, 4, false)));
        reject(() -> WolfLifecycleFixture.execute(plan(),
                () -> with(95, 6, 352, 4, true, 280, 0, 5, 4, 5, 4, false)));
        reject(() -> WolfLifecycleFixture.execute(plan(),
                () -> with(95, 7, 352, 4, false, 280, 0, 5, 4, 5, 4, false)));
        reject(() -> WolfLifecycleFixture.execute(plan(),
                () -> with(95, 7, 352, 4, true, 280, 0, 4, 4, 5, 4, false)));
        reject(() -> WolfLifecycleFixture.execute(plan(),
                () -> with(95, 7, 352, 4, true, 280, 0, 5, 5, 5, 4, false)));
        reject(() -> WolfLifecycleFixture.execute(plan(),
                () -> with(95, 7, 352, 4, true, 280, 0, 5, 4, 5, 4, true)));
        reject(() -> WolfLifecycleFixture.execute(plan(ConformanceLayer.ARCHETYPE),
                () -> observation(11, 10, 5, 4, 5, 4, false)));
    }

    private static WolfOwnerStateObservation observation(int wolf, int actor,
            int initial, int standing, int sitting, int finalStanding, boolean death) {
        return new WolfOwnerStateObservation(mob(wolf, 95), actor, 7, 352, 4, true,
                280, 0, initial, standing, sitting, finalStanding, death);
    }

    private static WolfOwnerStateObservation with(int type, int tame, int bone, int dye,
            boolean collar, int item, int button, int initial, int standing,
            int sitting, int finalStanding, boolean death) {
        return new WolfOwnerStateObservation(mob(11, type), 10, tame, bone, dye, collar,
                item, button, initial, standing, sitting, finalStanding, death);
    }

    private static RemoteMobSpawn mob(int entity, int type) {
        return new RemoteMobSpawn(entity, type, 32, 2048, 32, 0, 0, 1, 0);
    }

    private static EntityConformancePlan plan() { return plan(ConformanceLayer.SINGULAR); }

    private static EntityConformancePlan plan(ConformanceLayer interaction) {
        Map<String, ConformanceLayer> overrides = new LinkedHashMap<String, ConformanceLayer>();
        overrides.put("spawn-materialization", ConformanceLayer.UNIVERSAL);
        overrides.put("interaction-state", interaction);
        EntityConformanceProfile wolf = new EntityConformanceProfile(
                "b1.7.3:entity/095", Collections.singletonList("tameable"), true, overrides);
        return new EntityConformancePlan(Collections.singletonList(wolf), Arrays.asList(
                new EntityConformanceTemplate("spawn-materialization", ConformanceLayer.UNIVERSAL),
                new EntityConformanceTemplate("interaction-state", ConformanceLayer.ARCHETYPE)));
    }

    private static void reject(Runnable action) {
        try { action.run(); throw new AssertionError("invalid wolf lifecycle evidence accepted"); }
        catch (IllegalArgumentException | IllegalStateException expected) { }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
