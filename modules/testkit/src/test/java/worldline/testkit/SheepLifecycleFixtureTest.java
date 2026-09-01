package worldline.testkit;

import java.util.Arrays;
import java.util.Collections;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteMobDeath;
import worldline.api.RemoteMobMovement;
import worldline.api.RemoteMobSpawn;

/** Proves the complete sheep subsystem, normalization, bounds and fail-closed edges. */
public final class SheepLifecycleFixtureTest {
    private SheepLifecycleFixtureTest() { }

    public static void main(String[] arguments) {
        execute();
        System.out.println("SheepLifecycleFixtureTest passed");
    }

    static void execute() {
        EntityConformancePlan plan = plan();
        SheepLifecycleEvidence first = SheepLifecycleFixture.execute(plan, 8, scenario(10));
        require(first.claims().size() == 5
                && first.claims().get(0).layer() == ConformanceLayer.UNIVERSAL
                && first.claims().get(1).layer() == ConformanceLayer.UNIVERSAL
                && first.claims().get(2).layer() == ConformanceLayer.ARCHETYPE
                && first.claims().get(4).layer() == ConformanceLayer.ARCHETYPE,
                "sheep conformance routes drifted");
        require(first.maximumAttempts() == 8 && first.lethal().maximumAttempts() == 8,
                "sheep bounded death/drop contract drifted");
        SheepLifecycleEvidence second = SheepLifecycleFixture.execute(plan, 8, scenario(100));
        require(first.equals(second) && first.hashCode() == second.hashCode(),
                "fresh sheep identities leaked into evidence");
        rejectsInvalidInteraction(plan);
        rejectsInvalidPersistence(plan);
        reject(() -> SheepLifecycleFixture.execute(plan, 0, scenario(1)));
        reject(() -> SheepLifecycleFixture.execute(plan, 8, new SheepLifecycleScenario() {
            @Override public EntityLifecycleScenario lethal() { return null; }
            @Override public SheepDyeShearObservation observeDyeAndShear() { return interaction(1); }
            @Override public SheepPersistenceObservation observePersistence() { return persistence(1); }
        }));
    }

    private static void rejectsInvalidInteraction(EntityConformancePlan plan) {
        rejectInteraction(plan, new SheepDyeShearObservation(spawn(1), spawn(1), 1, 11,
                wool(3, 14), wool(4, 4), false));
        rejectInteraction(plan, new SheepDyeShearObservation(spawn(1), spawn(2), 0, 11,
                wool(3, 14), wool(4, 4), false));
        rejectInteraction(plan, new SheepDyeShearObservation(spawn(1), spawn(2), 1, 11,
                wool(3, 0), wool(4, 4), false));
        rejectInteraction(plan, new SheepDyeShearObservation(spawn(1), spawn(2), 1, 11,
                wool(3, 14), wool(4, 4), true));
    }

    private static void rejectsInvalidPersistence(EntityConformancePlan plan) {
        rejectPersistence(plan, persistence(14, 29, 30, 0, false,
                true, false, 14, 30, 1, 3, wool(5, 14)));
        rejectPersistence(plan, persistence(14, 30, 30, 0, true,
                true, false, 14, 30, 1, 3, wool(5, 14)));
        rejectPersistence(plan, persistence(14, 30, 30, 0, false,
                false, false, 14, 30, 1, 3, wool(5, 14)));
        rejectPersistence(plan, persistence(14, 30, 30, 0, false,
                true, false, 14, 30, 2, 3, wool(5, 14)));
        rejectPersistence(plan, persistence(14, 30, 30, 0, false,
                true, false, 14, 30, 1, 2, wool(5, 14)));
        rejectPersistence(plan, persistence(14, 30, 30, 0, false,
                true, false, 14, 30, 1, 3, wool(5, 4)));
    }

    private static void rejectInteraction(EntityConformancePlan plan,
            SheepDyeShearObservation observation) {
        reject(() -> SheepLifecycleFixture.execute(plan, 8,
                scenario(new FixedLethal(20), observation, persistence(20))));
    }

    private static void rejectPersistence(EntityConformancePlan plan,
            SheepPersistenceObservation observation) {
        reject(() -> SheepLifecycleFixture.execute(plan, 8,
                scenario(new FixedLethal(20), interaction(20), observation)));
    }

    private static SheepLifecycleScenario scenario(int base) {
        return scenario(new FixedLethal(base), interaction(base), persistence(base));
    }

    private static SheepLifecycleScenario scenario(EntityLifecycleScenario lethal,
            SheepDyeShearObservation interaction, SheepPersistenceObservation persistence) {
        return new SheepLifecycleScenario() {
            @Override public EntityLifecycleScenario lethal() { return lethal; }
            @Override public SheepDyeShearObservation observeDyeAndShear() { return interaction; }
            @Override public SheepPersistenceObservation observePersistence() { return persistence; }
        };
    }

    private static SheepDyeShearObservation interaction(int base) {
        return new SheepDyeShearObservation(spawn(base), spawn(base + 1), 1, 11,
                wool(base + 2, 14), wool(base + 3, 4), false);
    }

    private static SheepPersistenceObservation persistence(int base) {
        return persistence(14, 30, 30, 0, false,
                true, false, 14, 30, 1, 3, wool(base + 4, 14));
    }

    private static SheepPersistenceObservation persistence(int dyed, int sheared,
            int persisted, int control, boolean repeat, boolean nbtBefore, boolean nbtAfter,
            int mutated, int resheared, int changed, int restarts, RemoteDroppedItem recovered) {
        return new SheepPersistenceObservation(dyed, sheared, persisted, control, repeat,
                nbtBefore, nbtAfter, mutated, resheared, changed, restarts, recovered);
    }

    private static RemoteMobSpawn spawn(int entity) {
        return new RemoteMobSpawn(entity, 91, entity * 32, 2048, 32, 0, 0, 2, 0);
    }

    private static RemoteDroppedItem wool(int entity, int damage) {
        return new RemoteDroppedItem(entity, new RemoteItemStack(35, 1, damage),
                entity, 64, 1, 0, 0, 0);
    }

    private static EntityConformancePlan plan() {
        EntityConformanceProfile sheep = new EntityConformanceProfile(
                "b1.7.3:entity/091", Collections.singletonList("animal"), false,
                Collections.<String, ConformanceLayer>emptyMap());
        return new EntityConformancePlan(Collections.singletonList(sheep), Arrays.asList(
                new EntityConformanceTemplate("spawn-materialization", ConformanceLayer.UNIVERSAL),
                new EntityConformanceTemplate("save-reload", ConformanceLayer.UNIVERSAL),
                new EntityConformanceTemplate("damage-death", ConformanceLayer.ARCHETYPE),
                new EntityConformanceTemplate("drop-matrix", ConformanceLayer.ARCHETYPE),
                new EntityConformanceTemplate("interaction-state", ConformanceLayer.ARCHETYPE)));
    }

    private static final class FixedLethal implements EntityLifecycleScenario {
        private final int entity;
        private boolean killed;
        FixedLethal(int entity) { this.entity = entity; }
        @Override public RemoteMobSpawn materialize(int expected) { return spawn(entity); }
        @Override public RemoteMobMovement awaitMovement(int id) { return null; }
        @Override public void kill(int id) { killed = true; }
        @Override public RemoteMobDeath awaitDeath(int id) {
            if (!killed) throw new AssertionError("sheep death observed before kill");
            return new RemoteMobDeath(entity, 3, true);
        }
        @Override public RemoteDroppedItem awaitDrop(RemoteItemStack expected) {
            return wool(entity + 20, 0);
        }
    }

    private static void reject(Runnable action) {
        try { action.run(); throw new AssertionError("invalid sheep lifecycle accepted"); }
        catch (IllegalArgumentException | IllegalStateException expected) { }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
