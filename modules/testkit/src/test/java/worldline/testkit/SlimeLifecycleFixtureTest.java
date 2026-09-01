package worldline.testkit;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import worldline.api.MobObservationSession;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteMobDeath;
import worldline.api.RemoteMobSpawn;

/** Proves the slime subsystem routing, bounds, normalization, failures and public adapter. */
public final class SlimeLifecycleFixtureTest {
    private static final String SUBJECT = "b1.7.3:entity/055";

    private SlimeLifecycleFixtureTest() { }

    public static void main(String[] arguments) {
        execute();
        System.out.println("SlimeLifecycleFixtureTest passed");
    }

    static void execute() {
        EntityConformancePlan plan = plan(Collections.<String, ConformanceLayer>emptyMap());
        SlimeLifecycleEvidence evidence = SlimeLifecycleFixture.execute(plan, SUBJECT, 8, 12,
                scenario(split(10, 4, 11), drop(20, 1, 30, 341, 2),
                        motion(true, true, 101), motion(false, false, 699)));
        require(evidence.claims().size() == 4
                && evidence.claims().get(0).layer() == ConformanceLayer.UNIVERSAL
                && evidence.claims().get(1).layer() == ConformanceLayer.SINGULAR,
                "slime conformance routes drifted");
        boundedRetries(plan);
        normalizedIdentity(plan, evidence);
        adapterExecutes(plan);
        rejectsInvalidEvidence(plan);
    }

    private static void boundedRetries(EntityConformancePlan plan) {
        AtomicInteger splits = new AtomicInteger();
        AtomicInteger drops = new AtomicInteger();
        SlimeLifecycleScenario scenario = new SlimeLifecycleScenario() {
            @Override public SlimeMotionObservation observeMotion(SlimeMotionScene scene) {
                return scene == SlimeMotionScene.OPEN ? motion(true, true, 101)
                        : motion(false, false, 699);
            }
            @Override public SlimeSplitObservation attemptSplit(int attempt) {
                splits.incrementAndGet();
                return attempt == 3 ? split(40, 2, 41) : null;
            }
            @Override public SlimeDropObservation attemptDrop(int attempt) {
                drops.incrementAndGet();
                return attempt == 4 ? drop(50, 1, 60, 341, 1) : null;
            }
        };
        SlimeLifecycleFixture.execute(plan, SUBJECT, 3, 4, scenario);
        require(splits.get() == 3 && drops.get() == 4, "slime attempt bounds drifted");
        reject(() -> SlimeLifecycleFixture.execute(plan, SUBJECT, 2, 4, scenario));
    }

    private static void normalizedIdentity(EntityConformancePlan plan,
            SlimeLifecycleEvidence first) {
        SlimeLifecycleEvidence second = SlimeLifecycleFixture.execute(plan, SUBJECT, 8, 12,
                scenario(split(110, 4, 111), drop(120, 1, 130, 341, 1),
                        motion(true, true, 500), motion(true, true, 200)));
        require(first.equals(second) && first.hashCode() == second.hashCode(),
                "fresh slime identities or exact spans leaked into evidence");
    }

    private static void adapterExecutes(EntityConformancePlan plan) {
        MobObservationSession session = (MobObservationSession) Proxy.newProxyInstance(
                SlimeLifecycleFixtureTest.class.getClassLoader(),
                new Class<?>[] { MobObservationSession.class },
                (proxy, method, arguments) -> { throw new UnsupportedOperationException(method.getName()); });
        AtomicInteger calls = new AtomicInteger();
        SlimeLifecycleScenario scenario = new SlimeObservationLifecycleScenario(session,
                (current, scene) -> { calls.incrementAndGet(); return scene == SlimeMotionScene.OPEN
                        ? motion(true, true, 101) : motion(false, false, 699); },
                (current, attempt) -> { calls.incrementAndGet(); return split(70, 2, 71); },
                (current, attempt) -> { calls.incrementAndGet();
                    return drop(80, 1, 90, 341, 1); });
        SlimeLifecycleFixture.execute(plan, SUBJECT, 8, 12, scenario);
        require(calls.get() == 4, "slime observation adapter did not delegate all scenes");
    }

    private static void rejectsInvalidEvidence(EntityConformancePlan plan) {
        reject(() -> SlimeLifecycleFixture.execute(plan, SUBJECT, 8, 12,
                scenario(split(1, 2, 2), drop(3, 1, 4, 341, 1),
                        motion(true, false, 101), motion(false, false, 699))));
        reject(() -> SlimeLifecycleFixture.execute(plan, SUBJECT, 8, 12,
                scenario(split(1, 2, 2), drop(3, 1, 4, 341, 1),
                        motion(true, true, 101), motion(false, false, 700))));
        reject(() -> SlimeLifecycleFixture.execute(plan, SUBJECT, 8, 12,
                scenario(new SlimeSplitObservation(spawn(1, 54, 0), 2, death(1),
                                Collections.singletonList(spawn(2, 55, 1))),
                        drop(3, 1, 4, 341, 1), motion(true, true, 101),
                        motion(false, false, 699))));
        reject(() -> SlimeLifecycleFixture.execute(plan, SUBJECT, 8, 12,
                scenario(split(1, 2, 1), drop(3, 1, 4, 341, 1),
                        motion(true, true, 101), motion(false, false, 699))));
        reject(() -> SlimeLifecycleFixture.execute(plan, SUBJECT, 8, 12,
                scenario(split(1, 2, 2), drop(3, 2, 4, 341, 1),
                        motion(true, true, 101), motion(false, false, 699))));
        reject(() -> SlimeLifecycleFixture.execute(plan, SUBJECT, 8, 12,
                scenario(split(1, 2, 2), drop(3, 1, 4, 287, 1),
                        motion(true, true, 101), motion(false, false, 699))));
        Map<String, ConformanceLayer> override = new LinkedHashMap<String, ConformanceLayer>();
        override.put("movement-policy", ConformanceLayer.ARCHETYPE);
        reject(() -> SlimeLifecycleFixture.execute(plan(override), SUBJECT, 8, 12,
                scenario(split(1, 2, 2), drop(3, 1, 4, 341, 1),
                        motion(true, true, 101), motion(false, false, 699))));
        reject(() -> SlimeLifecycleFixture.execute(plan, SUBJECT, 0, 12,
                scenario(split(1, 2, 2), drop(3, 1, 4, 341, 1),
                        motion(true, true, 101), motion(false, false, 699))));
    }

    private static SlimeLifecycleScenario scenario(SlimeSplitObservation split,
            SlimeDropObservation drop, SlimeMotionObservation open,
            SlimeMotionObservation roof) {
        return new SlimeLifecycleScenario() {
            @Override public SlimeMotionObservation observeMotion(SlimeMotionScene scene) {
                return scene == SlimeMotionScene.OPEN ? open : roof;
            }
            @Override public SlimeSplitObservation attemptSplit(int attempt) { return split; }
            @Override public SlimeDropObservation attemptDrop(int attempt) { return drop; }
        };
    }

    private static SlimeSplitObservation split(int parent, int size, int child) {
        return new SlimeSplitObservation(spawn(parent, 55, 0), size, death(parent),
                Collections.singletonList(spawn(child, 55, 1)));
    }

    private static SlimeDropObservation drop(int slime, int size, int item,
            int legacyId, int count) {
        return new SlimeDropObservation(spawn(slime, 55, 0), size, death(slime),
                new RemoteDroppedItem(item, new RemoteItemStack(legacyId, count, 0),
                        0, 10, 0, 0, 0, 0));
    }

    private static SlimeMotionObservation motion(boolean air, boolean ground, int span) {
        return new SlimeMotionObservation(air, ground, span);
    }

    private static RemoteMobSpawn spawn(int id, int type, int fixedX) {
        return new RemoteMobSpawn(id, type, fixedX, 10 * 32, 0, 0, 0, 2, 0);
    }

    private static RemoteMobDeath death(int id) { return new RemoteMobDeath(id, 3, true); }

    private static EntityConformancePlan plan(Map<String, ConformanceLayer> overrides) {
        return new EntityConformancePlan(Collections.singletonList(new EntityConformanceProfile(
                SUBJECT, Collections.singletonList("slime"), true, overrides)), Arrays.asList(
                new EntityConformanceTemplate("spawn-materialization", ConformanceLayer.UNIVERSAL),
                new EntityConformanceTemplate("movement-policy", ConformanceLayer.ARCHETYPE),
                new EntityConformanceTemplate("interaction-state", ConformanceLayer.ARCHETYPE),
                new EntityConformanceTemplate("drop-matrix", ConformanceLayer.ARCHETYPE)));
    }

    private static void reject(Runnable action) {
        try { action.run(); throw new AssertionError("invalid slime lifecycle accepted"); }
        catch (IllegalArgumentException | IllegalStateException expected) { }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
