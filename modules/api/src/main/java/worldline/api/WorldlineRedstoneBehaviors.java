package worldline.api;

/** Qualified redstone boundaries kept outside the primary compatibility catalog. */
public final class WorldlineRedstoneBehaviors {
    public static final WorldlineBehavior REPEATER_DIODE = define("repeater-diode",
            "Repeater conducts a pulse forward and isolates reverse input");
    public static final WorldlineBehavior REDSTONE_ORE_GLOW = define("redstone-ore-glow",
            "Stepping on or clicking placed redstone ore lights it then darkens");
    public static final WorldlineBehavior DETECTOR_RAIL_VACATE = define("detector-rail-vacate",
            "Occupied detector rail emits power and unpowers after the cart leaves");
    public static final WorldlineBehavior STICKY_HEAD_BREAK = define("sticky-head-break",
            "Breaking an extended sticky piston head removes the leftover sticky base");
    public static final WorldlineBehavior PISTON_PUSH_ENTITY = define("piston-push-entity",
            "Piston extension displaces a player or dropped item entity");
    public static final WorldlineBehavior STICKY_PISTON_BUD = define("sticky-piston-bud",
            "Sticky piston QC-latched until a neighbor update extends without direct power");
    public static final WorldlineBehavior REDSTONE_ONE_TICK = define("redstone-one-tick",
            "A 1-tick pulse cuts at lever 69:9 and drops a sticky piston payload");

    private WorldlineRedstoneBehaviors() {}

    private static WorldlineBehavior define(String token, String subject) {
        return WorldlineBehavior.define(token, WorldlineFamily.REDSTONE, subject);
    }
}
