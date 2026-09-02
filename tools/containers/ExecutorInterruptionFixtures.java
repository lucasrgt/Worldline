import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/** Qualifies crash classification and atomic evidence after executor death. */
final class ExecutorInterruptionFixtures {
    private static final long MEMORY = 512L << 20;
    private static final int PROCESSES = 4;
    private static final long LIMIT = 1L << 20;
    private static final long CRASH_CODE = -1_073_741_819L;

    private ExecutorInterruptionFixtures() { }

    static void execute(Path root, Path executor) throws Exception {
        Path base = root.resolve(".worldline/executor-interruption-fixtures");
        require(!Files.exists(base), "interruption fixture residue exists");
        Path temporary = base.resolve("private temp"); Files.createDirectories(temporary);
        ExecutionEnvironmentManifest environment = ExecutionEnvironmentManifest.hostile(temporary);
        Path launcher = WindowsJobBootstrap.ensure();
        StringBuilder evidence = new StringBuilder("schema=worldline.executor-interruption-fixtures.v1\n");
        try {
            Observation legacyCrash = legacyCrash(root, base.resolve("legacy-crash"), launcher, environment);
            Observation rustCrash = rust(root, executor, base.resolve("rust-crash"), environment,
                    command(root, "crash"), "crash");
            require(legacyCrash.termination.equals("crashed") && legacyCrash.exitCode == CRASH_CODE,
                    "legacy crash classification drift");
            compare("crash", legacyCrash, rustCrash, evidence);
            interruptLegacy(root, base.resolve("legacy-interrupted"), launcher, environment, evidence);
            interruptRust(root, base.resolve("rust-interrupted"), executor, environment, evidence);
            Observation restart = rust(root, executor, base.resolve("rust-restart"), environment,
                    command(root, "normal", "restart-after-executor-death"), "restart");
            require(restart.termination.equals("exited") && Long.valueOf(0).equals(restart.exitCode),
                    "Rust executor did not restart cleanly after interruption");
            evidence.append("restart=").append(ExecutionJson.sha256(restart.bytes)).append('\n');
            System.out.println("  executor interruption fixtures: crash differential, two atomic "
                    + "executor-death boundaries, clean restart; evidence="
                    + ExecutionJson.sha256(ExecutionJson.bytes(evidence.toString())));
        } finally { ProcessTreeTermination.delete(root, base, "executor interruption fixture workspace"); }
    }

    static void selfTest() {
        require(CRASH_CODE < 0L && PROCESSES >= 3 && LIMIT >= (64L << 10),
                "interruption fixture bounds drift");
    }

    private static Observation legacyCrash(Path root, Path output, Path launcher,
            ExecutionEnvironmentManifest environment) throws Exception {
        Files.createDirectories(output); Process runner = startLegacy(root, launcher, environment,
                output, command(root, "crash"));
        require(runner.waitFor(20, TimeUnit.SECONDS), "legacy crash fixture timed out");
        Properties values = properties(output.resolve("metrics.properties"));
        require("0".equals(values.getProperty("processes.active")),
                "legacy crash retained descendants");
        long raw = Long.parseLong(values.getProperty("exit.code"));
        long signed = (int) raw;
        return new Observation(signed < 0L ? "crashed" : "exited", signed,
                Files.readAllBytes(output.resolve("console.log")));
    }

    private static Observation rust(Path root, Path executor, Path output,
            ExecutionEnvironmentManifest environment, List<String> command, String id) throws Exception {
        WorldlineExecutionPlan plan = plan(root, output, environment, command, "interruption-" + id);
        WorldlineExecutionReceipt.Verified value = RustExecutorShadow.execute(executor,
                output.resolveSibling(output.getFileName() + "-plan.json"), plan);
        require(Long.valueOf(0).equals(value.descendantsRemaining())
                && !value.stdoutTruncated() && !value.stderrTruncated(),
                "Rust interruption fixture retained descendants or truncated output");
        require(Files.size(plan.outputs().stderr()) == 0L, "Rust interruption fixture wrote stderr");
        return new Observation(value.termination(), value.exitCode(), Files.readAllBytes(plan.outputs().stdout()));
    }

    private static void interruptLegacy(Path root, Path output, Path launcher,
            ExecutionEnvironmentManifest environment, StringBuilder evidence) throws Exception {
        Files.createDirectories(output); Path completed = output.resolve("escaped.txt");
        String marker = output.resolve("legacy-descendant-marker").toString();
        Process runner = startLegacy(root, launcher, environment, output,
                command(root, "interrupted-root", marker, completed.toString()));
        try {
            waitFor(output.resolve("console.log"), "interrupted=ready\n"); killOnly(runner, "legacy executor");
            assertDrained(marker, completed); require(!Files.exists(output.resolve("metrics.properties")),
                    "legacy launcher published metrics after abrupt death");
            byte[] bytes = Files.readAllBytes(output.resolve("console.log"));
            evidence.append("legacy-interrupted=").append(ExecutionJson.sha256(bytes)).append('\n');
        } finally { terminate(runner, "legacy interrupted fixture"); }
    }

    private static void interruptRust(Path root, Path output, Path executor,
            ExecutionEnvironmentManifest environment, StringBuilder evidence) throws Exception {
        Path completed = output.resolve("escaped.txt");
        String marker = output.resolve("rust-descendant-marker").toString();
        WorldlineExecutionPlan plan = plan(root, output, environment,
                command(root, "interrupted-root", marker, completed.toString()), "interruption-rust-death");
        Path planPath = output.resolveSibling("rust-interrupted-plan.json"); plan.write(planPath);
        Path launcherLog = planPath.resolveSibling("rust-interrupted-launcher.log");
        Process runner = new ProcessBuilder(executor.toString(), "execute", planPath.toString())
                .directory(planPath.getParent().toFile()).redirectErrorStream(true)
                .redirectOutput(launcherLog.toFile()).start();
        try {
            waitFor(plan.outputs().stdout(), "interrupted=ready\n"); killOnly(runner, "Rust executor");
            assertDrained(marker, completed); assertNoAtomicEvidence(output);
            byte[] bytes = Files.readAllBytes(plan.outputs().stdout());
            evidence.append("rust-interrupted=").append(ExecutionJson.sha256(bytes)).append('\n');
        } finally { terminate(runner, "Rust interrupted fixture"); }
    }

    private static Process startLegacy(Path root, Path launcher, ExecutionEnvironmentManifest environment,
            Path output, List<String> command) throws Exception {
        List<String> invocation = new ArrayList<>(List.of(launcher.toString(), "--memory",
                Long.toString(MEMORY), "--cpu-rate", "10000", "--active-processes",
                Integer.toString(PROCESSES), "--timeout-seconds", "30", "--parent-pid",
                Long.toString(ProcessHandle.current().pid()), "--cwd", root.toString(), "--log",
                output.resolve("console.log").toString(), "--metrics",
                output.resolve("metrics.properties").toString(), "--"));
        invocation.addAll(command); ProcessBuilder builder = new ProcessBuilder(invocation).directory(root.toFile());
        environment.apply(builder); return builder.start();
    }

    private static WorldlineExecutionPlan plan(Path root, Path output,
            ExecutionEnvironmentManifest environment, List<String> command, String id) {
        return new WorldlineExecutionPlan(id, root, command, environment.environment(),
                new WorldlineExecutionPlan.Limits(30_000L, MEMORY, PROCESSES, null, 100, LIMIT, LIMIT),
                new WorldlineExecutionPlan.Isolation("windows_job", "inherit", ProcessHandle.current().pid()),
                WorldlineExecutionPlan.Filesystem.inherit(), new WorldlineExecutionPlan.Outputs(output,
                output.resolve("stdout.log"), output.resolve("stderr.log"), output.resolve("metrics.json"),
                output.resolve("manifest.json"), output.resolve("receipt.json")));
    }

    private static void assertNoAtomicEvidence(Path output) throws Exception {
        require(!Files.exists(output.resolve("metrics.json")) && !Files.exists(output.resolve("manifest.json"))
                && !Files.exists(output.resolve("receipt.json")), "abrupt executor death published partial evidence");
        try (var paths = Files.walk(output)) {
            require(paths.noneMatch(path -> path.getFileName().toString().contains(".tmp-")),
                    "abrupt executor death retained an atomic-output temporary");
        }
    }

    private static void assertDrained(String marker, Path completed) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (active(marker) && System.nanoTime() < deadline) BoundedSleep.millis(25L);
        require(!active(marker) && !Files.exists(completed), "executor death left a live descendant");
    }
    private static boolean active(String marker) {
        return ProcessHandle.allProcesses().anyMatch(process -> process.isAlive()
                && process.info().commandLine().orElse("").contains(marker));
    }
    private static void killOnly(Process process, String label) throws Exception {
        require(process.isAlive(), label + " exited before interruption"); process.destroyForcibly();
        require(process.waitFor(10, TimeUnit.SECONDS), label + " did not stop after forced termination");
    }
    private static void terminate(Process process, String label) throws Exception {
        if (process != null && process.isAlive()) ProcessTreeTermination.kill(process, label);
    }
    private static void waitFor(Path path, String expected) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            if (Files.isRegularFile(path) && Files.readString(path, StandardCharsets.UTF_8).contains(expected)) return;
            BoundedSleep.millis(25L);
        }
        throw new IllegalStateException("fixture readiness marker was not published: " + path);
    }
    private static void compare(String id, Observation legacy, Observation rust,
            StringBuilder evidence) throws Exception {
        require(legacy.termination.equals(rust.termination) && legacy.exitCode.equals(rust.exitCode),
                id + " termination differential");
        require(Arrays.equals(legacy.bytes, rust.bytes), id + " output differential");
        evidence.append(id).append('=').append(rust.termination).append(',').append(rust.exitCode)
                .append(',').append(ExecutionJson.sha256(rust.bytes)).append('\n');
    }
    private static Properties properties(Path path) throws Exception {
        Properties value = new Properties(); try (var reader = Files.newBufferedReader(path)) { value.load(reader); }
        return value;
    }
    private static List<String> command(Path root, String... arguments) {
        List<String> value = new ArrayList<>(List.of(java(), "-Xms16m", "-Xmx128m", "-XX:-UsePerfData",
                root.resolve("tools/containers/ExecutorFixtureProbe.java").toString()));
        value.addAll(List.of(arguments)); return value;
    }
    private static String java() { return Path.of(System.getProperty("java.home"), "bin", "java.exe").toString(); }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
    private record Observation(String termination, Long exitCode, byte[] bytes) { }
}
