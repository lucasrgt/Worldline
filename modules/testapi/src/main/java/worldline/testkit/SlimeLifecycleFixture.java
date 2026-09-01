package worldline.testkit;

import java.util.Arrays;
import java.util.List;
import worldline.api.RemoteDroppedItem;
import worldline.api.RemoteMobDeath;
import worldline.api.RemoteMobSpawn;

/** Executes slime spawn, motion, split and slimeball drop as one singular capability. */
public final class SlimeLifecycleFixture {
    private static final int TYPE = 55;
    private static final EntityDropExpectation SLIMEBALL = new EntityDropExpectation(341, 1, 2, 0);

    private SlimeLifecycleFixture() { }

    public static SlimeLifecycleEvidence execute(EntityConformancePlan plan, String subject,
            int splitMaximumAttempts, int dropMaximumAttempts, SlimeLifecycleScenario scenario) {
        if (plan == null || scenario == null) throw new NullPointerException("slime lifecycle");
        bounded(splitMaximumAttempts); bounded(dropMaximumAttempts);
        List<EntityConformanceCase> claims = Arrays.asList(
                claim(plan, subject, "spawn-materialization", ConformanceLayer.UNIVERSAL),
                claim(plan, subject, "movement-policy", ConformanceLayer.SINGULAR),
                claim(plan, subject, "interaction-state", ConformanceLayer.SINGULAR),
                claim(plan, subject, "drop-matrix", ConformanceLayer.SINGULAR));

        SlimeMotionObservation open = scenario.observeMotion(SlimeMotionScene.OPEN);
        SlimeMotionObservation roof = scenario.observeMotion(SlimeMotionScene.LOW_ROOF);
        EntityDynamicsFixture.validateSlime(vertical(open), vertical(roof));

        SlimeSplitObservation split = awaitSplit(splitMaximumAttempts, scenario);
        SlimeDropObservation drop = awaitDrop(dropMaximumAttempts, scenario);
        return new SlimeLifecycleEvidence(claims, split, drop,
                splitMaximumAttempts, dropMaximumAttempts);
    }

    private static SlimeSplitObservation awaitSplit(int maximum,
            SlimeLifecycleScenario scenario) {
        for (int attempt = 1; attempt <= maximum; attempt++) {
            SlimeSplitObservation value = scenario.attemptSplit(attempt);
            if (value == null) continue;
            RemoteMobSpawn parent = value.parent();
            requireSpawn(parent); requireDeath(parent, value.death());
            for (RemoteMobSpawn child : value.children()) {
                requireSpawn(child);
                if (child.entityId() == parent.entityId() || !near(parent, child)) {
                    throw new IllegalStateException("slime child identity or locality drifted");
                }
            }
            if (value.parentSize() > 1 && !value.children().isEmpty()) return value;
        }
        throw new IllegalStateException("slime split absent after bounded attempts: " + maximum);
    }

    private static SlimeDropObservation awaitDrop(int maximum, SlimeLifecycleScenario scenario) {
        for (int attempt = 1; attempt <= maximum; attempt++) {
            SlimeDropObservation value = scenario.attemptDrop(attempt);
            if (value == null) continue;
            RemoteMobSpawn slime = value.slime();
            requireSpawn(slime); requireDeath(slime, value.death());
            RemoteDroppedItem drop = value.drop();
            if (drop == null) continue;
            if (value.size() != 1 || drop.entityId() == slime.entityId()
                    || !SLIMEBALL.matches(drop)) {
                throw new IllegalStateException("small-slime drop boundary drifted");
            }
            return value;
        }
        throw new IllegalStateException("slimeball absent after bounded attempts: " + maximum);
    }

    private static EntityConformanceCase claim(EntityConformancePlan plan, String subject,
            String template, ConformanceLayer layer) {
        EntityConformanceCase claim = plan.caseFor(subject, template);
        if (claim.layer() != layer) throw new IllegalArgumentException(template + " must be " + layer);
        return claim;
    }

    private static EntityDynamicsObservation vertical(SlimeMotionObservation value) {
        if (value == null) throw new IllegalStateException("slime motion scene absent");
        return EntityDynamicsObservation.vertical(value.verticalSpanMilli(),
                value.sawAir(), value.sawGround());
    }

    private static void requireSpawn(RemoteMobSpawn spawn) {
        if (spawn == null || spawn.entityId() <= 0 || spawn.legacyType() != TYPE || spawn.y() >= 16D) {
            throw new IllegalStateException("slime Packet24 identity drifted");
        }
    }

    private static void requireDeath(RemoteMobSpawn spawn, RemoteMobDeath death) {
        if (death == null || death.entityId() != spawn.entityId() || death.deathStatus() != 3
                || death.destroyPacket() != 29 || !death.hurtObserved()) {
            throw new IllegalStateException("slime death boundary drifted");
        }
    }

    private static boolean near(RemoteMobSpawn parent, RemoteMobSpawn child) {
        double x = parent.x() - child.x(), y = parent.y() - child.y(), z = parent.z() - child.z();
        return x * x + y * y + z * z <= 36D;
    }

    private static void bounded(int value) {
        if (value < 1 || value > 64) throw new IllegalArgumentException("maximum attempts");
    }
}
