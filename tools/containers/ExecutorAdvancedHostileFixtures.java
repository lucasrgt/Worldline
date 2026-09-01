import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/** Qualifies owner loss, process exhaustion, and concurrent executor jobs. */
final class ExecutorAdvancedHostileFixtures {
    private static final long MEMORY = 512L << 20;
    private static final long OUTPUT_LIMIT = 1L << 20;
    private static final int CONCURRENT_JOBS = 4;

    private ExecutorAdvancedHostileFixtures() { }

    static void execute(Path root, Path executor) throws Exception {
        Path base = root.resolve(".worldline/executor-advanced-hostile");
        require(!Files.exists(base), "advanced hostile fixture residue exists");
        Path temporary = base.resolve("private temp"); Files.createDirectories(temporary);
        ExecutionEnvironmentManifest environment = ExecutionEnvironmentManifest.hostile(temporary);
        Path launcher = WindowsJobBootstrap.ensure();
        StringBuilder evidence = new StringBuilder("schema=worldline.executor-advanced-hostile.v1\n");
        try {
            compareOwner(legacyOwner(root, base.resolve("legacy-owner"), launcher, environment),
                    rustOwner(root, base.resolve("rust-owner"), executor, environment), evidence);
            Observation legacyLimit = legacy(root, base.resolve("legacy-limit"), launcher, environment,
                    command(root, "process-limit"), 1, true);
            Observation rustLimit = rust(root, base.resolve("rust-limit"), executor, environment,
                    command(root, "process-limit"), 1, true);
            compare("process-limit", legacyLimit, rustLimit, evidence);
            require(Long.valueOf(31).equals(rustLimit.exitCode) && legacyLimit.processes >= 1L
                    && Long.valueOf(1).equals(rustLimit.peakProcesses),
                    "active-process limit did not block the attempted child");
            concurrent(root, base, executor, launcher, environment, evidence);
            System.out.println("  advanced hostile executor fixtures: owner loss, process limit, "
                    + CONCURRENT_JOBS + " concurrent pairs; evidence="
                    + ExecutionJson.sha256(ExecutionJson.bytes(evidence.toString())));
        } finally { ProcessTreeTermination.delete(root, base, "advanced hostile fixture workspace"); }
    }

    static void selfTest() {
        require(CONCURRENT_JOBS >= 4 && MEMORY >= (256L << 20) && OUTPUT_LIMIT >= (64L << 10),
                "advanced hostile fixture bounds drift");
    }

    private static void concurrent(Path root, Path base, Path executor, Path launcher,
            ExecutionEnvironmentManifest environment, StringBuilder evidence) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENT_JOBS * 2);
        List<Future<Observation>> legacy = new ArrayList<>(), rust = new ArrayList<>();
        try {
            for (int index = 0; index < CONCURRENT_JOBS; index++) {
                final int id = index; List<String> command = command(root, "normal", "concurrent-" + id);
                legacy.add(pool.submit(task(() -> legacy(root, base.resolve("legacy-concurrent-" + id),
                        launcher, environment, command, 4, false))));
                rust.add(pool.submit(task(() -> rust(root, base.resolve("rust-concurrent-" + id),
                        executor, environment, command, 4, false))));
            }
            for (int index = 0; index < CONCURRENT_JOBS; index++)
                compare("concurrent-" + index, legacy.get(index).get(), rust.get(index).get(), evidence);
        } finally {
            pool.shutdownNow(); require(pool.awaitTermination(30, TimeUnit.SECONDS),
                    "concurrent hostile fixture workers did not stop");
        }
    }

    private static Observation legacyOwner(Path root, Path output, Path launcher,
            ExecutionEnvironmentManifest environment) throws Exception {
        Process owner = owner(root); Files.createDirectories(output); Path log = output.resolve("console.log");
        Path metrics = output.resolve("metrics.properties");
        Process runner = startLegacy(root, launcher, environment, command(root, "owner-target"),
                output, owner.pid(), 4, 30);
        try {
            waitFor(log, "owner-target=ready\n"); stop(owner, "legacy owner");
            require(runner.waitFor(20, TimeUnit.SECONDS) && runner.exitValue() == 130,
                    "legacy owner exit did not close the job");
            Properties values = properties(metrics);
            require("true".equals(values.getProperty("parent.exited"))
                    && "0".equals(values.getProperty("processes.active")),
                    "legacy owner-exit accounting drift");
            return new Observation("owner_exited", null, Files.readAllBytes(log),
                    number(values, "processes.total"), null);
        } finally { stop(owner, "legacy owner"); stop(runner, "legacy owner runner"); }
    }

    private static Observation rustOwner(Path root, Path output, Path executor,
            ExecutionEnvironmentManifest environment) throws Exception {
        Process owner = owner(root); Path planPath = output.resolveSibling("rust-owner-plan.json");
        WorldlineExecutionPlan plan = plan(root, output, command(root, "owner-target"),
                environment, owner.pid(), 4, 30_000L);
        plan.write(planPath); Path launcherLog = planPath.resolveSibling("rust-owner-launcher.log");
        Process runner = new ProcessBuilder(executor.toString(), "execute", planPath.toString())
                .directory(base(planPath).toFile()).redirectErrorStream(true)
                .redirectOutput(launcherLog.toFile()).start();
        try {
            waitFor(plan.outputs().stdout(), "owner-target=ready\n"); stop(owner, "Rust owner");
            require(runner.waitFor(20, TimeUnit.SECONDS) && runner.exitValue() == 125,
                    "Rust owner exit did not close the job");
            WorldlineExecutionReceipt.Verified value = WorldlineExecutionReceipt.verify(plan);
            require("owner_exited".equals(value.termination())
                    && Long.valueOf(0).equals(value.descendantsRemaining()),
                    "Rust owner-exit receipt drift");
            require(Files.size(plan.outputs().stderr()) == 0L, "Rust owner target wrote stderr");
            return new Observation(value.termination(), value.exitCode(),
                    Files.readAllBytes(plan.outputs().stdout()), 0L, value.telemetry().peakProcesses());
        } finally { stop(owner, "Rust owner"); stop(runner, "Rust owner runner"); }
    }

    private static Observation legacy(Path root, Path output, Path launcher,
            ExecutionEnvironmentManifest environment, List<String> command,
            int processes, boolean stderr) throws Exception {
        Files.createDirectories(output); Process runner = startLegacy(root, launcher, environment,
                command, output, ProcessHandle.current().pid(), processes, 15);
        require(runner.waitFor(30, TimeUnit.SECONDS), "legacy advanced fixture timed out");
        Properties values = properties(output.resolve("metrics.properties"));
        require("false".equals(values.getProperty("timed.out"))
                && "false".equals(values.getProperty("parent.exited"))
                && "0".equals(values.getProperty("processes.active")),
                "legacy advanced fixture accounting drift");
        return new Observation("exited", Long.valueOf(values.getProperty("exit.code")),
                Files.readAllBytes(output.resolve("console.log")), number(values, "processes.total"), null);
    }

    private static Observation rust(Path root, Path output, Path executor,
            ExecutionEnvironmentManifest environment, List<String> command,
            int processes, boolean stderr) throws Exception {
        WorldlineExecutionPlan plan = plan(root, output, command, environment,
                ProcessHandle.current().pid(), processes, 15_000L);
        WorldlineExecutionReceipt.Verified value = RustExecutorShadow.execute(executor,
                output.resolveSibling(output.getFileName() + "-plan.json"), plan);
        require(Long.valueOf(0).equals(value.descendantsRemaining())
                && !value.stdoutTruncated() && !value.stderrTruncated(),
                "Rust advanced fixture retained descendants or truncated output");
        Path selected = stderr ? plan.outputs().stderr() : plan.outputs().stdout();
        Path empty = stderr ? plan.outputs().stdout() : plan.outputs().stderr();
        require(Files.size(empty) == 0L, "Rust advanced fixture wrote an unexpected stream");
        return new Observation(value.termination(), value.exitCode(), Files.readAllBytes(selected),
                0L, value.telemetry().peakProcesses());
    }

    private static Process startLegacy(Path root, Path launcher, ExecutionEnvironmentManifest environment,
            List<String> command, Path output, long owner, int processes, int timeoutSeconds) throws Exception {
        List<String> invocation = new ArrayList<>(List.of(launcher.toString(), "--memory",
                Long.toString(MEMORY), "--cpu-rate", "10000", "--active-processes",
                Integer.toString(processes), "--timeout-seconds", Integer.toString(timeoutSeconds),
                "--parent-pid", Long.toString(owner), "--cwd", root.toString(), "--log",
                output.resolve("console.log").toString(), "--metrics",
                output.resolve("metrics.properties").toString(), "--"));
        invocation.addAll(command); ProcessBuilder builder = new ProcessBuilder(invocation).directory(root.toFile());
        environment.apply(builder); return builder.start();
    }

    private static WorldlineExecutionPlan plan(Path root, Path output, List<String> command,
            ExecutionEnvironmentManifest environment, long owner, int processes, long timeout) {
        return new WorldlineExecutionPlan("advanced-" + output.getFileName(), root, command,
                environment.environment(), new WorldlineExecutionPlan.Limits(timeout, MEMORY,
                processes, null, 100, OUTPUT_LIMIT, OUTPUT_LIMIT),
                new WorldlineExecutionPlan.Isolation("windows_job", "inherit", owner),
                WorldlineExecutionPlan.Filesystem.inherit(), new WorldlineExecutionPlan.Outputs(output,
                output.resolve("stdout.log"), output.resolve("stderr.log"), output.resolve("metrics.json"),
                output.resolve("manifest.json"), output.resolve("receipt.json")));
    }

    private static void compareOwner(Observation legacy, Observation rust, StringBuilder evidence)
            throws Exception {
        compare("owner-exit", legacy, rust, evidence);
    }
    private static void compare(String id, Observation legacy, Observation rust, StringBuilder evidence)
            throws Exception {
        require(legacy.termination.equals(rust.termination)
                && java.util.Objects.equals(legacy.exitCode, rust.exitCode), id + " termination differential");
        require(Arrays.equals(legacy.bytes, rust.bytes), id + " output differential");
        evidence.append(id).append('=').append(rust.termination).append(',')
                .append(rust.exitCode).append(',').append(ExecutionJson.sha256(rust.bytes)).append('\n');
    }

    private static Process owner(Path root) throws Exception {
        return new ProcessBuilder(command(root, "sleep", "60000")).directory(root.toFile()).start();
    }
    private static List<String> command(Path root, String... arguments) {
        List<String> value = new ArrayList<>(List.of(java(), "-Xms16m", "-Xmx128m", "-XX:-UsePerfData",
                root.resolve("tools/containers/ExecutorFixtureProbe.java").toString()));
        value.addAll(List.of(arguments)); return value;
    }
    private static void waitFor(Path path, String expected) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            if (Files.isRegularFile(path) && Files.readString(path, StandardCharsets.UTF_8).contains(expected)) return;
            Thread.sleep(25L);
        }
        throw new IllegalStateException("fixture readiness marker was not published: " + path);
    }
    private static void stop(Process process, String label) throws Exception {
        if (process == null || !process.isAlive()) return;
        ProcessTreeTermination.kill(process, label); require(!process.isAlive(), label + " remained active");
    }
    private static Properties properties(Path path) throws Exception {
        Properties value = new Properties(); try (var reader = Files.newBufferedReader(path)) { value.load(reader); }
        return value;
    }
    private static long number(Properties values, String key) {
        long value = Long.parseLong(values.getProperty(key, "")); require(value >= 0L, key + " is negative"); return value;
    }
    private static Path base(Path path) { return path.toAbsolutePath().normalize().getParent(); }
    private static String java() { return Path.of(System.getProperty("java.home"), "bin", "java.exe").toString(); }
    private static <T> Callable<T> task(Checked<T> action) { return () -> action.run(); }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
    @FunctionalInterface private interface Checked<T> { T run() throws Exception; }
    private record Observation(String termination, Long exitCode, byte[] bytes,
            long processes, Long peakProcesses) { }
}
