package worldline.symbolgraph;

final class OfficialSymbolKey implements Comparable<OfficialSymbolKey> {
    private final SymbolKind kind;
    private final String owner;
    private final String name;
    private final String descriptor;

    OfficialSymbolKey(SymbolKind kind, String owner, String name, String descriptor) {
        if (kind == null || owner == null || name == null || descriptor == null || name.isEmpty()) {
            throw new IllegalArgumentException("official symbol identity");
        }
        this.kind = kind;
        this.owner = owner;
        this.name = name;
        this.descriptor = descriptor;
    }

    String canonical() { return kind.name() + "|" + owner + "|" + name + "|" + descriptor; }
    @Override public int compareTo(OfficialSymbolKey other) { return canonical().compareTo(other.canonical()); }
    @Override public boolean equals(Object other) {
        return other instanceof OfficialSymbolKey
                && canonical().equals(((OfficialSymbolKey) other).canonical());
    }
    @Override public int hashCode() { return canonical().hashCode(); }
    @Override public String toString() { return canonical(); }
}
