package worldline.api;

/** Immutable Beta block ID and metadata pair. */
public final class BlockState {
    private final int legacyId;
    private final int metadata;

    public BlockState(int legacyId, int metadata) {
        if (legacyId < 0 || legacyId > 255) throw new IllegalArgumentException("block ID must be 0..255");
        if (metadata < 0 || metadata > 15) throw new IllegalArgumentException("metadata must be 0..15");
        this.legacyId = legacyId;
        this.metadata = metadata;
    }

    public int legacyId() { return legacyId; }

    public int metadata() { return metadata; }

    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof BlockState)) return false;
        BlockState value = (BlockState) other;
        return legacyId == value.legacyId && metadata == value.metadata;
    }

    @Override public int hashCode() { return 31 * legacyId + metadata; }

    @Override public String toString() { return "BlockState[" + legacyId + ":" + metadata + "]"; }
}
