package worldline.api;

/** One crafting or smelting transform: consume {@code inputs}, produce {@code outputs}. */
public final class ItemRecipe {
    private final ItemCensus inputs, outputs;

    public ItemRecipe(ItemCensus inputs, ItemCensus outputs) {
        if (inputs == null || outputs == null) throw new NullPointerException("recipe");
        if (inputs.total() == 0 || outputs.total() == 0) throw new IllegalArgumentException("recipe totals");
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public ItemCensus inputs() {
        return inputs;
    }

    public ItemCensus outputs() {
        return outputs;
    }

    @Override public boolean equals(Object other) {
        return other instanceof ItemRecipe && inputs.equals(((ItemRecipe) other).inputs)
                && outputs.equals(((ItemRecipe) other).outputs);
    }

    @Override public int hashCode() {
        return 31 * inputs.hashCode() + outputs.hashCode();
    }
}
