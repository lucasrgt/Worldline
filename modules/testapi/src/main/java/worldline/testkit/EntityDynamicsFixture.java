package worldline.testkit;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Executes the complete qualified ghast, slime, boat and minecart dynamics matrix. */
public final class EntityDynamicsFixture {
    private EntityDynamicsFixture() { }

    public static EntityDynamicsEvidence execute(EntityConformancePlan plan,
            EntityDynamicsScenario scenario) {
        if (plan == null || scenario == null) throw new NullPointerException("entity dynamics");
        List<EntityConformanceCase> claims = Arrays.asList(
                singular(plan, "b1.7.3:entity/040"), singular(plan, "b1.7.3:entity/041"),
                singular(plan, "b1.7.3:entity/055"), singular(plan, "b1.7.3:entity/056"));
        Map<EntityDynamicsScene, EntityDynamicsObservation> observations =
                new EnumMap<EntityDynamicsScene, EntityDynamicsObservation>(EntityDynamicsScene.class);
        for (EntityDynamicsScene scene : EntityDynamicsScene.values()) {
            EntityDynamicsObservation observation = scenario.observe(scene);
            if (observation == null || observations.put(scene, observation) != null) {
                throw new IllegalStateException("entity dynamics scene absent: " + scene);
            }
        }
        validate(observations);
        return new EntityDynamicsEvidence(claims, observations);
    }

    static void validateSlime(EntityDynamicsObservation open,
            EntityDynamicsObservation roof) {
        vertical(open, "slime open"); vertical(roof, "slime roof");
        if (!open.sawAir() || !open.sawGround() || open.verticalSpanMilli() <= 100) {
            throw new IllegalStateException("open slime jump/landing envelope drifted");
        }
        if (roof.verticalSpanMilli() >= 700) {
            throw new IllegalStateException("roof-bounded slime envelope drifted");
        }
    }

    private static void validate(Map<EntityDynamicsScene, EntityDynamicsObservation> values) {
        EntityDynamicsObservation ghastOpen = values.get(EntityDynamicsScene.GHAST_OPEN);
        EntityDynamicsObservation ghastRoof = values.get(EntityDynamicsScene.GHAST_ROOF);
        vertical(ghastOpen, "ghast open"); vertical(ghastRoof, "ghast roof");
        if (ghastOpen.verticalSpanMilli() <= 200 || ghastRoof.verticalSpanMilli() >= 200) {
            throw new IllegalStateException("ghast vertical envelope drifted");
        }
        validateSlime(values.get(EntityDynamicsScene.SLIME_OPEN),
                values.get(EntityDynamicsScene.SLIME_LOW_ROOF));
        EntityDynamicsObservation boatOpen = values.get(EntityDynamicsScene.BOAT_OPEN);
        EntityDynamicsObservation boatWall = values.get(EntityDynamicsScene.BOAT_WALL);
        horizontal(boatOpen, "boat open"); horizontal(boatWall, "boat wall");
        if (boatOpen.horizontalCollision() || boatOpen.finalXMilli() <= 9300
                || !boatWall.horizontalCollision()) {
            throw new IllegalStateException("boat travel/collision envelope drifted");
        }
        EntityDynamicsObservation cartShort = values.get(EntityDynamicsScene.MINECART_SHORT_RAIL);
        EntityDynamicsObservation cartLong = values.get(EntityDynamicsScene.MINECART_LONG_RAIL);
        horizontal(cartShort, "minecart short"); horizontal(cartLong, "minecart long");
        if (Math.abs(cartShort.motionXMilli()) >= 50
                || Math.abs(cartLong.motionXMilli()) <= 50) {
            throw new IllegalStateException("minecart rail/brake envelope drifted");
        }
    }

    private static EntityConformanceCase singular(EntityConformancePlan plan, String subject) {
        EntityConformanceCase claim = plan.caseFor(subject, "movement-policy");
        if (claim.layer() != ConformanceLayer.SINGULAR) {
            throw new IllegalArgumentException(subject + " movement must be singular");
        }
        return claim;
    }

    private static void vertical(EntityDynamicsObservation value, String scene) {
        if (value == null || !value.vertical()) {
            throw new IllegalStateException(scene + " must be vertical");
        }
    }

    private static void horizontal(EntityDynamicsObservation value, String scene) {
        if (value == null || value.vertical()) {
            throw new IllegalStateException(scene + " must be horizontal");
        }
    }
}
