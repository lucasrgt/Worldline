package worldline.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Immutable observed item counts. Empty stacks and item ID {@code -1} are omitted.
 * Counts are totals, not slot layout.
 */
public final class ItemCensus {
    private final Map<Integer, Integer> counts;

    private ItemCensus(Map<Integer, Integer> counts) {
        this.counts = Collections.unmodifiableMap(new TreeMap<Integer, Integer>(counts));
    }

    public static ItemCensus empty() {
        return new ItemCensus(new TreeMap<Integer, Integer>());
    }

    public static ItemCensus of(int itemId, int count) {
        return empty().plus(itemId, count);
    }

    public static ItemCensus fromNodes(List<GameUiNode> nodes) {
        if (nodes == null) throw new NullPointerException("nodes");
        ItemCensus census = empty();
        for (GameUiNode node : nodes) {
            if (node == null) throw new NullPointerException("node");
            if (!GameUiNode.SLOT.equals(node.role()) || node.empty()) continue;
            census = census.plus(node.itemId(), node.count());
        }
        return census;
    }

    public ItemCensus plus(int itemId, int count) {
        if (itemId < 0) throw new IllegalArgumentException("item id");
        if (count < 0) throw new IllegalArgumentException("item count");
        if (count == 0) return this;
        Map<Integer, Integer> next = new TreeMap<Integer, Integer>(counts);
        long total = (long) countOf(next, itemId) + count;
        if (total > Integer.MAX_VALUE) throw new IllegalArgumentException("item count overflow");
        next.put(itemId, (int) total);
        return new ItemCensus(next);
    }

    public ItemCensus plus(ItemCensus other) {
        if (other == null) throw new NullPointerException("census");
        ItemCensus result = this;
        for (Map.Entry<Integer, Integer> entry : other.counts.entrySet()) {
            result = result.plus(entry.getKey().intValue(), entry.getValue().intValue());
        }
        return result;
    }

    public int count(int itemId) {
        if (itemId < 0) throw new IllegalArgumentException("item id");
        return countOf(counts, itemId);
    }

    public int total() {
        int sum = 0;
        for (int count : counts.values()) sum += count;
        return sum;
    }

    public Set<Integer> itemIds() {
        return counts.keySet();
    }

    /** Counts this has strictly more of than {@code later}. */
    public ItemCensus decrease(ItemCensus later) {
        if (later == null) throw new NullPointerException("census");
        ItemCensus result = empty();
        for (int itemId : itemIds()) {
            int drop = count(itemId) - later.count(itemId);
            if (drop > 0) result = result.plus(itemId, drop);
        }
        return result;
    }

    public boolean contains(ItemCensus required) {
        if (required == null) throw new NullPointerException("census");
        for (int itemId : required.itemIds()) {
            if (count(itemId) < required.count(itemId)) return false;
        }
        return true;
    }

    /** True when any item ID has a strictly higher count than {@code baseline}. */
    public boolean exceeds(ItemCensus baseline) {
        if (baseline == null) throw new NullPointerException("baseline");
        for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > baseline.count(entry.getKey())) return true;
        }
        return false;
    }

    @Override public boolean equals(Object other) {
        return other instanceof ItemCensus && counts.equals(((ItemCensus) other).counts);
    }

    @Override public int hashCode() {
        return counts.hashCode();
    }

    @Override public String toString() {
        return "ItemCensus" + counts;
    }

    private static int countOf(Map<Integer, Integer> counts, int itemId) {
        Integer value = counts.get(itemId);
        return value == null ? 0 : value.intValue();
    }
}
