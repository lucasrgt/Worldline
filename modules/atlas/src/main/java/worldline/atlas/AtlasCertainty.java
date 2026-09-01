package worldline.atlas;

/** Stable three-level knowledge class derived without erasing the exact Atlas status. */
public final class AtlasCertainty {
    public static final String VERIFIED = "VERIFIED";
    public static final String INFERRED = "INFERRED";
    public static final String UNKNOWN = "UNKNOWN";

    private AtlasCertainty() {}

    public static String of(AtlasRecord record) {
        String status = record.status();
        if (AtlasStatus.VERIFIED.equals(status)) return VERIFIED;
        if (AtlasStatus.STRONG.equals(status) || AtlasStatus.EXPERIMENTAL.equals(status)
                || AtlasStatus.OBSERVATIONAL.equals(status) || AtlasStatus.PARTIAL.equals(status)) {
            return INFERRED;
        }
        return UNKNOWN;
    }
}
