package worldline.api;

/** Qualified world-state boundaries kept outside the primary compatibility catalog. */
public final class WorldlineWorldBehaviors {
    public static final WorldlineBehavior FURNACE_SUBSYSTEM = define("furnace-subsystem",
            "Idle and active furnace domains, smelting, persistence, lifecycle, and physics");
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
    public static final WorldlineBehavior ICE_FORMATION = define("ice-formation",
            "Cold-biome still water freezes under low block light while lit water stays liquid");
    public static final WorldlineBehavior FLOWING_WATER_FREEZE = define("flowing-water-freeze",
            "Cold-biome still water freezes while adjacent flowing water remains flowing");
    public static final WorldlineBehavior SNOW_ACCUMULATION = define("snow-accumulation",
            "Cold-biome snowfall accumulates a snow layer while a dry control stays air");
    public static final WorldlineBehavior SNOW_LAYER_NONSTACKING = define("snow-layer-nonstacking",
            "Continued cold-biome snowfall leaves the same snow layer unstacked");
    public static final WorldlineBehavior NATURAL_WOLF_PACK = define("natural-wolf-pack",
            "The peaceful spawner creates a distinct spatially coherent wolf pack without a spawner");
    public static final WorldlineBehavior DOUBLE_CHEST_MERGE = define("double-chest-merge",
            "Two adjacent chests form one Large chest Packet100 window");
    public static final WorldlineBehavior PORTAL_SEARCH_RADIUS = define("portal-search-radius",
            "Portal travel links to an existing destination frame inside the search radius");
    public static final WorldlineBehavior PORTAL_INVALID_FRAME = define("portal-invalid-frame",
            "Flint ignition of an upright obsidian frame missing one required top block creates no portal cells");
    public static final WorldlineBehavior PORTAL_REENTRY_COOLDOWN = define(
            "portal-reentry-cooldown",
            "Arrival-side portal contact suppresses an immediate return until the player exits for a cooldown window");
    public static final WorldlineBehavior PORTAL_BLOCK_SUBSYSTEM = define(
            "portal-block-subsystem",
            "Portal frame materialization, lifecycle, persistence, physics, ticks, and collapse");
    public static final WorldlineBehavior DUNGEON_GENERATION = define("dungeon-generation",
            "Fixed-seed populated chunks contain replay-stable spawners and nonempty loot chests");
    public static final WorldlineBehavior CHUNK_UNLOAD_RELOAD = define("chunk-unload-reload",
            "Burning furnaces, dropped items, and minecarts survive a server chunk unload and reload");
    public static final WorldlineBehavior CHUNK_RESTART_PERSISTENCE = define(
            "chunk-restart-persistence",
            "Stocked chests, dropped items, and minecarts keep their semantic state across a "
                    + "dedicated-server stop and restart after an observed chunk unload");
    public static final WorldlineBehavior PROTOCOL14_EDGE_PACKETS = define("protocol14-edge-packets",
            "Packet130 sign framing and Packet131 map data precede a silent timeout without Packet0");
    public static final WorldlineBehavior MAP_DATA_CONTENT = define("map-data-content",
            "A held map converges to replay-stable Packet131 colors at a fixed seed and position");
    public static final WorldlineBehavior BONEMEAL_WHEAT = define("bonemeal-wheat",
            "Beta bonemeal damage fifteen matures planted wheat from age zero to seven");
    public static final WorldlineBehavior BLOCK_STABILITY_CONFORMANCE = define(
            "block-stability-conformance",
            "Public bounded tick and direct-neighbor-removal stability conformance");
    public static final WorldlineBehavior SIGN_SUBSYSTEM = define("sign-subsystem",
            "Standing and wall sign placement, text, physical envelope, support, and persistence lifecycle");

    private WorldlineWorldBehaviors() {}

    private static WorldlineBehavior define(String token, String subject) {
        return WorldlineBehavior.define(token, WorldlineFamily.WORLD, subject);
    }
}
