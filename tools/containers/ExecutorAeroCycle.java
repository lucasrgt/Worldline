import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/** Qualifies the pinned Aero visual oracle through legacy and Rust containment. */
public final class ExecutorAeroCycle {
    private static final String ID = "m778-aero-culling-visual-integrity";
    private static final String REVISION = "091940b8e0cbd839169d8eb977992dfee83faff6";
    private static final long TIMEOUT_MS = 7_200_000L;
    private static final long MEMORY = 12L << 30;
    private static final int PROCESSES = 64;

    private ExecutorAeroCycle() { }

    public static void main(String[] arguments) {
        try { require(arguments.length == 0, "usage: ExecutorAeroCycle"); execute(); }
        catch (Exception error) {
            System.err.println("executor Aero cycle failed: " + error.getMessage()); System.exit(1);
        }
    }

    static void selfTest() throws Exception {
        Properties metrics = new Properties();
        metrics.setProperty("format", "1"); metrics.setProperty("backend", "windows-job");
        metrics.setProperty("timed.out", "false"); metrics.setProperty("parent.exited", "false");
        metrics.setProperty("exit.code", "0"); metrics.setProperty("processes.total", "6");
        metrics.setProperty("processes.active", "0"); verifyLegacy(metrics);
        metrics.setProperty("processes.active", "1"); expectFailure(() -> verifyLegacy(metrics));
        ExecutorAeroWorkspace.selfTest();
    }

    private static void execute() throws Exception {
        require(windows(), "the executor Aero differential currently requires Windows");
        Path source = Path.of("").toAbsolutePath().normalize(); requireClean(source);
        Path donor = source.resolve("local/candidates/aero-model-lib");
        RuntimeLeaseIdentity lease = RuntimeLeaseIdentity.environment();
        Path control = Path.of(requiredEnvironment("WORLDLINE_GATE_CONTROL")).toAbsolutePath().normalize();
        Path lock = control.resolve("official-b173.lock");
        require(Files.isRegularFile(lock), "canonical runtime lock is absent");
        Path executor = executor(); require(noForeignRuntime(), "an official Minecraft runtime is already active");

        byte[] baseline;
        try (ExecutorAeroWorkspace workspace = ExecutorAeroWorkspace.open(source, "legacy", donor, REVISION)) {
            workspace.prepare(); Path token = workspace.authorize(lock, lease);
            ExecutionEnvironmentManifest environment = environment(
                    workspace.root(), token, lease, "legacy");
            baseline = legacy(workspace.root(), command(workspace.root()), environment);
        }

        NativeResult nativeResult;
        try (ExecutorAeroWorkspace workspace = ExecutorAeroWorkspace.open(source, "rust", donor, REVISION)) {
            workspace.prepare(); Path token = workspace.authorize(lock, lease);
            ExecutionEnvironmentManifest environment = environment(
                    workspace.root(), token, lease, "rust");
            nativeResult = rust(workspace.root(), executor, command(workspace.root()), environment);
        }
        require(Arrays.equals(baseline, nativeResult.evidence),
                "legacy/Rust Aero visual evidence differential");
        String telemetry = ExecutionTelemetryHistory.record(source.resolve(".worldline/executor-telemetry"),
                "gui-aero", ID, "official-windows-job", nativeResult.verified.telemetry());
        System.out.println("Rust executor Aero cycle passed; smoke=" + ID + "; request="
                + nativeResult.request + "; evidence=" + ExecutionJson.sha256(baseline)
                + "; telemetry=" + telemetry + "; checkout=" + REVISION + "; network=inherit");
    }

    private static NativeResult rust(Path root, Path executor, List<String> command,
            ExecutionEnvironmentManifest environment) throws Exception {
        Path output = root.resolve(".worldline/executor-aero-cycle/rust");
        Files.createDirectories(output.resolve("tmp")); Files.createDirectories(output.resolve("gradle-home"));
        WorldlineExecutionPlan plan = plan(root, output, command, environment);
        Path planPath = output.getParent().resolve("rust-plan.json");
        WorldlineExecutionReceipt.Verified result = RustExecutorShadow.execute(executor, planPath, plan);
        require("exited".equals(result.termination()) && Long.valueOf(0).equals(result.exitCode()),
                "Rust Aero execution failed:\n" + Files.readString(plan.outputs().stderr()));
        require(!result.stdoutTruncated() && !result.stderrTruncated()
                && Long.valueOf(0).equals(result.descendantsRemaining()),
                "Rust Aero execution did not prove complete output and an empty process tree");
        require(result.runtimeMs() > 0L, "Rust Aero execution lacks runtime phase telemetry");
        return new NativeResult(evidence(root), result, plan.requestSha256());
    }

    private static byte[] legacy(Path root, List<String> command,
            ExecutionEnvironmentManifest environment) throws Exception {
        Path output = root.resolve(".worldline/executor-aero-cycle/legacy");
        Files.createDirectories(output.resolve("tmp")); Files.createDirectories(output.resolve("gradle-home"));
        Path log = output.resolve("console.log"), metrics = output.resolve("metrics.properties");
        List<String> invocation = new ArrayList<>(List.of(WindowsJobBootstrap.ensure().toString(),
                "--memory", Long.toString(MEMORY), "--cpu-rate", "10000", "--active-processes",
                Integer.toString(PROCESSES), "--timeout-seconds", Long.toString(TIMEOUT_MS / 1_000),
                "--parent-pid", Long.toString(ProcessHandle.current().pid()), "--cwd", root.toString(),
                "--log", log.toString(), "--metrics", metrics.toString(), "--"));
        invocation.addAll(command); ProcessBuilder builder = new ProcessBuilder(invocation)
                .directory(root.toFile()); environment.apply(builder); Process process = builder.start();
        waitLegacy(process, log);
        Properties values = new Properties(); try (var reader = Files.newBufferedReader(metrics)) {
            values.load(reader);
        }
        verifyLegacy(values); return evidence(root);
    }

    private static void waitLegacy(Process process, Path log) throws Exception {
        try {
            if (!process.waitFor(TIMEOUT_MS / 1_000 + 600, TimeUnit.SECONDS)) {
                terminate(process); throw new IllegalStateException("legacy Aero execution timed out");
            }
            require(process.exitValue() == 0, "legacy Aero execution failed:\n" + Files.readString(log));
        } catch (InterruptedException error) {
            terminate(process); Thread.currentThread().interrupt(); throw error;
        }
    }

    private static void terminate(Process process) throws Exception {
        List<ProcessHandle> descendants = process.descendants().sorted(
                java.util.Comparator.comparingInt((ProcessHandle child) ->
                        depth(child, process.pid())).reversed()).toList();
        descendants.forEach(ProcessHandle::destroyForcibly); process.destroyForcibly();
        require(process.waitFor(10, TimeUnit.SECONDS), "Aero coordinator did not terminate");
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (descendants.stream().anyMatch(ProcessHandle::isAlive) && System.nanoTime() < deadline)
            Thread.sleep(20L);
        require(descendants.stream().noneMatch(ProcessHandle::isAlive),
                "Aero coordinator descendants remained active");
    }

    private static int depth(ProcessHandle process, long ancestor) {
        int value = 0;
        for (ProcessHandle current = process; current.pid() != ancestor; value++)
            current = current.parent().orElseThrow();
        return value;
    }

    private static WorldlineExecutionPlan plan(Path root, Path output, List<String> command,
            ExecutionEnvironmentManifest environment) {
        return new WorldlineExecutionPlan("official-gui-aero", root, command, environment.environment(),
                new WorldlineExecutionPlan.Limits(TIMEOUT_MS, MEMORY, PROCESSES, null, 100,
                        64L << 20, 64L << 20),
                new WorldlineExecutionPlan.Isolation("windows_job", "inherit",
                        ProcessHandle.current().pid()), WorldlineExecutionPlan.Filesystem.inherit(),
                new WorldlineExecutionPlan.Outputs(output, output.resolve("stdout.log"),
                        output.resolve("stderr.log"), output.resolve("metrics.json"),
                        output.resolve("manifest.json"), output.resolve("receipt.json")));
    }

    private static ExecutionEnvironmentManifest environment(Path root, Path token,
            RuntimeLeaseIdentity lease, String label) throws Exception {
        Path output = root.resolve(".worldline/executor-aero-cycle").resolve(label);
        Path control = output.resolve("control"); Files.createDirectories(control);
        return ExecutionEnvironmentManifest.guiAero(output.resolve("tmp"), output.resolve("gradle-home"),
                control, token, lease.pid(), lease.started());
    }
    private static List<String> command(Path root) {
        return List.of(java(), root.resolve("tools/harness/Gate.java").toString(), "--milestone", ID);
    }
    private static byte[] evidence(Path root) throws Exception {
        Path path = root.resolve(".worldline/build/smoke").resolve(ID).resolve("evidence.txt");
        require(Files.isRegularFile(path), "Aero visual evidence is absent");
        byte[] bytes = Files.readAllBytes(path); String text = new String(bytes, StandardCharsets.UTF_8);
        require(text.contains("visual.changed.pixels=0,visual.max.delta=0")
                && text.contains("work.every-session-reduced=true")
                && text.contains("signature=a7473c0e9e1992edf4dc77c15ea1ab929c5876768b668b2be84a05ef559f674a\n"),
                "Aero visual evidence contract drift");
        return bytes;
    }

    private static void verifyLegacy(Properties values) {
        require("1".equals(values.getProperty("format"))
                && "windows-job".equals(values.getProperty("backend"))
                && "false".equals(values.getProperty("timed.out"))
                && "false".equals(values.getProperty("parent.exited"))
                && "0".equals(values.getProperty("exit.code")), "legacy Aero metrics drift");
        require(number(values, "processes.total") >= 6L && number(values, "processes.active") == 0L,
                "legacy Aero execution did not drain its process tree");
    }
    private static long number(Properties values, String name) { try {
        long value = Long.parseLong(values.getProperty(name, "")); require(value >= 0L, name + " is negative");
        return value; } catch (NumberFormatException error) { throw new IllegalArgumentException(name, error); } }
    private static Path executor() { Path path = Path.of(requiredEnvironment("WORLDLINE_EXECUTOR"))
            .toAbsolutePath().normalize(); require(Files.isRegularFile(path), "WORLDLINE_EXECUTOR is invalid");
        return path; }
    private static String requiredEnvironment(String name) { String value = System.getenv(name);
        require(value != null && !value.isBlank(), "missing " + name); return value; }
    private static String java() { return Path.of(System.getProperty("java.home"), "bin", "java.exe").toString(); }
    private static boolean windows() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
    }
    private static boolean noForeignRuntime() { return ProcessHandle.allProcesses().noneMatch(process -> {
        String line = process.info().commandLine().orElse("").toLowerCase(Locale.ROOT).replace('\\', '/');
        return line.contains("tools/smoke/") || line.contains("runclient")
                || line.contains("minecraft-b1.7.3-"); }); }
    private static void requireClean(Path root) throws Exception { Process process = new ProcessBuilder(
            "git", "status", "--porcelain=v1", "--untracked-files=all").directory(root.toFile())
            .redirectErrorStream(true).start(); require(process.waitFor(30, TimeUnit.SECONDS)
                    && process.exitValue() == 0 && new String(process.getInputStream().readAllBytes(),
                            StandardCharsets.UTF_8).isBlank(), "Aero qualification requires a clean commit"); }
    private static void expectFailure(Checked action) throws Exception { try { action.run();
        throw new IllegalStateException("invalid Aero evidence was accepted");
    } catch (IllegalArgumentException expected) { } }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
    @FunctionalInterface private interface Checked { void run() throws Exception; }
    private record NativeResult(byte[] evidence, WorldlineExecutionReceipt.Verified verified,
            String request) { }
}
