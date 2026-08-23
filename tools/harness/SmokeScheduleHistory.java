import java.io.Reader;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

/** Records local run outcomes and maintains reviewed failure/duration scheduling history. */
final class SmokeScheduleHistory {
    private static final List<String> FIELDS = List.of("attempts", "failures", "duration.total.ms",
            "retry.attempts", "retries", "retry.failures", "retry.policy.calls", "await.waits",
            "await.polls", "await.failures", "await.observed.ticks", "await.polls.maximum");
    private final Path root, tracked, observations;
    private final Properties values, policy;

    SmokeScheduleHistory(Path root) throws Exception {
        this.root = root.toAbsolutePath().normalize();
        this.tracked = this.root.resolve("smokes/schedule.properties");
        this.observations = this.root.resolve(".worldline/reports/smoke-history");
        this.values = load(tracked);
        this.policy = load(this.root.resolve("quality/smoke-telemetry.properties"));
        validate(values);
    }

    static void main(String[] arguments) {
        try {
            if (List.of(arguments).equals(List.of("--self-test"))) {
                selfTest();
                return;
            }
            require(List.of(arguments).equals(List.of("update")),
                    "usage: SmokeScheduleHistory update|--self-test");
            new SmokeScheduleHistory(Path.of("").toAbsolutePath().normalize()).update();
        } catch (Exception error) {
            System.err.println("smoke schedule history failed: " + error.getMessage());
            System.exit(1);
        }
    }

    Score score(String id, long fallbackDuration) {
        long attempts = number(values, key(id, "attempts"));
        long failures = number(values, key(id, "failures"));
        long total = number(values, key(id, "duration.total.ms"));
        long duration = attempts == 0L ? fallbackDuration : Math.max(1L, total / attempts);
        return new Score(attempts, failures, duration);
    }

    void observed(String id, boolean passed, long duration) throws Exception {
        observed(id, passed, duration, SmokeProcess.Telemetry.EMPTY);
    }

    void observed(String id, boolean passed, long duration, SmokeProcess.Telemetry telemetry)
            throws Exception {
        require(id.matches("[a-z0-9]+(?:-[a-z0-9]+)*") && duration >= 0L, "invalid smoke observation");
        Files.createDirectories(observations);
        Path path = observations.resolve(id + ".properties");
        Path lock = observations.resolve(id + ".lock");
        try (FileChannel channel = FileChannel.open(lock, StandardOpenOption.CREATE,
                StandardOpenOption.WRITE); FileLock lease = channel.lock()) {
            require(lease.isValid(), "invalid smoke observation lock");
            Properties local = Files.isRegularFile(path) ? load(path) : new Properties();
            long attempts = Math.max(number(values, key(id, "attempts")), number(local, "attempts")) + 1L;
            long failures = Math.max(number(values, key(id, "failures")), number(local, "failures"));
            long total = Math.max(number(values, key(id, "duration.total.ms")),
                    number(local, "duration.total.ms"));
            local.setProperty("schema", "2");
            local.setProperty("id", id);
            local.setProperty("attempts", Long.toString(attempts));
            local.setProperty("failures", Long.toString(failures + (passed ? 0L : 1L)));
            local.setProperty("duration.total.ms", Long.toString(Math.addExact(total, duration)));
            add(local, id, "retry.attempts", telemetry.attempts());
            add(local, id, "retries", telemetry.retries());
            add(local, id, "retry.failures", telemetry.retryFailures());
            add(local, id, "retry.policy.calls", telemetry.policyCalls());
            add(local, id, "await.waits", telemetry.waits());
            add(local, id, "await.polls", telemetry.polls());
            add(local, id, "await.failures", telemetry.awaitFailures());
            add(local, id, "await.observed.ticks", telemetry.observedTicks());
            local.setProperty("await.polls.maximum", Long.toString(Math.max(telemetry.polls(),
                    Math.max(number(values, key(id, "await.polls.maximum")),
                            number(local, "await.polls.maximum")))));
            store(path, local);
        }
    }

    void update() throws Exception {
        Properties merged = new Properties();
        merged.putAll(values);
        if (Files.isDirectory(observations)) try (var paths = Files.list(observations)) {
            for (Path path : paths.filter(item -> item.toString().endsWith(".properties")).sorted().toList()) {
                Properties local = load(path);
                require(Set.of("1", "2").contains(local.getProperty("schema")),
                        "invalid observation schema: " + path.getFileName());
                String id = local.getProperty("id", "");
                enforceReviewPolicy(id, local);
                for (String field : FIELDS) {
                    long next = Math.max(number(merged, key(id, field)), number(local, field));
                    merged.setProperty(key(id, field), Long.toString(next));
                }
            }
        }
        validate(merged);
        store(tracked, merged);
        values.clear();
        values.putAll(merged);
        System.out.println("smoke scheduling history updated");
    }

    void validateCatalog(List<SmokeDiscovery.Entry> catalog) {
        Set<String> ids = new HashSet<>();
        for (SmokeDiscovery.Entry entry : catalog) ids.add(entry.id);
        for (String key : values.stringPropertyNames()) if (key.startsWith("smoke.")) {
            String id = id(key);
            require(ids.contains(id),
                    "scheduling history names an unknown smoke: " + key);
        }
    }

    static int compare(Score left, Score right) {
        int failures = Double.compare(right.failureRate(), left.failureRate());
        if (failures != 0) return failures;
        int duration = Long.compare(left.duration, right.duration);
        return duration != 0 ? duration : Long.compare(right.attempts, left.attempts);
    }

    private static void validate(Properties values) {
        require("2".equals(values.getProperty("schema")), "smoke schedule schema drifted");
        Set<String> ids = new HashSet<>();
        for (String key : values.stringPropertyNames()) if (key.startsWith("smoke.")) {
            ids.add(id(key));
            number(values, key);
        }
        for (String id : ids) {
            long attempts = number(values, key(id, "attempts"));
            long failures = number(values, key(id, "failures"));
            require(failures <= attempts, "failure count exceeds attempts for " + id);
            require(number(values, key(id, "retries")) <= number(values, key(id, "retry.attempts")),
                    "retry count exceeds attempts for " + id);
        }
    }

    private void enforceReviewPolicy(String id, Properties local) {
        long priorRetries = number(values, key(id, "retries"));
        long retries = number(local, "retries");
        require(priorRetries != 0L || retries == 0L,
                "review required: previously clean smoke now retries: " + id);
        long priorPolls = number(values, key(id, "await.polls.maximum"));
        long polls = number(local, "await.polls.maximum");
        long minimum = number(policy, "await.polls.regression.minimum");
        long factor = number(policy, "await.polls.regression.factor.percent");
        require(priorPolls == 0L || polls <= minimum || polls * 100L <= priorPolls * factor,
                "review required: await polling regressed: " + id + " " + priorPolls + " -> " + polls);
    }

    static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-smoke-schedule-");
        try {
            Files.createDirectories(root.resolve("smokes"));
            Files.createDirectories(root.resolve("quality"));
            Files.writeString(root.resolve("smokes/schedule.properties"), "schema=2\n");
            Files.writeString(root.resolve("quality/smoke-telemetry.properties"),
                    "await.polls.regression.minimum=50\nawait.polls.regression.factor.percent=150\n");
            SmokeScheduleHistory history = new SmokeScheduleHistory(root);
            history.observed("m1-flaky", false, 30L,
                    new SmokeProcess.Telemetry(1, 3, 0, 3, 1, 0, 0, 0));
            history.observed("m1-flaky", true, 10L,
                    new SmokeProcess.Telemetry(1, 2, 0, 2, 1, 0, 0, 0));
            history.observed("m2-slow", true, 100L);
            history.update();
            history.update();
            SmokeScheduleHistory restored = new SmokeScheduleHistory(root);
            Score flaky = restored.score("m1-flaky", Long.MAX_VALUE);
            Score slow = restored.score("m2-slow", Long.MAX_VALUE);
            require(flaky.attempts == 2L && flaky.failures == 1L && flaky.duration == 20L,
                    "failure history aggregation drifted");
            require(number(restored.values, key("m1-flaky", "await.polls")) == 5L
                            && number(restored.values, key("m1-flaky", "await.polls.maximum")) == 3L,
                    "await history aggregation drifted");
            require(compare(flaky, slow) < 0 && compare(slow, new Score(0, 0, 5)) > 0,
                    "failure/duration priority drifted");
            Properties retry = new Properties(); retry.setProperty("retries", "1");
            rejects(() -> restored.enforceReviewPolicy("m1-flaky", retry));
            restored.values.setProperty(key("m2-slow", "await.polls.maximum"), "60");
            Properties polling = new Properties(); polling.setProperty("await.polls.maximum", "100");
            rejects(() -> restored.enforceReviewPolicy("m2-slow", polling));
            System.out.println("smoke schedule history self-test passed");
        } finally { delete(root); }
    }

    private static Properties load(Path path) throws Exception {
        Properties result = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { result.load(reader); }
        return result;
    }
    private static void store(Path path, Properties values) throws Exception {
        TreeMap<String, String> sorted = new TreeMap<>();
        for (String key : values.stringPropertyNames()) sorted.put(key, values.getProperty(key));
        StringBuilder text = new StringBuilder();
        for (var entry : sorted.entrySet())
            text.append(entry.getKey()).append('=').append(entry.getValue()).append('\n');
        Files.createDirectories(path.getParent());
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        Files.writeString(temporary, text, StandardCharsets.UTF_8);
        Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
    }
    private static long number(Properties values, String key) {
        String value = values.getProperty(key, "0");
        try {
            long parsed = Long.parseLong(value);
            require(parsed >= 0L, "negative " + key);
            return parsed;
        }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid " + key, error); }
    }
    private static String key(String id, String field) { return "smoke." + id + "." + field; }
    private static String id(String key) {
        for (String field : FIELDS.stream().sorted(java.util.Comparator
                .comparingInt(String::length).reversed()).toList()) {
            String suffix = "." + field;
            if (key.endsWith(suffix) && key.length() > 6 + suffix.length())
                return key.substring(6, key.length() - suffix.length());
        }
        throw new IllegalStateException("invalid smoke schedule key: " + key);
    }
    private static void delete(Path root) throws Exception {
        if (!Files.exists(root)) return;
        try (var paths = Files.walk(root)) {
            for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
        }
    }
    private static void rejects(CheckedAction action) throws Exception {
        try { action.run(); throw new AssertionError("expected telemetry review rejection"); }
        catch (IllegalStateException expected) { }
    }
    private void add(Properties local, String id, String field, long delta) {
        long prior = Math.max(number(values, key(id, field)), number(local, field));
        local.setProperty(field, Long.toString(Math.addExact(prior, delta)));
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    record Score(long attempts, long failures, long duration) {
        double failureRate() { return attempts == 0L ? 0.0d : (double) failures / attempts; }
    }
    @FunctionalInterface private interface CheckedAction { void run() throws Exception; }
}
