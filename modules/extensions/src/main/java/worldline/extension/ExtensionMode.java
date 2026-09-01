package worldline.extension;

/** Stable comparison modes supported by extension contracts. */
public enum ExtensionMode {
    CONFORMANCE("conformance"),
    DIFFERENTIAL("differential"),
    CUSTOM_CONTRACT("custom-contract");

    private final String token;

    ExtensionMode(String token) { this.token = token; }

    public String token() { return token; }

    public static ExtensionMode parse(String value) {
        if (value != null) for (ExtensionMode mode : values()) {
            if (mode.token.equals(value.trim())) return mode;
        }
        throw new IllegalArgumentException("unknown extension mode " + value);
    }
}
