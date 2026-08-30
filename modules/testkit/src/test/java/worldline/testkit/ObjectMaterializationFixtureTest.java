package worldline.testkit;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import worldline.api.ObjectObservationSession;
import worldline.api.RemoteObjectSpawn;

/** Proves the universal Packet23 matrix, normalization, failures and public adapter. */
public final class ObjectMaterializationFixtureTest {
    private static final String[] SUBJECTS = {
        "b1.7.3:entity/041", "b1.7.3:entity/040", "b1.7.3:entity/020",
        "b1.7.3:entity/010", "b1.7.3:entity/011", "b1.7.3:entity/021"
    };
    private static final int[] TYPES = {1, 10, 50, 60, 61, 70};

    private ObjectMaterializationFixtureTest() { }

    public static void main(String[] arguments) {
        execute();
        System.out.println("ObjectMaterializationFixtureTest passed");
    }

    static void execute() {
        EntityConformancePlan plan = plan(Collections.<String, ConformanceLayer>emptyMap());
        ObjectSpawnExpectation[] expectations = {
            ObjectSpawnExpectation.typeOnly(1), ObjectSpawnExpectation.stationary(10, 0),
            ObjectSpawnExpectation.withThrower(50, 0),
            ObjectSpawnExpectation.withThrower(60, 7),
            ObjectSpawnExpectation.withThrower(61, 0),
            ObjectSpawnExpectation.typeOnly(70)
        };
        for (int index = 0; index < SUBJECTS.length; index++) {
            int thrower = index == 3 ? 7 : 0;
            RemoteObjectSpawn spawn = spawn(40 + index, TYPES[index], thrower, 0);
            ObjectMaterializationEvidence evidence = ObjectMaterializationFixture.execute(plan,
                    SUBJECTS[index], expectations[index], ignored -> spawn);
            require(evidence.claim().layer() == ConformanceLayer.UNIVERSAL
                    && evidence.spawn().type() == TYPES[index],
                    "Packet23 universal row drifted: " + SUBJECTS[index]);
        }
        normalizedThrownIdentity(plan);
        adapterExecutes(plan);
        reject(() -> ObjectMaterializationFixture.execute(plan, SUBJECTS[0],
                ObjectSpawnExpectation.typeOnly(1), ignored -> spawn(2, 10, 0, 0)));
        reject(() -> ObjectMaterializationFixture.execute(plan, SUBJECTS[3],
                ObjectSpawnExpectation.withThrower(60, 7),
                ignored -> spawn(2, 60, 8, 1)));
        reject(() -> ObjectMaterializationFixture.execute(plan, SUBJECTS[3],
                ObjectSpawnExpectation.stationary(60, 7),
                ignored -> spawn(2, 60, 7, 1)));
        Map<String, ConformanceLayer> override = new LinkedHashMap<String, ConformanceLayer>();
        override.put("spawn-materialization", ConformanceLayer.ARCHETYPE);
        reject(() -> ObjectMaterializationFixture.execute(plan(override), SUBJECTS[0],
                ObjectSpawnExpectation.typeOnly(1), ignored -> spawn(2, 1, 0, 0)));
        reject(() -> ObjectSpawnExpectation.withThrower(1, -1));
    }

    private static void normalizedThrownIdentity(EntityConformancePlan plan) {
        ObjectMaterializationEvidence first = ObjectMaterializationFixture.execute(plan,
                SUBJECTS[3], ObjectSpawnExpectation.withThrower(60, 7),
                ignored -> spawn(100, 60, 7, 1));
        ObjectMaterializationEvidence second = ObjectMaterializationFixture.execute(plan,
                SUBJECTS[3], ObjectSpawnExpectation.withThrower(60, 19),
                ignored -> spawn(200, 60, 19, 2));
        require(first.equals(second) && first.hashCode() == second.hashCode(),
                "fresh Packet23 identities leaked into evidence");
    }

    private static void adapterExecutes(EntityConformancePlan plan) {
        AtomicInteger calls = new AtomicInteger();
        ObjectObservationSession session = (ObjectObservationSession) Proxy.newProxyInstance(
                ObjectMaterializationFixtureTest.class.getClassLoader(),
                new Class<?>[] { ObjectObservationSession.class }, (proxy, method, arguments) -> {
                    if (method.getName().equals("awaitObjectSpawn")) {
                        calls.incrementAndGet();
                        return spawn(77, ((Integer) arguments[0]).intValue(), 0, 0);
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
        ObjectMaterializationEvidence evidence = ObjectMaterializationFixture.execute(plan,
                SUBJECTS[1], ObjectSpawnExpectation.stationary(10, 0),
                new ObjectObservationMaterializationScenario(session));
        require(calls.get() == 1 && evidence.spawn().type() == 10,
                "object-observation adapter did not execute public session");
    }

    private static EntityConformancePlan plan(Map<String, ConformanceLayer> overrides) {
        List<EntityConformanceProfile> profiles = new ArrayList<EntityConformanceProfile>();
        for (String subject : SUBJECTS) profiles.add(new EntityConformanceProfile(subject,
                Collections.singletonList("packet23-object"), false, overrides));
        return new EntityConformancePlan(profiles, Collections.singletonList(
                new EntityConformanceTemplate("spawn-materialization",
                        ConformanceLayer.UNIVERSAL)));
    }

    private static RemoteObjectSpawn spawn(int entity, int type, int thrower, int velocity) {
        return new RemoteObjectSpawn(entity, type, 32, 2048, 32,
                thrower, velocity, 0, 0);
    }

    private static void reject(Runnable action) {
        try { action.run(); throw new AssertionError("invalid Packet23 materialization accepted"); }
        catch (IllegalArgumentException | IllegalStateException expected) { }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
