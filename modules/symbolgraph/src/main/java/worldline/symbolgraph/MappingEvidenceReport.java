package worldline.symbolgraph;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/** Fail-closed cross-version and cross-namespace evidence for one exact queue. */
public final class MappingEvidenceReport {
    private static final Pattern SHA = Pattern.compile("[0-9a-f]{64}");
    private static final Pattern SOURCE = Pattern.compile("[a-z0-9][a-z0-9._-]{0,62}");
    private static final Pattern ALIAS = Pattern.compile("[A-Za-z0-9_$/<>.-]{1,160}");
    private static final Set<String> KINDS = kinds();
    private final Map<String, String> statuses;
    private final String body;

    private MappingEvidenceReport(MappingQualificationQueue queue, List<Evidence> evidence) {
        LinkedHashMap<String, String> values = new LinkedHashMap<String, String>();
        StringBuilder text = new StringBuilder("schema=1\nqueue.sha256=")
                .append(queue.sha256()).append("\nevidence=").append(evidence.size())
                .append("\nitems=").append(queue.items().size()).append('\n');
        text.append("item\tsource\tevidence\talias\treference\n");
        for (Evidence row : evidence) text.append(row.render()).append('\n');
        text.append("status\titem\tgap\tsources\taliases\n");
        int unqualified = 0, supported = 0, corroborated = 0, conflict = 0;
        for (MappingQualificationQueue.Item item : queue.items()) {
            TreeSet<String> sources = new TreeSet<String>(), aliases = new TreeSet<String>();
            for (Evidence row : evidence) if (row.item.equals(item.id())) {
                sources.add(row.source); if (!row.alias.isEmpty()) aliases.add(row.alias);
            }
            String status;
            if (sources.isEmpty()) { status = "UNQUALIFIED"; unqualified++; }
            else if (aliases.size() > 1) { status = "CONFLICT"; conflict++; }
            else if (sources.size() > 1 && aliases.size() == 1) { status = "CORROBORATED"; corroborated++; }
            else { status = "SUPPORTED"; supported++; }
            values.put(item.id(), status);
            text.append(status).append('\t').append(item.id()).append('\t').append(item.gap())
                    .append('\t').append(join(sources)).append('\t').append(join(aliases)).append('\n');
        }
        text.append("summary.unqualified=").append(unqualified).append('\n')
                .append("summary.supported=").append(supported).append('\n')
                .append("summary.corroborated=").append(corroborated).append('\n')
                .append("summary.conflict=").append(conflict).append('\n');
        statuses = Collections.unmodifiableMap(values); body = text.toString();
    }

    public static MappingEvidenceReport create(MappingQualificationQueue queue, Path path) throws Exception {
        if (queue == null || path == null) throw new NullPointerException("mapping evidence input");
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        require(lines.size() >= 3 && "schema=1".equals(lines.get(0)), "unsupported evidence schema");
        require(("queue.sha256=" + queue.sha256()).equals(lines.get(1)), "evidence queue digest drift");
        require("item\tsource\tevidence\talias\treference".equals(lines.get(2)),
                "invalid evidence header");
        Set<String> known = new HashSet<String>();
        for (MappingQualificationQueue.Item item : queue.items())
            require(known.add(item.id()), "queue item digest collision");
        Set<String> unique = new HashSet<String>(); List<Evidence> evidence = new ArrayList<Evidence>();
        for (int index = 3; index < lines.size(); index++) {
            String line = lines.get(index); require(!line.isEmpty(), "blank evidence row");
            String[] fields = line.split("\t", -1); require(fields.length == 5, "invalid evidence row");
            require(SHA.matcher(fields[0]).matches() && known.contains(fields[0]), "unknown evidence item");
            require(SOURCE.matcher(fields[1]).matches(), "invalid evidence source");
            require(KINDS.contains(fields[2]), "invalid evidence kind");
            String alias = "-".equals(fields[3]) ? "" : fields[3];
            require(alias.isEmpty() || ALIAS.matcher(alias).matches(), "invalid evidence alias");
            require(validReference(fields[4]), "invalid evidence reference");
            require(unique.add(fields[0] + "\t" + fields[1]), "duplicate evidence source for item");
            evidence.add(new Evidence(fields[0], fields[1], fields[2], alias, fields[4]));
        }
        Collections.sort(evidence); return new MappingEvidenceReport(queue, evidence);
    }

    public Map<String, String> statuses() { return statuses; }
    public String status(String item) {
        String value = statuses.get(item);
        if (value == null) throw new IllegalArgumentException("unknown evidence item " + item);
        return value;
    }
    public String sha256() { return digest(body); }
    public String render() { return body + "report.sha256=" + sha256() + "\n"; }

    private static String join(Set<String> values) { return values.isEmpty() ? "-" : String.join(",", values); }
    private static boolean validReference(String value) {
        return !value.isEmpty() && value.length() <= 512 && value.indexOf('\t') < 0
                && value.indexOf('\n') < 0 && value.indexOf('\r') < 0;
    }
    private static Set<String> kinds() { Set<String> values = new HashSet<String>();
        Collections.addAll(values, "cross-version", "cross-namespace", "behavior", "bytecode");
        return Collections.unmodifiableSet(values); }
    private static String digest(String value) { try {
        byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder text = new StringBuilder();
        for (byte item : bytes) text.append(String.format("%02x", Integer.valueOf(item & 255)));
        return text.toString();
    } catch (java.security.NoSuchAlgorithmException error) { throw new IllegalStateException(error); } }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message); }

    private static final class Evidence implements Comparable<Evidence> {
        final String item, source, kind, alias, reference;
        Evidence(String item, String source, String kind, String alias, String reference) {
            this.item = item; this.source = source; this.kind = kind; this.alias = alias; this.reference = reference; }
        String render() { return item + "\t" + source + "\t" + kind + "\t"
                + (alias.isEmpty() ? "-" : alias) + "\t" + reference; }
        @Override public int compareTo(Evidence other) { return render().compareTo(other.render()); }
    }
}
