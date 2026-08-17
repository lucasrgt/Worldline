package worldline.api;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/** Damageable-item totals: stack count and summed damage per item ID. */
public final class WearCensus {
    private final Map<Integer, int[]> values;

    private WearCensus(Map<Integer, int[]> values) {
        this.values = Collections.unmodifiableMap(new TreeMap<Integer, int[]>(values));
    }

    public static WearCensus empty() {
        return new WearCensus(new TreeMap<Integer, int[]>());
    }

    public WearCensus plus(int itemId, int damage, int stacks) {
        if (itemId < 0 || damage < 0 || stacks < 0) throw new IllegalArgumentException("wear");
        if (stacks == 0) return this;
        Map<Integer, int[]> next = new TreeMap<Integer, int[]>(values);
        int[] prior = next.get(itemId);
        int count = (prior == null ? 0 : prior[0]) + stacks;
        int total = (prior == null ? 0 : prior[1]) + damage;
        next.put(itemId, new int[] {count, total});
        return new WearCensus(next);
    }

    public WearCensus plus(WearCensus other) {
        if (other == null) throw new NullPointerException("wear");
        WearCensus result = this;
        for (Map.Entry<Integer, int[]> entry : other.values.entrySet()) {
            result = result.plus(entry.getKey().intValue(), entry.getValue()[1], entry.getValue()[0]);
        }
        return result;
    }

    public int count(int itemId) {
        int[] value = values.get(itemId);
        return value == null ? 0 : value[0];
    }

    public int damage(int itemId) {
        int[] value = values.get(itemId);
        return value == null ? 0 : value[1];
    }

    public Set<Integer> itemIds() {
        return values.keySet();
    }

    /** True when some ID kept or lost stacks but lost damage points (a repair). */
    public boolean repairedVersus(WearCensus previous) {
        if (previous == null) throw new NullPointerException("wear");
        for (int itemId : previous.itemIds()) {
            if (count(itemId) <= previous.count(itemId) && damage(itemId) < previous.damage(itemId)) {
                return true;
            }
        }
        return false;
    }
}
