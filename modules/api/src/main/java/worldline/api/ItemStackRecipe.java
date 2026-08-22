package worldline.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/** Immutable recipe multiset that preserves legacy item damage metadata. */
public final class ItemStackRecipe {
    private static final int MAX_DISTINCT_STACKS = 64;
    private final List<RemoteItemStack> inputs, outputs;

    public ItemStackRecipe(List<RemoteItemStack> inputs, List<RemoteItemStack> outputs) {
        this.inputs = canonical(inputs, "inputs");
        this.outputs = canonical(outputs, "outputs");
    }

    public List<RemoteItemStack> inputs() { return inputs; }
    public List<RemoteItemStack> outputs() { return outputs; }

    private static List<RemoteItemStack> canonical(List<RemoteItemStack> source, String label) {
        if (source == null) throw new NullPointerException(label);
        if (source.isEmpty()) throw new IllegalArgumentException("empty recipe " + label);
        TreeMap<Long, Integer> totals = new TreeMap<Long, Integer>();
        for (RemoteItemStack stack : source) {
            if (stack == null) throw new NullPointerException(label + " stack");
            long key = ((long) stack.legacyId() << 32) | stack.damage();
            int total = totals.getOrDefault(key, 0) + stack.count();
            if (total > 127) throw new IllegalArgumentException("recipe stack count overflow");
            totals.put(key, total);
        }
        if (totals.size() > MAX_DISTINCT_STACKS)
            throw new IllegalArgumentException("too many distinct recipe stacks");
        ArrayList<RemoteItemStack> result = new ArrayList<RemoteItemStack>();
        for (Map.Entry<Long, Integer> entry : totals.entrySet()) {
            long key = entry.getKey();
            result.add(new RemoteItemStack((int) (key >>> 32), entry.getValue(), (int) key));
        }
        return Collections.unmodifiableList(result);
    }

    @Override public boolean equals(Object other) {
        if (!(other instanceof ItemStackRecipe)) return false;
        ItemStackRecipe value = (ItemStackRecipe) other;
        return inputs.equals(value.inputs) && outputs.equals(value.outputs);
    }

    @Override public int hashCode() { return Objects.hash(inputs, outputs); }
}
