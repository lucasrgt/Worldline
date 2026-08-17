package worldline.b173;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.api.CauseDrop;
import worldline.api.ItemCensus;
import worldline.api.ItemRecipe;

/** Block-ID swaps and world-tick presence, including plants and cobble. */
final class B173Transforms {
    private B173Transforms() {}

    static List<ItemRecipe> swaps() {
        List<ItemRecipe> recipes = new ArrayList<ItemRecipe>();
        recipes.add(swap(3, 2));
        recipes.add(swap(2, 3));
        recipes.add(swap(3, 60));
        recipes.add(swap(60, 3));
        recipes.add(swap(8, 9));
        recipes.add(swap(9, 8));
        recipes.add(swap(10, 11));
        recipes.add(swap(11, 10));
        recipes.add(swap(79, 8));
        recipes.add(swap(79, 9));
        recipes.add(swap(8, 79));
        recipes.add(swap(9, 79));
        pair(recipes, 61, 62);
        pair(recipes, 73, 74);
        pair(recipes, 75, 76);
        pair(recipes, 93, 94);
        return Collections.unmodifiableList(recipes);
    }

    static List<CauseDrop> fluids() {
        List<CauseDrop> drops = new ArrayList<CauseDrop>();
        drops.add(around(8, 8, 9, 4, 49));
        drops.add(around(9, 8, 9, 4, 49));
        drops.add(around(10, 10, 11, 4, 49));
        drops.add(around(11, 10, 11, 4, 49));
        drops.add(around(51, 51));
        drops.add(around(81, 81));
        drops.add(around(83, 83));
        drops.add(around(39, 39));
        drops.add(around(40, 40));
        drops.add(around(2, 78));
        drops.add(around(78, 78));
        drops.add(around(49, 90));
        drops.add(around(90, 90));
        drops.add(spent(6, 17, 18));
        drops.add(CauseDrop.death("minecraft:falling-block", ItemCensus.of(12, 64).plus(13, 64)));
        return Collections.unmodifiableList(drops);
    }

    private static void pair(List<ItemRecipe> recipes, int a, int b) {
        recipes.add(swap(a, b));
        recipes.add(swap(b, a));
    }

    private static ItemRecipe swap(int inputId, int outputId) {
        return new ItemRecipe(ItemCensus.of(inputId, 1), ItemCensus.of(outputId, 1));
    }

    private static CauseDrop around(int host, int... outputs) {
        return CauseDrop.presence("block:" + host, many(outputs));
    }

    private static CauseDrop spent(int host, int... outputs) {
        return CauseDrop.death("block:" + host, many(outputs));
    }

    private static ItemCensus many(int... outputs) {
        ItemCensus census = ItemCensus.empty();
        for (int output : outputs) census = census.plus(output, 64);
        return census;
    }
}
