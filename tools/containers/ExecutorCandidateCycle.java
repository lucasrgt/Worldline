import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/** Qualifies a closed real Candidate build matrix through legacy and Rust containment. */
public final class ExecutorCandidateCycle {
    private static final List<String> IDS = List.of("m702-powered-rail-slope-propagation",
            "m135-player-respawn", "m629-door-sound-event");
    private static final long TIMEOUT_MS = 1_200_000L;
    private static final long MEMORY = 6L << 30;
    private static final int PROCESSES = 64;

    private ExecutorCandidateCycle() { }

    public static void main(String[] arguments) {
        try {
            require(arguments.length == 0, "usage: ExecutorCandidateCycle"); execute();
        } catch (Exception error) {
            System.err.println("executor Candidate cycle failed: " + error.getMessage()); System.exit(1);
        }
    }

    static void selfTest() throws Exception {
        require(IDS.size() == 3 && IDS.stream().distinct().count() == IDS.size()
                && IDS.get(0).equals("m702-powered-rail-slope-propagation")
                && IDS.get(1).equals("m135-player-respawn")
                && IDS.get(2).equals("m629-door-sound-event"), "Candidate matrix drifted");
        Properties metrics = new Properties();
        metrics.setProperty("format", "1"); metrics.setProperty("backend", "windows-job");
        metrics.setProperty("timed.out", "false"); metrics.setProperty("parent.exited", "false");
        metrics.setProperty("exit.code", "0"); metrics.setProperty("processes.total", "3");
        metrics.setProperty("processes.active", "0"); verifyLegacy(metrics);
        metrics.setProperty("processes.active", "1"); expectFailure(() -> verifyLegacy(metrics));
        CandidateBuildEvidence.selfTest();
    }

    private static void execute() throws Exception {
        require(windows(), "the executor Candidate differential currently requires Windows");
        Path source = Path.of("").toAbsolutePath().normalize(); requireClean(source);
        Path executor = executor(); Map<String, CandidateBuildEvidence.Evidence> baselines =
                new LinkedHashMap<>();
        try (ExecutorCandidateWorkspace workspace = ExecutorCandidateWorkspace.open(source, "legacy")) {
            for (String id : IDS) {
                Path output = output(workspace.root(), "legacy", id);
                ExecutionEnvironmentManifest environment = ExecutionEnvironmentManifest.candidate(
                        output.resolve("tmp"), output.resolve("control"));
                legacy(workspace.root(), command(workspace.root(), id), output, environment);
                baselines.put(id, evidence(workspace.root(), id));
            }
        }

        Map<String, NativeResult> nativeResults = new LinkedHashMap<>();
        try (ExecutorCandidateWorkspace workspace = ExecutorCandidateWorkspace.open(source, "rust")) {
            for (String id : IDS) {
                Path output = output(workspace.root(), "rust", id);
                ExecutionEnvironmentManifest environment = ExecutionEnvironmentManifest.candidate(
                        output.resolve("tmp"), output.resolve("control"));
                NativeResult result = rust(workspace.root(), executor,
                        command(workspace.root(), id), output, environment, id);
                require(baselines.get(id).equals(result.evidence),
                        "legacy/Rust Candidate build evidence differential: " + id);
                nativeResults.put(id, result);
            }
        }
        Path telemetryRoot = source.resolve(".worldline/executor-telemetry");
        List<String> observations = new ArrayList<>(); long peak = 0L;
        for (String id : IDS) {
            NativeResult result = nativeResults.get(id); CandidateBuildEvidence.Evidence evidence =
                    baselines.get(id);
            String telemetry = ExecutionTelemetryHistory.record(telemetryRoot, "candidate", id,
                    "candidate-worker", result.verified.telemetry());
            peak = Math.max(peak, result.verified.telemetry().peakMemoryBytes());
            observations.add(id + ":request=" + result.request + ":evidence=" + evidence.digest()
                    + ":files=" + evidence.files() + ":bytes=" + evidence.bytes()
                    + ":telemetry=" + telemetry);
        }
        Admission admission = admission(telemetryRoot.resolve("admission.properties"),
                peak);
        long configured = configuredWorker(source), effective = Math.max(configured, admission.minimum);
        require(effective >= configured, "telemetry admission increased Candidate width");
        System.out.println("Rust executor Candidate matrix passed; count=" + observations.size()
                + "; observations=" + String.join(",", observations) + "; observed-memory="
                + admission.observed + "; admission-floor=" + admission.minimum
                + "; effective-worker=" + effective + "; lower-only=true");
    }

    private static NativeResult rust(Path root, Path executor, List<String> command, Path output,
            ExecutionEnvironmentManifest environment, String id) throws Exception {
        Files.createDirectories(output.resolve("tmp"));
        WorldlineExecutionPlan plan = plan(root, output, command, environment, id);
        Path planPath = output.getParent().resolve(id + "-plan.json");
        WorldlineExecutionReceipt.Verified result = RustExecutorShadow.execute(executor, planPath, plan);
        require("exited".equals(result.termination()) && Long.valueOf(0).equals(result.exitCode()),
                "Rust Candidate execution failed:\n" + Files.readString(plan.outputs().stderr()));
        require(!result.stdoutTruncated() && !result.stderrTruncated()
                && Long.valueOf(0).equals(result.descendantsRemaining()),
                "Rust Candidate execution did not prove complete output and an empty process tree");
        require(result.runtimeMs() > 0L && result.telemetry().peakMemoryBytes() != null,
                "Rust Candidate execution lacks runtime memory telemetry");
        return new NativeResult(evidence(root, id), result, plan.requestSha256());
    }

    private static void legacy(Path root, List<String> command, Path output,
            ExecutionEnvironmentManifest environment) throws Exception {
        Files.createDirectories(output.resolve("tmp")); Path log = output.resolve("console.log");
        Path metrics = output.resolve("metrics.properties");
        List<String> invocation = new ArrayList<>(List.of(WindowsJobBootstrap.ensure().toString(),
                "--memory", Long.toString(MEMORY), "--cpu-rate", "10000", "--active-processes",
                Integer.toString(PROCESSES), "--timeout-seconds", Long.toString(TIMEOUT_MS / 1_000),
                "--parent-pid", Long.toString(ProcessHandle.current().pid()), "--cwd", root.toString(),
                "--log", log.toString(), "--metrics", metrics.toString(), "--"));
        invocation.addAll(command); ProcessBuilder builder = new ProcessBuilder(invocation)
                .directory(root.toFile()); environment.apply(builder); Process process = builder.start();
        require(process.waitFor(25, TimeUnit.MINUTES) && process.exitValue() == 0,
                "legacy Candidate execution failed:\n" + Files.readString(log));
        Properties values = new Properties(); try (var reader = Files.newBufferedReader(metrics)) {
            values.load(reader);
        }
        verifyLegacy(values);
    }

    private static WorldlineExecutionPlan plan(Path root, Path output, List<String> command,
            ExecutionEnvironmentManifest environment, String id) {
        require(IDS.contains(id), "Candidate plan escaped the closed matrix");
        return new WorldlineExecutionPlan("candidate-static-" + id, root, command,
                environment.environment(),
                new WorldlineExecutionPlan.Limits(TIMEOUT_MS, MEMORY, PROCESSES, null, 100,
                        16L << 20, 16L << 20),
                new WorldlineExecutionPlan.Isolation("windows_job", "inherit",
                        ProcessHandle.current().pid()), WorldlineExecutionPlan.Filesystem.inherit(),
                new WorldlineExecutionPlan.Outputs(output, output.resolve("stdout.log"),
                        output.resolve("stderr.log"), output.resolve("metrics.json"),
                        output.resolve("manifest.json"), output.resolve("receipt.json")));
    }

    private static Path output(Path root, String label, String id) {
        require(IDS.contains(id), "Candidate output escaped the closed matrix");
        return root.resolve(".worldline/executor-candidate-cycle").resolve(label).resolve(id);
    }
    private static List<String> command(Path root, String id) {
        require(IDS.contains(id), "Candidate command escaped the closed matrix");
        return List.of(java(), root.resolve("tools/harness/Gate.java").toString(), "--candidate", id);
    }
    private static CandidateBuildEvidence.Evidence evidence(Path root, String id) throws Exception {
        require(IDS.contains(id), "Candidate evidence escaped the closed matrix");
        return CandidateBuildEvidence.read(root.resolve(".worldline/candidates").resolve(id));
    }

    private static Admission admission(Path path, Long currentPeak) throws Exception {
        require(currentPeak != null && currentPeak > 0L && Files.isRegularFile(path),
                "Candidate admission evidence is absent");
        String text = Files.readString(path, StandardCharsets.UTF_8);
        require(text.endsWith("\n") && !text.contains("\r"), "Candidate admission encoding drift");
        Map<String, String> values = new LinkedHashMap<>();
        for (String line : text.substring(0, text.length() - 1).split("\n", -1)) {
            int split = line.indexOf('='); require(split > 0, "invalid Candidate admission row");
            require(values.put(line.substring(0, split), line.substring(split + 1)) == null,
                    "duplicate Candidate admission field");
        }
        require(values.keySet().equals(new LinkedHashSet<>(List.of("schema", "os", "architecture",
                "runtime.capability", "sample.count", "observed.peak.memory.bytes",
                "minimum.worker.memory.bytes", "direction"))), "Candidate admission fields drifted");
        require("worldline.telemetry-admission.v1".equals(values.get("schema"))
                && "candidate-worker".equals(values.get("runtime.capability"))
                && "lower-only".equals(values.get("direction")), "Candidate admission policy drift");
        require(values.get("os").equals(ExecutionTelemetrySample.hostToken(System.getProperty("os.name")))
                && values.get("architecture").equals(ExecutionTelemetrySample.hostToken(
                        System.getProperty("os.arch"))), "Candidate admission host drift");
        long samples = number(values, "sample.count"), observed = number(values,
                "observed.peak.memory.bytes"), minimum = number(values, "minimum.worker.memory.bytes");
        require(samples > 0L && observed >= currentPeak
                && minimum == Math.addExact(observed, Math.max(1L, Math.floorDiv(observed, 4L))),
                "Candidate admission floor drift");
        return new Admission(observed, minimum);
    }

    private static long configuredWorker(Path root) throws Exception {
        Properties values = new Properties(); try (var reader = Files.newBufferedReader(
                root.resolve("coordination/swarm/parallelism.properties"), StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return Math.multiplyExact(number(values, "candidate.memory.gib"), 1L << 30);
    }
    private static long number(Map<String, String> values, String name) {
        try { long value = Long.parseLong(values.get(name)); require(value >= 0L, name + " is negative");
            return value; } catch (NumberFormatException error) { throw new IllegalArgumentException(name, error); }
    }
    private static long number(Properties values, String name) {
        try { long value = Long.parseLong(values.getProperty(name, "")); require(value >= 0L,
            name + " is negative"); return value; } catch (NumberFormatException error) {
            throw new IllegalArgumentException(name, error); }
    }
    private static void verifyLegacy(Properties values) {
        require("1".equals(values.getProperty("format"))
                && "windows-job".equals(values.getProperty("backend"))
                && "false".equals(values.getProperty("timed.out"))
                && "false".equals(values.getProperty("parent.exited"))
                && "0".equals(values.getProperty("exit.code")), "legacy Candidate metrics drift");
        require(number(values, "processes.total") >= 2L && number(values, "processes.active") == 0L,
                "legacy Candidate execution did not drain its process tree");
    }
    private static Path executor() { Path path = Path.of(requiredEnvironment("WORLDLINE_EXECUTOR"))
            .toAbsolutePath().normalize(); require(Files.isRegularFile(path), "WORLDLINE_EXECUTOR is invalid");
        return path; }
    private static String requiredEnvironment(String name) { String value = System.getenv(name);
        require(value != null && !value.isBlank(), "missing " + name); return value; }
    private static String java() { return Path.of(System.getProperty("java.home"), "bin", "java.exe").toString(); }
    private static boolean windows() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
    }
    private static void requireClean(Path root) throws Exception { Process process = new ProcessBuilder(
            "git", "status", "--porcelain=v1", "--untracked-files=all").directory(root.toFile())
            .redirectErrorStream(true).start(); require(process.waitFor(30, TimeUnit.SECONDS)
                    && process.exitValue() == 0 && new String(process.getInputStream().readAllBytes(),
                            StandardCharsets.UTF_8).isBlank(), "Candidate qualification requires a clean commit"); }
    private static void expectFailure(Checked action) throws Exception { try { action.run();
        throw new IllegalStateException("invalid Candidate evidence was accepted");
    } catch (IllegalArgumentException expected) { } }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
    @FunctionalInterface private interface Checked { void run() throws Exception; }
    private record NativeResult(CandidateBuildEvidence.Evidence evidence,
            WorldlineExecutionReceipt.Verified verified, String request) { }
    private record Admission(long observed, long minimum) { }
}
