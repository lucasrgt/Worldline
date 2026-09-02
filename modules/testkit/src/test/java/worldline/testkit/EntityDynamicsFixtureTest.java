package worldline.testkit;
import worldline.testapi.ConformanceLayer;
import worldline.testapi.EntityConformanceCase;
import worldline.testapi.EntityConformancePlan;
import worldline.testapi.EntityConformanceProfile;
import worldline.testapi.EntityConformanceTemplate;
import worldline.testapi.EntityDynamicsEvidence;
import worldline.testapi.EntityDynamicsFixture;
import worldline.testapi.EntityDynamicsObservation;
import worldline.testapi.EntityDynamicsScenario;
import worldline.testapi.EntityDynamicsScene;
import worldline.testapi.EntityObservationDynamicsScenario;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import worldline.api.MobObservationSession;

/** Proves all qualified dynamics routes, thresholds, normalization and adapter delegation. */
public final class EntityDynamicsFixtureTest {
    private static final List<String> SUBJECTS = Arrays.asList(
            "b1.7.3:entity/040", "b1.7.3:entity/041",
            "b1.7.3:entity/055", "b1.7.3:entity/056");

    private EntityDynamicsFixtureTest() { }

    public static void main(String[] arguments) {
        execute();
        System.out.println("EntityDynamicsFixtureTest passed");
    }

    static void execute() {
        EntityConformancePlan plan = plan(true);
        EntityDynamicsEvidence evidence = EntityDynamicsFixture.execute(plan,
                EntityDynamicsFixtureTest::valid);
        require(evidence.claims().size() == 4 && evidence.observations().size() == 8,
                "complete dynamics matrix was not retained");
        for (EntityConformanceCase claim : evidence.claims()) {
            require(claim.layer() == ConformanceLayer.SINGULAR,
                    "dynamics route was not singular");
        }
        normalizedIdentity(plan, evidence);
        adapterExecutes(plan);
        rejectsThresholdDrift(plan);
        reject(() -> EntityDynamicsFixture.execute(plan(false), EntityDynamicsFixtureTest::valid));
    }

    private static void normalizedIdentity(EntityConformancePlan plan,
            EntityDynamicsEvidence first) {
        EntityDynamicsEvidence second = EntityDynamicsFixture.execute(plan, scene -> {
            switch (scene) {
                case GHAST_OPEN: return vertical(900, false, false);
                case GHAST_ROOF: return vertical(1, false, false);
                case SLIME_OPEN: return vertical(600, true, true);
                case SLIME_LOW_ROOF: return vertical(1, true, true);
                case BOAT_OPEN: return horizontal(20000, -100, false);
                case BOAT_WALL: return horizontal(-20, 200, true);
                case MINECART_SHORT_RAIL: return horizontal(0, -1, false);
                default: return horizontal(0, -900, false);
            }
        });
        require(first.equals(second) && first.hashCode() == second.hashCode(),
                "exact motion samples leaked into normalized evidence");
    }

    private static void adapterExecutes(EntityConformancePlan plan) {
        MobObservationSession session = (MobObservationSession) Proxy.newProxyInstance(
                EntityDynamicsFixtureTest.class.getClassLoader(),
                new Class<?>[] { MobObservationSession.class },
                (proxy, method, arguments) -> { throw new UnsupportedOperationException(method.getName()); });
        AtomicInteger calls = new AtomicInteger();
        EntityDynamicsScenario scenario = new EntityObservationDynamicsScenario(session,
                (current, scene) -> { calls.incrementAndGet(); return valid(scene); });
        EntityDynamicsFixture.execute(plan, scenario);
        require(calls.get() == 8, "dynamics observation adapter did not delegate every scene");
    }

    private static void rejectsThresholdDrift(EntityConformancePlan plan) {
        rejectScene(plan, EntityDynamicsScene.GHAST_OPEN, vertical(200, false, false));
        rejectScene(plan, EntityDynamicsScene.GHAST_ROOF, vertical(200, false, false));
        rejectScene(plan, EntityDynamicsScene.SLIME_OPEN, vertical(101, true, false));
        rejectScene(plan, EntityDynamicsScene.SLIME_LOW_ROOF, vertical(700, false, false));
        rejectScene(plan, EntityDynamicsScene.BOAT_OPEN, horizontal(9300, 0, false));
        rejectScene(plan, EntityDynamicsScene.BOAT_WALL, horizontal(0, 0, false));
        rejectScene(plan, EntityDynamicsScene.MINECART_SHORT_RAIL, horizontal(0, 50, false));
        rejectScene(plan, EntityDynamicsScene.MINECART_LONG_RAIL, horizontal(0, 50, false));
        rejectScene(plan, EntityDynamicsScene.GHAST_OPEN, horizontal(0, 0, false));
        rejectScene(plan, EntityDynamicsScene.BOAT_OPEN, vertical(1, false, false));
    }

    private static void rejectScene(EntityConformancePlan plan, EntityDynamicsScene changed,
            EntityDynamicsObservation observation) {
        reject(() -> EntityDynamicsFixture.execute(plan,
                scene -> scene == changed ? observation : valid(scene)));
    }

    private static EntityDynamicsObservation valid(EntityDynamicsScene scene) {
        switch (scene) {
            case GHAST_OPEN: return vertical(201, false, false);
            case GHAST_ROOF: return vertical(199, false, false);
            case SLIME_OPEN: return vertical(101, true, true);
            case SLIME_LOW_ROOF: return vertical(699, false, false);
            case BOAT_OPEN: return horizontal(9301, 100, false);
            case BOAT_WALL: return horizontal(9200, 0, true);
            case MINECART_SHORT_RAIL: return horizontal(0, 49, false);
            default: return horizontal(0, 51, false);
        }
    }

    private static EntityDynamicsObservation vertical(int span, boolean air, boolean ground) {
        return EntityDynamicsObservation.vertical(span, air, ground);
    }

    private static EntityDynamicsObservation horizontal(int x, int motion, boolean collision) {
        return EntityDynamicsObservation.horizontal(x, motion, collision);
    }

    private static EntityConformancePlan plan(boolean singular) {
        List<EntityConformanceProfile> profiles = new ArrayList<EntityConformanceProfile>();
        for (String subject : SUBJECTS) {
            profiles.add(new EntityConformanceProfile(subject, Collections.singletonList("motion"),
                    singular, Collections.<String, ConformanceLayer>emptyMap()));
        }
        return new EntityConformancePlan(profiles, Collections.singletonList(
                new EntityConformanceTemplate("movement-policy", ConformanceLayer.ARCHETYPE)));
    }

    private static void reject(Runnable action) {
        try { action.run(); throw new AssertionError("invalid dynamics evidence accepted"); }
        catch (IllegalArgumentException | IllegalStateException expected) { }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
