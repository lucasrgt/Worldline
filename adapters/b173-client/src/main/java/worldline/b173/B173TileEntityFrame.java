package worldline.b173;

/** One immutable observation from native tile-entity rendering. */
public final class B173TileEntityFrame {
    private final int legacyId, metadata, geometryPixels;
    private final String renderer, frameSha256, provenance;

    B173TileEntityFrame(int legacyId, int metadata, String renderer, int geometryPixels,
            String frameSha256, String provenance) {
        this.legacyId = legacyId;
        this.metadata = metadata;
        this.renderer = renderer;
        this.geometryPixels = geometryPixels;
        this.frameSha256 = frameSha256;
        this.provenance = provenance;
    }

    public int legacyId() { return legacyId; }
    public int metadata() { return metadata; }
    public String renderer() { return renderer; }
    public int geometryPixels() { return geometryPixels; }
    public String frameSha256() { return frameSha256; }
    public String provenance() { return provenance; }
}
