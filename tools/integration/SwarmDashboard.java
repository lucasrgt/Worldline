import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** Generates one self-contained operational view of swarm state and evidence. */
public final class SwarmDashboard {
    public static void main(String[] arguments) {
        try {
            if (List.of(arguments).equals(List.of("--self-test"))) { selfTest(); return; }
            if (arguments.length > 1) throw new IllegalArgumentException("usage: SwarmDashboard.java [OUTPUT]");
            Path root = Path.of("").toAbsolutePath().normalize();
            Path output = root.resolve(arguments.length == 0
                    ? ".worldline/reports/swarm-dashboard.html" : arguments[0]).normalize();
            require(output.startsWith(root.resolve(".worldline")), "dashboard must stay under .worldline");
            Files.createDirectories(output.getParent());
            Files.writeString(output, render(root), StandardCharsets.UTF_8);
            System.out.println("swarm dashboard: " + root.relativize(output));
        } catch (Exception error) {
            System.err.println("swarm dashboard failed: " + error.getMessage()); System.exit(1);
        }
    }

    static String render(Path root) throws Exception {
        String worktrees = read(root.resolve(".worldline/reports/worktrees.json"));
        String branches = read(root.resolve(".worldline/reports/branches.json"));
        String verify = read(root.resolve(".worldline/reports/verify.json"));
        String smokeSuite = read(root.resolve(".worldline/reports/smoke-suite.json"));
        long pins = Files.readAllLines(root.resolve("smokes/qualification.lock")).stream()
                .filter(line -> line.endsWith(".status=passed")).count();
        List<Properties> handoffs = handoffs(root.resolve("coordination/handoffs"));
        StringBuilder html = new StringBuilder("<!doctype html><html><head><meta charset=\"utf-8\">"
                + "<title>Worldline Swarm</title><style>body{font:14px system-ui;margin:2rem;max-width:1200px}"
                + "table{border-collapse:collapse}td,th{border:1px solid #bbb;padding:.35rem}"
                + "pre{white-space:pre-wrap;background:#f5f5f5;padding:1rem}</style></head><body>"
                + "<h1>Worldline Swarm</h1><p>Generated " + escape(Instant.now().toString())
                + "</p><ul><li>Portable PASS pins: " + pins + "</li><li>Handoffs: "
                + handoffs.size() + "</li></ul><h2>Handoffs</h2><table><tr><th>Branch</th>"
                + "<th>Head</th><th>Base</th><th>Disposition</th><th>Receipt</th></tr>");
        for (Properties value : handoffs) html.append("<tr><td>").append(escape(value.getProperty("branch")))
                .append("</td><td>").append(escape(shortSha(value.getProperty("head"))))
                .append("</td><td>").append(escape(shortSha(value.getProperty("base"))))
                .append("</td><td>").append(escape(value.getProperty("disposition")))
                .append("</td><td>").append(escape(value.getProperty("receipt.sha256"))).append("</td></tr>");
        html.append("</table>"); section(html, "Worktrees", worktrees); section(html, "Branches", branches);
        section(html, "Smoke proofs", smokeSuite); section(html, "Latest Gate", verify);
        return html.append("</body></html>\n").toString();
    }

    private static List<Properties> handoffs(Path directory) throws Exception {
        List<Properties> result = new ArrayList<>(); if (!Files.isDirectory(directory)) return result;
        try (var paths = Files.list(directory)) { for (Path path : paths.sorted().toList()) {
            if (!path.toString().endsWith(".properties")) continue; Properties value = new Properties();
            SwarmHandoff.validate(path);
            try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { value.load(reader); }
            result.add(value);
        } }
        return result;
    }
    private static String read(Path path) throws Exception {
        return Files.isRegularFile(path) ? Files.readString(path, StandardCharsets.UTF_8) : "not generated";
    }
    private static void section(StringBuilder html, String title, String value) {
        html.append("<h2>").append(title).append("</h2><pre>").append(escape(value)).append("</pre>");
    }
    private static String shortSha(String value) {
        return value == null ? "" : value.substring(0, Math.min(12, value.length()));
    }
    private static String escape(String value) { return value.replace("&", "&amp;").replace("<", "&lt;")
            .replace(">", "&gt;").replace("\"", "&quot;"); }
    private static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-dashboard-");
        try {
            Files.createDirectories(root.resolve("smokes"));
            Files.writeString(root.resolve("smokes/qualification.lock"), "smoke.m1.status=passed\n");
            String html = render(root); require(html.contains("Portable PASS pins: 1")
                    && html.contains("not generated") && !html.contains("<script>"), "dashboard render drifted");
            System.out.println("swarm dashboard self-test passed");
        } finally { SafeTreeDelete.delete(root); }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
