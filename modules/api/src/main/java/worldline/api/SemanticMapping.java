package worldline.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Immutable semantic annotation of one b1.7.3 symbol. Confidence is parts per
 * 10,000 (9998 means 0.9998). Empty read/write/dep/evidence tokens fail closed.
 */
public final class SemanticMapping {
    public static final int MAX_CONFIDENCE = 10000;
    public static final int MIN_KNOWN = 7000;
    private static final Pattern TOKEN = Pattern.compile("[A-Z][A-Z0-9_]{0,63}");
    private static final Pattern CATEGORY = Pattern.compile("[a-z][a-z0-9-]{0,31}");
    private static final Pattern OWNER = Pattern.compile("[A-Za-z0-9_$./-]{1,127}");
    private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_$.]{1,127}");
    private static final Pattern KIND = Pattern.compile("class|field|method|boundary");
    private final String category, role, owner, kind, name, descriptor, official;
    private final List<String> reads, writes, deps, evidence;
    private final int confidence;

    public SemanticMapping(String category, String role, String owner, String kind, String name,
            String descriptor, List<String> reads, List<String> writes, List<String> deps,
            List<String> evidence, String official, int confidence) {
        this.category = token(category, CATEGORY, "category");
        this.role = token(role, TOKEN, "role");
        this.owner = token(owner, OWNER, "owner");
        this.kind = token(kind, KIND, "kind");
        this.name = token(name, NAME, "name");
        if (descriptor == null || descriptor.isEmpty()) throw new IllegalArgumentException("descriptor");
        this.descriptor = descriptor;
        this.reads = copy("read", reads);
        this.writes = copy("write", writes);
        this.deps = copy("dep", deps);
        this.evidence = copy("evidence", evidence);
        this.official = officialName(official);
        if (confidence < 1 || confidence > MAX_CONFIDENCE) throw new IllegalArgumentException("confidence");
        this.confidence = confidence;
    }

    public static SemanticMapping of(String category, String role, String owner, String kind,
            String name, String descriptor, String reads, String writes, String deps,
            String evidence, int confidence) {
        return of(category, role, owner, kind, name, descriptor, reads, writes, deps, evidence, "",
                confidence);
    }

    public static SemanticMapping of(String category, String role, String owner, String kind,
            String name, String descriptor, String reads, String writes, String deps,
            String evidence, String official, int confidence) {
        return new SemanticMapping(category, role, owner, kind, name, descriptor,
                split(reads), split(writes), split(deps), split(evidence), official, confidence);
    }

    public String category() { return category; }
    public String role() { return role; }
    public String owner() { return owner; }
    public String kind() { return kind; }
    public String name() { return name; }
    public String descriptor() { return descriptor; }
    public String official() { return official; }
    public List<String> reads() { return reads; }
    public List<String> writes() { return writes; }
    public List<String> deps() { return deps; }
    public List<String> evidence() { return evidence; }
    public int confidence() { return confidence; }
    public boolean known() { return confidence >= MIN_KNOWN; }

    public String canonical() {
        return category + "|" + role + "|" + owner + "|" + kind + "|" + name + "|" + descriptor
                + "|o=" + official + "|r=" + join(reads) + "|w=" + join(writes) + "|d=" + join(deps)
                + "|e=" + join(evidence) + "|c=" + confidence;
    }

    @Override public boolean equals(Object other) {
        return other instanceof SemanticMapping && canonical().equals(((SemanticMapping) other).canonical());
    }

    @Override public int hashCode() { return canonical().hashCode(); }

    @Override public String toString() { return "SemanticMapping[" + role + "@" + owner + "." + name + "]"; }

    private static List<String> split(String value) {
        if (value == null) throw new NullPointerException("tokens");
        if (value.isEmpty()) return Collections.emptyList();
        List<String> tokens = new ArrayList<String>();
        for (String item : value.split(",")) tokens.add(item);
        return tokens;
    }

    private static List<String> copy(String label, List<String> values) {
        if (values == null) throw new NullPointerException(label);
        List<String> copy = new ArrayList<String>();
        for (String value : values) {
            if (value == null || value.isEmpty()) throw new IllegalArgumentException(label);
            copy.add(value);
        }
        return Collections.unmodifiableList(copy);
    }

    private static String officialName(String value) {
        if (value == null) throw new NullPointerException("official");
        return value.isEmpty() ? "" : token(value, NAME, "official");
    }

    private static String token(String value, Pattern pattern, String label) {
        if (value == null || !pattern.matcher(value).matches()) throw new IllegalArgumentException(label);
        return value;
    }

    private static String join(List<String> values) {
        StringBuilder text = new StringBuilder();
        for (int index = 0; index < values.size(); index++) {
            if (index > 0) text.append(',');
            text.append(values.get(index));
        }
        return text.toString();
    }
}
