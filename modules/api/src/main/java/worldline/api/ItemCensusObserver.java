package worldline.api;

/** Receives one item census after a controlled tick. */
public interface ItemCensusObserver {
    void observe(ItemCensus census);

    default void observe(ItemCensus items, ItemCensus blocks) {
        observe(items);
    }

    default void observe(InvariantSample sample) {
        if (sample == null) throw new NullPointerException("sample");
        observe(sample.items(), sample.blocks());
    }
}
