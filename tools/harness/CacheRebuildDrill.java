import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/** Reconstructs every static Gate cache in an isolated control directory and measures it. */
public final class CacheRebuildDrill {
    private final Path root = Path.of("").toAbsolutePath().normalize();

    public static void main(String[] arguments) {
        try {
            if (List.of(arguments).equals(List.of("--self-test"))) { selfTest(); return; }
            require(List.of(arguments).equals(List.of("run")),
                    "usage: CacheRebuildDrill run|--self-test");
            new CacheRebuildDrill().execute();
        } catch (Exception error) {
            System.err.println("cache rebuild drill failed: " + error.getMessage()); System.exit(1);
        }
    }

    private void execute() throws Exception {
        require(ProcessCapture.require(root, List.of("git", "status", "--porcelain",
                "--untracked-files=all"), 60).isBlank(), "cache rebuild drill requires a clean tree");
        Policy policy = Policy.load(root.resolve("quality/cache-rebuild-baseline.properties"));
        Path temporary = Files.createTempDirectory("worldline-cache-rebuild-");
        Path checkout = temporary.resolve("checkout"), control = temporary.resolve("control");
        Path cache = control.resolve("cache"); boolean registered = false;
        Path reports = root.resolve(".worldline/reports"); Files.createDirectories(reports);
        Path log = reports.resolve("cache-rebuild.log"); long started = System.nanoTime();
        try {
            ProcessCapture.require(root, List.of("git", "worktree", "add", "--detach",
                    checkout.toString(), "HEAD"), 120); registered = true;
            Path gate = checkout.resolve("tools/harness/Gate.java");
            run(List.of(javaTool(), gate.toString()), checkout, control, log,
                    policy.maximumSeconds + 60, false);
            run(List.of(javaTool(), gate.toString(), "--cache-doctor"), checkout,
                    control, log, 120, true);
            long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
            long entries = cacheEntries(cache), bytes = SafeTreeDelete.size(cache);
            require(entries >= policy.minimumEntries, "cold cache entry census is incomplete");
            require(elapsed <= TimeUnit.SECONDS.toMillis(policy.maximumSeconds),
                    "cold cache reconstruction exceeded " + policy.maximumSeconds + " seconds");
            writeReport(reports.resolve("cache-rebuild.json"), policy, elapsed, entries, bytes);
            System.out.println("WORLDLINE_CACHE_REBUILD_DRILL=PASS");
            System.out.println("cache-rebuild.elapsed-ms=" + elapsed + ";reference-ms="
                    + policy.referenceMillis + ";entries=" + entries + ";bytes=" + bytes);
        } finally {
            SafeTreeDelete.delete(checkout.resolve(".worldline"));
            if (registered) ProcessCapture.require(root,
                    List.of("git", "worktree", "remove", checkout.toString()), 120);
            SafeTreeDelete.delete(temporary);
        }
    }

    private void run(List<String> command, Path directory, Path control, Path log, long seconds,
            boolean append) throws Exception {
        ProcessBuilder builder = new ProcessBuilder(command).directory(directory.toFile())
                .redirectErrorStream(true).redirectOutput(append
                        ? ProcessBuilder.Redirect.appendTo(log.toFile())
                        : ProcessBuilder.Redirect.to(log.toFile()));
        builder.environment().put("WORLDLINE_CONTROL_DIR", control.toString());
        builder.environment().put("WORLDLINE_SELF_TEST_CACHE", "off");
        Process process = builder.start();
        if (!process.waitFor(seconds, TimeUnit.SECONDS)) {
            destroy(process); throw new IllegalStateException("cold Gate timed out; log=" + log);
        }
        if (process.exitValue() != 0) throw new IllegalStateException(
                "cold Gate exited " + process.exitValue() + "; log=" + log + "\n" + tail(log));
    }

    private static long cacheEntries(Path cache) throws Exception {
        long count = 0L;
        for (Path path : SafeTreeDelete.paths(cache)) {
            String name = path.getFileName().toString();
            if (name.matches("[0-9a-f]{64}(?:[.](?:properties|log))?")) count++;
        }
        return count;
    }

    private static void writeReport(Path path, Policy policy, long elapsed, long entries, long bytes)
            throws Exception {
        String json = "{\n  \"schema\": 1,\n  \"status\": \"passed\",\n"
                + "  \"algorithm\": \"" + policy.algorithm + "\",\n"
                + "  \"platform\": \"" + platform() + "\",\n"
                + "  \"measured_at\": \"" + Instant.now() + "\",\n"
                + "  \"elapsed_ms\": " + elapsed + ",\n  \"reference_ms\": "
                + policy.referenceMillis + ",\n  \"maximum_ms\": "
                + TimeUnit.SECONDS.toMillis(policy.maximumSeconds) + ",\n"
                + "  \"cache_entries\": " + entries + ",\n  \"cache_bytes\": " + bytes + "\n}\n";
        Path pending = path.resolveSibling(path.getFileName() + ".tmp");
        Files.writeString(pending, json, StandardCharsets.UTF_8);
        Files.move(pending, path, StandardCopyOption.REPLACE_EXISTING);
    }

    private static String tail(Path path) throws Exception {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        return String.join("\n", lines.subList(Math.max(0, lines.size() - 40), lines.size()));
    }

    private static void destroy(Process process) {
        process.descendants().sorted(Comparator.comparingLong(ProcessHandle::pid).reversed())
                .forEach(ProcessHandle::destroyForcibly);
        process.destroyForcibly();
    }

    private static String platform() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")
                ? "windows" : "linux";
    }

    private static String javaTool() {
        boolean windows = platform().equals("windows");
        return Path.of(System.getProperty("java.home"), "bin", "java" + (windows ? ".exe" : ""))
                .toString();
    }

    static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-cache-drill-test-");
        try {
            Path policy = root.resolve("baseline.properties");
            Files.writeString(policy, baseline(1234L), StandardCharsets.UTF_8);
            Policy loaded = Policy.load(policy);
            require(loaded.maximumSeconds == 240L && loaded.referenceMillis == 1234L
                    && loaded.minimumEntries == 1L, "cache rebuild policy self-test drifted");
        } finally { SafeTreeDelete.delete(root); }
        System.out.println("  cache rebuild drill self-test: passed");
    }

    private static String baseline(long reference) {
        return "schema=1\nalgorithm=cold-static-gate-v1\nmaximum.seconds.windows=240\n"
                + "maximum.seconds.linux=240\nreference.ms.windows=" + reference
                + "\nreference.ms.linux=" + reference + "\nminimum.entries=1\n";
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private record Policy(String algorithm, long maximumSeconds, long referenceMillis,
            long minimumEntries) {
        static Policy load(Path path) throws Exception {
            Properties values = StrictProperties.load(path);
            require(values.size() == 7 && "1".equals(values.getProperty("schema")),
                    "invalid cache rebuild baseline schema");
            String platform = platform(), algorithm = required(values, "algorithm");
            require("cold-static-gate-v1".equals(algorithm), "unsupported cache rebuild algorithm");
            long maximum = positive(values, "maximum.seconds." + platform);
            long reference = positive(values, "reference.ms." + platform);
            long entries = positive(values, "minimum.entries");
            require(maximum >= 60L && maximum <= 600L, "cache rebuild maximum is unsafe");
            return new Policy(algorithm, maximum, reference, entries);
        }
        private static long positive(Properties values, String key) {
            try { long value = Long.parseLong(required(values, key));
                require(value > 0L, "cache rebuild value must be positive: " + key); return value; }
            catch (NumberFormatException error) { throw new IllegalStateException(
                    "invalid cache rebuild value: " + key); }
        }
        private static String required(Properties values, String key) {
            String value = values.getProperty(key);
            require(value != null && !value.isBlank(), "missing cache rebuild value: " + key);
            return value.trim();
        }
    }
}
