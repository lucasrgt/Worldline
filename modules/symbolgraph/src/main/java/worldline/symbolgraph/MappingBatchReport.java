package worldline.symbolgraph;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/** Source-corroborated, cumulative qualification batch for the maintained mapping graph. */
public final class MappingBatchReport {
    private final Map<String, String> metrics;
    private final List<String> excludedIds;
    private final String body;

    private MappingBatchReport(Map<String, String> values, List<Row> excluded, String rows) {
        metrics = Collections.unmodifiableMap(new LinkedHashMap<String, String>(values));
        List<String> ids = new ArrayList<String>();
        for (Row row : excluded) ids.add(row.id());
        excludedIds = Collections.unmodifiableList(ids);
        StringBuilder text = new StringBuilder("schema=1\n");
        for (Map.Entry<String, String> metric : metrics.entrySet())
            text.append(metric.getKey()).append('=').append(metric.getValue()).append('\n');
        body = text.append(rows).toString();
    }

    public static MappingBatchReport create(MappingCoverageReport coverage,
            TinyMapping intermediary, TinyMapping nostalgia, TinyMapping feather,
            SymbolGraph retroGraph, int targetPercent) {
        require(targetPercent == 25 || targetPercent == 50 || targetPercent == 100,
                "mapping batch target must be 25, 50, or 100");
        if (coverage == null || intermediary == null || nostalgia == null || feather == null
                || retroGraph == null) throw new NullPointerException("mapping batch input");
        require(intermediary.namespace("clientOfficial") >= 0, "Calamus official namespace absent");
        require(nostalgia.namespace("named") >= 0 && feather.namespace("named") >= 0,
                "named mapping namespace absent");
        List<Row> qualified = new ArrayList<Row>(), excluded = new ArrayList<Row>();
        for (SymbolRecord record : retroGraph.records()) {
            Row row = row(record, intermediary, nostalgia, feather);
            if (row.sources.size() >= 2) qualified.add(row);
            else {
                require(!record.inventoryPresent(), "official mapping identity has fewer than two sources: "
                        + record.key());
                excluded.add(row);
            }
        }
        Comparator<Row> order = Comparator.comparing(row -> row.key.canonical());
        Collections.sort(qualified, order); Collections.sort(excluded, order);
        int selected = (qualified.size() * targetPercent + 99) / 100;
        List<Row> batch = qualified.subList(0, selected);
        LinkedHashMap<String, String> values = new LinkedHashMap<String, String>();
        values.put("target.percent", Integer.toString(targetPercent));
        values.put("coverage.sha256", coverage.sha256());
        values.put("graph.sha256", retroGraph.sha256());
        values.put("graph.total", Integer.toString(retroGraph.records().size()));
        values.put("qualified.total", Integer.toString(qualified.size()));
        values.put("excluded.total", Integer.toString(excluded.size()));
        values.put("selected.total", Integer.toString(batch.size()));
        values.put("selected.qualified", Integer.toString(batch.size()));
        values.put("complete", Boolean.toString(targetPercent == 100 && batch.size() == qualified.size()));
        StringBuilder rows = new StringBuilder("selected\tid\tkind\tidentity\tsources\n");
        for (Row row : batch) rows.append(row.render("yes"));
        rows.append("excluded\tid\tkind\tidentity\ttouched\tsources\n");
        for (Row row : excluded) rows.append(row.render("no"));
        return new MappingBatchReport(values, excluded, rows.toString());
    }

    public Map<String, String> metrics() { return metrics; }
    public List<String> excludedIds() { return excludedIds; }
    public String metric(String key) {
        String value = metrics.get(key);
        if (value == null) throw new IllegalArgumentException("unknown mapping batch metric " + key);
        return value;
    }
    public String sha256() { return digest(body); }
    public String render() { return body + "report.sha256=" + sha256() + "\n"; }

    private static Row row(SymbolRecord record, TinyMapping intermediary,
            TinyMapping nostalgia, TinyMapping feather) {
        TreeSet<String> sources = new TreeSet<String>(); SymbolKey key = record.key();
        if (intermediary.symbols().containsKey(key)) sources.add("calamus");
        if (nostalgia.symbols().containsKey(key)) sources.add("nostalgia");
        if (feather.symbols().containsKey(key)) sources.add("feather");
        if (!record.retroMcpClient().isEmpty() || !record.retroMcpServer().isEmpty())
            sources.add("retromcp");
        return new Row(key, sources);
    }
    private static String digest(String value) { try {
        byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder text = new StringBuilder();
        for (byte item : bytes) text.append(String.format("%02x", Integer.valueOf(item & 255)));
        return text.toString();
    } catch (java.security.NoSuchAlgorithmException error) { throw new IllegalStateException(error); } }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static final class Row {
        private final SymbolKey key; private final Set<String> sources;
        private Row(SymbolKey key, Set<String> sources) {
            this.key = key;
            this.sources = Collections.unmodifiableSet(new TreeSet<String>(sources));
        }
        String id() { return digest(key.canonical()); }
        String render(String selected) {
            return selected + '\t' + id() + '\t' + key.kind().name().toLowerCase()
                    + '\t' + key.canonical() + '\t' + String.join(",", sources) + '\n';
        }
    }
}
