package worldline.semantics;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Closed aliases from Worldline trace field names to catalog roles. Unknown
 * fields stay empty so structural diffs do not invent a role.
 */
public final class SemanticFields {
    private static final Map<String, String> ALIAS = aliases();

    private SemanticFields() {}

    public static String role(String field) {
        if (field == null || field.isEmpty()) return "";
        String role = ALIAS.get(field);
        if (role == null) return "";
        return SemanticCatalog.standard().role(role).role();
    }

    private static Map<String, String> aliases() {
        Map<String, String> aliases = new LinkedHashMap<String, String>();
        aliases.put("x", "ENTITY_POS_X");
        aliases.put("playerX", "ENTITY_POS_X");
        aliases.put("posX", "ENTITY_POS_X");
        aliases.put("y", "ENTITY_POS_Y");
        aliases.put("playerY", "ENTITY_POS_Y");
        aliases.put("posY", "ENTITY_POS_Y");
        aliases.put("z", "ENTITY_POS_Z");
        aliases.put("playerZ", "ENTITY_POS_Z");
        aliases.put("posZ", "ENTITY_POS_Z");
        aliases.put("health", "PLAYER_HEALTH");
        aliases.put("slot", "HOTBAR_SLOT");
        aliases.put("currentItem", "HOTBAR_SLOT");
        aliases.put("selectedSlot", "HOTBAR_SLOT");
        aliases.put("time", "WORLD_TIME");
        aliases.put("worldTime", "WORLD_TIME");
        aliases.put("clientTick", "CLIENT_TICK_COUNTER");
        aliases.put("ticksRan", "CLIENT_TICK_COUNTER");
        aliases.put("cloudTick", "CLOUD_OFFSET");
        aliases.put("cloudOffsetX", "CLOUD_OFFSET");
        aliases.put("guiTick", "HUD_COUNTER");
        aliases.put("updateCounter", "HUD_COUNTER");
        aliases.put("rendererTick", "RENDERER_COUNTER");
        aliases.put("rendererUpdateCount", "RENDERER_COUNTER");
        aliases.put("username", "PLAYER_NAME");
        aliases.put("blockID", "BLOCK_ID");
        aliases.put("block64", "BLOCK_ID_READ");
        aliases.put("block65", "BLOCK_ID_READ");
        return Collections.unmodifiableMap(aliases);
    }
}
