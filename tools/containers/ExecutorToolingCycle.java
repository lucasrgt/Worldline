import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/** Qualifies the Rust Windows Job executor on a real Gate tooling cycle and its legacy rollback. */
public final class ExecutorToolingCycle {
    private static final Pattern SUCCESS = Pattern.compile(
            "Gate self-test passed; serialized elapsed=[0-9]+ms\\R?");
    private static final long MEMORY = 1L << 30;
    private static final int PROCESSES = 8;
    private static final long TIMEOUT_MS = 60_000L;
    private static final String JAVA_OPTIONS = "-Xms16m -Xmx128m -XX:-UsePerfData";

    private ExecutorToolingCycle() { }

    public static void main(String[] arguments) {
        try {
            require(arguments.length == 0, "usage: ExecutorToolingCycle");
            execute();
        } catch (Exception error) {
            System.err.println("executor tooling cycle failed: " + error.getMessage()); System.exit(1);
        }
    }

    static void selfTest() throws Exception {
        require(normalize("Picked up JAVA_TOOL_OPTIONS: " + JAVA_OPTIONS
                + "\r\nGate self-test passed; serialized elapsed=801ms\r\n")
                .equals("Picked up JAVA_TOOL_OPTIONS: " + JAVA_OPTIONS
                        + "\nGate self-test passed; serialized elapsed=<elapsed>ms\n"),
                "tooling output normalization drift");
        expectFailure(() -> normalize("Gate self-test passed; serialized elapsed=801ms\nextra\n"));
        Properties valid = new Properties(); valid.setProperty("format", "1");
        valid.setProperty("backend", "windows-job"); valid.setProperty("timed.out", "false");
        valid.setProperty("parent.exited", "false"); valid.setProperty("exit.code", "0");
        valid.setProperty("processes.total", "3"); valid.setProperty("processes.active", "0");
        verifyLegacy(valid); valid.setProperty("processes.active", "1");
        expectFailure(() -> verifyLegacy(valid));
        ExecutorHostileFixtures.selfTest();
        ExecutorAdvancedHostileFixtures.selfTest();
    }

    private static void execute() throws Exception {
        require(windows(), "the qualified executor tooling cycle currently requires Windows");
        Path root = Path.of("").toAbsolutePath().normalize(); requireClean(root);
        String configured = System.getenv("WORLDLINE_EXECUTOR");
        require(configured != null && !configured.isBlank(), "WORLDLINE_EXECUTOR is required");
        Path executable = Path.of(configured).toAbsolutePath().normalize();
        require(executable.isAbsolute() && Files.isRegularFile(executable),
                "WORLDLINE_EXECUTOR is not an absolute regular file");
        ExecutorHostileFixtures.execute(root, executable);
        ExecutorAdvancedHostileFixtures.execute(root, executable);
        Path parent = root.resolve(".worldline/executor-tooling-cycle");
        Path run = parent.resolve("current"), published = parent.resolve("published");
        require(!Files.exists(run) && !Files.exists(published), "executor tooling cycle residue exists");
        Path temporary = run.resolve("java-tmp"); Files.createDirectories(temporary);
        ExecutionEnvironmentManifest environment = ExecutionEnvironmentManifest.tooling(temporary);
        ExecutionFilesystemManifest filesystem = ExecutionFilesystemManifest.tooling(root, temporary, run);
        try {
            List<String> command = List.of(java(), root.resolve("tools/harness/Gate.java").toString(),
                    "--self-test");
            byte[] baseline = legacy(root, run.resolve("legacy-baseline"), command, environment);
            WorldlineExecutionPlan plan = plan(root, run.resolve("rust"), command, environment, filesystem);
            WorldlineExecutionReceipt.Verified result = RustExecutorShadow.execute(executable,
                    run.resolve("rust-plan.json"), plan);
            require("exited".equals(result.termination()) && Long.valueOf(0).equals(result.exitCode()),
                    "Rust tooling execution did not exit successfully: " + result + "\nstdout:\n"
                            + Files.readString(plan.outputs().stdout()) + "\nstderr:\n"
                            + Files.readString(plan.outputs().stderr()));
            require(!result.stdoutTruncated() && !result.stderrTruncated()
                    && Long.valueOf(0).equals(result.descendantsRemaining()),
                    "Rust tooling execution did not prove complete output and an empty process tree");
            require(result.runtimeMs() > 0L && result.bootstrapMs() <= result.elapsedMs(),
                    "Rust tooling execution did not report real phase telemetry");
            byte[] primary = joined(plan.outputs().stderr(), plan.outputs().stdout());
            byte[] rollback = legacy(root, run.resolve("legacy-rollback"), command, environment);
            String expected = normalize(new String(baseline, StandardCharsets.UTF_8));
            require(expected.equals(normalize(new String(primary, StandardCharsets.UTF_8)))
                    && expected.equals(normalize(new String(rollback, StandardCharsets.UTF_8))),
                    "legacy/Rust/rollback observable output differential");
            filesystem.verifyOutput();
            String transaction = WorkspaceTransaction.promote(run, published);
            String telemetry = ExecutionTelemetryHistory.record(
                    root.resolve(".worldline/executor-telemetry"), "tooling", "gate-self-test",
                    "structural-windows-job", result.telemetry());
            System.out.println("Rust executor tooling cycle passed; command=Gate --self-test; request="
                    + plan.requestSha256() + "; normalized="
                    + ExecutionJson.sha256(expected.getBytes(StandardCharsets.UTF_8))
                    + "; transaction=" + transaction + "; telemetry=" + telemetry
                    + "; rollback=legacy-windows-job");
        } finally { delete(root, run); delete(root, published); Files.deleteIfExists(parent); }
    }

    private static WorldlineExecutionPlan plan(Path root, Path output, List<String> command,
            ExecutionEnvironmentManifest environment, ExecutionFilesystemManifest filesystem) {
        return new WorldlineExecutionPlan("gate-self-test-tooling-cycle", root, command,
                environment.environment(),
                new WorldlineExecutionPlan.Limits(TIMEOUT_MS, MEMORY, PROCESSES, null, 100,
                        1L << 20, 1L << 20),
                new WorldlineExecutionPlan.Isolation("windows_job", "inherit",
                        ProcessHandle.current().pid()), filesystem.executorPolicy(),
                new WorldlineExecutionPlan.Outputs(output, output.resolve("stdout.log"),
                        output.resolve("stderr.log"), output.resolve("metrics.json"),
                        output.resolve("manifest.json"), output.resolve("receipt.json")));
    }

    private static byte[] legacy(Path root, Path output, List<String> command,
            ExecutionEnvironmentManifest environment) throws Exception {
        Files.createDirectories(output); Path log = output.resolve("console.log");
        Path metrics = output.resolve("metrics.properties");
        List<String> invocation = new ArrayList<>(List.of(WindowsJobBootstrap.ensure().toString(),
                "--memory", Long.toString(MEMORY), "--cpu-rate", "10000", "--active-processes",
                Integer.toString(PROCESSES), "--timeout-seconds", Long.toString(TIMEOUT_MS / 1_000),
                "--parent-pid", Long.toString(ProcessHandle.current().pid()), "--cwd", root.toString(),
                "--log", log.toString(), "--metrics", metrics.toString(), "--"));
        invocation.addAll(command); ProcessBuilder builder = new ProcessBuilder(invocation).directory(root.toFile());
        environment.apply(builder);
        Process process = builder.start();
        require(process.waitFor(90, TimeUnit.SECONDS) && process.exitValue() == 0,
                "legacy Windows Job tooling execution failed");
        Properties values = new Properties(); try (var reader = Files.newBufferedReader(metrics)) {
            values.load(reader);
        }
        verifyLegacy(values); return Files.readAllBytes(log);
    }

    private static void verifyLegacy(Properties values) {
        require("1".equals(values.getProperty("format"))
                && "windows-job".equals(values.getProperty("backend"))
                && "false".equals(values.getProperty("timed.out"))
                && "false".equals(values.getProperty("parent.exited"))
                && "0".equals(values.getProperty("exit.code")), "legacy Windows Job metrics drift");
        long total = number(values, "processes.total"), active = number(values, "processes.active");
        require(total >= 3L && active == 0L, "legacy rollback did not prove an empty process tree");
    }

    private static String normalize(String output) {
        String portable = output.replace("\r\n", "\n");
        String prefix = "Picked up JAVA_TOOL_OPTIONS: " + JAVA_OPTIONS + "\n";
        require(portable.startsWith(prefix) && SUCCESS.matcher(portable.substring(prefix.length())).matches(),
                "unexpected Gate tooling output");
        return portable.replaceAll("elapsed=[0-9]+ms", "elapsed=<elapsed>ms");
    }

    private static byte[] joined(Path first, Path second) throws Exception {
        byte[] one = Files.readAllBytes(first), two = Files.readAllBytes(second);
        byte[] result = java.util.Arrays.copyOf(one, one.length + two.length);
        System.arraycopy(two, 0, result, one.length, two.length); return result;
    }

    private static void requireClean(Path root) throws Exception {
        Process process = new ProcessBuilder("git", "status", "--porcelain=v1", "--untracked-files=all")
                .directory(root.toFile()).redirectErrorStream(true).start();
        require(process.waitFor(30, TimeUnit.SECONDS) && process.exitValue() == 0,
                "could not inspect repository state");
        String status = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        require(status.isBlank(), "executor tooling qualification requires a clean exact commit");
    }

    private static void delete(Path root, Path target) throws Exception {
        Process process = new ProcessBuilder(java(), root.resolve("tools/harness/SafeTreeDelete.java").toString(),
                target.toString(), target.getParent().toString()).directory(root.toFile()).inheritIO().start();
        require(process.waitFor(60, TimeUnit.SECONDS) && process.exitValue() == 0,
                "executor tooling evidence cleanup failed");
    }

    private static long number(Properties values, String name) {
        try { long value = Long.parseLong(values.getProperty(name, ""));
            require(value >= 0L, name + " is negative"); return value;
        } catch (NumberFormatException error) { throw new IllegalArgumentException(name + " is invalid", error); }
    }
    private static String java() { return Path.of(System.getProperty("java.home"), "bin", "java.exe").toString(); }
    private static boolean windows() { return System.getProperty("os.name").toLowerCase().contains("windows"); }
    private static void expectFailure(Checked action) throws Exception { try { action.run();
        throw new IllegalStateException("invalid tooling evidence was accepted");
    } catch (IllegalArgumentException expected) { } }
    private static void require(boolean value, String message) { if (!value) throw new IllegalArgumentException(message); }
    @FunctionalInterface private interface Checked { void run() throws Exception; }
}
