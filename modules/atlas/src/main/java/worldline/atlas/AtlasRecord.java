package worldline.atlas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/** Immutable canonical Atlas entry. Unknown keys and empty required fields fail. */
public final class AtlasRecord {
    private static final Pattern FIELD = Pattern.compile("[a-z]+=.*");
    private final String id, kind, status, artifact, scope, subject, control;
    private final int denominator;
    private final List<String> evidence, refs;
    private final String canonical;

    private AtlasRecord(String id, String kind, String status, String artifact, String scope,
            String subject, String control, int denominator, List<String> evidence,
            List<String> refs, String canonical) {
        this.id = id;
        this.kind = kind;
        this.status = status;
        this.artifact = artifact;
        this.scope = scope;
        this.subject = subject;
        this.control = control;
        this.denominator = denominator;
        this.evidence = evidence;
        this.refs = refs;
        this.canonical = canonical;
    }

    public static AtlasRecord of(String id, String kind, String status, String artifact,
            String scope, String subject, String control, int denominator,
            List<String> evidence, List<String> refs) {
        String canonicalKind = AtlasKind.parse(kind);
        String canonicalId = AtlasSchema.requireId(id);
        if (!canonicalKind.equals(AtlasKind.ofId(canonicalId))) {
            throw new IllegalArgumentException("kind mismatch " + id);
        }
        if (artifact == null || artifact.isEmpty()) throw new IllegalArgumentException("artifact");
        if (!AtlasSchema.SCOPE.equals(scope)) throw new IllegalArgumentException("scope");
        if (subject == null || subject.indexOf('\n') >= 0 || subject.indexOf('=') >= 0) {
            throw new IllegalArgumentException("subject");
        }
        if (control == null || control.indexOf(',') >= 0 || control.indexOf('\n') >= 0) {
            throw new IllegalArgumentException("control");
        }
        if (denominator < 0) throw new IllegalArgumentException("denominator");
        if (AtlasKind.COVERAGE_UNIT.equals(canonicalKind) && denominator < 1) {
            throw new IllegalArgumentException("coverage unit denominator");
        }
        List<String> copiedEvidence = copy("evidence", evidence);
        List<String> copiedRefs = copy("refs", refs);
        String body = body(canonicalId, canonicalKind, AtlasStatus.parse(status), artifact, scope,
                subject, control, denominator, copiedEvidence, copiedRefs);
        String canonical = body + "sha256=" + AtlasHashes.sha256(body) + "\n";
        return new AtlasRecord(canonicalId, canonicalKind, AtlasStatus.parse(status), artifact,
                scope, subject, control, denominator, copiedEvidence, copiedRefs, canonical);
    }

    public static AtlasRecord parse(String document) {
        if (document == null) throw new NullPointerException("record");
        String[] lines = document.split("\n", -1);
        require(lines.length == 13 && lines[12].isEmpty(), "invalid record framing");
        require(AtlasSchema.HEADER.equals(lines[0]), "unsupported atlas version");
        String id = value(lines[1], "id");
        String kind = value(lines[2], "kind");
        String status = value(lines[3], "status");
        String artifact = value(lines[4], "artifact");
        String scope = value(lines[5], "scope");
        String subject = value(lines[6], "subject");
        String control = value(lines[7], "control");
        int denominator;
        try { denominator = Integer.parseInt(value(lines[8], "denominator")); }
        catch (NumberFormatException error) { throw new IllegalArgumentException("denominator"); }
        List<String> evidence = split(value(lines[9], "evidence"));
        List<String> refs = split(value(lines[10], "refs"));
        require(lines[11].startsWith("sha256="), "missing sha256");
        AtlasRecord record = of(id, kind, status, artifact, scope, subject, control, denominator,
                evidence, refs);
        require(document.equals(record.canonical), "record is not canonical");
        return record;
    }

    public String id() { return id; }
    public String kind() { return kind; }
    public String status() { return status; }
    public String artifact() { return artifact; }
    public String scope() { return scope; }
    public String subject() { return subject; }
    public String control() { return control; }
    public int denominator() { return denominator; }
    public List<String> evidence() { return evidence; }
    public List<String> refs() { return refs; }
    public String canonical() { return canonical; }
    public String sha256() { return AtlasHashes.sha256(canonical); }

    static String body(String id, String kind, String status, String artifact, String scope,
            String subject, String control, int denominator, List<String> evidence,
            List<String> refs) {
        StringBuilder text = new StringBuilder();
        text.append(AtlasSchema.HEADER).append('\n');
        text.append("id=").append(id).append('\n');
        text.append("kind=").append(kind).append('\n');
        text.append("status=").append(status).append('\n');
        text.append("artifact=").append(artifact).append('\n');
        text.append("scope=").append(scope).append('\n');
        text.append("subject=").append(subject).append('\n');
        text.append("control=").append(control).append('\n');
        text.append("denominator=").append(denominator).append('\n');
        text.append("evidence=").append(join(evidence)).append('\n');
        text.append("refs=").append(join(refs)).append('\n');
        return text.toString();
    }

    private static List<String> copy(String label, List<String> values) {
        if (values == null) throw new NullPointerException(label);
        List<String> copy = new ArrayList<String>();
        for (String value : values) {
            if (value == null || value.isEmpty() || value.indexOf(',') >= 0
                    || value.indexOf('\n') >= 0) {
                throw new IllegalArgumentException(label);
            }
            copy.add(value);
        }
        return Collections.unmodifiableList(copy);
    }

    private static List<String> split(String value) {
        if (value.isEmpty()) return Collections.emptyList();
        List<String> tokens = new ArrayList<String>();
        for (String item : value.split(",", -1)) tokens.add(item);
        return tokens;
    }

    private static String join(List<String> values) {
        StringBuilder text = new StringBuilder();
        for (int index = 0; index < values.size(); index++) {
            if (index > 0) text.append(',');
            text.append(values.get(index));
        }
        return text.toString();
    }

    private static String value(String line, String key) {
        require(FIELD.matcher(line).matches() && line.startsWith(key + "="), "missing " + key);
        return line.substring(key.length() + 1);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
