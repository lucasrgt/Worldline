package worldline.testkit;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import worldline.api.MobObservationSession;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteMobDeath;
import worldline.api.RemoteMobMovement;
import worldline.api.RemoteMobSpawn;

/** Proves executable, normalized lifecycle evidence and the protocol-session adapter. */
public final class EntityLifecycleFixtureTest {
    private static final RemoteItemStack PORK = new RemoteItemStack(319, 1, 0);

    private EntityLifecycleFixtureTest() { }

    public static void main(String[] arguments) {
        execute();
        System.out.println("EntityLifecycleFixtureTest passed");
    }

    static void execute() {
        EntityConformancePlan plan = EntityConformancePlanTest.lifecyclePlan();
        EntityLifecycleEvidence first = EntityLifecycleFixture.execute(plan,
                "b1.7.3:entity/090", 90, PORK, all(), new FixedScenario(41, 90, PORK));
        EntityLifecycleEvidence second = EntityLifecycleFixture.execute(plan,
                "b1.7.3:entity/090", 90, PORK, all(), new FixedScenario(99, 90, PORK));
        require(first.equals(second) && first.hashCode() == second.hashCode(),
                "fresh runtime identities leaked into semantic evidence");
        require(first.claims().size() == 4
                && first.claims().get(0).layer() == ConformanceLayer.UNIVERSAL
                && first.claims().get(1).layer() == ConformanceLayer.ARCHETYPE,
                "pig lifecycle routing drifted");

        Set<String> lethal = set("spawn-materialization", "damage-death", "drop-matrix");
        RemoteItemStack gunpowder = new RemoteItemStack(289, 1, 0);
        EntityLifecycleEvidence creeper = EntityLifecycleFixture.execute(plan,
                "b1.7.3:entity/050", 50, gunpowder, lethal,
                new FixedScenario(50, 50, gunpowder));
        require(creeper.claims().get(1).layer() == ConformanceLayer.SINGULAR
                && creeper.claims().get(2).layer() == ConformanceLayer.SINGULAR,
                "singular lifecycle routing drifted");
        adapterExecutes(plan);
        reject(() -> EntityLifecycleFixture.execute(plan, "b1.7.3:entity/090", 90,
                PORK, set("drop-matrix"), new FixedScenario(1, 90, PORK)));
        reject(() -> EntityLifecycleFixture.execute(plan, "b1.7.3:entity/090", 90,
                PORK, all(), new FixedScenario(1, 91, PORK)));
    }

    private static void adapterExecutes(EntityConformancePlan plan) {
        AtomicInteger attacks = new AtomicInteger();
        MobObservationSession session = (MobObservationSession) Proxy.newProxyInstance(
                EntityLifecycleFixtureTest.class.getClassLoader(),
                new Class<?>[] { MobObservationSession.class }, (proxy, method, arguments) -> {
                    int entity = 77;
                    if (method.getName().equals("awaitMobSpawn")) return spawn(entity, 90);
                    if (method.getName().equals("awaitMobMovement")) return movement(entity);
                    if (method.getName().equals("attackMob")) { attacks.incrementAndGet(); return null; }
                    if (method.getName().equals("awaitMobDeath")) return death(entity);
                    if (method.getName().equals("peekDroppedItem")) return drop(entity + 1, PORK);
                    throw new UnsupportedOperationException(method.getName());
                });
        EntityLifecycleEvidence evidence = EntityLifecycleFixture.execute(plan,
                "b1.7.3:entity/090", 90, PORK, all(),
                new MobObservationEntityScenario(session,
                        (current, entity) -> current.attackMob(entity)));
        require(attacks.get() == 1 && evidence.drop().item().equals(PORK),
                "mob-observation scenario did not execute the public session");
    }

    private static Set<String> all() {
        return set("spawn-materialization", "movement-policy", "damage-death", "drop-matrix");
    }

    private static Set<String> set(String... values) {
        return new LinkedHashSet<String>(Arrays.asList(values));
    }

    private static RemoteMobSpawn spawn(int entity, int type) {
        return new RemoteMobSpawn(entity, type, 32, 2048, 32, 0, 0, 2, 0);
    }

    private static RemoteMobMovement movement(int entity) {
        return new RemoteMobMovement(entity, 31, 32, 2048, 32, 33, 2048, 32, 0, 0);
    }

    private static RemoteMobDeath death(int entity) {
        return new RemoteMobDeath(entity, 3, true);
    }

    private static RemoteDroppedItem drop(int entity, RemoteItemStack item) {
        return new RemoteDroppedItem(entity, item, 1, 64, 1, 0, 0, 0);
    }

    private static final class FixedScenario implements EntityLifecycleScenario {
        private final int entity, type;
        private final RemoteItemStack item;
        private boolean killed;
        FixedScenario(int entity, int type, RemoteItemStack item) {
            this.entity = entity; this.type = type; this.item = item;
        }
        @Override public RemoteMobSpawn materialize(int expected) { return spawn(entity, type); }
        @Override public RemoteMobMovement awaitMovement(int id) { return movement(entity); }
        @Override public void kill(int id) { killed = true; }
        @Override public RemoteMobDeath awaitDeath(int id) {
            if (!killed) throw new AssertionError("death observed before kill");
            return death(entity);
        }
        @Override public RemoteDroppedItem awaitDrop(RemoteItemStack expected) {
            return drop(entity + 1, item);
        }
    }

    private static void reject(Runnable action) {
        try { action.run(); throw new AssertionError("invalid entity lifecycle accepted"); }
        catch (IllegalArgumentException | IllegalStateException expected) { }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
