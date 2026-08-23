package worldline.api;

/** Qualified player boundaries kept outside the primary compatibility catalog. */
public final class WorldlinePlayerBehaviors {
    public static final WorldlineBehavior WOLF_SIT = define("wolf-sit",
            "Tamed wolf sits then stands on successive owner clicks");
    public static final WorldlineBehavior ARMOR_DURABILITY_HIT = define("armor-durability-hit",
            "Worn armor loses durability after a hostile melee hit");
    public static final WorldlineBehavior FALL_WATER_CANCEL = define("fall-water-cancel",
            "Damaging-height water landing emits no fall Packet8");
    public static final WorldlineBehavior LADDER_POSE_CLIMB = define("ladder-pose-climb",
            "Player colliding with a ladder climbs or holds versus falling in air");

    private WorldlinePlayerBehaviors() {}

    private static WorldlineBehavior define(String token, String subject) {
        return WorldlineBehavior.define(token, WorldlineFamily.PLAYER, subject);
    }
}
