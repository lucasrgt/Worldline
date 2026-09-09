import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/** Qualifies an official headless-server behavior matrix through legacy and Rust containment. */
public final class ExecutorServerCycle {
    private static final List<String> IDS = List.of(
            "m20-server-bootstrap", "m22-multiplayer-wire", "m130-nether-login",
            "m135-player-respawn", "m629-door-sound-event",
            "m702-powered-rail-slope-propagation");
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
        require(IDS.size() == 6 && IDS.stream().distinct().count() == IDS.size()
                && IDS.get(0).equals("m20-server-bootstrap")
                && IDS.get(1).equals("m22-multiplayer-wire")
                && IDS.get(2).equals("m130-nether-login")
                && IDS.get(3).equals("m135-player-respawn")
                && IDS.get(4).equals("m629-door-sound-event")
                && IDS.get(5).equals("m702-powered-rail-slope-propagation"),
                "server matrix drifted");
        Properties metrics = new Properties();
        metrics.setProperty("format", "1"); metrics.setProperty("backend", "windows-job");
        metrics.setProperty("timed.out", "false"); metrics.setProperty("parent.exited", "false");
        metrics.setProperty("exit.code", "0"); metrics.setProperty("processes.total", "8");
        metrics.setProperty("processes.active", "0"); verifyLegacy(metrics);
        metrics.setProperty("processes.active", "1"); expectFailure(() -> verifyLegacy(metrics));
        ExecutorServerFailureCapture.selfTest();
    }

    private static void execute() throws Exception {
        require(windows(), "the executor server differential currently requires Windows");
        Path source = Path.of("").toAbsolutePath().normalize(); requireClean(source);
        RuntimeLeaseIdentity lease = RuntimeLeaseIdentity.environment();
        Path control = Path.of(requiredEnvironment("WORLDLINE_GATE_CONTROL"))
                .toAbsolutePath().normalize();
        Path lock = control.resolve("official-b173.lock"); require(Files.isRegularFile(lock),
                "canonical runtime lock is absent");
        Path artifact = officialArtifact(source); Path executor = executor();
        require(noForeignRuntime(), "an official Minecraft runtime is already active");

        Map<String, byte[]> baselines = new LinkedHashMap<>();
        try (ExecutorServerWorkspace workspace = ExecutorServerWorkspace.open(
                source, "legacy", artifact)) {
            workspace.prepare();
            Path token = workspace.authorize(lock, lease);
            for (String id : IDS) {
                ExecutionEnvironmentManifest environment = environment(workspace.root(), token,
                        lease, "legacy", id);
                try { baselines.put(id, legacy(workspace.root(), command(workspace.root(), id),
                            environment, id));
                } catch (Exception error) {
                    ExecutorServerFailureCapture.capture(
                            source, workspace.root(), "legacy", id, error); throw error;
                }
            }
        }

        List<String> observations = new ArrayList<>();
        try (ExecutorServerWorkspace workspace = ExecutorServerWorkspace.open(
                source, "rust", artifact)) {
            workspace.prepare();
            Path token = workspace.authorize(lock, lease);
            for (String id : IDS) {
                ExecutionEnvironmentManifest environment = environment(workspace.root(), token,
                        lease, "rust", id);
                NativeResult result;
                try { result = rust(workspace.root(), executor,
                            command(workspace.root(), id), environment, id);
                } catch (Exception error) {
                    ExecutorServerFailureCapture.capture(
                            source, workspace.root(), "rust", id, error); throw error;
                }
                byte[] baseline = baselines.get(id);
                require(Arrays.equals(baseline, result.evidence),
                        "legacy/Rust official server evidence differential: " + id);
                String telemetry = ExecutionTelemetryHistory.record(
                        source.resolve(".worldline/executor-telemetry"), "server-headless", id,
                        "official-windows-job", result.verified.telemetry());
                observations.add(id + ":request=" + result.request + ":evidence="
                        + ExecutionJson.sha256(baseline) + ":telemetry=" + telemetry);
            }
        }
        require(observations.size() == IDS.size(), "server matrix observation count drifted");
        System.out.println("Rust executor server matrix passed; count=" + observations.size()
                + "; observations=" + String.join(",", observations)
                + "; network=loopback-observed-inherit");
    }

    private static NativeResult rust(Path root, Path executor, List<String> command,
            ExecutionEnvironmentManifest environment, String id) throws Exception {
        Path output = root.resolve(".worldline/executor-server-cycle/rust").resolve(id);
        Files.createDirectories(output.resolve("tmp"));
        WorldlineExecutionPlan plan = plan(root, output, command, environment, id);
        Path planPath = output.resolveSibling(id + "-plan.json");
        WorldlineExecutionReceipt.Verified result = RustExecutorShadow.execute(executor, planPath, plan);
        require("exited".equals(result.termination()) && Long.valueOf(0).equals(result.exitCode()),
                "Rust server execution failed:\n" + Files.readString(plan.outputs().stderr()));
        require(!result.stdoutTruncated() && !result.stderrTruncated()
                && Long.valueOf(0).equals(result.descendantsRemaining()),
                "Rust server execution did not prove complete output and an empty process tree");
        require(result.runtimeMs() > 0L, "Rust server execution lacks runtime phase telemetry");
        return new NativeResult(evidence(root, id), result, plan.requestSha256());
    }

    private static byte[] legacy(Path root, List<String> command,
            ExecutionEnvironmentManifest environment, String id) throws Exception {
        Path output = root.resolve(".worldline/executor-server-cycle/legacy").resolve(id);
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
        verifyLegacy(values); return evidence(root, id);
    }

    private static WorldlineExecutionPlan plan(Path root, Path output, List<String> command,
            ExecutionEnvironmentManifest environment, String id) {
        return new WorldlineExecutionPlan("official-server-headless-" + id, root, command,
                environment.environment(), new WorldlineExecutionPlan.Limits(TIMEOUT_MS, MEMORY,
                        PROCESSES, null, 100, 16L << 20, 16L << 20),
                new WorldlineExecutionPlan.Isolation("windows_job", "inherit",
                        ProcessHandle.current().pid()), WorldlineExecutionPlan.Filesystem.inherit(),
                new WorldlineExecutionPlan.Outputs(output, output.resolve("stdout.log"),
                        output.resolve("stderr.log"), output.resolve("metrics.json"),
                        output.resolve("manifest.json"), output.resolve("receipt.json")));
    }

    private static ExecutionEnvironmentManifest environment(Path root, Path token,
            RuntimeLeaseIdentity lease, String label, String id) throws Exception {
        require(IDS.contains(id), "server matrix environment escaped the closed set");
        Path output = root.resolve(".worldline/executor-server-cycle").resolve(label).resolve(id);
        Path control = output.resolve("control"); Files.createDirectories(control);
        return ExecutionEnvironmentManifest.serverHeadless(output.resolve("tmp"), control,
                token, lease.pid(), lease.started());
    }

    private static List<String> command(Path root, String id) {
        require(IDS.contains(id), "server matrix command escaped the closed set");
        return List.of(java(), root.resolve("tools/harness/Gate.java").toString(),
                "--milestone", id);
    }

    private static byte[] evidence(Path root, String id) throws Exception {
        require(IDS.contains(id), "server matrix evidence escaped the closed set");
        Path path = root.resolve(".worldline/smokes").resolve(id).resolve("evidence.txt");
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
            ContainerHexDigest.sha256().digest(Files.readAllBytes(path))); }
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
