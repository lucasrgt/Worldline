package worldline.testapi;

import java.util.Arrays;
import java.util.List;
import worldline.api.RemoteMobSpawn;

/** Executes controlled materialization and owner sit/stand as one wolf subsystem. */
public final class WolfLifecycleFixture {
    private static final String SUBJECT = "b1.7.3:entity/095";

    private WolfLifecycleFixture() { }

    public static WolfLifecycleEvidence execute(EntityConformancePlan plan,
            WolfLifecycleScenario scenario) {
        if (plan == null || scenario == null) throw new NullPointerException("wolf lifecycle");
        EntityConformanceCase spawn = claim(plan, "spawn-materialization",
                ConformanceLayer.UNIVERSAL);
        EntityConformanceCase interaction = claim(plan, "interaction-state",
                ConformanceLayer.SINGULAR);
        WolfOwnerStateObservation value = scenario.observe();
        if (value == null || value.actorEntityId() <= 0) {
            throw new IllegalStateException("wolf owner-state scene absent");
        }
        RemoteMobSpawn wolf = value.wolf();
        if (wolf == null || wolf.entityId() <= 0 || wolf.legacyType() != 95
                || wolf.entityId() == value.actorEntityId()) {
            throw new IllegalStateException("wolf Packet24 identity drifted");
        }
        if (value.tameStatus() != 7 || value.boneItemId() != 352
                || value.collarDyeDamage() != 4 || !value.redCollarObserved()
                || value.interactionItemId() != 280 || value.interactionButton() != 0
                || !sitting(value.initialSittingFlags())
                || !standing(value.standingFlags()) || !sitting(value.sittingFlags())
                || !standing(value.finalStandingFlags()) || value.deathObserved()) {
            throw new IllegalStateException("wolf tame or owner-state boundary drifted");
        }
        List<EntityConformanceCase> claims = Arrays.asList(spawn, interaction);
        return new WolfLifecycleEvidence(claims, value);
    }

    private static boolean sitting(int flags) { return (flags & 5) == 5; }
    private static boolean standing(int flags) { return (flags & 4) != 0 && (flags & 1) == 0; }

    private static EntityConformanceCase claim(EntityConformancePlan plan,
            String template, ConformanceLayer expected) {
        EntityConformanceCase claim = plan.caseFor(SUBJECT, template);
        if (claim.layer() != expected) {
            throw new IllegalArgumentException(template + " must be " + expected);
        }
        return claim;
    }
}
