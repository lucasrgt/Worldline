package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/**
 * Local player, player type, name, health, hotbar, and inventory symbols for
 * b1.7.3.
 */
final class PlayerSemantics {
    private PlayerSemantics() {}

    static List<SemanticMapping> mappings() {
        return Collections.unmodifiableList(Arrays.asList(
                SemanticMapping.of("player", "LOCAL_PLAYER", "net/minecraft/src/EntityPlayerSP",
                        "class", "EntityPlayerSP", "-", "", "", "PLAYER",
                        "controlled-client-tick,symbols.map,m3-domain-api", "", 9998),
                SemanticMapping.of("player", "PLAYER_TYPE", "net/minecraft/src/EntityPlayer",
                        "class", "EntityPlayer", "-", "", "", "PLAYER",
                        "controlled-client-tick,symbols.map,m3-domain-api", "", 9998),
                SemanticMapping.of("player", "LIVING_TYPE", "net/minecraft/src/EntityLiving",
                        "class", "EntityLiving", "-", "", "", "PLAYER",
                        "controlled-client-tick,symbols.map,m3-domain-api", "", 9998),
                SemanticMapping.of("player", "PLAYER_NAME", "net/minecraft/src/EntityPlayer",
                        "field", "username", "Ljava/lang/String;", "PLAYER", "", "PLAYER",
                        "controlled-client-tick,symbols.map,m3-domain-api", "l", 9998),
                SemanticMapping.of("player", "PLAYER_HEALTH", "net/minecraft/src/EntityLiving",
                        "field", "health", "I", "PLAYER", "PLAYER", "PLAYER",
                        "controlled-client-tick,symbols.map,m3-domain-api", "Y", 9998),
                SemanticMapping.of("player", "HOTBAR_SLOT", "net/minecraft/src/InventoryPlayer",
                        "field", "currentItem", "I", "PLAYER", "PLAYER", "PLAYER",
                        "controlled-client-tick,symbols.map,m3-domain-api,lab-cycle", "c", 9998),
                SemanticMapping.of("player", "PLAYER_INVENTORY", "net/minecraft/src/InventoryPlayer",
                        "class", "InventoryPlayer", "-", "", "", "PLAYER",
                        "controlled-client-tick,symbols.map", "", 9998),
                SemanticMapping.of("player", "INVENTORY_FIELD", "net/minecraft/src/EntityPlayer",
                        "field", "inventory", "Lnet/minecraft/src/InventoryPlayer;", "PLAYER",
                        "PLAYER", "PLAYER", "m3-domain-api,lab-cycle,gui-tree", "", 9990)));
    }
}
