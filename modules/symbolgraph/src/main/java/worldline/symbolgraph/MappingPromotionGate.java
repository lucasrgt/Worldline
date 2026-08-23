package worldline.symbolgraph;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/** Exact review boundary for mapping batches and complete-game promotion. */
public final class MappingPromotionGate {
    private MappingPromotionGate() {}

    public static Result verify(MappingCoverageReport coverage, MappingQualificationQueue queue,
            MappingEvidenceReport evidence, Path policyPath) throws Exception {
        if (coverage == null || queue == null || evidence == null || policyPath == null)
            throw new NullPointerException("mapping promotion input");
        Properties policy = new Properties();
        try (Reader reader = Files.newBufferedReader(policyPath, StandardCharsets.UTF_8)) {
            policy.load(reader);
        }
        require("1".equals(required(policy, "schema")), "unsupported mapping promotion schema");
        require(policy.size() == 5, "mapping promotion policy must contain exactly five properties");
        String mode = required(policy, "mode");
        require("batch".equals(mode) || "complete-game".equals(mode), "unsupported promotion mode");
        pin(policy, "coverage", coverage.sha256()); pin(policy, "queue", queue.sha256());
        pin(policy, "evidence", evidence.sha256());
        LinkedHashMap<String, Integer> counts = statusCounts(queue, evidence);
        require(counts.get("CONFLICT").intValue() == 0,
                "mapping promotion has conflicting aliases");
        boolean complete = complete(coverage.metrics(), queue.items().size());
        if ("complete-game".equals(mode)) {
            require(counts.get("UNQUALIFIED").intValue() == 0,
                    "complete-game mapping promotion has unqualified queue items");
            require(counts.get("SUPPORTED").intValue() == 0,
                    "complete-game mapping promotion requires independent alias corroboration");
            require(complete, "complete-game mapping definition is not satisfied");
        } else require(counts.get("CORROBORATED").intValue() > 0,
                "mapping batch has no independently corroborated items");
        return new Result(mode, coverage.sha256(), queue.sha256(), evidence.sha256(),
                counts, complete, queue, evidence);
    }

    static boolean complete(Map<String, String> metrics, int queueItems) {
        if (queueItems != 0 || number(metrics, "graph.symbols") <= 0) return false;
        int symbols = number(metrics, "graph.symbols");
        if (number(metrics, "namespace.MATCH") != symbols
                || number(metrics, "retro.matched") != symbols
                || !zero(metrics, "retro.unmatched", "retro.side-name-differences", "retro.missing"))
            return false;
        for (NamespaceIssue issue : NamespaceIssue.values())
            if (issue != NamespaceIssue.MATCH && number(metrics, "namespace." + issue.name()) != 0)
                return false;
        for (SymbolKind kind : SymbolKind.values()) {
            String named = "nostalgia." + kind.name().toLowerCase();
            if (!same(metrics, named + ".inventory", named + ".named")
                    || !zero(metrics, named + ".missing", named + ".extra")) return false;
            for (String side : new String[] {"client", "server"}) {
                String official = "official." + side + "." + kind.name().toLowerCase();
                if (!same(metrics, official + ".total", official + ".mapped")
                        || !zero(metrics, official + ".missing", official + ".phantom")) return false;
            }
        }
        for (String side : new String[] {"client", "server"}) {
            if (number(metrics, "official." + side + ".descriptor-candidates") != 0) return false;
            for (OfficialGapKind gap : OfficialGapKind.values())
                if (number(metrics, "official." + side + ".gap." + gap.name()) != 0) return false;
        }
        return true;
    }

    private static LinkedHashMap<String, Integer> statusCounts(MappingQualificationQueue queue,
            MappingEvidenceReport evidence) {
        LinkedHashMap<String, Integer> values = new LinkedHashMap<String, Integer>();
        for (String status : new String[] {"UNQUALIFIED", "SUPPORTED", "CORROBORATED", "CONFLICT"})
            values.put(status, Integer.valueOf(0));
        for (MappingQualificationQueue.Item item : queue.items()) {
            String status = evidence.status(item.id()); Integer count = values.get(status);
            require(count != null, "unknown mapping evidence status " + status);
            values.put(status, Integer.valueOf(count.intValue() + 1));
        }
        return values;
    }

    private static boolean same(Map<String, String> values, String left, String right) {
        return number(values, left) == number(values, right);
    }
    private static boolean zero(Map<String, String> values, String... keys) {
        for (String key : keys) if (number(values, key) != 0) return false;
        return true;
    }
    private static int number(Map<String, String> values, String key) {
        String value = values.get(key); require(value != null, "missing completeness metric " + key);
        try { int parsed = Integer.parseInt(value); require(parsed >= 0, "negative completeness metric " + key);
            return parsed; }
        catch (NumberFormatException failure) { throw new IllegalStateException("invalid completeness metric " + key); }
    }
    private static void pin(Properties policy, String name, String actual) {
        require(actual.equals(required(policy, "expected." + name + ".sha256")),
                "mapping promotion " + name + " digest drift");
    }
    private static String required(Properties policy, String key) {
        String value = policy.getProperty(key);
        require(value != null && !value.trim().isEmpty(), "missing mapping promotion property " + key);
        return value.trim();
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    public static final class Result {
        private final String body;
        private Result(String mode, String coverage, String queue, String evidence,
                Map<String, Integer> counts, boolean complete,
                MappingQualificationQueue qualification, MappingEvidenceReport report) {
            StringBuilder text = new StringBuilder("schema=2\nmode=").append(mode)
                    .append("\ncoverage.sha256=").append(coverage).append("\nqueue.sha256=")
                    .append(queue).append("\nevidence.sha256=").append(evidence).append('\n');
            for (Map.Entry<String, Integer> entry : counts.entrySet())
                text.append("status.").append(entry.getKey().toLowerCase()).append('=')
                        .append(entry.getValue()).append('\n');
            text.append("complete-game=").append(complete).append("\npromoted=")
                    .append(counts.get("CORROBORATED")).append('\n')
                    .append("item\talias\tsources\n");
            for (MappingQualificationQueue.Item item : qualification.items())
                if ("CORROBORATED".equals(report.status(item.id())))
                    text.append(item.id()).append('\t').append(report.aliases(item.id()))
                            .append('\t').append(report.sources(item.id())).append('\n');
            body = text.toString();
        }
        public String sha256() { return digest(body); }
        public String render() { return body + "decision.sha256=" + sha256() + "\n"; }
    }

    private static String digest(String value) { try {
        byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder text = new StringBuilder();
        for (byte item : bytes) text.append(String.format("%02x", Integer.valueOf(item & 255)));
        return text.toString();
    } catch (java.security.NoSuchAlgorithmException error) { throw new IllegalStateException(error); } }
}
