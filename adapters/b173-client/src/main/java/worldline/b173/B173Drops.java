package worldline.b173;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.src.Block;
import worldline.api.ItemCensus;
import worldline.api.ItemRecipe;

/** Snapshots block-break drops as consume-block / produce-item recipes. */
final class B173Drops {
    private B173Drops() {}

    static List<ItemRecipe> snapshot() {
        List<ItemRecipe> recipes = new ArrayList<ItemRecipe>();
        for (int id = 0; id < Block.blocksList.length; id++) {
            Block block = Block.blocksList[id];
            if (block != null) recipes.addAll(sample(id, block));
        }
        return Collections.unmodifiableList(recipes);
    }

    private static List<ItemRecipe> sample(int id, Block block) {
        Set<ItemRecipe> unique = new LinkedHashSet<ItemRecipe>();
        for (int seed = 0; seed < 256; seed++) {
            int dropId = block.idDropped(0, new Random((long) seed));
            int qty = block.quantityDropped(new Random((long) seed));
            if (dropId > 0 && qty > 0) {
                unique.add(new ItemRecipe(ItemCensus.of(id, 1), ItemCensus.of(dropId, qty)));
            }
        }
        return new ArrayList<ItemRecipe>(unique);
    }
}
