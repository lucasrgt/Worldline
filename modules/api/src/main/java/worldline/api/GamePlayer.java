package worldline.api;

/** Stable neutral automation surface for the controlled local player. */
public interface GamePlayer extends GameEntity {
    String username();

    int health();

    int selectedHotbarSlot();

    void selectHotbarSlot(int slot);

    /** Read-only main, armor, and cursor totals. Not inventory manipulation. */
    ItemCensus items();

    /**
     * Adds {@code count} items of a legacy item id to the main inventory using
     * vanilla stack merging. Bounded counts fail closed on overflow.
     */
    default void give(int itemId, int count) {
        throw new UnsupportedOperationException("give is not supported by this runtime");
    }

    default WearCensus wear() {
        return WearCensus.empty();
    }
}
