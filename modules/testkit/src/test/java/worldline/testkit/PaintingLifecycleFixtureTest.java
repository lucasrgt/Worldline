package worldline.testkit;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import worldline.api.PaintingObservationSession;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;
import worldline.api.RemotePaintingSpawn;

/** Proves the complete painting fixture, normalization, failures and public adapter. */
public final class PaintingLifecycleFixtureTest {
    private static final String SUBJECT = "b1.7.3:entity/009";
    private static final List<PaintingSpawnExpectation> EXPECTATIONS = Arrays.asList(
            new PaintingSpawnExpectation(5, 72, 4, 1),
            new PaintingSpawnExpectation(3, 72, 3, 3));

    private PaintingLifecycleFixtureTest() { }

    public static void main(String[] arguments) {
        execute();
        System.out.println("PaintingLifecycleFixtureTest passed");
    }

    static void execute() {
        EntityConformancePlan plan = plan(Collections.<String, ConformanceLayer>emptyMap());
        PaintingLifecycleEvidence evidence = PaintingLifecycleFixture.execute(plan, SUBJECT,
                EXPECTATIONS, scenario(spawn(41, "Kebab", 5, 72, 4, 1),
                        spawn(42, "Aztec", 3, 72, 3, 3), 41, drop(91, 321)));
        require(evidence.claims().get(0).layer() == ConformanceLayer.UNIVERSAL
                && evidence.claims().get(1).layer() == ConformanceLayer.SINGULAR
                && evidence.claims().get(2).layer() == ConformanceLayer.SINGULAR,
                "painting conformance layers drifted");
        normalizedFreshIdentity(plan, evidence);
        adapterExecutes(plan);
        reject(() -> PaintingLifecycleFixture.execute(plan, SUBJECT,
                Arrays.asList(EXPECTATIONS.get(0), EXPECTATIONS.get(0)), scenario(
                        spawn(1, "Kebab", 5, 72, 4, 1),
                        spawn(2, "Aztec", 5, 72, 4, 1), 1, drop(3, 321))));
        reject(() -> PaintingLifecycleFixture.execute(plan, SUBJECT, EXPECTATIONS, scenario(
                spawn(1, "Kebab", 6, 72, 4, 1),
                spawn(2, "Aztec", 3, 72, 3, 3), 1, drop(3, 321))));
        reject(() -> PaintingLifecycleFixture.execute(plan, SUBJECT, EXPECTATIONS, scenario(
                spawn(1, "Kebab", 5, 72, 4, 1),
                spawn(1, "Aztec", 3, 72, 3, 3), 1, drop(3, 321))));
        reject(() -> PaintingLifecycleFixture.execute(plan, SUBJECT, EXPECTATIONS, scenario(
                spawn(1, "Kebab", 5, 72, 4, 1),
                spawn(2, "Aztec", 3, 72, 3, 3), 2, drop(3, 321))));
        reject(() -> PaintingLifecycleFixture.execute(plan, SUBJECT, EXPECTATIONS, scenario(
                spawn(1, "Kebab", 5, 72, 4, 1),
                spawn(2, "Aztec", 3, 72, 3, 3), 1, drop(3, 287))));
        Map<String, ConformanceLayer> override = new LinkedHashMap<String, ConformanceLayer>();
        override.put("environment-response", ConformanceLayer.ARCHETYPE);
        reject(() -> PaintingLifecycleFixture.execute(plan(override), SUBJECT, EXPECTATIONS,
                scenario(spawn(1, "Kebab", 5, 72, 4, 1),
                        spawn(2, "Aztec", 3, 72, 3, 3), 1, drop(3, 321))));
    }

    private static void normalizedFreshIdentity(EntityConformancePlan plan,
            PaintingLifecycleEvidence first) {
        PaintingLifecycleEvidence second = PaintingLifecycleFixture.execute(plan, SUBJECT,
                EXPECTATIONS, scenario(spawn(141, "DonkeyKong", 5, 72, 4, 1),
                        spawn(142, "Plant", 3, 72, 3, 3), 141, drop(191, 321)));
        require(first.equals(second) && first.hashCode() == second.hashCode(),
                "fresh painting identities or motives leaked into evidence");
    }

    private static void adapterExecutes(EntityConformancePlan plan) {
        AtomicInteger spawnIndex = new AtomicInteger();
        AtomicInteger removals = new AtomicInteger();
        RemotePaintingSpawn[] spawns = {
            spawn(51, "Kebab", 5, 72, 4, 1), spawn(52, "Aztec", 3, 72, 3, 3)
        };
        PaintingObservationSession session = (PaintingObservationSession) Proxy.newProxyInstance(
                PaintingLifecycleFixtureTest.class.getClassLoader(),
                new Class<?>[] { PaintingObservationSession.class },
                (proxy, method, arguments) -> {
                    if (method.getName().equals("awaitPaintingSpawn")) {
                        return spawns[spawnIndex.getAndIncrement()];
                    }
                    if (method.getName().equals("awaitPaintingDestroy")) return arguments[0];
                    if (method.getName().equals("peekDroppedItem")) return drop(61, 321);
                    throw new UnsupportedOperationException(method.getName());
                });
        PaintingLifecycleEvidence evidence = PaintingLifecycleFixture.execute(plan, SUBJECT,
                EXPECTATIONS, new PaintingObservationLifecycleScenario(session,
                        (observed, painting) -> removals.incrementAndGet()));
        require(spawnIndex.get() == 2 && removals.get() == 1
                && evidence.drop().item().legacyId() == 321,
                "painting observation adapter did not execute public session");
    }

    private static PaintingLifecycleScenario scenario(RemotePaintingSpawn first,
            RemotePaintingSpawn second, int destroy, RemoteDroppedItem drop) {
        return new PaintingLifecycleScenario() {
            private int index;
            @Override public RemotePaintingSpawn materialize(PaintingSpawnExpectation ignored) {
                return index++ == 0 ? first : second;
            }
            @Override public void removeSupport(RemotePaintingSpawn ignored) { }
            @Override public int awaitDestroy(int ignored) { return destroy; }
            @Override public RemoteDroppedItem awaitDrop(RemoteItemStack ignored) { return drop; }
        };
    }

    private static EntityConformancePlan plan(Map<String, ConformanceLayer> overrides) {
        EntityConformanceProfile profile = new EntityConformanceProfile(SUBJECT,
                Collections.singletonList("painting"), true, overrides);
        return new EntityConformancePlan(Collections.singletonList(profile), Arrays.asList(
                new EntityConformanceTemplate("spawn-materialization", ConformanceLayer.UNIVERSAL),
                new EntityConformanceTemplate("interaction-state", ConformanceLayer.ARCHETYPE),
                new EntityConformanceTemplate("environment-response", ConformanceLayer.ARCHETYPE)));
    }

    private static RemotePaintingSpawn spawn(int id, String title,
            int x, int y, int z, int direction) {
        return new RemotePaintingSpawn(id, title, x, y, z, direction);
    }

    private static RemoteDroppedItem drop(int id, int legacyId) {
        return new RemoteDroppedItem(id, new RemoteItemStack(legacyId, 1, 0),
                5.5, 72.5, 4.5, 0, 0, 0);
    }

    private static void reject(Runnable action) {
        try { action.run(); throw new AssertionError("invalid painting lifecycle accepted"); }
        catch (IllegalArgumentException | IllegalStateException expected) { }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
