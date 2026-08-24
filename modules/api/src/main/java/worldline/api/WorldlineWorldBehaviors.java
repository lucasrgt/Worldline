package worldline.api;

/** Qualified world-state boundaries kept outside the primary compatibility catalog. */
public final class WorldlineWorldBehaviors {
    public static final WorldlineBehavior WHEAT_LIGHT_HALT = define("wheat-light-halt",
            "Wheat on farmland does not increment age in darkness while a lit crop can age");
    public static final WorldlineBehavior CACTUS_ADJACENT_BREAK = define("cactus-adjacent-break",
            "Cactus pops to an item when a solid block is horizontally adjacent");
    public static final WorldlineBehavior DOOR_UPPER_BREAK = define("door-upper-break",
            "Breaking a wooden door upper half removes both halves and drops the door");
    public static final WorldlineBehavior FALLING_SAND_ENTITY = define("falling-sand-entity",
            "Unsupported sand becomes a falling-sand entity then places as a block");
    public static final WorldlineBehavior GRAVEL_FALL = define("gravel-fall",
            "Unsupported gravel falls as entity then places or drops an item");
    public static final WorldlineBehavior TORCH_WASH = define("torch-wash",
            "Water occupying a torch cell pops it into a Packet21 drop");
    public static final WorldlineBehavior SUGAR_CANE_DRY_BREAK = define("sugar-cane-dry-break",
            "Removing adjacent water from sugar cane pops the cane");
    public static final WorldlineBehavior SAPLING_DARK_HALT = define("sapling-dark-halt",
            "A covered sapling stays a sapling in darkness while a lit sapling can stage");
    public static final WorldlineBehavior ICE_MELT_LIGHT = define("ice-melt-light",
            "Torch block light melts adjacent ice to water");
    public static final WorldlineBehavior DOUBLE_CHEST_MERGE = define("double-chest-merge",
            "Two adjacent chests form one Large chest Packet100 window");
    public static final WorldlineBehavior PORTAL_SEARCH_RADIUS = define("portal-search-radius",
            "Portal travel links to an existing destination frame inside the search radius");
    public static final WorldlineBehavior DUNGEON_GENERATION = define("dungeon-generation",
            "Fixed-seed populated chunks contain replay-stable spawners and nonempty loot chests");
    public static final WorldlineBehavior CHUNK_UNLOAD_RELOAD = define("chunk-unload-reload",
            "Burning furnaces, dropped items, and minecarts survive a server chunk unload and reload");
    public static final WorldlineBehavior PROTOCOL14_EDGE_PACKETS = define("protocol14-edge-packets",
            "Packet130 sign framing and Packet131 map data precede a silent timeout without Packet0");
    public static final WorldlineBehavior MAP_DATA_CONTENT = define("map-data-content",
            "A held map converges to replay-stable Packet131 colors at a fixed seed and position");

    private WorldlineWorldBehaviors() {}

    private static WorldlineBehavior define(String token, String subject) {
        return WorldlineBehavior.define(token, WorldlineFamily.WORLD, subject);
    }
}
