import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** Supervises one smoke process with a runtime lease, bounded time, logs, and tree cleanup. */
final class SmokeProcess {
    private final Path root;
    private final Path productRoot;
    private Telemetry telemetry = Telemetry.EMPTY;

    SmokeProcess(Path root) { this(root, null); }
    SmokeProcess(Path root, Path products) { this.root = root; this.productRoot = products; }

    long run(SmokeDiscovery.Entry smoke) throws Exception {
        requireRuntimeLease();
        int timeout = timeout(smoke.id);
        Path logs = root.resolve(".worldline/smoke-logs");
        Files.createDirectories(logs);
        Path log = logs.resolve(smoke.id + ".log");
        Path await = logs.resolve(smoke.id + "-" + UUID.randomUUID() + ".await");
        String classpath = System.getenv("WORLDLINE_HARNESS_CP");
        if (classpath == null || classpath.isBlank())
            throw new IllegalStateException("missing compiled harness classpath");
        List<String> command = List.of(javaTool(), "--class-path", classpath, smoke.runner, smoke.id);
        long started = System.nanoTime();
        ProcessBuilder builder = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true)
                .redirectOutput(log.toFile());
        builder.environment().put("WORLDLINE_AWAIT_TELEMETRY_FILE", await.toString());
        if (productRoot != null)
            builder.environment().put("WORLDLINE_PRODUCT_ROOT", productRoot.toAbsolutePath().normalize().toString());
        Process process = builder.start();
        boolean complete;
        try { complete = process.waitFor(timeout, TimeUnit.SECONDS); }
        catch (InterruptedException error) {
            destroy(process); Thread.currentThread().interrupt(); throw error;
        }
        if (!complete) destroy(process);
        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        String output = Files.readString(log, StandardCharsets.UTF_8);
        telemetry = Telemetry.read(await, output); Files.deleteIfExists(await);
        String summary = "WORLDLINE_AWAIT_TELEMETRY=id=" + smoke.id + ";" + telemetry.awaitEvidence()
                + "\nWORLDLINE_FLAKE_TELEMETRY=id=" + smoke.id + ";" + telemetry.retryEvidence() + "\n";
        Files.writeString(log, summary, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        if (!output.isBlank()) System.out.print(output);
        System.out.print(summary);
        if (!complete) {
            throw new IllegalStateException(smoke.id + " timed out after " + timeout + "s; log=" + log);
        }
        if (process.exitValue() != 0)
            throw new IllegalStateException(smoke.id + " exited " + process.exitValue() + "; log=" + log);
        System.out.println("  smoke " + smoke.id + ": " + elapsed + "ms");
        return elapsed;
    }

    Telemetry telemetry() { return telemetry; }

    static void execute(Path root, String id) throws Exception {
        SmokeDiscovery.Entry smoke = SmokeDiscovery.require(root, id);
        new SmokeProcess(root).run(smoke);
    }

    private int timeout(String id) throws IOException {
        String override = System.getenv("WORLDLINE_SMOKE_TIMEOUT_SECONDS");
        if (override != null && !override.isBlank()) return positive(override, "WORLDLINE_SMOKE_TIMEOUT_SECONDS");
        Properties descriptor = new Properties();
        Path path = root.resolve("smokes").resolve(id).resolve("smoke.properties");
        if (Files.isRegularFile(path)) try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            descriptor.load(reader);
        }
        return positive(descriptor.getProperty("timeout.seconds", "900"), id + " timeout.seconds");
    }

    private static int positive(String value, String name) {
        try {
            int result = Integer.parseInt(value.trim());
            if (result < 1 || result > 7200) throw new NumberFormatException();
            return result;
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException(name + " must be between 1 and 7200");
        }
    }

    private static void requireRuntimeLease() {
        String lease = System.getenv("WORLDLINE_RUNTIME_LEASE");
        if (lease == null || !lease.matches("[0-9]+")) throw new IllegalStateException(
                "smokes must run through java tools/harness/Gate.java --smoke[-id ID]");
    }

    private static void destroy(Process process) {
        process.descendants().sorted(Comparator.comparingLong(ProcessHandle::pid).reversed())
                .forEach(ProcessHandle::destroyForcibly);
        process.destroyForcibly();
    }

    private static String javaTool() {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        return Path.of(System.getProperty("java.home"), "bin", "java" + (windows ? ".exe" : "")).toString();
    }

    record Telemetry(long waits, long polls, long awaitFailures, long observedTicks,
            long attempts, long retries, long retryFailures, long policyCalls) {
        static final Telemetry EMPTY = new Telemetry(0, 0, 0, 0, 0, 0, 0, 0);
        static Telemetry read(Path await, String output) throws IOException {
            long[] values = new long[8];
            if (Files.isRegularFile(await)) for (String line : Files.readAllLines(await,
                    StandardCharsets.UTF_8)) add(values, line, 0,
                    List.of("waits", "polls", "failures", "observed-ticks"));
            for (String line : output.lines().filter(value ->
                    value.startsWith("WORLDLINE_FLAKE_TELEMETRY=")).toList())
                add(values, line.substring(line.indexOf('=') + 1), 4,
                        List.of("attempts", "retries", "failures", "policy-calls"));
            return new Telemetry(values[0], values[1], values[2], values[3], values[4],
                    values[5], values[6], values[7]);
        }
        private static void add(long[] totals, String line, int offset, List<String> fields) {
            Map<String, Long> parsed = new HashMap<>();
            for (String part : line.split(";")) { String[] pair = part.split("=", 2);
                if (pair.length == 2) try { parsed.put(pair[0], Long.parseLong(pair[1])); }
                catch (NumberFormatException ignored) { }
            }
            for (int index = 0; index < fields.size(); index++)
                totals[offset + index] += parsed.getOrDefault(fields.get(index), 0L);
        }
        String awaitEvidence() { return "waits=" + waits + ";polls=" + polls + ";failures="
                + awaitFailures + ";observed-ticks=" + observedTicks; }
        String retryEvidence() { return "attempts=" + attempts + ";retries=" + retries + ";failures="
                + retryFailures + ";policy-calls=" + policyCalls; }
    }
}
