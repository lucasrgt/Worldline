package worldline.testkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteMobDeath;
import worldline.api.RemoteMobMovement;
import worldline.api.RemoteMobSpawn;

/** Executes selected spawn, movement, death and drop claims as one coherent lifecycle. */
public final class EntityLifecycleFixture {
    private static final List<String> ORDER = Arrays.asList("spawn-materialization",
            "movement-policy", "damage-death", "drop-matrix");

    private EntityLifecycleFixture() { }

    public static EntityLifecycleEvidence execute(EntityConformancePlan plan, String subject,
            int legacyType, RemoteItemStack expectedDrop, Set<String> dimensions,
            EntityLifecycleScenario scenario) {
        if (plan == null || scenario == null) throw new NullPointerException("entity lifecycle");
        if (legacyType < 1 || legacyType > 127) throw new IllegalArgumentException("legacy type");
        Set<String> selected = selected(dimensions);
        if (selected.contains("drop-matrix") && !selected.contains("damage-death")) {
            throw new IllegalArgumentException("drop requires death");
        }
        if (selected.contains("drop-matrix") && expectedDrop == null) {
            throw new IllegalArgumentException("drop expectation");
        }
        List<EntityConformanceCase> claims = claims(plan, subject, selected);
        RemoteMobSpawn spawn = scenario.materialize(legacyType);
        require(spawn != null && spawn.legacyType() == legacyType && spawn.entityId() > 0,
                "entity materialization identity drifted");
        RemoteMobMovement movement = null;
        if (selected.contains("movement-policy")) {
            movement = scenario.awaitMovement(spawn.entityId());
            require(movement != null && movement.entityId() == spawn.entityId(),
                    "entity movement identity drifted");
        }
        RemoteMobDeath death = null;
        if (selected.contains("damage-death")) {
            scenario.kill(spawn.entityId());
            death = scenario.awaitDeath(spawn.entityId());
            require(death != null && death.entityId() == spawn.entityId()
                    && death.deathStatus() == 3 && death.destroyPacket() == 29,
                    "entity death boundary drifted");
        }
        RemoteDroppedItem drop = null;
        if (selected.contains("drop-matrix")) {
            drop = scenario.awaitDrop(expectedDrop);
            require(drop != null && drop.entityId() != spawn.entityId()
                    && expectedDrop.equals(drop.item()), "entity drop boundary drifted");
        }
        return new EntityLifecycleEvidence(claims, spawn, movement, death, drop, expectedDrop);
    }

    private static Set<String> selected(Set<String> source) {
        if (source == null) throw new NullPointerException("dimensions");
        Set<String> result = new LinkedHashSet<String>(source);
        if (!result.contains("spawn-materialization") || !ORDER.containsAll(result)) {
            throw new IllegalArgumentException("unsupported lifecycle dimensions");
        }
        return result;
    }

    private static List<EntityConformanceCase> claims(EntityConformancePlan plan, String subject,
            Set<String> selected) {
        List<EntityConformanceCase> result = new ArrayList<EntityConformanceCase>();
        for (String dimension : ORDER) {
            if (selected.contains(dimension)) result.add(plan.caseFor(subject, dimension));
        }
        return result;
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
