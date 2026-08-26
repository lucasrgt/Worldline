package worldline.api;

/** Qualified hostile-mob boundaries kept outside the primary compatibility catalog. */
public final class WorldlineHostileBehaviors {
    public static final WorldlineBehavior GHAST_FIREBALL_PUNCH = define("ghast-fireball-punch",
            "Player Packet7 punch redirects a type-63 fireball");
    public static final WorldlineBehavior SPAWN_LIGHT_CAP = define("spawn-light-cap",
            "Hostile spawn is blocked at light >= 8 and permitted in darkness");
    public static final WorldlineBehavior UNDEAD_SUN_BURN = define("undead-sun-burn",
            "Zombie or skeleton burns in sunlight and not at night or under cover");
    public static final WorldlineBehavior NATURAL_SLIME_SPAWN = define("natural-slime-spawn",
            "A formula-selected chunk naturally spawns a slime below height sixteen without a spawner");
    public static final WorldlineBehavior CREEPER_TNT_DIFFERENTIAL =
            define("creeper-tnt-differential",
                    "Creeper Packet60 strength three is one below TNT strength four");
    public static final WorldlineBehavior POWERED_CREEPER = define("powered-creeper",
            "Observed lightning transforms the same unpowered creeper into powered state");

    private WorldlineHostileBehaviors() {}

    private static WorldlineBehavior define(String token, String subject) {
        return WorldlineBehavior.define(token, WorldlineFamily.HOSTILE, subject);
    }
}
