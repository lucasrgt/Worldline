import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/** Builds a failure-then-duration ordered cold-sweep plan containing only unproved inputs. */
final class SmokePoolPlan {
    private static final List<String> LANES = List.of(
            "server-headless", "windows-client-gui", "tooling");

    public static void main(String[] arguments) {
        try {
            if (arguments.length != 0) throw new IllegalArgumentException(
                    "usage: java tools/harness/Gate.java --smoke-plan");
            new SmokePoolPlan().execute();
        } catch (Exception error) {
            System.err.println("smoke pool plan failed: " + error.getMessage()); System.exit(1);
        }
    }

    private final Path root = Path.of("").toAbsolutePath().normalize();

    private void execute() throws Exception {
        SmokeGitState state = SmokeGitState.read(root);
        require(state.clean(), "cold-sweep planning requires a clean tracked tree");
        SmokeReceiptCache cache = new SmokeReceiptCache(root);
        SmokeScheduleHistory history = new SmokeScheduleHistory(root);
        List<Planned> missing = new ArrayList<>(); Map<String, Integer> totals = counts();
        List<SmokeDiscovery.Entry> catalog = SmokeDiscovery.discover(root);
        int reusable = 0;
        for (SmokeDiscovery.Entry smoke : catalog) {
            String lane = lane(smoke); totals.put(lane, totals.get(lane) + 1);
            String fingerprint = cache.fingerprint(smoke);
            if (cache.availablePin(smoke) != null) { reusable++; continue; }
            long cachedDuration = cache.historicalDuration(smoke.id);
            missing.add(new Planned(smoke, lane, timeout(smoke.id),
                    history.score(smoke.id, cachedDuration), fingerprint));
        }
        history.validateCatalog(catalog);
        missing.sort((left, right) -> {
            int priority = SmokeScheduleHistory.compare(left.score, right.score);
            return priority != 0 ? priority : left.smoke.id.compareTo(right.smoke.id);
        });
        Path output = root.resolve(".worldline/runtime-plan"); Files.createDirectories(output);
        for (String lane : LANES) writeManifest(output, lane, missing);
        writeJson(output, state, catalog.size(), reusable, totals, missing);
        System.out.println("  smoke pool plan: total=" + catalog.size() + ", reusable=" + reusable
                + ", missing=" + missing.size());
        for (Planned item : missing) System.out.println("    missing " + item.smoke.id
                + " failures=" + item.score.failures() + "/" + item.score.attempts()
                + " duration-ms=" + item.score.duration() + " fingerprint=" + item.fingerprint);
        System.out.println("  plan: .worldline/runtime-plan/plan.json");
    }

    private String lane(SmokeDiscovery.Entry smoke) throws Exception {
        Properties descriptor = descriptor(smoke.id);
        if ("tooling-cycle".equals(descriptor.getProperty("qualification.proof"))) return "tooling";
        String source = Files.readString(root.resolve(smoke.runner), StandardCharsets.UTF_8);
        if (source.contains("minecraft-b1.7.3-client.properties")
                || source.contains("aero-model-lib") || source.contains("runClient")
                || source.contains("WORLDLINE_AERO")) return "windows-client-gui";
        return "server-headless";
    }

    private int timeout(String id) throws Exception {
        String value = descriptor(id).getProperty("timeout.seconds", "900").trim();
        try {
            int timeout = Integer.parseInt(value);
            require(timeout >= 30 && timeout <= 3600, "unsafe timeout for " + id); return timeout;
        } catch (NumberFormatException error) {
            throw new IllegalStateException("invalid timeout for " + id, error);
        }
    }

    private Properties descriptor(String id) throws Exception {
        Properties values = new Properties(); Path path = root.resolve("smokes").resolve(id)
                .resolve("smoke.properties");
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return values;
    }

    private void writeManifest(Path output, String lane, List<Planned> missing) throws Exception {
        StringBuilder text = new StringBuilder("# id\tlane\tsource\targument\ttimeout-seconds\n");
        for (Planned item : missing) if (item.lane.equals(lane)) text.append(item.smoke.id)
                .append('\t').append(lane).append('\t').append(item.smoke.runner).append('\t')
                .append(item.smoke.id).append('\t').append(item.timeout).append('\n');
        Files.writeString(output.resolve(lane + ".tsv"), text, StandardCharsets.UTF_8);
    }

    private void writeJson(Path output, SmokeGitState state, int total, int reusable,
            Map<String, Integer> totals, List<Planned> missing) throws Exception {
        Map<String, Integer> planned = counts();
        for (Planned item : missing) planned.put(item.lane, planned.get(item.lane) + 1);
        String json = "{\n  \"schema\": 1,\n  \"created\": \"" + Instant.now()
                + "\",\n  \"head\": \"" + state.head() + "\",\n  \"tree\": \"" + state.tree()
                + "\",\n  \"clean\": true,\n  \"total\": " + total + ",\n  \"reusable\": "
                + reusable + ",\n  \"missing\": " + missing.size() + ",\n  \"lanes\": {\n"
                + rows(totals, planned) + "\n  }\n}\n";
        Files.writeString(output.resolve("plan.json"), json, StandardCharsets.UTF_8);
    }

    private static String rows(Map<String, Integer> totals, Map<String, Integer> planned) {
        StringBuilder result = new StringBuilder();
        for (int index = 0; index < LANES.size(); index++) {
            String lane = LANES.get(index);
            result.append("    \"").append(lane).append("\": {\"total\": ")
                    .append(totals.get(lane)).append(", \"missing\": ").append(planned.get(lane))
                    .append('}').append(index + 1 == LANES.size() ? "" : ",").append('\n');
        }
        return result.toString().stripTrailing();
    }

    private static Map<String, Integer> counts() {
        Map<String, Integer> values = new HashMap<>();
        for (String lane : LANES) values.put(lane, 0); return values;
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    private record Planned(SmokeDiscovery.Entry smoke, String lane, int timeout,
                           SmokeScheduleHistory.Score score, String fingerprint) {}
}
