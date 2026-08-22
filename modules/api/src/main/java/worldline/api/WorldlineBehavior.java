package worldline.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Public semantic identity of one vanilla behavior. Constants are the catalog.
 * Atlas id is {@code atlas.scenario.<token>}. Milestone numbers stay out.
 */
public final class WorldlineBehavior {
    private static final Pattern TOKEN = Pattern.compile("[a-z][a-z0-9-]{0,62}");
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
    public static final WorldlineBehavior MONSTER_BED_INTERRUPT = define("monster-bed-interrupt", WorldlineFamily.PLAYER,
            "Bed occupy interrupted by nearby hostile");
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
    public static final WorldlineBehavior NOTE_BLOCK_INSTRUMENT = define("note-block-instrument", WorldlineFamily.REDSTONE,
            "Note instrument selection from the supporting block");
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
    public static final WorldlineBehavior DUAL_DIMENSION_SESSION = define("dual-dimension-session", WorldlineFamily.WORLD,
            "Simultaneous Overworld and Nether typed sessions");
    public static final WorldlineBehavior SAME_DIMENSION_RESPAWN = define("same-dimension-respawn", WorldlineFamily.PLAYER,
            "Overworld death and Packet9 respawn into the Overworld");
    public static final WorldlineBehavior CROSS_DIMENSION_RESPAWN = define("cross-dimension-respawn", WorldlineFamily.PLAYER,
            "Nether death and Packet9 respawn into the Overworld");
    public static final WorldlineBehavior BLOCK_PLACEMENT_PERSISTENCE = define("block-placement-persistence", WorldlineFamily.WORLD,
            "Server-authoritative held-block placement across fresh login");
    public static final WorldlineBehavior FOOD_CONSUMPTION = define("food-consumption", WorldlineFamily.ITEM,
            "Selected food consumption, health restoration, and container result");
    public static final WorldlineBehavior ENVIRONMENTAL_DAMAGE = define("environmental-damage", WorldlineFamily.ENVIRONMENT,
            "Server-authored health loss from hazardous block environments");
    public static final WorldlineBehavior FENCE_COLLISION = define("fence-collision", WorldlineFamily.ENVIRONMENT,
            "Server correction blocks a walk through an adjacent fence path");
    private static final Map<String, WorldlineBehavior> BY_TOKEN = index();
    private final String token, family, subject;

    private WorldlineBehavior(String token, String family, String subject) {
        if (token == null || !TOKEN.matcher(token).matches() || looksLikeProgress(token))
            throw new IllegalArgumentException("invalid behavior token");
        this.token = token;
        this.family = WorldlineFamily.parse(family);
        if (subject == null || subject.isEmpty() || subject.indexOf('\n') >= 0 || subject.indexOf('\r') >= 0)
            throw new IllegalArgumentException("invalid behavior subject");
        this.subject = subject;
    }

    public String token() { return token; }
    public String family() { return family; }
    public String subject() { return subject; }
    public String atlasId() { return "atlas.scenario." + token; }

    public static WorldlineBehavior require(String tokenOrAtlasOrProgress) {
        if (tokenOrAtlasOrProgress == null || tokenOrAtlasOrProgress.trim().isEmpty())
            throw new IllegalArgumentException("unknown behavior");
        String raw = tokenOrAtlasOrProgress.trim();
        if (raw.startsWith("atlas.scenario.")) raw = raw.substring("atlas.scenario.".length());
        else if (looksLikeProgress(raw)) raw = tokenOfProgress(raw);
        WorldlineBehavior value = BY_TOKEN.get(raw);
        if (value == null) throw new IllegalArgumentException("unknown behavior " + tokenOrAtlasOrProgress);
        return value;
    }

    public static Map<String, WorldlineBehavior> all() { return BY_TOKEN; }

    static String tokenOfProgress(String progressId) {
        String id = progressId.trim();
        if (looksLikeProgress(id)) id = id.substring(id.indexOf('-') + 1);
        if (id.endsWith("-set")) id = id.substring(0, id.length() - 4);
        if (!TOKEN.matcher(id).matches()) throw new IllegalArgumentException("invalid progress id");
        return id;
    }

    @Override public boolean equals(Object other) {
        return other instanceof WorldlineBehavior && token.equals(((WorldlineBehavior) other).token);
    }

    @Override public int hashCode() { return Objects.hash(token); }

    private static boolean looksLikeProgress(String raw) {
        if (raw.length() < 4 || raw.charAt(0) != 'm') return false;
        int dash = raw.indexOf('-');
        if (dash < 2) return false;
        for (int i = 1; i < dash; i++) if (raw.charAt(i) < '0' || raw.charAt(i) > '9') return false;
        return true;
    }

    private static WorldlineBehavior define(String token, String family, String subject) {
        return new WorldlineBehavior(token, family, subject);
    }

    private static Map<String, WorldlineBehavior> index() {
        WorldlineBehavior[] values = { CREEPER_FUSE, CREEPER_CANCEL, MELEE_PURSUIT, KNOCKBACK_COOLDOWN,
                SPIDER_LEAP, SLIME_TOUCH, GHAST_FIREBALL_HIT, MONSTER_BED_INTERRUPT, BOW_MOB_HIT,
                DIFFICULTY_DAMAGE, VOID_DEATH, PEACEFUL_DESPAWN, PLAYER_DEATH_DROPS, PIGMAN_ANGER,
                SKELETON_RANGED_AI, PIG_SPAWN, PIG_AI_MOVEMENT, PIG_DEATH, PIG_PORK_DROP,
                BED_SLEEP_SKIP, NOTE_BLOCK_CLICK, SIGN_TEXT_PERSISTENCE, PAINTING_SPAWN,
                NOTE_BLOCK_INSTRUMENT, PAINTING_ORIENTATION, JUKEBOX_RECORD_PLAY, SHEARS_HARVEST,
                SWORD_DAMAGE, MILK_BUCKET_CYCLE, DUAL_DIMENSION_SESSION, SAME_DIMENSION_RESPAWN,
                CROSS_DIMENSION_RESPAWN, BLOCK_PLACEMENT_PERSISTENCE, FOOD_CONSUMPTION,
                ENVIRONMENTAL_DAMAGE, FENCE_COLLISION };
        Map<String, WorldlineBehavior> map = new LinkedHashMap<String, WorldlineBehavior>();
        for (int i = 0; i < values.length; i++) {
            if (map.put(values[i].token(), values[i]) != null)
                throw new IllegalStateException("duplicate behavior " + values[i].token());
        }
        return Collections.unmodifiableMap(map);
    }
}
