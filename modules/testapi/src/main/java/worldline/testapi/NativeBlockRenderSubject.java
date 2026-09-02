package worldline.testapi;

/** One canonical block-state row in the native 3D inventory render contract. */
public final class NativeBlockRenderSubject {
    private final String subject;
    private final int legacyId, metadata, renderType;

    public NativeBlockRenderSubject(String subject, int legacyId, int metadata, int renderType) {
        if (subject == null || !subject.matches("b1\\.7\\.3:block/[0-9]{3}")) {
            throw new IllegalArgumentException("invalid native render subject");
        }
        if (legacyId < 1 || legacyId > 255
                || !subject.endsWith(String.format("%03d", legacyId))) {
            throw new IllegalArgumentException("native render subject id mismatch");
        }
        if (metadata < 0 || metadata > 15) {
            throw new IllegalArgumentException("invalid native render metadata");
        }
        if (!supports3d(renderType)) {
            throw new IllegalArgumentException("render type is not native inventory 3D: "
                    + renderType);
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

    public static boolean supports3d(int renderType) {
        return renderType == 0 || renderType == 10 || renderType == 11
                || renderType == 13 || renderType == 16;
    }
}
