package worldline.extension;

/** Maintained reusable extension oracle implementations. */
public final class ExtensionOracles {
    private ExtensionOracles() {}

    public static ExtensionOracle equatable() {
        return new ExtensionOracle() {
            @Override public void verify(ExtensionMode mode, ExtensionEvidence evidence,
                    String expectedSignature) {
                if (mode == ExtensionMode.DIFFERENTIAL) return;
                if (expectedSignature == null || !expectedSignature.equals(evidence.signature())) {
                    throw new AssertionError("extension evidence diverged for "
                            + evidence.contractId() + " expected=" + expectedSignature
                            + " actual=" + evidence.signature());
                }
            }
        };
    }
}
