package worldline.extension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/** Mod-owned Atlas page with versioned provenance and explicit relations. */
public final class ExtensionAtlasPage {
    private static final Pattern ID = Pattern.compile(
            "atlas[.](scenario|api|loader|ecosystem-claim)[.][A-Za-z0-9][A-Za-z0-9_.-]*");
    private static final Pattern TAG = Pattern.compile("[a-z][a-z0-9-]{0,62}");
    private final String id, title, provenance;
    private final List<String> tags, relations;

    private ExtensionAtlasPage(Builder builder) {
        if (builder.id == null || !ID.matcher(builder.id).matches())
            throw new IllegalArgumentException("atlas id");
        if (builder.title == null || builder.title.trim().isEmpty()
                || unsafe(builder.title))
            throw new IllegalArgumentException("atlas title");
        if (builder.provenance == null || builder.provenance.trim().isEmpty()
                || unsafe(builder.provenance))
            throw new IllegalArgumentException("atlas provenance");
        id = builder.id; title = builder.title.trim(); provenance = builder.provenance.trim();
        tags = copy(builder.tags, "tag"); relations = copy(builder.relations, "relation");
    }

    public static Builder builder(String id, String title) { return new Builder(id, title); }
    public String id() { return id; }
    public String title() { return title; }
    public String provenance() { return provenance; }
    public List<String> tags() { return tags; }
    public List<String> relations() { return relations; }

    public static final class Builder {
        private final String id, title;
        private String provenance;
        private final List<String> tags = new ArrayList<String>();
        private final List<String> relations = new ArrayList<String>();
        private Builder(String id, String title) { this.id = id; this.title = title; }
        public Builder tag(String value) { tags.add(value); return this; }
        public Builder relation(String value) { relations.add(value); return this; }
        public Builder provenance(String value) { provenance = value; return this; }
        public ExtensionAtlasPage build() { return new ExtensionAtlasPage(this); }
    }

    private static List<String> copy(List<String> source, String label) {
        List<String> values = new ArrayList<String>();
        for (String value : source) {
            if (value == null || value.trim().isEmpty() || value.indexOf(',') >= 0
                    || unsafe(value)) throw new IllegalArgumentException("atlas " + label);
            String clean = value.trim();
            if ("tag".equals(label) && !TAG.matcher(clean).matches()) {
                throw new IllegalArgumentException("atlas tag");
            }
            if ("relation".equals(label) && !ID.matcher(clean).matches()
                    && !"atlas.subsystem.mod-ecosystem".equals(clean)) {
                throw new IllegalArgumentException("atlas relation");
            }
            if (values.contains(clean)) throw new IllegalArgumentException("duplicate atlas " + label);
            values.add(clean);
        }
        return Collections.unmodifiableList(values);
    }

    private static boolean unsafe(String value) {
        return value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0 || value.indexOf('=') >= 0;
    }
}
