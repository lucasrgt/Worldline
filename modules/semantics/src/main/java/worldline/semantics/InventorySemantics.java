package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/**
 * Item-census type and player/world item and block census symbols.
 */
final class InventorySemantics {
    private InventorySemantics() {}

    static List<SemanticMapping> mappings() {
        return Collections.unmodifiableList(Arrays.asList(
                SemanticMapping.of("inventory", "INVENTORY_TYPE", "worldline/api/ItemCensus",
                        "class", "ItemCensus", "-", "", "", "INVENTORY", "invariants", "", 9990),
                SemanticMapping.of("inventory", "PLAYER_ITEMS", "worldline/api/GamePlayer",
                        "method", "items", "()Lworldline/api/ItemCensus;", "PLAYER", "",
                        "INVENTORY", "invariants", "", 9990),
                SemanticMapping.of("inventory", "WORLD_ITEMS", "worldline/api/GameWorld",
                        "method", "items", "()Lworldline/api/ItemCensus;", "WORLD", "",
                        "INVENTORY", "invariants", "", 9990),
                SemanticMapping.of("inventory", "WORLD_BLOCKS", "worldline/api/GameWorld",
                        "method", "blocks", "()Lworldline/api/ItemCensus;", "WORLD", "",
                        "INVENTORY", "invariants", "", 9990),
                SemanticMapping.of("inventory", "MAIN_ITEMS", "net/minecraft/src/InventoryPlayer",
                        "field", "mainInventory", "[Lnet/minecraft/src/ItemStack;", "PLAYER",
                        "PLAYER", "INVENTORY", "invariants,lab-cycle", "", 9920),
                SemanticMapping.of("inventory", "ARMOR_ITEMS", "net/minecraft/src/InventoryPlayer",
                        "field", "armorInventory", "[Lnet/minecraft/src/ItemStack;", "PLAYER",
                        "PLAYER", "INVENTORY", "invariants,lab-cycle", "", 9920),
                SemanticMapping.of("inventory", "CURSOR_STACK", "net/minecraft/src/InventoryPlayer",
                        "method", "getItemStack", "()Lnet/minecraft/src/ItemStack;", "PLAYER",
                        "", "INVENTORY", "invariants,lab-cycle", "", 9920)));
    }
}
