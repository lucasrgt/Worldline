package worldline.api;

/** Qualified hostile-mob boundaries kept outside the primary compatibility catalog. */
public final class WorldlineHostileBehaviors {
    public static final WorldlineBehavior GHAST_FIREBALL_PUNCH = define("ghast-fireball-punch",
            "Player Packet7 punch redirects a type-63 fireball");
    public static final WorldlineBehavior SPAWN_LIGHT_CAP = define("spawn-light-cap",
            "Hostile spawn is blocked at light >= 8 and permitted in darkness");
    public static final WorldlineBehavior UNDEAD_SUN_BURN = define("undead-sun-burn",
            "Zombie or skeleton burns in sunlight and not at night or under cover");

    private WorldlineHostileBehaviors() {}

    private static WorldlineBehavior define(String token, String subject) {
        return WorldlineBehavior.define(token, WorldlineFamily.HOSTILE, subject);
    }
}
