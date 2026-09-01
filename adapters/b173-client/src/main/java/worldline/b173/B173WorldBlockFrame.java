package worldline.b173;

/** One immutable observation from native special world block rendering. */
public final class B173WorldBlockFrame {
    private final int legacyId, metadata, renderType, geometryPixels;
    private final String frameSha256, provenance;

    B173WorldBlockFrame(int legacyId, int metadata, int renderType, int geometryPixels,
            String frameSha256, String provenance) {
        this.legacyId = legacyId;
        this.metadata = metadata;
        this.renderType = renderType;
        this.geometryPixels = geometryPixels;
        this.frameSha256 = frameSha256;
        this.provenance = provenance;
    }

    public int legacyId() { return legacyId; }
    public int metadata() { return metadata; }
    public int renderType() { return renderType; }
    public int geometryPixels() { return geometryPixels; }
    public String frameSha256() { return frameSha256; }
    public String provenance() { return provenance; }
}
