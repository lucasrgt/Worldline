package worldline.b173;

/** One immutable observation from the official native inventory renderer. */
public final class B173BlockInventoryFrame {
    private final int legacyId, metadata, renderType, geometryPixels, drawCalls;
    private final String frameSha256, provenance;

    B173BlockInventoryFrame(int legacyId, int metadata, int renderType,
            int geometryPixels, int drawCalls, String frameSha256, String provenance) {
        this.legacyId = legacyId;
        this.metadata = metadata;
        this.renderType = renderType;
        this.geometryPixels = geometryPixels;
        this.drawCalls = drawCalls;
        this.frameSha256 = frameSha256;
        this.provenance = provenance;
    }

    public int legacyId() { return legacyId; }
    public int metadata() { return metadata; }
    public int renderType() { return renderType; }
    public int geometryPixels() { return geometryPixels; }
    public int drawCalls() { return drawCalls; }
    public String frameSha256() { return frameSha256; }
    public String provenance() { return provenance; }
}
