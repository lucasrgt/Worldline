package worldline.semantics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/**
 * Crafting and furnace recipe list symbols used by the adapter census.
 */
final class RecipeSemantics {
    private RecipeSemantics() {}

    static List<SemanticMapping> mappings() {
        return Collections.unmodifiableList(Arrays.asList(
                SemanticMapping.of("recipe", "CRAFTING", "net/minecraft/src/CraftingManager",
                        "class", "CraftingManager", "-", "", "", "INVENTORY",
                        "invariants", "", 9920),
                SemanticMapping.of("recipe", "CRAFTING_LIST", "net/minecraft/src/CraftingManager",
                        "method", "getRecipeList", "()Ljava/util/List;", "INVENTORY", "",
                        "INVENTORY", "invariants", "", 9920),
                SemanticMapping.of("recipe", "FURNACE", "net/minecraft/src/FurnaceRecipes",
                        "class", "FurnaceRecipes", "-", "", "", "INVENTORY",
                        "invariants", "", 9920),
                SemanticMapping.of("recipe", "FURNACE_LIST", "net/minecraft/src/FurnaceRecipes",
                        "method", "getSmeltingList", "()Ljava/util/Map;", "INVENTORY", "",
                        "INVENTORY", "invariants", "", 9920),
                SemanticMapping.of("recipe", "RECIPE_TYPE", "net/minecraft/src/IRecipe", "class",
                        "IRecipe", "-", "", "", "INVENTORY", "invariants", "", 9920),
                SemanticMapping.of("recipe", "RECIPE_OUTPUT", "net/minecraft/src/IRecipe",
                        "method", "getRecipeOutput", "()Lnet/minecraft/src/ItemStack;",
                        "INVENTORY", "", "INVENTORY", "invariants", "", 9920),
                SemanticMapping.of("recipe", "RECIPE_SHAPED", "net/minecraft/src/ShapedRecipes",
                        "field", "recipeItems", "[Lnet/minecraft/src/ItemStack;", "INVENTORY",
                        "", "INVENTORY", "invariants", "", 9920),
                SemanticMapping.of("recipe", "RECIPE_SHAPELESS",
                        "net/minecraft/src/ShapelessRecipes", "field", "recipeItems",
                        "Ljava/util/List;", "INVENTORY", "", "INVENTORY",
                        "invariants", "", 9920)));
    }
}
