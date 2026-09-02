package worldline.testapi;

/** One canonical block-state row routed through native world RenderBlocks. */
public final class NativeWorldBlockRenderSubject {
    private final String subject;
    private final int legacyId, metadata, renderType;

    public NativeWorldBlockRenderSubject(String subject, int legacyId, int metadata,
            int renderType) {
        if (subject == null || !subject.matches("b1\\.7\\.3:block/[0-9]{3}")) {
            throw new IllegalArgumentException("invalid native world-render subject");
        }
        if (legacyId < 1 || legacyId > 255
                || !subject.endsWith(String.format("%03d", legacyId))) {
            throw new IllegalArgumentException("native world-render subject id mismatch");
        }
        if (metadata < 0 || metadata > 15 || !supportsSpecialRoute(renderType)) {
            throw new IllegalArgumentException("invalid native world-render route");
        }
        this.subject = subject;
        this.legacyId = legacyId;
        this.metadata = metadata;
        this.renderType = renderType;
    }

    public String subject() { return subject; }
    public int legacyId() { return legacyId; }
    public int metadata() { return metadata; }
    public int renderType() { return renderType; }

    public String canonical() {
        return subject + '|' + legacyId + ':' + metadata + "|render-type=" + renderType;
    }

    public static boolean supportsSpecialRoute(int renderType) {
        return renderType >= 1 && renderType <= 17
                && !NativeBlockRenderSubject.supports3d(renderType);
    }
}
