package worldline.testkit;

import java.util.Objects;

/** One versioned block subject observed in the native registry census. */
public final class BlockRegistryObservation {
    private final String subject;
    private final String row;

    public BlockRegistryObservation(String subject, String row) {
        if (subject == null || !subject.matches("b[0-9]+\\.[0-9]+\\.[0-9]+:block/[0-9]{3}")) {
            throw new IllegalArgumentException("subject");
        }
        if (row == null || row.isEmpty() || row.length() > 512
                || row.indexOf('\n') >= 0 || row.indexOf('\r') >= 0) {
            throw new IllegalArgumentException("registry row");
        }
        this.subject = subject;
        this.row = row;
    }

    public String subject() { return subject; }
    public String row() { return row; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof BlockRegistryObservation)) return false;
        BlockRegistryObservation value = (BlockRegistryObservation) other;
        return subject.equals(value.subject) && row.equals(value.row);
    }

    @Override public int hashCode() { return Objects.hash(subject, row); }
}
