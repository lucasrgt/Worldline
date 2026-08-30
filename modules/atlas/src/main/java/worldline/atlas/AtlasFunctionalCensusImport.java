package worldline.atlas;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.DirectoryStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Expands the finite Functional Census into one Atlas claim per subject/template cell. */
final class AtlasFunctionalCensusImport {
    private static final String BASE = "behavior/functional-census/b1.7.3/";

    private AtlasFunctionalCensusImport() {}

    static List<AtlasRecord> load(Path root) throws IOException {
        List<Row> subjects = table(root.resolve(BASE + "subjects.tsv"));
        List<Row> templates = table(root.resolve(BASE + "templates.tsv"));
        Map<String, Row> profiles = index(table(root.resolve(BASE + "profiles.tsv")), "subject_id");
        Map<String, Row> claims = expandClaims(table(root.resolve(BASE + "claims.tsv")), subjects);
        Map<String, List<Row>> provenance = provenance(claims);
        applyDeltas(root, claims, provenance);
        Map<String, Row> exceptions = indexPair(table(root.resolve(BASE + "exceptions.tsv")));
        List<AtlasRecord> result = new ArrayList<AtlasRecord>();
        for (Row subject : subjects) for (Row template : templates) {
            String key = key(subject.get("subject_id"), template.get("template_id"));
            Row source = claims.get(key);
            boolean exception = false;
            if (source == null) { source = exceptions.get(key); exception = source != null; }
            List<Row> sources = provenance.get(key);
            if (sources == null && source != null) sources = Collections.singletonList(source);
            result.add(record(subject, template, profiles.get(subject.get("subject_id")),
                    source, sources, exception));
        }
        return Collections.unmodifiableList(result);
    }

    private static AtlasRecord record(Row subject, Row template, Row profile, Row source,
            List<Row> sources, boolean exception) {
        String subjectId = subject.get("subject_id");
        String templateId = template.get("template_id");
        String status = source == null ? AtlasStatus.UNKNOWN : source.get("status");
        List<String> evidence = new ArrayList<String>();
        List<String> refs = new ArrayList<String>();
        if (sources != null) for (Row proof : sources) {
            add(refs, "atlas.experiment.", proof.get("evidence_id"));
            add(evidence, "expected.signature=", proof.get("signature"));
            add(evidence, "claim=", proof.get("claim_id"));
            add(evidence, "justification=", safe(proof.get("justification")));
        }
        refs.add("atlas.subsystem." + subsystem(templateId));
        String layer = layer(template, profile);
        String control = "layer=" + layer + ";applicability="
                + (exception ? "NOT_APPLICABLE" : source == null ? "UNKNOWN"
                        : source.get("applicability")) + ";automation=" + automation(source);
        String block = subjectId.substring(subjectId.lastIndexOf('/') + 1);
        String id = "atlas.claim.block-" + block + "." + templateId;
        String description = subject.get("name") + " " + template.get("observable");
        return AtlasRecord.of(id, AtlasKind.CLAIM, status, artifact(source), AtlasSchema.SCOPE,
                description, control, 0, evidence, refs);
    }

    private static Map<String, Row> expandClaims(List<Row> rows, List<Row> subjects) {
        Map<String, Row> result = new LinkedHashMap<String, Row>();
        for (Row row : rows) {
            if ("*".equals(row.get("subject_id"))) {
                for (Row subject : subjects) {
                    result.put(key(subject.get("subject_id"), row.get("template_id")), row);
                }
            } else result.put(key(row.get("subject_id"), row.get("template_id")), row);
        }
        return result;
    }

    private static void applyDeltas(Path root, Map<String, Row> claims,
            Map<String, List<Row>> provenance) throws IOException {
        List<Path> deltas = new ArrayList<Path>();
        try (DirectoryStream<Path> dirs = Files.newDirectoryStream(root.resolve("smokes"))) {
            for (Path dir : dirs) {
                Path delta = dir.resolve("census-delta.tsv");
                if (Files.isRegularFile(delta)) deltas.add(delta);
            }
        }
        Collections.sort(deltas);
        for (Path delta : deltas) for (Row row : table(delta)) {
            String key = key(row.get("subject_id"), row.get("template_id"));
            if (!claims.containsKey(key)) claims.put(key, row);
            List<Row> proofs = provenance.get(key);
            if (proofs == null) { proofs = new ArrayList<Row>(); provenance.put(key, proofs); }
            if (!contains(proofs, row)) proofs.add(row);
        }
    }

    private static boolean equivalent(Row left, Row right) {
        return left.get("status").equals(right.get("status"))
                && left.get("evidence_id").equals(right.get("evidence_id"))
                && left.get("signature").equals(right.get("signature"));
    }

    private static boolean contains(List<Row> rows, Row candidate) {
        for (Row row : rows) if (equivalent(row, candidate)) return true;
        return false;
    }

    private static Map<String, List<Row>> provenance(Map<String, Row> claims) {
        Map<String, List<Row>> result = new LinkedHashMap<String, List<Row>>();
        for (Map.Entry<String, Row> entry : claims.entrySet()) {
            List<Row> rows = new ArrayList<Row>(); rows.add(entry.getValue());
            result.put(entry.getKey(), rows);
        }
        return result;
    }

    private static void add(List<String> target, String prefix, String value) {
        if (!value.isEmpty()) {
            String item = prefix + value;
            if (!target.contains(item)) target.add(item);
        }
    }

    private static String layer(Row template, Row profile) {
        String layer = template.get("default_layer");
        if ("ARCHETYPE".equals(layer) && "true".equals(profile.get("singular"))) return "SINGULAR";
        return layer;
    }

    private static String artifact(Row source) {
        if (source == null) return AtlasSchema.WORLDLINE;
        if ("OFFICIAL_CLIENT".equals(source.get("oracle"))) return AtlasSchema.CLIENT;
        if ("OFFICIAL_SERVER".equals(source.get("oracle"))) return AtlasSchema.SERVER;
        return AtlasSchema.WORLDLINE;
    }

    private static String automation(Row source) {
        if (source == null || source.get("automation_surface").isEmpty()) return "NONE";
        return source.get("automation_surface");
    }

    private static String subsystem(String template) {
        if (template.contains("light")) return "lighting";
        if (template.contains("collision")) return "player";
        if (template.contains("render")) return "rendering";
        if (template.contains("save")) return "saves";
        if (template.contains("tick") || template.contains("neighbor")) return "block-ticks";
        if (template.contains("registry")) return "mappings";
        return "inventory";
    }

    private static Map<String, Row> index(List<Row> rows, String field) {
        Map<String, Row> result = new LinkedHashMap<String, Row>();
        for (Row row : rows) result.put(row.get(field), row);
        return result;
    }

    private static Map<String, Row> indexPair(List<Row> rows) {
        Map<String, Row> result = new LinkedHashMap<String, Row>();
        for (Row row : rows) result.put(key(row.get("subject_id"), row.get("template_id")), row);
        return result;
    }

    private static String key(String subject, String template) { return subject + "#" + template; }
    private static String safe(String value) { return value.replace(',', ';').replace('\n', ' '); }

    private static List<Row> table(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        if (lines.isEmpty()) throw new IllegalArgumentException("empty TSV " + path);
        String[] header = lines.get(0).split("\t", -1);
        List<Row> rows = new ArrayList<Row>();
        for (int line = 1; line < lines.size(); line++) {
            if (lines.get(line).trim().isEmpty() || lines.get(line).startsWith("#")) continue;
            String[] values = lines.get(line).split("\t", -1);
            if (values.length != header.length) throw new IllegalArgumentException("TSV width " + path);
            Map<String, String> fields = new LinkedHashMap<String, String>();
            for (int field = 0; field < header.length; field++) fields.put(header[field], values[field]);
            rows.add(new Row(fields));
        }
        return rows;
    }

    private static final class Row {
        private final Map<String, String> fields;
        Row(Map<String, String> fields) { this.fields = fields; }
        String get(String key) {
            String value = fields.get(key); return value == null ? "" : value;
        }
    }
}
