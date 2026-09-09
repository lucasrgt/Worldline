package worldline.testapi;

/** Renderer dispatch identity and nonempty native RGBA output for a canonical entity. */
public final class NativeEntityRenderObservation {
    private final NativeEntityRenderSubject subject;
    private final String rendererClass, frameSha256;
    private final int geometryPixels;

    public NativeEntityRenderObservation(NativeEntityRenderSubject subject, String rendererClass,
            int geometryPixels, String frameSha256) {
        if (subject == null || rendererClass == null || !rendererClass.matches("Render[A-Za-z0-9]*")
                || geometryPixels < 1 || geometryPixels > 128 * 128
                || frameSha256 == null || !frameSha256.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("invalid native entity render observation");
        }
        this.subject = subject; this.rendererClass = rendererClass;
        this.geometryPixels = geometryPixels; this.frameSha256 = frameSha256;
    }

    public NativeEntityRenderSubject subject() { return subject; }
    public String rendererClass() { return rendererClass; }
    public int geometryPixels() { return geometryPixels; }
    public String frameSha256() { return frameSha256; }
    public String canonical() {
        return subject.canonical() + "|renderer=" + rendererClass
                + "|geometry-pixels=" + geometryPixels + "|frame=" + frameSha256;
    }
}
