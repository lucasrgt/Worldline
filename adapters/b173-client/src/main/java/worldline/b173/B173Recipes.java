package worldline.b173;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.src.CraftingManager;
import net.minecraft.src.FurnaceRecipes;
import net.minecraft.src.IRecipe;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ShapedRecipes;
import net.minecraft.src.ShapelessRecipes;
import worldline.api.ItemCensus;
import worldline.api.ItemRecipe;

/** Snapshots vanilla crafting and smelting transforms as ID totals. */
final class B173Recipes {
    private B173Recipes() {}

    static List<ItemRecipe> snapshot() {
        List<ItemRecipe> recipes = new ArrayList<ItemRecipe>();
        for (Object raw : CraftingManager.getInstance().getRecipeList()) {
            ItemRecipe recipe = craft((IRecipe) raw);
            if (recipe != null) recipes.add(recipe);
        }
        for (Object raw : FurnaceRecipes.smelting().getSmeltingList().entrySet()) {
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) raw;
            ItemCensus input = ItemCensus.of(((Integer) entry.getKey()).intValue(), 1);
            ItemCensus output = B173Items.add(ItemCensus.empty(), (ItemStack) entry.getValue());
            if (output.total() > 0) recipes.add(new ItemRecipe(input, output));
        }
        recipes.addAll(B173Drops.snapshot());
        return Collections.unmodifiableList(recipes);
    }

    private static ItemRecipe craft(IRecipe recipe) {
        ItemCensus inputs = ItemCensus.empty();
        if (recipe instanceof ShapedRecipes) {
            inputs = B173Items.add(inputs,
                    (ItemStack[]) B173Reflect.get(ShapedRecipes.class, "recipeItems", recipe));
        } else if (recipe instanceof ShapelessRecipes) {
            List<?> stacks = (List<?>) B173Reflect.get(ShapelessRecipes.class, "recipeItems", recipe);
            if (stacks != null) {
                for (Object stack : stacks) inputs = B173Items.add(inputs, (ItemStack) stack);
            }
        } else {
            return null;
        }
        ItemCensus leftovers = leftovers(recipe);
        ItemCensus outputs = B173Items.add(ItemCensus.empty(), recipe.getRecipeOutput()).plus(leftovers);
        if (inputs.total() == 0 || outputs.total() == 0) return null;
        return new ItemRecipe(inputs, outputs);
    }

    private static ItemCensus leftovers(IRecipe recipe) {
        if (recipe instanceof ShapedRecipes) {
            return B173Items.addContainer(ItemCensus.empty(),
                    (ItemStack[]) B173Reflect.get(ShapedRecipes.class, "recipeItems", recipe));
        }
        ItemCensus census = ItemCensus.empty();
        if (recipe instanceof ShapelessRecipes) {
            List<?> stacks = (List<?>) B173Reflect.get(ShapelessRecipes.class, "recipeItems", recipe);
            if (stacks != null) {
                for (Object stack : stacks) census = B173Items.addContainer(census, (ItemStack) stack);
            }
        }
        return census;
    }
}
