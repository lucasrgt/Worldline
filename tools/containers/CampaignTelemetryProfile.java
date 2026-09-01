import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/** Stable campaign workload-shape identity for comparable telemetry histories. */
final class CampaignTelemetryProfile {
    private CampaignTelemetryProfile() { }

    static String id(Path root, String campaign, String[] arguments,
            Map<String, String> environment) throws Exception {
        Path path = root.resolve("quality/campaigns").resolve(campaign + ".properties");
        require(Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)
                && !Files.isSymbolicLink(path) && Files.size(path) <= 8_192L,
                "invalid campaign telemetry spec");
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        require("worldline.campaign-spec.v1".equals(values.getProperty("schema"))
                && campaign.equals(values.getProperty("id")), "campaign telemetry spec identity drifted");
        int workloads = number(values, "workload.count", 1, 32);
        Set<String> keys = new HashSet<>(Set.of("schema", "id", "shards.default", "workload.count"));
        int shards = option(arguments, "--shard-count",
                number(values, "shards.default", 1, 1_024), 1, 1_024);
        int shard = option(arguments, "--shard-index", 0, 0, shards - 1);
        int budget = option(arguments, "--budget-seconds", -1, 10, 7_200);
        StringBuilder shape = new StringBuilder("campaign=").append(campaign)
                .append("\nbudget=").append(budget).append("\nshards=").append(shards)
                .append("\nshard=").append(shard);
        for (int index = 0; index < workloads; index++) {
            String stem = "workload." + index + ".";
            keys.addAll(List.of(stem + "id", stem + "kind", stem + "cases.default",
                    stem + "cases.environment"));
            String id = token(values, stem + "id"), kind = token(values, stem + "kind");
            int defaults = number(values, stem + "cases.default", 1, Integer.MAX_VALUE);
            String variable = environmentName(values.getProperty(stem + "cases.environment"));
            int cases = environment.containsKey(variable) ? environmentNumber(environment, variable) : defaults;
            shape.append("\nworkload=").append(id).append(',').append(kind).append(',').append(cases);
        }
        require(values.stringPropertyNames().equals(keys), "campaign telemetry spec fields drifted");
        String digest = ExecutionJson.sha256((shape + "\n").getBytes(StandardCharsets.UTF_8));
        return campaign + "-" + digest.substring(0, 16);
    }

    static void selfTest() throws Exception {
        Path root = Path.of("").toAbsolutePath().normalize();
        String first = id(root, "structural-instability", new String[] {
                "--budget-seconds", "120", "--seed", "1"}, Map.of());
        String second = id(root, "structural-instability", new String[] {
                "--seed", "999", "--budget-seconds", "120"}, Map.of());
        String volume = id(root, "structural-instability", new String[] {
                "--budget-seconds", "120", "--seed", "1"}, Map.of("WORLDLINE_INSTABILITY_CASES", "11"));
        require(first.equals(second) && !first.equals(volume)
                && first.matches("structural-instability-[0-9a-f]{16}"),
                "campaign telemetry profile stability drifted");
    }

    private static int option(String[] arguments, String name, int fallback, int minimum, int maximum) {
        Integer found = null;
        for (int index = 0; index < arguments.length; index++) if (name.equals(arguments[index])) {
            require(found == null && index + 1 < arguments.length, "invalid campaign profile option: " + name);
            try { found = Integer.parseInt(arguments[++index]); }
            catch (NumberFormatException error) {
                throw new IllegalArgumentException("campaign profile option is not numeric: " + name, error);
            }
        }
        int value = found == null ? fallback : found;
        require(value >= minimum && value <= maximum, "campaign profile option is out of bounds: " + name);
        return value;
    }
    private static int number(Properties values, String key, int minimum, int maximum) {
        try { int value = Integer.parseInt(values.getProperty(key));
            require(value >= minimum && value <= maximum, key + " is out of bounds"); return value;
        } catch (NumberFormatException error) { throw new IllegalArgumentException(key + " is not numeric", error); }
    }
    private static int environmentNumber(Map<String, String> values, String key) {
        String value = values.get(key); require(value.matches("[0-9]+"), key + " is not numeric");
        try { int number = Integer.parseInt(value); require(number > 0, key + " is not positive"); return number;
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException(key + " is out of bounds", error);
        }
    }
    private static String token(Properties values, String key) {
        String value = values.getProperty(key); require(value != null
                && value.matches("[a-z0-9][a-z0-9-]{0,62}"), "invalid campaign telemetry token: " + key);
        return value;
    }
    private static String environmentName(String value) {
        require(value != null && value.matches("WORLDLINE_[A-Z0-9_]{1,54}"),
                "invalid campaign telemetry environment name"); return value;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
}
