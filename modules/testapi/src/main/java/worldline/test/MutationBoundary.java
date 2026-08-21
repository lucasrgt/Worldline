package worldline.test;

/** One mapped mutation boundary and its promoted evidence. */
public final class MutationBoundary {
    private final String id, mapping, evidence;
    private final MutationQuality quality;

    public MutationBoundary(String id, String mapping, MutationQuality quality, String evidence) {
        if (!token(id) || !token(mapping) || quality == null || evidence == null
                || evidence.trim().isEmpty()) throw new IllegalArgumentException("mutation boundary");
        this.id = id; this.mapping = mapping; this.quality = quality; this.evidence = evidence.trim();
    }
    public String id() { return id; }
    public String mapping() { return mapping; }
    public MutationQuality quality() { return quality; }
    public String evidence() { return evidence; }
    private static boolean token(String value) {
        return value != null && value.matches("[a-z][a-z0-9_.-]*");
    }
}
