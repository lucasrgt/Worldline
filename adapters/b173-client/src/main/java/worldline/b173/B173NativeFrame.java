package worldline.b173;

/** Immutable observation from one offscreen Beta 1.7.3 renderer invocation. */
public final class B173NativeFrame {
    private final String role;
    private final String context;
    private final boolean displayCreated;
    private final int geometryPixels;
    private final String work;
    private final String sha256;
    private final String provenance;
    private final String gpu;

    B173NativeFrame(String role, String context, boolean displayCreated, int geometryPixels,
            String work, String sha256, String provenance, String gpu) {
        this.role = role;
        this.context = context;
        this.displayCreated = displayCreated;
        this.geometryPixels = geometryPixels;
        this.work = work;
        this.sha256 = sha256;
        this.provenance = provenance;
        this.gpu = gpu;
    }

    public String role() { return role; }
    public String context() { return context; }
    public boolean displayCreated() { return displayCreated; }
    public int geometryPixels() { return geometryPixels; }
    public String work() { return work; }
    public String sha256() { return sha256; }
    public String provenance() { return provenance; }
    public String gpu() { return gpu; }
}
