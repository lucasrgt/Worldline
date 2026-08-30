package worldline.atlas;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Collections;
import worldline.invariants.BlockConservation;
import worldline.invariants.DurabilityConservation;
import worldline.invariants.EntitySpawn;
import worldline.invariants.HealthConservation;
import worldline.invariants.ItemConservation;
import worldline.invariants.TimeMonotonic;

/** Declared vanilla subsystems and coverage dimensions. Denominators live on units. */
public final class AtlasSubsystems {
    public static final String[] ALL = { "tick-lifecycle", "worldgen", "chunks", "lighting",
            "weather", "block-ticks", "fluids", "entities", "tile-entities", "mob-ai",
            "player", "inventory", "crafting", "redstone", "saves", "dimensions", "protocol",
            "dedicated-server",
            "rendering", "gui", "resources", "mod-ecosystem", "mappings", "stationapi",
            "aero" };
    public static final String[] DOMAINS = { "simulation", "world", "actors", "gameplay",
            "runtime", "ecosystem" };
    public static final String[] DIMENSIONS = { "TESTABILITY", "CONTROL", "OBSERVABILITY",
            "ORACLE", "SEMANTIC", "REPRODUCIBILITY", "DETERMINISM" };
    private static final Map<String, String> CATEGORY = categories();
    private static final Map<String, String> INVARIANT = invariants();
    private static final Map<String, String> DOMAIN = domains();

    private AtlasSubsystems() {}

    public static boolean known(String subsystem) {
        for (String item : ALL) {
            if (item.equals(subsystem)) return true;
        }
        return false;
    }

    public static String forCategory(String category) {
        if (category == null) return "";
        String mapped = CATEGORY.get(category);
        return mapped == null ? "" : mapped;
    }

    public static String forBoundary(String token) {
        if (token == null || token.isEmpty()) return "";
        return forCategory(token.toLowerCase(Locale.US));
    }

    public static String forInvariant(String name) {
        String mapped = INVARIANT.get(name);
        return mapped == null ? "" : mapped;
    }

    public static String domain(String subsystem) {
        if (!known(subsystem)) throw new IllegalArgumentException("unknown subsystem " + subsystem);
        String mapped = DOMAIN.get(subsystem);
        if (mapped == null) throw new IllegalStateException("unclassified subsystem " + subsystem);
        return mapped;
    }

    private static Map<String, String> domains() {
        Map<String, String> mapped = new LinkedHashMap<String, String>();
        assign(mapped, "simulation", "tick-lifecycle", "weather", "block-ticks", "redstone");
        assign(mapped, "world", "worldgen", "chunks", "lighting", "fluids", "saves",
                "dimensions");
        assign(mapped, "actors", "entities", "tile-entities", "mob-ai", "player");
        assign(mapped, "gameplay", "inventory", "crafting");
        assign(mapped, "runtime", "protocol", "dedicated-server", "rendering", "gui",
                "resources");
        assign(mapped, "ecosystem", "mod-ecosystem", "mappings", "stationapi", "aero");
        if (mapped.size() != ALL.length) throw new IllegalStateException("subsystem taxonomy drift");
        return Collections.unmodifiableMap(mapped);
    }

    private static void assign(Map<String, String> target, String domain, String... subsystems) {
        for (String subsystem : subsystems) {
            if (!known(subsystem) || target.put(subsystem, domain) != null) {
                throw new IllegalStateException("invalid subsystem taxonomy " + subsystem);
            }
        }
    }

    private static Map<String, String> categories() {
        Map<String, String> mapped = new LinkedHashMap<String, String>();
        mapped.put("clock", "tick-lifecycle");
        mapped.put("rng", "tick-lifecycle");
        mapped.put("input", "tick-lifecycle");
        mapped.put("tick", "tick-lifecycle");
        mapped.put("scheduler", "tick-lifecycle");
        mapped.put("lifecycle", "tick-lifecycle");
        mapped.put("worldgen", "worldgen");
        mapped.put("block-tick", "block-ticks");
        mapped.put("block_tick", "block-ticks");
        mapped.put("fluid", "fluids");
        mapped.put("light", "lighting");
        mapped.put("weather", "weather");
        mapped.put("mob-ai", "mob-ai");
        mapped.put("mob_ai", "mob-ai");
        mapped.put("dimension", "dimensions");
        mapped.put("dimensions", "dimensions");
        mapped.put("chunk", "chunks");
        mapped.put("entity", "entities");
        mapped.put("tile-entity", "tile-entities");
        mapped.put("tile_entity", "tile-entities");
        mapped.put("player", "player");
        mapped.put("inventory", "inventory");
        mapped.put("item", "inventory");
        mapped.put("recipe", "crafting");
        mapped.put("redstone", "redstone");
        mapped.put("save", "saves");
        mapped.put("persistence", "saves");
        mapped.put("filesystem", "saves");
        mapped.put("network", "protocol");
        mapped.put("render", "rendering");
        mapped.put("gui", "gui");
        mapped.put("resource", "resources");
        mapped.put("audio", "resources");
        return Collections.unmodifiableMap(mapped);
    }

    private static Map<String, String> invariants() {
        Map<String, String> mapped = new LinkedHashMap<String, String>();
        mapped.put(ItemConservation.NAME, "inventory");
        mapped.put(EntitySpawn.NAME, "entities");
        mapped.put(BlockConservation.NAME, "chunks");
        mapped.put(HealthConservation.NAME, "player");
        mapped.put(DurabilityConservation.NAME, "inventory");
        mapped.put(TimeMonotonic.NAME, "tick-lifecycle");
        return Collections.unmodifiableMap(mapped);
    }
}
