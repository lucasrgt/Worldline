package worldline.b173;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.src.Item;
import net.minecraft.src.ItemFood;
import worldline.api.FoodHeal;

/** Vanilla food heal amounts from every ItemFood with a positive heal. */
final class B173Foods {
    private B173Foods() {}

    static List<FoodHeal> snapshot() {
        List<FoodHeal> foods = new ArrayList<FoodHeal>();
        Item[] items = Item.itemsList;
        for (int id = 0; id < items.length; id++) {
            Item item = items[id];
            if (!(item instanceof ItemFood)) continue;
            int heal = ((ItemFood) item).getHealAmount();
            if (heal > 0) foods.add(new FoodHeal(item.shiftedIndex, heal));
        }
        foods.add(new FoodHeal(92, 3));
        return Collections.unmodifiableList(foods);
    }
}
