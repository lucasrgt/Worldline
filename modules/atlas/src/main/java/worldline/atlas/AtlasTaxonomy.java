package worldline.atlas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Derived domain, subsystem, category, and tag facets for every Atlas record. */
public final class AtlasTaxonomy {
    public static final String SCHEMA = "WORLDLINE-ATLAS-TAXONOMY/1";
    private static final String KNOWLEDGE = "knowledge";

    private AtlasTaxonomy() {}

    public static List<String> subsystems(AtlasStore store, AtlasRecord record) {
        require(store, record);
        Set<String> found = new LinkedHashSet<String>();
        collect(store, record, found, new LinkedHashSet<String>());
        List<String> ordered = new ArrayList<String>();
        for (String subsystem : AtlasSubsystems.ALL) {
            if (found.contains(subsystem)) ordered.add(subsystem);
        }
        return Collections.unmodifiableList(ordered);
    }

    public static List<String> domains(AtlasStore store, AtlasRecord record) {
        Set<String> found = new LinkedHashSet<String>();
        for (String subsystem : subsystems(store, record)) {
            found.add(AtlasSubsystems.domain(subsystem));
        }
        List<String> ordered = new ArrayList<String>();
        for (String domain : AtlasSubsystems.DOMAINS) {
            if (found.contains(domain)) ordered.add(domain);
        }
        if (ordered.isEmpty()) ordered.add(KNOWLEDGE);
        return Collections.unmodifiableList(ordered);
    }

    public static List<String> tags(AtlasStore store, AtlasRecord record) {
        Set<String> tags = new LinkedHashSet<String>();
        for (String domain : domains(store, record)) tags.add("domain-" + domain);
        for (String subsystem : subsystems(store, record)) tags.add("subsystem-" + subsystem);
        tags.add("category-" + record.kind());
        tags.add("status-" + record.status().toLowerCase(Locale.US).replace('_', '-'));
        tags.add("certainty-" + AtlasCertainty.of(record).toLowerCase(Locale.US));
        tags.add("artifact-" + artifact(record.artifact()));
        String layer = control(record.control(), "layer");
        if (!layer.isEmpty()) tags.add("layer-" + layer.toLowerCase(Locale.US));
        String surface = control(record.control(), "automation");
        if (!surface.isEmpty()) {
            tags.add("surface-" + surface.toLowerCase(Locale.US).replace('_', '-'));
        }
        String extension = control(record.control(), "extension");
        if (!extension.isEmpty()) {
            tags.add("extension");
            tags.add("extension-" + extension);
            String contributed = control(record.control(), "extension-tags");
            if (!contributed.isEmpty()) for (String value : contributed.split("\\+", -1)) {
                if (!value.isEmpty()) tags.add("extension-tag-" + value);
            }
        }
        return Collections.unmodifiableList(new ArrayList<String>(tags));
    }

    public static String render(AtlasStore store) {
        Map<String, Integer> domains = new java.util.LinkedHashMap<String, Integer>();
        Map<String, Integer> subsystems = new java.util.LinkedHashMap<String, Integer>();
        for (String domain : AtlasSubsystems.DOMAINS) domains.put(domain, 0);
        domains.put(KNOWLEDGE, 0);
        for (String subsystem : AtlasSubsystems.ALL) subsystems.put(subsystem, 0);
        for (AtlasRecord record : store.records()) {
            for (String domain : domains(store, record)) increment(domains, domain);
            for (String subsystem : subsystems(store, record)) increment(subsystems, subsystem);
        }
        StringBuilder text = new StringBuilder(SCHEMA).append('\n');
        text.append("records=").append(store.size()).append('\n');
        for (String domain : AtlasSubsystems.DOMAINS) {
            text.append("domain=").append(domain).append("\trecords=")
                    .append(domains.get(domain)).append('\n');
            for (String subsystem : AtlasSubsystems.ALL) {
                if (domain.equals(AtlasSubsystems.domain(subsystem))) {
                    text.append("subsystem=").append(subsystem).append("\trecords=")
                            .append(subsystems.get(subsystem)).append('\n');
                }
            }
        }
        text.append("domain=").append(KNOWLEDGE).append("\trecords=")
                .append(domains.get(KNOWLEDGE)).append('\n');
        for (String kind : AtlasKind.values()) {
            text.append("category=").append(kind).append("\trecords=")
                    .append(store.kind(kind).size()).append('\n');
        }
        return text.toString();
    }

    public static String tagIndex(AtlasStore store) {
        Map<String, Integer> tags = new java.util.TreeMap<String, Integer>();
        for (AtlasRecord record : store.records()) {
            for (String tag : tags(store, record)) increment(tags, tag);
        }
        StringBuilder text = new StringBuilder("schema=").append(SCHEMA).append('\n');
        text.append("tags=").append(tags.size()).append('\n');
        for (Map.Entry<String, Integer> entry : tags.entrySet()) {
            text.append("tag=").append(entry.getKey()).append("\trecords=")
                    .append(entry.getValue()).append('\n');
        }
        return text.toString();
    }

    public static String markdown() {
        StringBuilder text = new StringBuilder();
        text.append("| Domain | Subsystems |\n| --- | --- |\n");
        for (String domain : AtlasSubsystems.DOMAINS) {
            List<String> members = new ArrayList<String>();
            for (String subsystem : AtlasSubsystems.ALL) {
                if (domain.equals(AtlasSubsystems.domain(subsystem))) members.add(subsystem);
            }
            text.append("| `").append(domain).append("` | `")
                    .append(join(members, "`, `")).append("` |\n");
        }
        return text.toString();
    }

    public static void validate(AtlasStore store) {
        for (String subsystem : AtlasSubsystems.ALL) {
            store.get("atlas.subsystem." + subsystem);
            AtlasSubsystems.domain(subsystem);
        }
        for (AtlasRecord record : store.records()) {
            if (domains(store, record).isEmpty() || tags(store, record).isEmpty()) {
                throw new IllegalArgumentException("unfaceted atlas record " + record.id());
            }
        }
    }

    private static void collect(AtlasStore store, AtlasRecord record, Set<String> found,
            Set<String> visited) {
        if (!visited.add(record.id())) return;
        if (AtlasKind.SUBSYSTEM.equals(record.kind())) found.add(AtlasKind.token(record.id()));
        if (AtlasKind.COVERAGE_UNIT.equals(record.kind())) {
            String token = AtlasKind.token(record.id());
            found.add(token.substring(0, token.lastIndexOf('.')));
        }
        for (String ref : record.refs()) {
            if (ref.startsWith("atlas.subsystem.")) {
                found.add(ref.substring("atlas.subsystem.".length()));
            } else {
                collect(store, store.get(ref), found, visited);
            }
        }
    }

    private static void increment(Map<String, Integer> counts, String key) {
        Integer value = counts.get(key);
        counts.put(key, value == null ? 1 : value + 1);
    }

    private static String artifact(String value) {
        if (AtlasSchema.CLIENT.equals(value)) return "client";
        if (AtlasSchema.SERVER.equals(value)) return "server";
        if (AtlasSchema.WORLDLINE.equals(value)) return "worldline";
        return "external";
    }

    private static String control(String value, String key) {
        for (String item : value.split(";", -1)) {
            if (item.startsWith(key + "=")) return item.substring(key.length() + 1);
        }
        return "";
    }

    private static String join(List<String> values, String separator) {
        StringBuilder text = new StringBuilder();
        for (int index = 0; index < values.size(); index++) {
            if (index > 0) text.append(separator);
            text.append(values.get(index));
        }
        return text.toString();
    }

    private static void require(AtlasStore store, AtlasRecord record) {
        if (store == null || record == null) throw new NullPointerException("taxonomy");
        store.get(record.id());
    }
}
