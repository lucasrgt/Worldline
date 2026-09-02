package worldline.testapi;

import java.util.Arrays;
import java.util.List;

/** Executes primed-TNT materialization and the exact isolated internal fuse lifecycle. */
public final class TntLifecycleFixture {
    private static final String SUBJECT = "b1.7.3:entity/020";

    private TntLifecycleFixture() { }

    public static TntLifecycleEvidence execute(EntityConformancePlan plan,
            TntLifecycleScenario scenario) {
        if (plan == null || scenario == null) throw new NullPointerException("TNT lifecycle");
        TntFuseLifecycleObservation value = scenario.observe();
        if (value == null || value.actorEntityId() <= 0) {
            throw new IllegalStateException("TNT lifecycle scene absent");
        }
        ObjectMaterializationEvidence spawn = ObjectMaterializationFixture.execute(plan,
                SUBJECT, ObjectSpawnExpectation.withThrower(50, 0), ignored -> value.primed());
        EntityConformanceCase fuse = plan.caseFor(SUBJECT, "tick-lifecycle");
        if (fuse.layer() != ConformanceLayer.SINGULAR) {
            throw new IllegalArgumentException("TNT fuse lifecycle must be singular");
        }
        if (value.primed().entityId() == value.actorEntityId() || value.seedFuse() != 80
                || value.tick1Fuse() != 79 || value.tick40Fuse() != 40
                || value.tick79Fuse() != 1 || value.tick80Fuse() != 0
                || value.tick81Fuse() != -1 || !value.seedPresent()
                || !value.midPresent() || value.midDead() || value.terminalPresent()
                || !value.terminalDead() || !value.motionZeroed()
                || value.unprimedBlockId() != 46 || value.unprimedEntityObserved()) {
            throw new IllegalStateException("TNT fuse or control boundary drifted");
        }
        List<EntityConformanceCase> claims = Arrays.asList(spawn.claim(), fuse);
        return new TntLifecycleEvidence(claims, spawn, value);
    }
}
