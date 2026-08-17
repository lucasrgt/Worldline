package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/**
 * Item stack, item type, container inventory, and dropped-item stack symbols.
 */
final class ItemSemantics {
    private ItemSemantics() {}

    static List<SemanticMapping> mappings() {
        return Collections.unmodifiableList(Arrays.asList(
                SemanticMapping.of("item", "ITEM_STACK", "net/minecraft/src/ItemStack", "class",
                        "ItemStack", "-", "", "", "INVENTORY",
                        "gui-tree,invariants,lab-cycle", "iz", 9990),
                SemanticMapping.of("item", "ITEM_ID", "net/minecraft/src/ItemStack", "field",
                        "itemID", "I", "INVENTORY", "", "INVENTORY",
                        "gui-tree,invariants", "c", 9990),
                SemanticMapping.of("item", "STACK_SIZE", "net/minecraft/src/ItemStack", "field",
                        "stackSize", "I", "INVENTORY", "INVENTORY", "INVENTORY",
                        "gui-tree,invariants", "a", 9990),
                SemanticMapping.of("item", "ITEM_TYPE", "net/minecraft/src/Item", "class",
                        "Item", "-", "", "", "INVENTORY", "invariants", "", 9920),
                SemanticMapping.of("item", "ITEM_LOOKUP", "net/minecraft/src/ItemStack", "method",
                        "getItem", "()Lnet/minecraft/src/Item;", "INVENTORY", "", "INVENTORY",
                        "invariants", "", 9920),
                SemanticMapping.of("item", "ITEM_DAMAGE", "net/minecraft/src/ItemStack", "method",
                        "getItemDamage", "()I", "INVENTORY", "", "INVENTORY",
                        "invariants", "", 9920),
                SemanticMapping.of("item", "CONTAINER_TYPE", "net/minecraft/src/IInventory",
                        "class", "IInventory", "-", "", "", "INVENTORY",
                        "invariants,lab-cycle", "", 9920),
                SemanticMapping.of("item", "SLOT_COUNT", "net/minecraft/src/IInventory", "method",
                        "getSizeInventory", "()I", "INVENTORY", "", "INVENTORY",
                        "invariants", "", 9920),
                SemanticMapping.of("item", "SLOT_GET", "net/minecraft/src/IInventory", "method",
                        "getStackInSlot", "(I)Lnet/minecraft/src/ItemStack;", "INVENTORY", "",
                        "INVENTORY", "invariants", "", 9920),
                SemanticMapping.of("item", "ENTITY_ITEM_STACK", "net/minecraft/src/EntityItem",
                        "field", "item", "Lnet/minecraft/src/ItemStack;", "ENTITY", "",
                        "INVENTORY", "invariants,lab-cycle", "", 9920)));
    }
}
