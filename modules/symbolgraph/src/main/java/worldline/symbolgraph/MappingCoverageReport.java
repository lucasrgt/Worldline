package worldline.symbolgraph;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Deterministic namespace and official-bytecode diagnostic report. */
public final class MappingCoverageReport {
    private final Map<String, String> metrics;
    private final String body;

    private MappingCoverageReport(Map<String, String> metrics) {
        this.metrics = Collections.unmodifiableMap(new LinkedHashMap<String, String>(metrics));
        StringBuilder text = new StringBuilder("schema=1\n");
        for (Map.Entry<String, String> entry : metrics.entrySet())
            text.append(entry.getKey()).append('=').append(entry.getValue()).append('\n');
        body = text.toString();
    }

    public static MappingCoverageReport create(Path clientJar, Path serverJar,
            Path intermediaryArchive, Path nostalgiaArchive, Path retroDescriptor, Path retroTiny)
            throws Exception {
        TinyMapping intermediary = MappingArchive.read(intermediaryArchive, "mappings/mappings.tiny");
        TinyMapping nostalgia = MappingArchive.read(nostalgiaArchive, "mappings/mappings.tiny");
        MappingPin.load(retroDescriptor).verify(retroTiny);
        TinyMapping retro;
        try (Reader reader = Files.newBufferedReader(retroTiny, StandardCharsets.UTF_8)) {
            retro = new TinyV2Reader().read(reader);
        }
        SymbolGraph base = new SymbolGraphBuilder().build(intermediary, nostalgia);
        RetroMcpImport.Result imported = new RetroMcpImport().apply(base, intermediary, retro);
        NamespaceAudit.Report namespaces = new NamespaceAudit().inspect(imported.graph());
        MappingAudit.Report named = new MappingAudit().compare(intermediary, nostalgia);
        OfficialBytecodeAudit audit = new OfficialBytecodeAudit();
        OfficialBytecodeAudit.Report client = audit.compare(
                OfficialJarInventory.read(clientJar), intermediary, "clientOfficial");
        OfficialBytecodeAudit.Report server = audit.compare(
                OfficialJarInventory.read(serverJar), intermediary, "serverOfficial");
        LinkedHashMap<String, String> values = new LinkedHashMap<String, String>();
        put(values, "graph.symbols", imported.graph().records().size());
        values.put("graph.sha256", imported.graph().sha256());
        put(values, "retro.matched", imported.matched());
        put(values, "retro.unmatched", imported.unmatched().size());
        put(values, "retro.side-name-differences", imported.nameDifferences().size());
        put(values, "retro.missing", imported.missing().size());
        for (NamespaceIssue issue : NamespaceIssue.values())
            put(values, "namespace." + issue.name(), namespaces.findings(issue).size());
        for (SymbolKind kind : SymbolKind.values()) {
            MappingAudit.Difference difference = named.difference(kind);
            String key = "nostalgia." + kind.name().toLowerCase();
            put(values, key + ".inventory", difference.inventoryCount());
            put(values, key + ".named", difference.namedCount());
            put(values, key + ".missing", difference.missing().size());
            put(values, key + ".extra", difference.extra().size());
        }
        side(values, "client", client);
        side(values, "server", server);
        return new MappingCoverageReport(values);
    }

    public Map<String, String> metrics() { return metrics; }
    public String metric(String key) {
        String value = metrics.get(key);
        if (value == null) throw new IllegalArgumentException("unknown mapping metric " + key);
        return value;
    }
    public String sha256() { return digest(body); }
    public String render() { return body + "report.sha256=" + sha256() + "\n"; }

    private static void side(Map<String, String> values, String side,
            OfficialBytecodeAudit.Report report) {
        for (SymbolKind kind : SymbolKind.values()) {
            String key = "official." + side + "." + kind.name().toLowerCase();
            put(values, key + ".total", report.official(kind));
            put(values, key + ".mapped", report.mapped(kind));
            put(values, key + ".missing", report.missing(kind));
            put(values, key + ".phantom", report.phantom(kind));
        }
        put(values, "official." + side + ".descriptor-candidates",
                report.descriptorConflictCandidates());
        for (Map.Entry<OfficialGapKind, Integer> entry : report.gapCounts().entrySet())
            put(values, "official." + side + ".gap." + entry.getKey().name(), entry.getValue().intValue());
    }

    private static void put(Map<String, String> values, String key, int value) {
        values.put(key, Integer.toString(value));
    }

    private static String digest(String text) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256")
                    .digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder value = new StringBuilder();
            for (byte item : bytes) value.append(String.format("%02x", Integer.valueOf(item & 255)));
            return value.toString();
        } catch (java.security.NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 unavailable", error);
        }
    }
}
