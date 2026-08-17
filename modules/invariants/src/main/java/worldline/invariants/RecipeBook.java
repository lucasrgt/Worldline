package worldline.invariants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.api.ItemCensus;
import worldline.api.ItemRecipe;

/**
 * Explains a census gain by greedily applying known recipes. Extra loss (fuel,
 * leftovers) is allowed. Unexplained gain is not.
 */
public final class RecipeBook {
    private static final int MAX_APPLIES = 64;
    private final List<ItemRecipe> recipes;

    private RecipeBook(List<ItemRecipe> recipes) {
        this.recipes = recipes;
    }

    public static RecipeBook none() {
        return new RecipeBook(Collections.<ItemRecipe>emptyList());
    }

    public static RecipeBook of(List<ItemRecipe> recipes) {
        if (recipes == null) throw new NullPointerException("recipes");
        List<ItemRecipe> copy = new ArrayList<ItemRecipe>();
        for (ItemRecipe recipe : recipes) {
            if (recipe == null) throw new NullPointerException("recipe");
            copy.add(recipe);
        }
        return new RecipeBook(Collections.unmodifiableList(copy));
    }

    public boolean explains(ItemCensus before, ItemCensus after) {
        return explains(before, after, ItemCensus.empty(), ItemCensus.empty());
    }

    public boolean explains(ItemCensus beforeItems, ItemCensus afterItems,
            ItemCensus beforeBlocks, ItemCensus afterBlocks) {
        if (beforeItems == null || afterItems == null || beforeBlocks == null || afterBlocks == null) {
            throw new NullPointerException("census");
        }
        return explainsGain(afterItems.decrease(beforeItems),
                beforeItems.decrease(afterItems).plus(beforeBlocks.decrease(afterBlocks)));
    }

    public boolean explainsGain(ItemCensus gain, ItemCensus loss) {
        return remainder(gain, loss).total() == 0;
    }

    public ItemCensus remainder(ItemCensus gain, ItemCensus loss) {
        if (gain == null || loss == null) throw new NullPointerException("census");
        for (int step = 0; step < MAX_APPLIES; step++) {
            if (gain.total() == 0) return gain;
            ItemRecipe match = match(gain, loss);
            if (match == null) return gain;
            gain = gain.decrease(match.outputs());
            loss = loss.decrease(match.inputs());
        }
        return gain;
    }

    public List<ItemRecipe> recipes() {
        return recipes;
    }

    private ItemRecipe match(ItemCensus gain, ItemCensus loss) {
        for (ItemRecipe recipe : recipes) {
            if (gain.contains(recipe.outputs()) && loss.contains(recipe.inputs())) return recipe;
        }
        return null;
    }
}
