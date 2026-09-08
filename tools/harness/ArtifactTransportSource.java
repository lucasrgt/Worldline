/** Closed provenance classes for transport candidate discovery. */
enum ArtifactTransportSource {
    TRACKED_REPOSITORY("tracked-repository"),
    GENERATED_LOCAL("generated-local"),
    NONE("none");

    private final String id;

    ArtifactTransportSource(String id) { this.id = id; }

    boolean tracked() { return this == TRACKED_REPOSITORY; }
    boolean generated() { return this == GENERATED_LOCAL; }

    static ArtifactTransportSource parse(String value) {
        for (ArtifactTransportSource candidate : values())
            if (candidate.id.equals(value)) return candidate;
        throw new IllegalStateException("unknown artifact transport source: " + value);
    }
}
