package worldline.extension;

import java.util.Objects;
import java.util.regex.Pattern;

/** Neutral identity for one mod-owned block, item, entity, or subsystem. */
public final class ExtensionSubject {
    private static final Pattern ID = Pattern.compile(
            "[a-z][a-z0-9_.-]{0,62}:[a-z][a-z0-9_./-]{0,126}");
    private final String id;
    private final ExtensionSubjectKind kind;
    private final String name;

    private ExtensionSubject(String id, ExtensionSubjectKind kind, String name) {
        if (id == null || !ID.matcher(id).matches()) throw new IllegalArgumentException("subject id");
        if (kind == null) throw new NullPointerException("kind");
        if (name == null || name.trim().isEmpty() || name.indexOf('\n') >= 0) {
            throw new IllegalArgumentException("subject name");
        }
        this.id = id; this.kind = kind; this.name = name.trim();
    }

    public static ExtensionSubject of(String id, ExtensionSubjectKind kind, String name) {
        return new ExtensionSubject(id, kind, name);
    }

    public String id() { return id; }
    public ExtensionSubjectKind kind() { return kind; }
    public String name() { return name; }

    @Override public boolean equals(Object other) {
        return other instanceof ExtensionSubject && id.equals(((ExtensionSubject) other).id);
    }

    @Override public int hashCode() { return Objects.hash(id); }
}
