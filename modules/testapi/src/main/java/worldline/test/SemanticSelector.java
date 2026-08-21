package worldline.test;

/** Evidence-backed versioned selector that never exposes obfuscated names. */
public final class SemanticSelector {
    public enum Kind { BLOCK, ITEM, ENTITY, PACKET }
    public enum Access { READ_ONLY, READ_WRITE }
    public enum Stability { EXPERIMENTAL, STABLE }
    private final String key, evidence;
    private final Kind kind;
    private final Access access;
    private final Stability stability;
    private final int legacyId, metadata;

    SemanticSelector(String key, Kind kind, int legacyId, int metadata,
            String evidence, Access access, Stability stability) {
        this.key = key; this.kind = kind; this.legacyId = legacyId; this.metadata = metadata;
        this.evidence = evidence; this.access = access; this.stability = stability;
    }
    public String key() { return key; }
    public Kind kind() { return kind; }
    public int legacyId() { return legacyId; }
    public int metadata() { return metadata; }
    public String evidence() { return evidence; }
    public Access access() { return access; }
    public Stability stability() { return stability; }
    public boolean writable() { return access == Access.READ_WRITE; }
    public SemanticSelector requireWrite() {
        if (!writable()) throw new IllegalStateException("WLTEST E2104: " + key
                + " has no promoted write mapping; evidence available: read-only " + evidence);
        return this;
    }
    @Override public String toString() {
        return key + " [" + legacyId + (metadata >= 0 ? ":" + metadata : "") + "]";
    }
}
