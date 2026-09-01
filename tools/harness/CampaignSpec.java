import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/** Strict tracked campaign configuration with numeric-only environment overrides. */
record CampaignSpec(String id, int defaultShards, List<Workload> workloads) {
    static CampaignSpec load(Path root, String id) throws Exception {
        require(id.matches("[a-z0-9]+(?:-[a-z0-9]+)*"), "invalid campaign id");
        Path path = root.resolve("quality/campaigns").resolve(id + ".properties");
        require(Files.isRegularFile(path), "unknown campaign: " + id);
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        require("worldline.campaign-spec.v1".equals(values.getProperty("schema")),
                "campaign spec schema drifted");
        require(id.equals(values.getProperty("id")), "campaign spec id drifted");
        int count = integer(values.getProperty("workload.count"), 1, 64, "workload count");
        int shards = integer(values.getProperty("shards.default"), 1, 1_024, "default shards");
        List<Workload> workloads = new ArrayList<>(); Set<String> expected = new HashSet<>(
                Set.of("schema", "id", "shards.default", "workload.count"));
        for (int index = 0; index < count; index++) {
            String prefix = "workload." + index + ".";
            String workloadId = required(values, prefix + "id");
            String kind = required(values, prefix + "kind");
            int cases = integer(required(values, prefix + "cases.default"), 1, 1_000_000_000,
                    workloadId + " cases");
            String environment = required(values, prefix + "cases.environment");
            require(environment.matches("WORLDLINE_[A-Z0-9_]+"), "invalid cases environment name");
            workloads.add(new Workload(workloadId, kind, cases, environment));
            expected.addAll(Set.of(prefix + "id", prefix + "kind", prefix + "cases.default",
                    prefix + "cases.environment"));
        }
        require(values.stringPropertyNames().equals(expected), "campaign spec fields drifted");
        return new CampaignSpec(id, shards, List.copyOf(workloads));
    }

    CampaignPlan plan(long seed) throws Exception {
        int shards = environment("WORLDLINE_CAMPAIGN_SHARDS", defaultShards, 1, 1_024);
        List<CampaignPlan.Workload> values = workloads.stream().map(workload ->
                new CampaignPlan.Workload(workload.id, workload.kind,
                        environment(workload.environment, workload.defaultCases, 1, 1_000_000_000)))
                .toList();
        return CampaignPlan.create(id, seed, shards, values);
    }

    private static int environment(String name, int fallback, int minimum, int maximum) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : integer(value, minimum, maximum, name);
    }
    private static int integer(String value, int minimum, int maximum, String label) {
        try { int parsed = Integer.parseInt(value);
            if (parsed >= minimum && parsed <= maximum) return parsed;
        } catch (NumberFormatException ignored) { }
        throw new IllegalArgumentException(label + " must be between " + minimum + " and " + maximum);
    }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key); require(value != null && !value.isBlank(), "missing " + key);
        return value;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
    record Workload(String id, String kind, int defaultCases, String environment) { }
}
