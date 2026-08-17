package worldline.invariants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.api.CauseDrop;
import worldline.api.EntityCensus;
import worldline.api.ItemCensus;

/**
 * Explains item gain from entity death or presence. Each application subtracts
 * at most the cause's maximum outputs. Extra loss is allowed.
 */
public final class DropBook {
    private static final int MAX_APPLIES = 64;
    private final List<CauseDrop> drops;

    private DropBook(List<CauseDrop> drops) {
        this.drops = drops;
    }

    public static DropBook none() {
        return new DropBook(Collections.<CauseDrop>emptyList());
    }

    public static DropBook of(List<CauseDrop> drops) {
        if (drops == null) throw new NullPointerException("drops");
        List<CauseDrop> copy = new ArrayList<CauseDrop>();
        for (CauseDrop drop : drops) {
            if (drop == null) throw new NullPointerException("drop");
            copy.add(drop);
        }
        return new DropBook(Collections.unmodifiableList(copy));
    }

    public boolean explains(ItemCensus gain, EntityCensus lost, EntityCensus current) {
        if (gain == null || lost == null || current == null) throw new NullPointerException("census");
        if (gain.total() == 0) return true;
        ItemCensus remaining = gain;
        remaining = apply(remaining, lost, true);
        remaining = apply(remaining, current, false);
        return remaining.total() == 0;
    }

    public List<CauseDrop> drops() {
        return drops;
    }

    private ItemCensus apply(ItemCensus remaining, EntityCensus census, boolean consume) {
        for (String type : census.types()) {
            CauseDrop drop = find(type, consume);
            if (drop == null) continue;
            int times = Math.min(census.count(type), MAX_APPLIES);
            for (int step = 0; step < times && remaining.total() > 0; step++) {
                remaining = remaining.decrease(capped(remaining, drop.outputs()));
            }
        }
        return remaining;
    }

    private CauseDrop find(String type, boolean consume) {
        for (CauseDrop drop : drops) {
            if (drop.consume() == consume && drop.type().equals(type)) return drop;
        }
        return null;
    }

    private static ItemCensus capped(ItemCensus gain, ItemCensus max) {
        ItemCensus taken = ItemCensus.empty();
        for (int itemId : max.itemIds()) {
            int count = Math.min(gain.count(itemId), max.count(itemId));
            if (count > 0) taken = taken.plus(itemId, count);
        }
        return taken;
    }
}
