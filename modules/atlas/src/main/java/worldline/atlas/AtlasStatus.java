package worldline.atlas;

/**
 * Closed epistemic states. Categories stay primary; numeric confidence is out
 * of scope for A0-A6.
 */
public final class AtlasStatus {
    public static final String VERIFIED = "VERIFIED";
    public static final String STRONG = "STRONG";
    public static final String EXPERIMENTAL = "EXPERIMENTAL";
    public static final String OBSERVATIONAL = "OBSERVATIONAL";
    public static final String REJECTED = "REJECTED";
    public static final String UNKNOWN = "UNKNOWN";
    public static final String NATIVE_NONDETERMINISTIC = "NATIVE_NONDETERMINISTIC";
    private static final String[] ALL = { VERIFIED, STRONG, EXPERIMENTAL, OBSERVATIONAL,
            REJECTED, UNKNOWN, NATIVE_NONDETERMINISTIC };

    private AtlasStatus() {}

    public static String parse(String value) {
        for (String status : ALL) {
            if (status.equals(value)) return status;
        }
        throw new IllegalArgumentException("unsupported status " + value);
    }

    public static String[] values() { return ALL.clone(); }
}
