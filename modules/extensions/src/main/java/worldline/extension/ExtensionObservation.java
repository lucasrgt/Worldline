package worldline.extension;

/** Captures one deterministic, equatable observation. */
public interface ExtensionObservation {
    String observe(ExtensionContext context) throws Exception;
}
