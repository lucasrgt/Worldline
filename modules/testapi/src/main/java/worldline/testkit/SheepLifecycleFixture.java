package worldline.testkit;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteMobSpawn;

/** Executes materialization, death/drop, dye/shear and persistence as one sheep subsystem. */
public final class SheepLifecycleFixture {
    private static final String SUBJECT = "b1.7.3:entity/091";
    private static final int TYPE = 91;
    private static final EntityDropExpectation WHITE_WOOL =
            new EntityDropExpectation(35, 1, 1, 0);

    private SheepLifecycleFixture() { }

    public static SheepLifecycleEvidence execute(EntityConformancePlan plan,
            int maximumAttempts, SheepLifecycleScenario scenario) {
        if (plan == null || scenario == null) throw new NullPointerException("sheep lifecycle");
        EntityLifecycleScenario lethalScenario = scenario.lethal();
        if (lethalScenario == null) throw new IllegalStateException("sheep lethal scene absent");
        EntityLifecycleEvidence lethal = EntityLifecycleFixture.execute(plan, SUBJECT, TYPE,
                WHITE_WOOL, maximumAttempts, lethalDimensions(), lethalScenario);
        SheepDyeShearObservation interaction = scenario.observeDyeAndShear();
        SheepPersistenceObservation persistence = scenario.observePersistence();
        validateInteraction(interaction); validatePersistence(persistence);
        List<EntityConformanceCase> claims = Arrays.asList(
                claim(plan, "spawn-materialization", ConformanceLayer.UNIVERSAL),
                claim(plan, "save-reload", ConformanceLayer.UNIVERSAL),
                claim(plan, "damage-death", ConformanceLayer.ARCHETYPE),
                claim(plan, "drop-matrix", ConformanceLayer.ARCHETYPE),
                claim(plan, "interaction-state", ConformanceLayer.ARCHETYPE));
        return new SheepLifecycleEvidence(claims, lethal, interaction,
                persistence, maximumAttempts);
    }

    private static void validateInteraction(SheepDyeShearObservation value) {
        if (value == null) throw new IllegalStateException("sheep interaction scene absent");
        RemoteMobSpawn first = value.first(), second = value.second();
        if (!sheep(first) || !sheep(second) || first.entityId() == second.entityId()
                || value.firstDyeDamage() != 1 || value.secondDyeDamage() != 11
                || value.deathObserved()) {
            throw new IllegalStateException("paired living sheep dye boundary drifted");
        }
        wool(value.firstWool(), 14, first, second);
        wool(value.secondWool(), 4, first, second);
    }

    private static void validatePersistence(SheepPersistenceObservation value) {
        if (value == null || value.dyedMetadata() != 14 || value.shearedMetadata() != 30
                || value.persistedMetadata() != 30 || value.controlMetadata() != 0
                || value.repeatWoolObserved() || !value.nbtShearedBefore()
                || value.nbtShearedAfter() || value.mutatedMetadata() != 14
                || value.reshearedMetadata() != 30 || value.changedSheepCount() != 1
                || value.restarts() != 3) {
            throw new IllegalStateException("sheared sheep persistence boundary drifted");
        }
        RemoteDroppedItem wool = value.recoveredWool();
        if (wool == null || wool.entityId() <= 0
                || !new RemoteItemStack(35, 1, 14).equals(wool.item())) {
            throw new IllegalStateException("recovered red wool boundary drifted");
        }
    }

    private static void wool(RemoteDroppedItem wool, int damage,
            RemoteMobSpawn first, RemoteMobSpawn second) {
        if (wool == null || wool.entityId() <= 0 || wool.entityId() == first.entityId()
                || wool.entityId() == second.entityId()
                || !new RemoteItemStack(35, 1, damage).equals(wool.item())) {
            throw new IllegalStateException("dyed wool drop boundary drifted: " + damage);
        }
    }

    private static boolean sheep(RemoteMobSpawn spawn) {
        return spawn != null && spawn.entityId() > 0 && spawn.legacyType() == TYPE;
    }

    private static EntityConformanceCase claim(EntityConformancePlan plan,
            String template, ConformanceLayer expected) {
        EntityConformanceCase claim = plan.caseFor(SUBJECT, template);
        if (claim.layer() != expected) {
            throw new IllegalArgumentException(template + " must be " + expected);
        }
        return claim;
    }

    private static Set<String> lethalDimensions() {
        return new LinkedHashSet<String>(Arrays.asList(
                "spawn-materialization", "damage-death", "drop-matrix"));
    }
}
