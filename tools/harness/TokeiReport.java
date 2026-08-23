import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Structured view of tokei JSON for one language. */
record TokeiReport(long code, List<FileReport> files) {
    static TokeiReport find(String json, String language) {
        Map<String, Object> root = MiniJson.object(json);
        Object value = root.get(language);
        if (value == null) return null;
        Map<String, Object> section = MiniJson.asObject(value, language);
        List<FileReport> files = new ArrayList<>();
        for (Object raw : MiniJson.array(section, "reports")) {
            Map<String, Object> report = MiniJson.asObject(raw, "tokei report");
            Map<String, Object> stats = MiniJson.asObject(report.get("stats"), "tokei stats");
            files.add(new FileReport(MiniJson.string(report, "name"),
                    MiniJson.integer(stats, "code")));
        }
        return new TokeiReport(MiniJson.integer(section, "code"), List.copyOf(files));
    }

    static TokeiReport required(String json, String language) {
        TokeiReport report = find(json, language);
        if (report == null) throw new IllegalStateException("tokei JSON did not contain " + language);
        return report;
    }

    record FileReport(String name, long code) { }
}
