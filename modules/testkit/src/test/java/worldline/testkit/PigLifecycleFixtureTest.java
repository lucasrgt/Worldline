package worldline.testkit;
import worldline.testapi.ConformanceLayer;
import worldline.testapi.EntityConformancePlan;
import worldline.testapi.EntityConformanceProfile;
import worldline.testapi.EntityConformanceTemplate;
import worldline.testapi.EntityLifecycleScenario;
import worldline.testapi.PigLifecycleEvidence;
import worldline.testapi.PigLifecycleFixture;
import worldline.testapi.PigLifecycleScenario;
import worldline.testapi.PigSaddleMountObservation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteMobDeath;
import worldline.api.RemoteMobMovement;
import worldline.api.RemoteMobSpawn;

/** Proves the complete pig lifecycle fixture and its saddle/mount boundaries. */
public final class PigLifecycleFixtureTest {
    private static final RemoteItemStack PORK = new RemoteItemStack(319, 1, 0);

    private PigLifecycleFixtureTest() { }

    public static void main(String[] arguments) {
        execute();
        System.out.println("PigLifecycleFixtureTest passed");
    }

    static void execute() {
        PigLifecycleEvidence first = PigLifecycleFixture.execute(plan(), scenario(10, 20, 30));
        PigLifecycleEvidence second = PigLifecycleFixture.execute(plan(), scenario(100, 200, 300));
        require(first.claims().size() == 5 && first.equals(second)
                && first.hashCode() == second.hashCode(),
                "pig runtime identities leaked into evidence");
        reject(() -> PigLifecycleFixture.execute(plan(), scenario(10, 20, 20)));
        reject(() -> PigLifecycleFixture.execute(plan(), scenario(10, 20, 30, 328, 1, 0)));
        reject(() -> PigLifecycleFixture.execute(plan(), scenario(10, 20, 30, 329, 1, 1)));
        reject(() -> PigLifecycleFixture.execute(plan(), wrongAttachment(10, 20, 30)));
        reject(() -> PigLifecycleFixture.execute(plan(ConformanceLayer.SINGULAR),
                scenario(10, 20, 30)));
    }

    private static PigLifecycleScenario scenario(int lifecycleId, int actorId, int pigId) {
        return scenario(lifecycleId, actorId, pigId, 329, 1, 0);
    }

    private static PigLifecycleScenario scenario(int lifecycleId, int actorId, int pigId,
            int saddle, int before, int after) {
        return new FixedScenario(lifecycleId, new PigSaddleMountObservation(spawn(pigId), actorId,
                saddle, before, after, 0, 0, actorId, pigId, false));
    }

    private static PigLifecycleScenario wrongAttachment(int lifecycleId, int actorId, int pigId) {
        return new FixedScenario(lifecycleId, new PigSaddleMountObservation(spawn(pigId), actorId,
                329, 1, 0, 0, 0, actorId, lifecycleId, false));
    }

    private static EntityConformancePlan plan() { return plan(ConformanceLayer.ARCHETYPE); }
    private static EntityConformancePlan plan(ConformanceLayer interaction) {
        EntityConformanceProfile pig = new EntityConformanceProfile("b1.7.3:entity/090",
                Collections.singletonList("animal"), false,
                Collections.singletonMap("interaction-state", interaction));
        List<EntityConformanceTemplate> templates = Arrays.asList(
                new EntityConformanceTemplate("spawn-materialization", ConformanceLayer.UNIVERSAL),
                new EntityConformanceTemplate("movement-policy", ConformanceLayer.ARCHETYPE),
                new EntityConformanceTemplate("damage-death", ConformanceLayer.ARCHETYPE),
                new EntityConformanceTemplate("drop-matrix", ConformanceLayer.ARCHETYPE),
                new EntityConformanceTemplate("interaction-state", ConformanceLayer.ARCHETYPE));
        return new EntityConformancePlan(Collections.singletonList(pig), templates);
    }

    private static RemoteMobSpawn spawn(int entity) {
        return new RemoteMobSpawn(entity, 90, 32, 2048, 32, 0, 0, 2, 0);
    }

    private static final class FixedScenario implements PigLifecycleScenario,
            EntityLifecycleScenario {
        private final int lifecycleId;
        private final PigSaddleMountObservation interaction;
        private boolean killed;
        FixedScenario(int lifecycleId, PigSaddleMountObservation interaction) {
            this.lifecycleId = lifecycleId; this.interaction = interaction;
        }
        @Override public EntityLifecycleScenario lifecycle() { return this; }
        @Override public PigSaddleMountObservation observeSaddleAndMount() { return interaction; }
        @Override public RemoteMobSpawn materialize(int type) { return spawn(lifecycleId); }
        @Override public RemoteMobMovement awaitMovement(int entity) {
            return new RemoteMobMovement(lifecycleId, 31, 32, 2048, 32, 33, 2048, 32, 0, 0);
        }
        @Override public void kill(int entity) { killed = true; }
        @Override public RemoteMobDeath awaitDeath(int entity) {
            if (!killed) throw new AssertionError("pig death observed before kill");
            return new RemoteMobDeath(lifecycleId, 3, true);
        }
        @Override public RemoteDroppedItem awaitDrop(RemoteItemStack expected) {
            return new RemoteDroppedItem(lifecycleId + 1000, PORK, 1, 64, 1, 0, 0, 0);
        }
    }

    private static void reject(Runnable action) {
        try { action.run(); throw new AssertionError("invalid pig lifecycle accepted"); }
        catch (IllegalArgumentException | IllegalStateException expected) { }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
