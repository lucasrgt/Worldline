package worldline.invariants;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import worldline.api.FoodHeal;
import worldline.api.ItemCensus;

/** Sums health restored by consumed food item IDs. */
public final class FoodBook {
    private final Map<Integer, Integer> heals;

    private FoodBook(Map<Integer, Integer> heals) {
        this.heals = heals;
    }

    public static FoodBook none() {
        return new FoodBook(Collections.<Integer, Integer>emptyMap());
    }

    public static FoodBook of(List<FoodHeal> foods) {
        if (foods == null) throw new NullPointerException("foods");
        Map<Integer, Integer> heals = new TreeMap<Integer, Integer>();
        for (FoodHeal food : foods) {
            if (food == null) throw new NullPointerException("food");
            heals.put(Integer.valueOf(food.itemId()), Integer.valueOf(food.heal()));
        }
        return new FoodBook(Collections.unmodifiableMap(heals));
    }

    public int heal(ItemCensus lost) {
        if (lost == null) throw new NullPointerException("census");
        int total = 0;
        for (int itemId : lost.itemIds()) {
            Integer heal = heals.get(Integer.valueOf(itemId));
            if (heal != null) total += heal.intValue() * lost.count(itemId);
        }
        return total;
    }

    /** One application of each heal whose ID is present, for placed cake. */
    public int presence(ItemCensus blocks) {
        if (blocks == null) throw new NullPointerException("census");
        int total = 0;
        for (int itemId : blocks.itemIds()) {
            Integer heal = heals.get(Integer.valueOf(itemId));
            if (heal != null && blocks.count(itemId) > 0) total += heal.intValue();
        }
        return total;
    }
}
