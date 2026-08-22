package worldline.symbolgraph;

import java.util.Objects;

/** Identity in the first Tiny namespace. */
public final class SymbolKey implements Comparable<SymbolKey> {
    private final SymbolKind kind;
    private final String owner;
    private final String name;
    private final String descriptor;

    public SymbolKey(SymbolKind kind, String owner, String name, String descriptor) {
        this.kind = Objects.requireNonNull(kind, "kind");
        this.owner = required(owner, "owner", kind != SymbolKind.CLASS);
        this.name = required(name, "name", true);
        this.descriptor = required(descriptor, "descriptor", kind != SymbolKind.CLASS);
        if (kind == SymbolKind.CLASS && (!owner.isEmpty() || !descriptor.isEmpty())) {
            throw new IllegalArgumentException("class owner and descriptor must be empty");
        }
    }

    public SymbolKind kind() { return kind; }
    public String owner() { return owner; }
    public String name() { return name; }
    public String descriptor() { return descriptor; }

    public String canonical() {
        return kind.name() + "|" + owner + "|" + name + "|" + descriptor;
    }

    @Override public int compareTo(SymbolKey other) { return canonical().compareTo(other.canonical()); }
    @Override public boolean equals(Object other) {
        return other instanceof SymbolKey && canonical().equals(((SymbolKey) other).canonical());
    }
    @Override public int hashCode() { return canonical().hashCode(); }
    @Override public String toString() { return canonical(); }

    private static String required(String value, String label, boolean nonempty) {
        if (value == null || (nonempty && value.isEmpty())) throw new IllegalArgumentException(label);
        return value;
    }
}
