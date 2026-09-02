package worldline.testapi;

/** Renderer-neutral evidence from one native tile-entity render invocation. */
public final class NativeTileEntityRenderObservation {
    private final NativeTileEntityRenderSubject subject;
    private final int geometryPixels;
    private final String frameSha256, work;

    public NativeTileEntityRenderObservation(NativeTileEntityRenderSubject subject,
            int geometryPixels, String frameSha256, String work) {
        if (subject == null || geometryPixels < 1 || geometryPixels > 128 * 128) {
            throw new IllegalArgumentException("invalid native tile-render coverage");
        }
        if (frameSha256 == null || !frameSha256.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("invalid native tile-render signature");
        }
        if (work == null || !work.matches("draw-calls=1,pixels=[1-9][0-9]*")) {
            throw new IllegalArgumentException("invalid native tile-render work");
        }
        this.subject = subject;
        this.geometryPixels = geometryPixels;
        this.frameSha256 = frameSha256;
        this.work = work;
    }

    public NativeTileEntityRenderSubject subject() { return subject; }
    public int geometryPixels() { return geometryPixels; }
    public String frameSha256() { return frameSha256; }
    public String work() { return work; }

    public String canonical() {
        return subject.canonical() + "|geometry-pixels=" + geometryPixels
                + "|frame=" + frameSha256 + "|work=" + work;
    }
}
