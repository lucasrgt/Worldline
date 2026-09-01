package worldline.extension;

/** Public kinds a mod may contribute without exposing loader-specific classes. */
public enum ExtensionSubjectKind {
    BLOCK("block"), ITEM("item"), ENTITY("entity"), SUBSYSTEM("subsystem");

    private final String token;

    ExtensionSubjectKind(String token) { this.token = token; }

    public String token() { return token; }

    public static ExtensionSubjectKind parse(String value) {
        if (value != null) for (ExtensionSubjectKind kind : values()) {
            if (kind.token.equals(value.trim())) return kind;
        }
        throw new IllegalArgumentException("unknown extension subject kind " + value);
    }
}
