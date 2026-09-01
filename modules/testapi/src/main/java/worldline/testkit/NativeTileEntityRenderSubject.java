package worldline.testkit;

/** One canonical block state presented through a native tile-entity renderer. */
public final class NativeTileEntityRenderSubject {
    private final String subject, renderer, layer;
    private final int legacyId, metadata;

    public NativeTileEntityRenderSubject(String subject, int legacyId, int metadata,
            String renderer, String layer) {
        if (subject == null || !subject.matches("b1\\.7\\.3:block/[0-9]{3}")
                || legacyId < 1 || legacyId > 255
                || !subject.endsWith(String.format("%03d", legacyId))) {
            throw new IllegalArgumentException("invalid native tile-render subject");
        }
        if (metadata < 0 || metadata > 15 || !supports(renderer, legacyId, layer)) {
            throw new IllegalArgumentException("invalid native tile-render route");
        }
        this.subject = subject;
        this.legacyId = legacyId;
        this.metadata = metadata;
        this.renderer = renderer;
        this.layer = layer;
    }

    public String subject() { return subject; }
    public int legacyId() { return legacyId; }
    public int metadata() { return metadata; }
    public String renderer() { return renderer; }
    public String layer() { return layer; }

    public String canonical() {
        return subject + '|' + legacyId + ':' + metadata + "|renderer=" + renderer
                + "|layer=" + layer;
    }

    private static boolean supports(String renderer, int legacyId, String layer) {
        boolean sign = "sign".equals(renderer) && (legacyId == 63 || legacyId == 68);
        boolean piston = "moving-piston".equals(renderer) && legacyId == 36;
        return sign && "ARCHETYPE".equals(layer) || piston && "SINGULAR".equals(layer);
    }
}
