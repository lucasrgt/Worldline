/** One declared cache or evidence family and its transport boundary. */
record ArtifactTransportFamily(String id, ArtifactTransportClass classification,
        String root, String suffix, long maxBytes, String validator) { }
