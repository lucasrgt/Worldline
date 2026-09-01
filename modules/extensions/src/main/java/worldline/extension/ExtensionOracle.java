package worldline.extension;

/** Decides whether captured extension evidence satisfies the selected mode. */
public interface ExtensionOracle {
    void verify(ExtensionMode mode, ExtensionEvidence evidence, String expectedSignature);
}
