package worldline.api;

/** Qualified environment boundaries kept outside the primary compatibility catalog. */
public final class WorldlineEnvironmentBehaviors {
    public static final WorldlineBehavior MUSHROOM_SPREAD = define("mushroom-spread",
            "Dark mushrooms spread onto adjacent opaque air while glass stays empty");
    public static final WorldlineBehavior GRASS_DIE_COVER = define("grass-die-cover",
            "Covered grass converts to dirt while exposed grass remains unchanged");
    public static final WorldlineBehavior FARMLAND_TRAMPLE = define("farmland-trample",
            "Player fall or jump onto farmland converts it to dirt");
    public static final WorldlineBehavior NETHERRACK_FIRE_PERSIST = define("netherrack-fire-persist",
            "Fire on netherrack persists while fire on wood or planks expires");
    public static final WorldlineBehavior SUFFOCATION = define("suffocation",
            "Head inside a solid block takes Packet8 damage");
    public static final WorldlineBehavior DROWNING = define("drowning",
            "Fully submerged player Packet8 drowning damage");
    public static final WorldlineBehavior LAVA_DAMAGE = define("lava-damage",
            "Standing in lava deals repeated Packet8 damage");
    public static final WorldlineBehavior FIRE_SPREAD_WOOD = define("fire-spread-wood",
            "Fire spreads onto adjacent flammable planks or wood");
    public static final WorldlineBehavior FARMLAND_DRY = define("farmland-dry",
            "Unhydrated farmland reverts to dirt after random ticks with no nearby water");

    private WorldlineEnvironmentBehaviors() {}

    private static WorldlineBehavior define(String token, String subject) {
        return WorldlineBehavior.define(token, WorldlineFamily.ENVIRONMENT, subject);
    }
}
