package worldline.testapi;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteMobSpawn;

/** Executes all five qualified pig lifecycle claims as one coherent subsystem. */
public final class PigLifecycleFixture {
    private static final String SUBJECT = "b1.7.3:entity/090";
    private static final int TYPE = 90;
    private static final RemoteItemStack PORK = new RemoteItemStack(319, 1, 0);

    private PigLifecycleFixture() { }

    public static PigLifecycleEvidence execute(EntityConformancePlan plan,
            PigLifecycleScenario scenario) {
        if (plan == null || scenario == null) throw new NullPointerException("pig lifecycle");
        EntityLifecycleScenario lifecycleScenario = scenario.lifecycle();
        if (lifecycleScenario == null) throw new IllegalStateException("pig lifecycle scene absent");
        EntityLifecycleEvidence lifecycle = EntityLifecycleFixture.execute(plan, SUBJECT,
                TYPE, PORK, dimensions(), lifecycleScenario);
        PigSaddleMountObservation interaction = scenario.observeSaddleAndMount();
        validateInteraction(interaction);
        List<EntityConformanceCase> claims = Arrays.asList(
                claim(plan, "spawn-materialization", ConformanceLayer.UNIVERSAL),
                claim(plan, "movement-policy", ConformanceLayer.ARCHETYPE),
                claim(plan, "damage-death", ConformanceLayer.ARCHETYPE),
                claim(plan, "drop-matrix", ConformanceLayer.ARCHETYPE),
                claim(plan, "interaction-state", ConformanceLayer.ARCHETYPE));
        return new PigLifecycleEvidence(claims, lifecycle, interaction);
    }

    private static void validateInteraction(PigSaddleMountObservation value) {
        if (value == null) throw new IllegalStateException("pig saddle scene absent");
        RemoteMobSpawn pig = value.pig();
        if (pig == null || pig.entityId() <= 0 || pig.legacyType() != TYPE
                || value.actorEntityId() <= 0 || pig.entityId() == value.actorEntityId()
                || value.saddleItemId() != 329 || value.saddleCountBefore() != 1
                || value.saddleCountAfter() != 0 || value.saddleInteractionButton() != 0
                || value.mountInteractionButton() != 0
                || value.attachedRiderEntityId() != value.actorEntityId()
                || value.attachedVehicleEntityId() != pig.entityId()
                || value.deathObserved()) {
            throw new IllegalStateException("pig saddle or mount boundary drifted");
        }
    }

    private static EntityConformanceCase claim(EntityConformancePlan plan,
            String template, ConformanceLayer expected) {
        EntityConformanceCase claim = plan.caseFor(SUBJECT, template);
        if (claim.layer() != expected) {
            throw new IllegalArgumentException(template + " must be " + expected);
        }
        return claim;
    }

    private static Set<String> dimensions() {
        return new LinkedHashSet<String>(Arrays.asList("spawn-materialization",
                "movement-policy", "damage-death", "drop-matrix"));
    }
}
