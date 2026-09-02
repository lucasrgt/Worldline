package worldline.testapi;

import java.util.Objects;

/** One versioned entity subject observed in the native EntityList census. */
public final class EntityRegistryObservation {
    private final String subject;
    private final String row;

    public EntityRegistryObservation(String subject, String row) {
        if (subject == null || !subject.matches("b[0-9]+\\.[0-9]+\\.[0-9]+:entity/[0-9]{3}")) {
            throw new IllegalArgumentException("subject");
        }
        if (row == null || !row.matches("e[0-9]{3}=name=[A-Za-z0-9]+\\|class=[A-Za-z0-9_$]+")
                || row.length() > 512) {
            throw new IllegalArgumentException("registry row");
        }
        this.subject = subject;
        this.row = row;
    }

    public String subject() { return subject; }
    public String row() { return row; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof EntityRegistryObservation)) return false;
        EntityRegistryObservation value = (EntityRegistryObservation) other;
        return subject.equals(value.subject) && row.equals(value.row);
    }

    @Override public int hashCode() { return Objects.hash(subject, row); }
}
