import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/** Qualifies one official headless-server behavior through legacy and Rust containment. */
public final class ExecutorServerCycle {
    private static final String ID = "m20-server-bootstrap";
    private static final long TIMEOUT_MS = 600_000L;
    private static final long MEMORY = 3L << 30;
    private static final int PROCESSES = 32;

    private ExecutorServerCycle() { }

    public static void main(String[] arguments) {
        try {
            require(arguments.length == 0, "usage: ExecutorServerCycle"); execute();
        } catch (Exception error) {
            System.err.println("executor server cycle failed: " + error.getMessage()); System.exit(1);
        }
    }

    static void selfTest() throws Exception {
        Properties metrics = new Properties();
        metrics.setProperty("format", "1"); metrics.setProperty("backend", "windows-job");
        metrics.setProperty("timed.out", "false"); metrics.setProperty("parent.exited", "false");
        metrics.setProperty("exit.code", "0"); metrics.setProperty("processes.total", "8");
        metrics.setProperty("processes.active", "0"); verifyLegacy(metrics);
        metrics.setProperty("processes.active", "1"); expectFailure(() -> verifyLegacy(metrics));
    }

    private static void execute() throws Exception {
        require(windows(), "the executor server differential currently requires Windows");
        Path source = Path.of("").toAbsolutePath().normalize(); requireClean(source);
        String leasePid = requiredEnvironment("WORLDLINE_RUNTIME_LEASE");
        require(leasePid.matches("[0-9]+")
                && ProcessHandle.of(Long.parseLong(leasePid)).filter(ProcessHandle::isAlive).isPresent(),
                "canonical runtime lease is not active");
        Path control = Path.of(requiredEnvironment("WORLDLINE_GATE_CONTROL"))
                .toAbsolutePath().normalize();
        Path lock = control.resolve("official-b173.lock"); require(Files.isRegularFile(lock),
                "canonical runtime lock is absent");
        Path artifact = officialArtifact(source); Path executor = executor();
        require(noForeignRuntime(), "an official Minecraft runtime is already active");

        byte[] baseline;
        try (ExecutorServerWorkspace workspace = ExecutorServerWorkspace.open(
                source, "legacy", artifact)) {
            workspace.prepare(ID);
            Path token = workspace.authorize(lock, leasePid);
            ExecutionEnvironmentManifest environment = environment(workspace.root(), token,
                    leasePid, "legacy");
            baseline = legacy(workspace.root(), command(workspace.root()), environment);
        }

        NativeResult nativeResult;
        try (ExecutorServerWorkspace workspace = ExecutorServerWorkspace.open(
                source, "rust", artifact)) {
            workspace.prepare(ID);
            Path token = workspace.authorize(lock, leasePid);
            ExecutionEnvironmentManifest environment = environment(workspace.root(), token,
                    leasePid, "rust");
            nativeResult = rust(workspace.root(), executor, command(workspace.root()), environment);
        }
        require(Arrays.equals(baseline, nativeResult.evidence),
                "legacy/Rust official server evidence differential");
        String telemetry = ExecutionTelemetryHistory.record(
                source.resolve(".worldline/executor-telemetry"), "server-headless", ID,
                "official-windows-job", nativeResult.verified.telemetry());
        System.out.println("Rust executor server cycle passed; smoke=" + ID + "; request="
                + nativeResult.request + "; evidence=" + ExecutionJson.sha256(baseline)
                + "; telemetry=" + telemetry + "; network=loopback-observed-inherit");
    }

    private static NativeResult rust(Path root, Path executor, List<String> command,
            ExecutionEnvironmentManifest environment) throws Exception {
        Path output = root.resolve(".worldline/executor-server-cycle/rust");
        Files.createDirectories(output.resolve("tmp"));
        WorldlineExecutionPlan plan = plan(root, output, command, environment);
        Path planPath = output.getParent().resolve("rust-plan.json");
        WorldlineExecutionReceipt.Verified result = RustExecutorShadow.execute(executor, planPath, plan);
        require("exited".equals(result.termination()) && Long.valueOf(0).equals(result.exitCode()),
                "Rust server execution failed:\n" + Files.readString(plan.outputs().stderr()));
        require(!result.stdoutTruncated() && !result.stderrTruncated()
                && Long.valueOf(0).equals(result.descendantsRemaining()),
                "Rust server execution did not prove complete output and an empty process tree");
        require(result.runtimeMs() > 0L, "Rust server execution lacks runtime phase telemetry");
        return new NativeResult(evidence(root), result, plan.requestSha256());
    }

    private static byte[] legacy(Path root, List<String> command,
            ExecutionEnvironmentManifest environment) throws Exception {
        Path output = root.resolve(".worldline/executor-server-cycle/legacy");
        Files.createDirectories(output.resolve("tmp")); Path log = output.resolve("console.log");
        Path metrics = output.resolve("metrics.properties");
        List<String> invocation = new ArrayList<>(List.of(WindowsJobBootstrap.ensure().toString(),
                "--memory", Long.toString(MEMORY), "--cpu-rate", "10000", "--active-processes",
                Integer.toString(PROCESSES), "--timeout-seconds", Long.toString(TIMEOUT_MS / 1_000),
                "--parent-pid", Long.toString(ProcessHandle.current().pid()), "--cwd", root.toString(),
                "--log", log.toString(), "--metrics", metrics.toString(), "--"));
        invocation.addAll(command); ProcessBuilder builder = new ProcessBuilder(invocation)
                .directory(root.toFile()); environment.apply(builder); Process process = builder.start();
        require(process.waitFor(12, TimeUnit.MINUTES) && process.exitValue() == 0,
                "legacy server execution failed:\n" + Files.readString(log));
        Properties values = new Properties(); try (var reader = Files.newBufferedReader(metrics)) {
            values.load(reader);
        }
        verifyLegacy(values); return evidence(root);
    }

    private static WorldlineExecutionPlan plan(Path root, Path output, List<String> command,
            ExecutionEnvironmentManifest environment) {
        return new WorldlineExecutionPlan("official-server-headless", root, command,
                environment.environment(), new WorldlineExecutionPlan.Limits(TIMEOUT_MS, MEMORY,
                        PROCESSES, null, 100, 16L << 20, 16L << 20),
                new WorldlineExecutionPlan.Isolation("windows_job", "inherit",
                        ProcessHandle.current().pid()), WorldlineExecutionPlan.Filesystem.inherit(),
                new WorldlineExecutionPlan.Outputs(output, output.resolve("stdout.log"),
                        output.resolve("stderr.log"), output.resolve("metrics.json"),
                        output.resolve("manifest.json"), output.resolve("receipt.json")));
    }

    private static ExecutionEnvironmentManifest environment(Path root, Path token,
            String leasePid, String label) throws Exception {
        Path output = root.resolve(".worldline/executor-server-cycle").resolve(label);
        Path control = output.resolve("control"); Files.createDirectories(control);
        return ExecutionEnvironmentManifest.serverHeadless(output.resolve("tmp"), control,
                token, leasePid);
    }

    private static List<String> command(Path root) {
        return List.of(java(), root.resolve("tools/harness/Gate.java").toString(),
                "--milestone", ID);
    }

    private static byte[] evidence(Path root) throws Exception {
        Path path = root.resolve(".worldline/smokes").resolve(ID).resolve("evidence.txt");
        require(Files.isRegularFile(path), "official server evidence is absent");
        return Files.readAllBytes(path);
    }

    private static Path officialArtifact(Path root) throws Exception {
        Properties values = new Properties(); try (var reader = Files.newBufferedReader(
                root.resolve("artifacts/minecraft-b1.7.3-server.properties"), StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        Path artifact = root.resolve(required(values, "local.path")).normalize();
        require(artifact.startsWith(root.resolve("local")) && Files.isRegularFile(artifact),
                "official server artifact is absent or escaped local/");
        require(Files.size(artifact) == Long.parseLong(required(values, "expected.bytes"))
                && digest(artifact).equals(required(values, "expected.sha256")),
                "official server artifact identity mismatch");
        return artifact;
    }

    private static Path executor() {
        Path path = Path.of(requiredEnvironment("WORLDLINE_EXECUTOR")).toAbsolutePath().normalize();
        require(path.isAbsolute() && Files.isRegularFile(path), "WORLDLINE_EXECUTOR is invalid");
        return path;
    }

    private static void verifyLegacy(Properties values) {
        require("1".equals(values.getProperty("format"))
                && "windows-job".equals(values.getProperty("backend"))
                && "false".equals(values.getProperty("timed.out"))
                && "false".equals(values.getProperty("parent.exited"))
                && "0".equals(values.getProperty("exit.code")), "legacy server metrics drift");
        require(number(values, "processes.total") >= 6L
                && number(values, "processes.active") == 0L,
                "legacy server execution did not drain its process tree");
    }

    private static long number(Properties values, String name) {
        try { long value = Long.parseLong(values.getProperty(name, ""));
            require(value >= 0L, name + " is negative"); return value;
        } catch (NumberFormatException error) { throw new IllegalArgumentException(name, error); }
    }
    private static String required(Properties values, String name) { String value = values.getProperty(name);
        require(value != null && !value.isBlank(), "missing property " + name); return value.trim(); }
    private static String requiredEnvironment(String name) { String value = System.getenv(name);
        require(value != null && !value.isBlank(), "missing " + name); return value; }
    private static String digest(Path path) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path))); }
    private static String java() { return Path.of(System.getProperty("java.home"), "bin", "java.exe").toString(); }
    private static boolean windows() { return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win"); }
    private static boolean noForeignRuntime() { return ProcessHandle.allProcesses().noneMatch(process -> {
        String line = process.info().commandLine().orElse("").toLowerCase(Locale.ROOT).replace('\\', '/');
        return line.contains("tools/smoke/") || line.contains("runclient") || line.contains("minecraft-b1.7.3-"); }); }
    private static void requireClean(Path root) throws Exception { Process process = new ProcessBuilder(
            "git", "status", "--porcelain=v1", "--untracked-files=all").directory(root.toFile())
            .redirectErrorStream(true).start(); require(process.waitFor(30, TimeUnit.SECONDS)
                    && process.exitValue() == 0 && new String(process.getInputStream().readAllBytes(),
                            StandardCharsets.UTF_8).isBlank(), "server qualification requires a clean commit"); }
    private static void expectFailure(Checked action) throws Exception { try { action.run();
        throw new IllegalStateException("invalid server evidence was accepted");
    } catch (IllegalArgumentException expected) { } }
    private static void require(boolean value, String message) { if (!value) throw new IllegalArgumentException(message); }
    @FunctionalInterface private interface Checked { void run() throws Exception; }
    private record NativeResult(byte[] evidence, WorldlineExecutionReceipt.Verified verified,
            String request) { }
}
