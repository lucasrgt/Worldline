package worldline.testapi;

/** Renderer-neutral observation returned by one native client invocation. */
public final class NativeBlockRenderObservation {
    private final String subject, frameSha256, work;
    private final int legacyId, metadata, renderType, geometryPixels;

    public NativeBlockRenderObservation(String subject, int legacyId, int metadata,
            int renderType, int geometryPixels, String frameSha256, String work) {
        new NativeBlockRenderSubject(subject, legacyId, metadata, renderType);
        if (geometryPixels < 1 || geometryPixels > 96 * 96) {
            throw new IllegalArgumentException("invalid native render geometry coverage");
        }
        if (frameSha256 == null || !frameSha256.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("invalid native render frame signature");
        }
        if (work == null || !work.matches("draw-calls=[1-9][0-9]*,pixels=[1-9][0-9]*")) {
            throw new IllegalArgumentException("invalid native render work");
        }
        this.subject = subject;
        this.legacyId = legacyId;
        this.metadata = metadata;
        this.renderType = renderType;
        this.geometryPixels = geometryPixels;
        this.frameSha256 = frameSha256;
        this.work = work;
    }

    public String subject() { return subject; }
    public int legacyId() { return legacyId; }
    public int metadata() { return metadata; }
    public int renderType() { return renderType; }
    public int geometryPixels() { return geometryPixels; }
    public String frameSha256() { return frameSha256; }
    public String work() { return work; }

    public String canonical() {
        return subject + '|' + legacyId + ':' + metadata + "|render-type=" + renderType
                + "|geometry-pixels=" + geometryPixels + "|frame=" + frameSha256
                + "|work=" + work;
    }
}
