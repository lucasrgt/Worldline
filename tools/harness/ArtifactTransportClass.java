/** Closed classification for content-addressed artifact transport. */
enum ArtifactTransportClass {
    PORTABLE_WORLDLINE_SOURCE_DERIVED("portable-worldline-source-derived", true),
    PORTABLE_BEHAVIOR_PROOF("portable-behavior-proof", true),
    SANITIZED_DIAGNOSTIC("sanitized-diagnostic", true),
    LOCAL_PRIVATE("local-private", false),
    OFFICIAL_OR_DERIVED_FORBIDDEN("official-or-derived-forbidden", false);

    private final String id;
    private final boolean remotelyEligible;

    ArtifactTransportClass(String id, boolean remotelyEligible) {
        this.id = id; this.remotelyEligible = remotelyEligible;
    }

    boolean remotelyEligible() { return remotelyEligible; }
    String id() { return id; }

    static ArtifactTransportClass parse(String value) {
        for (ArtifactTransportClass candidate : values())
            if (candidate.id.equals(value)) return candidate;
        throw new IllegalStateException("unknown artifact transport class: " + value);
    }
}
