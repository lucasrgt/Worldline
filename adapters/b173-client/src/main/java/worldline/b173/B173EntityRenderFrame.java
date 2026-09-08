package worldline.b173;

/** Native frame result with explicit class-origin evidence outside the canonical pixel signature. */
public final class B173EntityRenderFrame {
    private final int legacyId, pixels;
    private final String entityClass, rendererClass, sha256, entityOrigin, rendererOrigin;

    B173EntityRenderFrame(int legacyId, int pixels, String entityClass, String rendererClass,
            String sha256, String entityOrigin, String rendererOrigin) {
        this.legacyId = legacyId; this.pixels = pixels; this.entityClass = entityClass;
        this.rendererClass = rendererClass; this.sha256 = sha256;
        this.entityOrigin = entityOrigin; this.rendererOrigin = rendererOrigin;
    }
    public int legacyId() { return legacyId; }
    public int pixels() { return pixels; }
    public String entityClass() { return entityClass; }
    public String rendererClass() { return rendererClass; }
    public String sha256() { return sha256; }
    public String entityOrigin() { return entityOrigin; }
    public String rendererOrigin() { return rendererOrigin; }
}
