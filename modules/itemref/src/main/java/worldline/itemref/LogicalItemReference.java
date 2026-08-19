package worldline.itemref;

import java.util.regex.Pattern;

/** Neutral, canonical logical identity carried by a controlled ItemStack. */
public final class LogicalItemReference {
    private static final String HEX = "[0-9a-f]{32}";
    private static final String NAMESPACE = "[a-z][a-z0-9]*(?:\\.[a-z][a-z0-9]*)*";
    private static final String SCHEMA = "[a-z][a-z0-9-]*/[1-9][0-9]*";
    private static final Pattern CANONICAL = Pattern.compile(
            "minecraft:" + HEX + "\\|" + HEX + "\\|" + NAMESPACE + "\\." + SCHEMA);

    private final String canonical;

    private LogicalItemReference(String canonical) {
        this.canonical = canonical;
    }

    public static LogicalItemReference parse(String value) {
        if (value == null || !CANONICAL.matcher(value).matches()) {
            throw new IllegalArgumentException("invalid logical item reference");
        }
        return new LogicalItemReference(value);
    }

    public String canonical() {
        return canonical;
    }

    @Override
    public String toString() {
        return canonical;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof LogicalItemReference
                && canonical.equals(((LogicalItemReference) other).canonical);
    }

    @Override
    public int hashCode() {
        return canonical.hashCode();
    }
}
