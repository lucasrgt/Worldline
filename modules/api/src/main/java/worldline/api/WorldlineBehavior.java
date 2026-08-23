package worldline.api;

import java.util.Map;
import java.util.Objects;

/**
 * Public semantic identity of one vanilla behavior. Constants are the catalog.
 * Atlas id is {@code atlas.scenario.<token>}. Milestone numbers stay out.
 */
public final class WorldlineBehavior {
    public static final WorldlineBehavior CREEPER_FUSE = define("creeper-fuse", WorldlineFamily.HOSTILE,
            "Creeper proximity fuse then Packet60");
    public static final WorldlineBehavior CREEPER_CANCEL = define("creeper-cancel", WorldlineFamily.HOSTILE,
            "Creeper fuse cancel after leaving range");
    public static final WorldlineBehavior MELEE_PURSUIT = define("melee-pursuit", WorldlineFamily.HOSTILE,
            "Zombie and skeleton pursuit toward pose");
    public static final WorldlineBehavior KNOCKBACK_COOLDOWN = define("knockback-cooldown", WorldlineFamily.HOSTILE,
            "Zombie melee knockback plus hurt-time hold");
    public static final WorldlineBehavior SPIDER_LEAP = define("spider-leap", WorldlineFamily.HOSTILE,
            "Spider leap toward pose plus Packet8");
    public static final WorldlineBehavior SLIME_TOUCH = define("slime-touch", WorldlineFamily.HOSTILE,
            "Slime size family plus Packet8 contact");
    public static final WorldlineBehavior GHAST_FIREBALL_HIT = define("ghast-fireball-hit", WorldlineFamily.HOSTILE,
            "Ghast type-63 fireball Packet60 hit");
    public static final WorldlineBehavior MONSTER_BED_INTERRUPT = define("monster-bed-interrupt",
            WorldlineFamily.PLAYER, "Bed occupy interrupted by nearby hostile");
    public static final WorldlineBehavior BOW_MOB_HIT = define("bow-mob-hit", WorldlineFamily.ITEM,
            "Player bow type-60 hits pig and zombie");
    public static final WorldlineBehavior DIFFICULTY_DAMAGE = define("difficulty-damage", WorldlineFamily.WORLD,
            "Easy then Hard zombie melee Packet8");
    public static final WorldlineBehavior VOID_DEATH = define("void-death", WorldlineFamily.ENVIRONMENT,
            "Void walk-off Packet8 death plus Packet9");
    public static final WorldlineBehavior PEACEFUL_DESPAWN = define("peaceful-despawn", WorldlineFamily.WORLD,
            "Peaceful absence versus Easy persist");
    public static final WorldlineBehavior PLAYER_DEATH_DROPS = define("player-death-drops", WorldlineFamily.PLAYER,
            "Void death plus seeded hotbar Packet21");
    public static final WorldlineBehavior PIGMAN_ANGER = define("pigman-anger", WorldlineFamily.HOSTILE,
            "Nether pigman group aggro after Packet7");
    public static final WorldlineBehavior SKELETON_RANGED_AI = define("skeleton-ranged-ai", WorldlineFamily.HOSTILE,
            "Skeleton type-60 arrows with skeleton thrower");
    public static final WorldlineBehavior PIG_SPAWN = define("pig-spawn", WorldlineFamily.WORLD,
            "Default spawner Packet24 pig identity and metadata");
    public static final WorldlineBehavior PIG_AI_MOVEMENT = define("pig-ai-movement", WorldlineFamily.WORLD,
            "Pig horizontal Packet31, Packet33, or Packet34 movement");
    public static final WorldlineBehavior PIG_DEATH = define("pig-death", WorldlineFamily.WORLD,
            "Pig hurt, death status, and destroy packets");
    public static final WorldlineBehavior PIG_PORK_DROP = define("pig-pork-drop", WorldlineFamily.ITEM,
            "Pig death Packet21 porkchop drop");
    public static final WorldlineBehavior BED_SLEEP_SKIP = define("bed-sleep-skip", WorldlineFamily.PLAYER,
            "Day refusal, night Packet17 occupancy, and SMP time skip");
    public static final WorldlineBehavior NOTE_BLOCK_CLICK = define("note-block-click", WorldlineFamily.REDSTONE,
            "Empty-hand note block click Packet54 or Packet61 event");
    public static final WorldlineBehavior SIGN_TEXT_PERSISTENCE = define("sign-text-persistence", WorldlineFamily.WORLD,
            "Standing sign Packet130 text across fresh login");
    public static final WorldlineBehavior PAINTING_SPAWN = define("painting-spawn", WorldlineFamily.ITEM,
            "Painting placement Packet25 identity across peers");
    public static final WorldlineBehavior NOTE_BLOCK_INSTRUMENT = define("note-block-instrument",
            WorldlineFamily.REDSTONE, "Note instrument selection from the supporting block");
    public static final WorldlineBehavior PAINTING_ORIENTATION = define("painting-orientation", WorldlineFamily.ITEM,
            "Painting Packet25 orientation on opposite wall faces");
    public static final WorldlineBehavior JUKEBOX_RECORD_PLAY = define("jukebox-record-play", WorldlineFamily.ITEM,
            "Jukebox record insertion and Packet61 play event");
    public static final WorldlineBehavior SHEARS_HARVEST = define("shears-harvest", WorldlineFamily.ITEM,
            "Shears leaf harvest and nonlethal sheep wool drop");
    public static final WorldlineBehavior SWORD_DAMAGE = define("sword-damage", WorldlineFamily.ITEM,
            "Wood, iron, and diamond sword hit-to-death boundaries");
    public static final WorldlineBehavior MILK_BUCKET_CYCLE = define("milk-bucket-cycle", WorldlineFamily.ITEM,
            "Cow interaction fills and drinking empties a bucket");
    public static final WorldlineBehavior DUAL_DIMENSION_SESSION = define("dual-dimension-session",
            WorldlineFamily.WORLD, "Simultaneous Overworld and Nether typed sessions");
    public static final WorldlineBehavior SAME_DIMENSION_RESPAWN = define("same-dimension-respawn",
            WorldlineFamily.PLAYER, "Overworld death and Packet9 respawn into the Overworld");
    public static final WorldlineBehavior CROSS_DIMENSION_RESPAWN = define("cross-dimension-respawn",
            WorldlineFamily.PLAYER, "Nether death and Packet9 respawn into the Overworld");
    public static final WorldlineBehavior BLOCK_PLACEMENT_PERSISTENCE = define("block-placement-persistence",
            WorldlineFamily.WORLD, "Server-authoritative held-block placement across fresh login");
    public static final WorldlineBehavior FOOD_CONSUMPTION = define("food-consumption", WorldlineFamily.ITEM,
            "Selected food consumption, health restoration, and container result");
    public static final WorldlineBehavior ENVIRONMENTAL_DAMAGE = define("environmental-damage",
            WorldlineFamily.ENVIRONMENT, "Server-authored health loss from hazardous block environments");
    public static final WorldlineBehavior FENCE_COLLISION = define("fence-collision", WorldlineFamily.ENVIRONMENT,
            "Server correction blocks a walk through an adjacent fence path");
    public static final WorldlineBehavior REDSTONE_WIRE_POWER = define("redstone-wire-power", WorldlineFamily.REDSTONE,
            "Lever power and depower propagation through redstone wire");
    public static final WorldlineBehavior REDSTONE_IRON_DOOR = define("redstone-iron-door", WorldlineFamily.REDSTONE,
            "Lever power and recovery toggle both iron-door cells");
    public static final WorldlineBehavior PISTON_MOTION = define("piston-motion", WorldlineFamily.REDSTONE,
            "Piston extension, retraction, push, and sticky pull state transitions");
    public static final WorldlineBehavior PISTON_PUSH_LIMITS = define("piston-push-limits", WorldlineFamily.REDSTONE,
            "Piston immovable payload and maximum push-chain boundaries");
    public static final WorldlineBehavior PISTON_QUASI_CONNECTIVITY = define("piston-quasi-connectivity",
            WorldlineFamily.REDSTONE, "Piston activation from powered space above without direct power");
    public static final WorldlineBehavior PISTON_BUD_UPDATE = define("piston-bud-update", WorldlineFamily.REDSTONE,
            "Piston quasi-power responds to a neighboring block update");
    public static final WorldlineBehavior PISTON_HEAD_BREAK = define("piston-head-break", WorldlineFamily.REDSTONE,
            "Breaking an extended piston base removes its head and drops the base");
    public static final WorldlineBehavior FIRE_IGNITION = define("fire-ignition", WorldlineFamily.ENVIRONMENT,
            "Flint and steel creates a server-authored fire block");
    public static final WorldlineBehavior FIRE_PROPAGATION = define("fire-propagation", WorldlineFamily.ENVIRONMENT,
            "Fire persists on netherrack and consumes or spreads to flammable blocks");
    public static final WorldlineBehavior BUCKET_FLUID_CYCLE = define("bucket-fluid-cycle", WorldlineFamily.ITEM,
            "Water and lava bucket placement, source collection, and held-container result");
    public static final WorldlineBehavior FLUID_FLOW = define("fluid-flow", WorldlineFamily.ENVIRONMENT,
            "Water and lava sources flow into server-updated neighboring cells");
    public static final WorldlineBehavior WATER_LAVA_SOLIDIFICATION = define("water-lava-solidification",
            WorldlineFamily.ENVIRONMENT, "Water solidifies source or flowing lava into obsidian or cobblestone");
    public static final WorldlineBehavior JUKEBOX_EJECT = define("jukebox-eject", WorldlineFamily.ITEM,
            "Breaking a playing jukebox ejects its inserted record as an item");
    public static final WorldlineBehavior NETHER_BED_EXPLOSION = define("nether-bed-explosion",
            WorldlineFamily.ENVIRONMENT, "Activating a bed in the Nether destroys it in a strength-five explosion");
    public static final WorldlineBehavior BED_SPAWN_RESPAWN = define("bed-spawn-respawn", WorldlineFamily.PLAYER,
            "Sleeping sets the subsequent same-dimension respawn at the bed");
    public static final WorldlineBehavior FARMLAND_STATE = define("farmland-state", WorldlineFamily.ENVIRONMENT,
            "Hoe tilling, trampling, and nearby-water hydration update farmland state");
    public static final WorldlineBehavior PLANT_GROWTH = define("plant-growth", WorldlineFamily.ENVIRONMENT,
            "Sapling, crop, cactus, and sugar-cane growth under valid conditions");
    public static final WorldlineBehavior CROP_PLANTING = define("crop-planting", WorldlineFamily.ITEM,
            "Seeds plant wheat on hydrated farmland");
    public static final WorldlineBehavior CROP_HARVEST = define("crop-harvest", WorldlineFamily.ITEM,
            "Breaking mature crops emits their server-authored item drops");
    public static final WorldlineBehavior LEAF_DECAY = define("leaf-decay", WorldlineFamily.ENVIRONMENT,
            "Leaves decay after their supporting logs are removed");
    public static final WorldlineBehavior GRASS_SPREAD = define("grass-spread", WorldlineFamily.ENVIRONMENT,
            "Lit exposed dirt becomes grass while covered dirt remains unchanged");
    public static final WorldlineBehavior LIGHT_OPACITY = define("light-opacity", WorldlineFamily.ENVIRONMENT,
            "Glass, ice, and leaves preserve their distinct skylight attenuation");
    public static final WorldlineBehavior LIGHT_MELTING = define("light-melting", WorldlineFamily.ENVIRONMENT,
            "Torch light melts snow to air and ice to water");
    public static final WorldlineBehavior WOODEN_DOOR_TOGGLE = define("wooden-door-toggle", WorldlineFamily.REDSTONE,
            "Direct activation opens and closes both wooden-door cells");
    public static final WorldlineBehavior TRAPDOOR_TOGGLE = define("trapdoor-toggle", WorldlineFamily.REDSTONE,
            "Direct activation opens and closes oriented trapdoors");
    public static final WorldlineBehavior PRESSURE_PLATE = define("pressure-plate", WorldlineFamily.REDSTONE,
            "Player contact powers and departure releases stone and wooden plates");
    public static final WorldlineBehavior REDSTONE_INPUT_STATE = define("redstone-input-state",
            WorldlineFamily.REDSTONE, "Lever toggles and button pulses expose their powered metadata transitions");
    public static final WorldlineBehavior REPEATER_STATE = define("repeater-state", WorldlineFamily.REDSTONE,
            "Repeater orientation, delay, and powered-state metadata transitions");
    public static final WorldlineBehavior RAIL_POWER = define("rail-power", WorldlineFamily.REDSTONE,
            "Powered and detector rails expose power metadata under torch or cart input");
    public static final WorldlineBehavior REDSTONE_TORCH_INVERSION = define("redstone-torch-inversion",
            WorldlineFamily.REDSTONE, "Powered support extinguishes a redstone torch and recovery relights it");
    public static final WorldlineBehavior DISPENSER_QUASI_CONNECTIVITY = define("dispenser-quasi-connectivity",
            WorldlineFamily.REDSTONE, "Power above a dispenser triggers its loaded item without adjacent power");
    public static final WorldlineBehavior TNT_QUASI_CONNECTIVITY = define("tnt-quasi-connectivity",
            WorldlineFamily.REDSTONE, "Power above TNT primes its entity and produces a strength-four explosion");
    public static final WorldlineBehavior CAKE_CONSUMPTION = define("cake-consumption", WorldlineFamily.ITEM,
            "Cake activation advances bite metadata, heals, and removes the final slice");
    public static final WorldlineBehavior TOOL_BLOCK_BREAK = define("tool-block-break", WorldlineFamily.ITEM,
            "Tool-qualified block breaking removes cells, emits drops, and consumes durability");
    public static final WorldlineBehavior FRAGILE_BLOCK_BREAK = define("fragile-block-break",
            WorldlineFamily.ENVIRONMENT, "Glass breaks without a drop while ice breaks or melts into water");
    public static final WorldlineBehavior GRAVITY_BLOCK_FALL = define("gravity-block-fall", WorldlineFamily.ENVIRONMENT,
            "Unsupported sand and gravel fall as entities into lower cells");
    public static final WorldlineBehavior TNT_PRIMING = define("tnt-priming", WorldlineFamily.REDSTONE,
            "Flint and steel primes TNT entities that explode and can chain-prime TNT");
    public static final WorldlineBehavior VEHICLE_SPAWN = define("vehicle-spawn", WorldlineFamily.ITEM,
            "Boat and minecart items create peer-visible typed vehicle objects");
    public static final WorldlineBehavior PROJECTILE_SPAWN = define("projectile-spawn", WorldlineFamily.ITEM,
            "Bow, throwable, and fishing-rod use creates typed projectile objects");
    public static final WorldlineBehavior DISPENSER_PROJECTILE = define("dispenser-projectile",
            WorldlineFamily.REDSTONE, "A powered dispenser launches loaded snowballs and eggs as projectiles");
    public static final WorldlineBehavior DROPPED_ITEM_COLLECTION = define("dropped-item-collection",
            WorldlineFamily.ITEM, "A dropped item entity is restored to player inventory after collection");
    public static final WorldlineBehavior HOSTILE_SPAWNER_IDENTITY = define("hostile-spawner-identity",
            WorldlineFamily.HOSTILE, "Retargeted spawners emit the requested hostile Packet24 identities");
    public static final WorldlineBehavior NATURAL_HOSTILE_SPAWN = define("natural-hostile-spawn",
            WorldlineFamily.HOSTILE, "Night-time monster spawning emits multiple hostile identities without spawners");
    public static final WorldlineBehavior PORTAL_ACTIVATION = define("portal-activation", WorldlineFamily.WORLD,
            "Igniting a valid obsidian frame fills its interior with portal blocks");
    public static final WorldlineBehavior PORTAL_COORDINATE_SCALE = define("portal-coordinate-scale",
            WorldlineFamily.WORLD, "Overworld-to-Nether portal travel applies the quantized eight-to-one scale");
    public static final WorldlineBehavior CLOSABLE_BLOCK_TOGGLE = define("closable-block-toggle",
            WorldlineFamily.REDSTONE, "Direct activation opens and closes both a wooden door and a trapdoor");
    public static final WorldlineBehavior DOUBLE_CHEST_WINDOW = define("double-chest-window", WorldlineFamily.ITEM,
            "Adjacent chests open one Large chest window with the combined slot topology");
    public static final WorldlineBehavior NETHER_LOGIN = define("nether-login", WorldlineFamily.WORLD,
            "A player persisted in dimension minus one logs into decoded Nether terrain");
    public static final WorldlineBehavior GHAST_FIREBALL_SPAWN = define("ghast-fireball-spawn", WorldlineFamily.HOSTILE,
            "A Ghast emits a type-63 fireball object whose thrower is the Ghast");
    public static final WorldlineBehavior FISHING_CATCH = define("fishing-catch", WorldlineFamily.ITEM,
            "Casting and reeling a fishing hook produces a raw-fish item entity");
    public static final WorldlineBehavior SHEEP_DYE_COLOR = define("sheep-dye-color", WorldlineFamily.ITEM,
            "Dyeing a living sheep determines the wool damage emitted by shearing");
    public static final WorldlineBehavior EMPTY_MAP_RETENTION = define("empty-map-retention", WorldlineFamily.ITEM,
            "Empty map air use retains its server-authoritative inventory stack");
    public static final WorldlineBehavior ARMOR_EQUIPMENT = define("armor-equipment", WorldlineFamily.PLAYER,
            "Equipped armor occupies its personal slot and appears in the matching peer slot");
    public static final WorldlineBehavior DISPENSER_ITEM_EJECT = define("dispenser-item-eject",
            WorldlineFamily.REDSTONE, "A powered dispenser ejects ordinary inventory items as dropped entities");
    public static final WorldlineBehavior MOB_DEATH_DROPS = define("mob-death-drops", WorldlineFamily.ITEM,
            "Killing typed mobs emits their canonical server-authored item drops");
    public static final WorldlineBehavior VEHICLE_BREAK_DROPS = define("vehicle-break-drops", WorldlineFamily.ITEM,
            "Attacking boats and minecarts emits their canonical wreckage items");
    public static final WorldlineBehavior CRAFTING_RECIPES = define("crafting-recipes", WorldlineFamily.ITEM,
            "Official crafting recipes map ingredient totals to crafted output totals");
    public static final WorldlineBehavior FURNACE_RECIPES = define("furnace-recipes", WorldlineFamily.ITEM,
            "Official furnace recipes map input items to smelted output items");
    public static final WorldlineBehavior FURNACE_FUEL_BURN = define("furnace-fuel-burn", WorldlineFamily.ITEM,
            "Coal, planks, and lava expose their distinct furnace burn durations");
    public static final WorldlineBehavior SLIME_SPLIT = define("slime-split", WorldlineFamily.HOSTILE,
            "Killing a larger slime emits child slime spawn objects");
    public static final WorldlineBehavior FIXED_SEED_TERRAIN = define("fixed-seed-terrain", WorldlineFamily.WORLD,
            "A fixed seed yields an exact terrain and surface state for an absolute chunk");
    public static final WorldlineBehavior FIXED_SEED_LIGHTING = define("fixed-seed-lighting",
            WorldlineFamily.ENVIRONMENT, "Fixed seed yields exact block- and sky-light planes for an absolute chunk");
    public static final WorldlineBehavior LIGHT_PROPAGATION = define("light-propagation", WorldlineFamily.ENVIRONMENT,
            "Adding a light source updates persisted block-light samples around it");
    public static final WorldlineBehavior SPIDER_CLIMB = define("spider-climb", WorldlineFamily.HOSTILE,
            "Spider movement gains height while contacting climbable walls");
    public static final WorldlineBehavior WOLF_ANGER = define("wolf-anger", WorldlineFamily.HOSTILE,
            "Untamed wolf retaliation after a player attack");
    public static final WorldlineBehavior ARMOR_REDUCTION = define("armor-reduction", WorldlineFamily.PLAYER,
            "Equipped armor reduces incoming hostile melee damage");
    public static final WorldlineBehavior FALL_DAMAGE = define("fall-damage", WorldlineFamily.PLAYER,
            "Greater fall distance produces greater server-authored health loss");
    public static final WorldlineBehavior EXPLOSION_PLAYER_DAMAGE = define("explosion-player-damage",
            WorldlineFamily.PLAYER, "TNT and creeper explosions damage a surviving nearby player");
    public static final WorldlineBehavior SQUID_LAND_DEATH = define("squid-land-death", WorldlineFamily.HOSTILE,
            "A squid stranded outside water remains killable on land");
    public static final WorldlineBehavior TAMED_WOLF_ASSIST = define("tamed-wolf-assist", WorldlineFamily.PLAYER,
            "A tamed standing wolf attacks a mob struck by its owner");
    public static final WorldlineBehavior REDSTONE_LATCH = define("redstone-latch", WorldlineFamily.REDSTONE,
            "Cross-coupled redstone torches retain set and reset states");
    public static final WorldlineBehavior PORTAL_EXIT_CREATION = define("portal-exit-creation", WorldlineFamily.WORLD,
            "Portal travel creates a missing destination frame away from the source");
    public static final WorldlineBehavior SPAWNER_DELAY = define("spawner-delay", WorldlineFamily.HOSTILE,
            "Spawner delay blocks distant activation and permits a nearby spawn");
    private static final Map<String, WorldlineBehavior> BY_TOKEN = WorldlineBehaviorCatalog.freeze();
    private final String token, family, subject;

    WorldlineBehavior(String token, String family, String subject) {
        this.token = token; this.family = family; this.subject = subject;
    }

    public String token() { return token; } public String family() { return family; }
    public String subject() { return subject; } public String atlasId() { return "atlas.scenario." + token; }

    public static WorldlineBehavior require(String tokenOrAtlasOrProgress) {
        return WorldlineBehaviorRegistry.require(BY_TOKEN, tokenOrAtlasOrProgress);
    }

    public static Map<String, WorldlineBehavior> all() { return BY_TOKEN; }

    static String tokenOfProgress(String progressId) { return WorldlineBehaviorRegistry.tokenOfProgress(progressId); }

    @Override public boolean equals(Object other) {
        return other instanceof WorldlineBehavior && token.equals(((WorldlineBehavior) other).token);
    }

    @Override public int hashCode() { return Objects.hash(token); }

    static WorldlineBehavior define(String token, String family, String subject) {
        return WorldlineBehaviorRegistry.define(token, family, subject);
    }
}
