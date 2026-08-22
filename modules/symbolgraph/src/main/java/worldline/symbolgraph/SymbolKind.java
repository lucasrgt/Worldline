package worldline.symbolgraph;

public enum SymbolKind {
    CLASS("c"), FIELD("f"), METHOD("m");

    private final String tinyToken;

    SymbolKind(String tinyToken) { this.tinyToken = tinyToken; }

    public String tinyToken() { return tinyToken; }

    static SymbolKind fromTiny(String token) {
        for (SymbolKind kind : values()) {
            if (kind.tinyToken.equals(token)) return kind;
        }
        throw new IllegalArgumentException("unsupported Tiny symbol kind: " + token);
    }
}
