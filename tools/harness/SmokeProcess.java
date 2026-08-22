import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/** Supervises one smoke process with a runtime lease, bounded time, logs, and tree cleanup. */
final class SmokeProcess {
    private final Path root;

    SmokeProcess(Path root) { this.root = root; }

    long run(SmokeDiscovery.Entry smoke) throws Exception {
        requireRuntimeLease();
        int timeout = timeout(smoke.id);
        Path logs = root.resolve(".worldline/smoke-logs");
        Files.createDirectories(logs);
        Path log = logs.resolve(smoke.id + ".log");
        String classpath = System.getenv("WORLDLINE_HARNESS_CP");
        if (classpath == null || classpath.isBlank())
            throw new IllegalStateException("missing compiled harness classpath");
        List<String> command = List.of(javaTool(), "--class-path", classpath, smoke.runner, smoke.id);
        long started = System.nanoTime();
        Process process = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true)
                .redirectOutput(log.toFile()).start();
        boolean complete;
        try { complete = process.waitFor(timeout, TimeUnit.SECONDS); }
        catch (InterruptedException error) {
            destroy(process); Thread.currentThread().interrupt(); throw error;
        }
        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        String output = Files.readString(log, StandardCharsets.UTF_8);
        if (!output.isBlank()) System.out.print(output);
        if (!complete) {
            destroy(process);
            throw new IllegalStateException(smoke.id + " timed out after " + timeout + "s; log=" + log);
        }
        if (process.exitValue() != 0)
            throw new IllegalStateException(smoke.id + " exited " + process.exitValue() + "; log=" + log);
        System.out.println("  smoke " + smoke.id + ": " + elapsed + "ms");
        return elapsed;
    }

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
}
