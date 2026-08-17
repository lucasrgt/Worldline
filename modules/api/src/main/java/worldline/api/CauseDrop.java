package worldline.api;

/** One death or presence cause that may produce up to {@code outputs} items. */
public final class CauseDrop {
    private final String type;
    private final ItemCensus outputs;
    private final boolean consume;

    private CauseDrop(String type, ItemCensus outputs, boolean consume) {
        if (type == null || type.isEmpty()) throw new IllegalArgumentException("cause type");
        if (outputs == null || outputs.total() == 0) throw new IllegalArgumentException("cause outputs");
        this.type = type;
        this.outputs = outputs;
        this.consume = consume;
    }

    public static CauseDrop death(String type, ItemCensus outputs) {
        return new CauseDrop(type, outputs, true);
    }

    public static CauseDrop presence(String type, ItemCensus outputs) {
        return new CauseDrop(type, outputs, false);
    }

    public String type() {
        return type;
    }

    public ItemCensus outputs() {
        return outputs;
    }

    public boolean consume() {
        return consume;
    }

    @Override public boolean equals(Object other) {
        if (!(other instanceof CauseDrop)) return false;
        CauseDrop drop = (CauseDrop) other;
        return consume == drop.consume && type.equals(drop.type) && outputs.equals(drop.outputs);
    }

    @Override public int hashCode() {
        return 31 * (31 * type.hashCode() + outputs.hashCode()) + (consume ? 1 : 0);
    }
}
