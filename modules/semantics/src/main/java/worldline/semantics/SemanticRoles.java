package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Closed required-role contract for every Worldline-evidenced b1.7.3 symbol.
 * A catalog is complete only when each role appears exactly once.
 */
public final class SemanticRoles {
    public static final String CLOCK = "clock", RNG = "rng", INPUT = "input", TICK = "tick";
    public static final String FILESYSTEM = "filesystem", NETWORK = "network";
    public static final String SCHEDULER = "scheduler", WORLD = "world", BLOCK = "block";
    public static final String CHUNK = "chunk", PLAYER = "player", ENTITY = "entity";
    public static final String INVENTORY = "inventory", ITEM = "item", RECIPE = "recipe";
    public static final String GUI = "gui", RENDER = "render", AUDIO = "audio";
    public static final String RESOURCE = "resource", PERSISTENCE = "persistence";
    public static final String SAVE = "save", LIFECYCLE = "lifecycle";
    public static final String LAB = "lab", DOMAIN = "domain";
    private static final Map<String, List<String>> REQUIRED = required();

    private SemanticRoles() {}

    public static List<String> categories() {
        return Collections.unmodifiableList(new java.util.ArrayList<String>(REQUIRED.keySet()));
    }

    public static List<String> required(String category) {
        List<String> roles = REQUIRED.get(category);
        if (roles == null) throw new IllegalArgumentException("unknown category " + category);
        return roles;
    }

    public static int roleCount() {
        int total = 0;
        for (List<String> roles : REQUIRED.values()) total += roles.size();
        return total;
    }

    private static Map<String, List<String>> required() {
        Map<String, List<String>> roles = new LinkedHashMap<String, List<String>>();
        roles.put(CLOCK, list("CLIENT_CLOCK_SOURCE", "CLIENT_CLOCK_ACCUMULATOR", "WORLD_TIME",
                "CLIENT_SYSTEM_TIME"));
        roles.put(RNG, list("WORLD_RANDOM", "ENTITY_RANDOM", "CONTROLLED_SEED"));
        roles.put(INPUT, list("KEYBOARD", "KEYBOARD_NEXT", "KEYBOARD_PUSH", "KEYBOARD_RESET",
                "MOUSE", "MOUSE_BUTTON", "MOUSE_PUSH", "MOUSE_RESET", "MOVEMENT",
                "INVENTORY_KEY", "ESCAPE_KEY"));
        roles.put(TICK, list("CLIENT_TICK_ROOT", "CLIENT_TICK_COUNTER", "WORLD_TICK",
                "ENTITY_UPDATE", "CONTROLLER_TYPE", "CONTROLLER_TICK", "EFFECT_TICK"));
        roles.put(FILESYSTEM, list("VIRTUAL_FILESYSTEM", "SAVE_HANDLER", "STAT_FILE", "STAT_WRITER",
                "FS_FAIL", "FS_JOURNAL", "WORLD_LOAD", "WORLD_LOCK", "WORLD_FILE", "CHUNK_LOAD"));
        roles.put(NETWORK, list("OFFLINE_SESSION", "NETWORK_DISABLED"));
        roles.put(SCHEDULER, list("TIMER_THREAD", "TASK_SCHEDULER", "SCHEDULER_ADVANCE"));
        roles.put(WORLD, list("WORLD_TYPE", "LOADED_ENTITY_LIST", "TILE_ENTITIES", "BLOCK_ACCESS",
                "BLOCK_ID_READ", "BLOCK_READ", "BLOCK_WRITE", "BLOCK_NOTIFY", "WORLD_DIFFICULTY",
                "WORLD_PROVIDER"));
        roles.put(BLOCK, list("BLOCK_TYPE", "BLOCK_ID", "BLOCK_STONE", "BLOCK_BEDROCK", "BLOCK_SAND",
                "BLOCK_SAND_TYPE", "BLOCK_SAND_FALL"));
        roles.put(CHUNK, list("CHUNK_TYPE", "CHUNK_LOOKUP", "CHUNK_POPULATE", "CHUNK_POPULATED",
                "CHUNK_NEVER_SAVE", "CHUNK_RELIGHT", "CHUNK_LOADER", "LOADER_LOAD", "LOADER_SAVE",
                "LOADER_FLUSH"));
        roles.put(PLAYER, list("LOCAL_PLAYER", "PLAYER_TYPE", "LIVING_TYPE", "PLAYER_NAME",
                "PLAYER_HEALTH", "HOTBAR_SLOT", "PLAYER_INVENTORY", "INVENTORY_FIELD"));
        roles.put(ENTITY, list("ENTITY_TYPE", "ENTITY_ID", "ENTITY_POS_X", "ENTITY_POS_Y",
                "ENTITY_POS_Z", "ENTITY_DEAD", "ENTITY_SET_POSITION", "ENTITY_SET_LOCATION",
                "ENTITY_ITEM"));
        roles.put(INVENTORY, list("INVENTORY_TYPE", "PLAYER_ITEMS", "WORLD_ITEMS", "WORLD_BLOCKS",
                "MAIN_ITEMS", "ARMOR_ITEMS", "CURSOR_STACK"));
        roles.put(ITEM, list("ITEM_STACK", "ITEM_ID", "STACK_SIZE", "ITEM_TYPE", "ITEM_LOOKUP",
                "ITEM_DAMAGE", "CONTAINER_TYPE", "SLOT_COUNT", "SLOT_GET", "ENTITY_ITEM_STACK"));
        roles.put(RECIPE, list("CRAFTING", "CRAFTING_LIST", "FURNACE", "FURNACE_LIST", "RECIPE_TYPE",
                "RECIPE_OUTPUT", "RECIPE_SHAPED", "RECIPE_SHAPELESS"));
        roles.put(GUI, list("CURRENT_SCREEN", "INVENTORY_SCREEN", "CONTAINER_CLICK", "HUD_TYPE",
                "HUD_TICK", "HUD_COUNTER", "GUI_OPEN", "GUI_CLOSE", "GUI_SLOT", "GUI_CLICK",
                "GUI_SCREEN", "GUI_CONTAINER", "GUI_SLOT_TYPE", "CONTAINER_SLOTS", "WINDOW_ID"));
        roles.put(RENDER, list("DISPLAY", "DISPLAY_CREATED", "RENDER_ENGINE", "RENDER_ENGINE_FIELD",
                "ENTITY_RENDERER", "ENTITY_RENDERER_FIELD", "RENDERER_UPDATE", "RENDERER_COUNTER",
                "MOUSE_OVER", "RENDER_GLOBAL", "RENDER_GLOBAL_FIELD", "CLOUD_UPDATE",
                "CLOUD_OFFSET", "EFFECT_RENDERER", "EFFECT_UPDATE", "HUD_FIELD",
                "PLAYER_CONTROLLER_FIELD"));
        roles.put(AUDIO, list("SOUND_MANAGER", "HEADLESS_AUDIO"));
        roles.put(RESOURCE, list("TEXTURE_LOOKUP", "DYNAMIC_TEXTURE"));
        roles.put(PERSISTENCE, list("WORLD_SAVE", "CHUNK_SAVE", "PLAYER_SAVE", "LOAD_INFO",
                "SAVE_INTERFACE", "EXTRA_CHUNK", "CHUNK_FLUSH", "EXTRA_DATA", "SPAWN_SET",
                "SPAWN_POSITION"));
        roles.put(SAVE, list("HANDLER_LOAD", "HANDLER_LOCK", "HANDLER_LOADER", "HANDLER_PLAYERS",
                "HANDLER_INFO", "HANDLER_PLAYER_DATA", "HANDLER_CLOSE", "HANDLER_FILE",
                "PLAYER_FILES", "PLAYER_WRITE", "PLAYER_READ"));
        roles.put(LIFECYCLE, list("CLIENT_TYPE", "CLIENT_WORLD", "CLIENT_PLAYER", "CLIENT_SESSION",
                "RUNTIME_FACTORY", "BOOT_HEADLESS", "LOAD_WORLD", "MANUAL_TICK", "CLOSE"));
        roles.put(LAB, list("OBSERVATION", "SNAPSHOT", "CHECKPOINT", "HYPOTHESIS", "COMPARISON"));
        roles.put(DOMAIN, list("WORLD_API", "PLAYER_API", "ENTITY_API", "BLOCK_STATE", "READ_BLOCK",
                "WRITE_BLOCK", "LIST_ENTITIES", "TELEPORT"));
        return Collections.unmodifiableMap(roles);
    }

    private static List<String> list(String... values) {
        return Collections.unmodifiableList(Arrays.asList(values));
    }
}
