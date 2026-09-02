package worldline.testapi;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import worldline.api.BlockPosition;
import worldline.api.RemoteExplosion;
import worldline.api.RemoteMobSpawn;
import worldline.api.RemoteObjectSpawn;

/** Executes zombie identity, skeleton ranged, spider climb and creeper fuse as one matrix. */
public final class HostileBehaviorFixture {
    private HostileBehaviorFixture() { }

    public static HostileBehaviorEvidence execute(EntityConformancePlan plan,
            HostileBehaviorScenario scenario) {
        if (plan == null || scenario == null) throw new NullPointerException("hostile behavior");
        List<EntityConformanceCase> claims = Arrays.asList(
                claim(plan, "b1.7.3:entity/054", "spawn-materialization", ConformanceLayer.UNIVERSAL),
                claim(plan, "b1.7.3:entity/051", "interaction-state", ConformanceLayer.ARCHETYPE),
                claim(plan, "b1.7.3:entity/052", "movement-policy", ConformanceLayer.ARCHETYPE),
                claim(plan, "b1.7.3:entity/050", "tick-lifecycle", ConformanceLayer.SINGULAR));
        HostileBehaviorObservation value = scenario.observe();
        if (value == null || value.actorEntityId() <= 0 || value.nightTime() != 14000) {
            throw new IllegalStateException("hostile behavior scene absent");
        }
        validateMobs(value); validateSkeleton(value); validateSpider(value); validateCreeper(value);
        return new HostileBehaviorEvidence(claims, value);
    }

    private static void validateMobs(HostileBehaviorObservation value) {
        RemoteMobSpawn[] mobs = {value.zombie(), value.skeleton(), value.spider(), value.creeper()};
        int[] types = {54, 51, 52, 50};
        Set<Integer> identities = new HashSet<Integer>();
        identities.add(value.actorEntityId());
        for (int index = 0; index < mobs.length; index++) {
            RemoteMobSpawn mob = mobs[index];
            if (mob == null || mob.entityId() <= 0 || mob.legacyType() != types[index]
                    || !identities.add(mob.entityId())) {
                throw new IllegalStateException("hostile Packet24 identity drifted");
            }
        }
    }

    private static void validateSkeleton(HostileBehaviorObservation value) {
        RemoteObjectSpawn first = value.firstArrow(), second = value.secondArrow();
        int skeleton = value.skeleton().entityId();
        if (!arrow(first, skeleton) || !arrow(second, skeleton)
                || first.entityId() == second.entityId()
                || hostileIdentity(value, first.entityId())
                || hostileIdentity(value, second.entityId())
                || !value.diamondArmorObserved()) {
            throw new IllegalStateException("skeleton ranged boundary drifted");
        }
    }

    private static boolean hostileIdentity(HostileBehaviorObservation value, int identity) {
        return identity == value.actorEntityId()
                || identity == value.zombie().entityId()
                || identity == value.skeleton().entityId()
                || identity == value.spider().entityId()
                || identity == value.creeper().entityId();
    }

    private static boolean arrow(RemoteObjectSpawn value, int skeleton) {
        return value != null && value.entityId() > 0 && value.type() == 60
                && value.throwerId() == skeleton;
    }

    private static void validateSpider(HostileBehaviorObservation value) {
        if (!value.cobblestonePositiveY() || !value.planksPositiveY()
                || value.cobblestoneBlockId() != 4 || value.planksBlockId() != 5) {
            throw new IllegalStateException("spider wall-climb boundary drifted");
        }
    }

    private static void validateCreeper(HostileBehaviorObservation value) {
        RemoteExplosion explosion = value.creeperExplosion();
        BlockPosition dirt = value.dirtCell(), wool = value.woolCell();
        if (!value.proximityFuseObserved() || explosion == null || explosion.strength() != 3F
                || dirt == null || wool == null || dirt.equals(wool)
                || !explosion.destroyed().contains(dirt) || !explosion.destroyed().contains(wool)
                || !value.dirtPersistedAir() || !value.woolPersistedAir()) {
            throw new IllegalStateException("creeper fuse or crater boundary drifted");
        }
    }

    private static EntityConformanceCase claim(EntityConformancePlan plan, String subject,
            String template, ConformanceLayer expected) {
        EntityConformanceCase claim = plan.caseFor(subject, template);
        if (claim.layer() != expected) {
            throw new IllegalArgumentException(claim.claimId() + " must be " + expected);
        }
        return claim;
    }
}
