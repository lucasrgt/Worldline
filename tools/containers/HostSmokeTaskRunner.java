import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Executes one admitted host-pool task through a selected native containment backend. */
final class HostSmokeTaskRunner {
    private static final String POOL_SECRET = "WORLDLINE_RUNTIME_POOL_TOKEN";

    private HostSmokeTaskRunner() { }

    static void execute(Request request) throws Exception {
        request.validate();
        if (request.backend.equals("rust-windows-job")) runRust(request);
        else runLegacy(request);
    }

    static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-host-task-").toAbsolutePath().normalize();
        try {
            Path output = root.resolve("output"), temporary = output.resolve("tmp");
            Files.createDirectories(temporary);
            Request request = new Request(root, "rust-windows-job", "server-test", 30, "192m",
                    3L << 30, 0.5, 64, 8, output, temporary, output.resolve("gradle"),
                    output.resolve("pool.properties"), "never-serialize-this-secret", null,
                    List.of(java(), "-version"));
            WorldlineExecutionPlan plan = plan(request);
            String canonical = new String(plan.canonicalBytes(), StandardCharsets.UTF_8);
            require(canonical.contains(POOL_SECRET), "pool capability name is absent from the plan");
            require(!canonical.contains(request.poolSecret), "pool capability value entered the plan");
            require(plan.isolation().backend().equals("windows_job"), "Rust host isolation drift");
        } finally { try (var paths = Files.walk(root)) {
            for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList())
                Files.deleteIfExists(path);
        } }
    }

    private static void runRust(Request request) throws Exception {
        Path configured = Path.of(requiredEnvironment("WORLDLINE_EXECUTOR"))
                .toAbsolutePath().normalize();
        WorldlineExecutionPlan plan = plan(request);
        WorldlineExecutionReceipt.Verified result = RustExecutorShadow.execute(configured,
                request.output.resolve("execution-plan.json"), plan,
                Map.of(POOL_SECRET, request.poolSecret));
        require("exited".equals(result.termination()) && Long.valueOf(0).equals(result.exitCode()),
                "Rust task failed; see " + request.output);
        require(!result.stdoutTruncated() && !result.stderrTruncated()
                        && Long.valueOf(0).equals(result.descendantsRemaining()),
                "Rust task did not prove complete output and an empty process tree");
    }

    private static WorldlineExecutionPlan plan(Request request) throws Exception {
        Map<String, String> set = environment(request);
        List<String> inherit = List.of("PATH", "PATHEXT", "SYSTEMROOT", "WINDIR", POOL_SECRET);
        Path output = request.output;
        return new WorldlineExecutionPlan("host-smoke-" + request.id, request.root, request.command,
                new WorldlineExecutionPlan.Environment(inherit, List.of(), set),
                new WorldlineExecutionPlan.Limits(request.timeoutSeconds * 1_000L,
                        request.memoryLimitBytes, request.processLimit, null,
                        cpuPercent(request), 16L << 20, 16L << 20),
                new WorldlineExecutionPlan.Isolation("windows_job", "inherit",
                        ProcessHandle.current().pid()), WorldlineExecutionPlan.Filesystem.inherit(),
                new WorldlineExecutionPlan.Outputs(output, output.resolve("console.log"),
                        output.resolve("error.log"), output.resolve("metrics.json"),
                        output.resolve("manifest.json"), output.resolve("receipt.json")));
    }

    private static void runLegacy(Request request) throws Exception {
        Path log = request.output.resolve("console.log");
        List<String> wrapped;
        if (request.backend.equals("windows-job")) {
            wrapped = new ArrayList<>(List.of(
                    request.root.resolve(".worldline/tools/WindowsJobRunner.exe").toString(),
                    "--memory", Long.toString(request.memoryLimitBytes), "--cpu-rate",
                    Integer.toString(cpuPercent(request) * 100), "--active-processes",
                    Integer.toString(request.processLimit), "--timeout-seconds",
                    Integer.toString(request.timeoutSeconds), "--parent-pid",
                    Long.toString(ProcessHandle.current().pid()), "--cwd", request.root.toString(),
                    "--log", log.toString(), "--metrics",
                    request.output.resolve("metrics.properties").toString(), "--"));
        } else {
            wrapped = new ArrayList<>(List.of("bash", "tools/containers/linux-runtime.sh", "run",
                    "--mode", request.backend.equals("linux-sandbox") ? "sandbox" : "cgroup",
                    "--memory", Long.toString(request.memoryLimitBytes), "--cpu-percent",
                    Integer.toString((int) Math.ceil(request.cpuUnits * 100)), "--processes",
                    Integer.toString(request.processLimit), "--timeout-seconds",
                    Integer.toString(request.timeoutSeconds), "--cwd", request.root.toString(),
                    "--log", log.toString(), "--metrics",
                    request.output.resolve("metrics.properties").toString(), "--"));
        }
        wrapped.addAll(request.command);
        ProcessBuilder builder = new ProcessBuilder(wrapped).directory(request.root.toFile())
                .redirectErrorStream(true).redirectOutput(request.output.resolve("launcher.log").toFile());
        builder.environment().putAll(environment(request));
        builder.environment().put(POOL_SECRET, request.poolSecret);
        Process process = builder.start();
        boolean finished = process.waitFor(request.timeoutSeconds + 15L, TimeUnit.SECONDS);
        if (!finished) ProcessTreeTermination.kill(process, "host smoke task");
        require(finished, "timeout after " + request.timeoutSeconds + "s");
        require(process.exitValue() == 0, "exit " + process.exitValue() + "; see " + log);
    }

    private static Map<String, String> environment(Request request) throws Exception {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("JAVA_TOOL_OPTIONS", "-XX:+UseSerialGC -Xms16m -Xmx" + request.heap);
        values.put("TEMP", request.temporary.toString()); values.put("TMP", request.temporary.toString());
        values.put("GRADLE_USER_HOME", request.gradleHome.toString());
        values.put("WORLDLINE_RUNTIME_SLOT", request.id);
        values.put("WORLDLINE_RUNTIME_POOL_FILE", request.poolFile.toString());
        Path control = request.output.resolve("control"); Files.createDirectories(control);
        values.put("WORLDLINE_CONTROL_DIR", control.toString());
        if (request.prebuilt != null) {
            values.put("WORLDLINE_AERO_PREBUILT", request.prebuilt.toString());
            values.put("WORLDLINE_RUNTIME_TIMEOUT_EXTRA", "900");
        }
        return Map.copyOf(values);
    }

    private static int cpuPercent(Request request) {
        return Math.max(1, (int) Math.ceil(100.0 * request.cpuUnits / request.hostCpus));
    }

    record Request(Path root, String backend, String id, int timeoutSeconds, String heap,
            long memoryLimitBytes, double cpuUnits, int processLimit, int hostCpus, Path output,
            Path temporary, Path gradleHome, Path poolFile, String poolSecret, Path prebuilt,
            List<String> command) {
        Request { command = List.copyOf(command); }
        void validate() {
            require(root.isAbsolute() && output.startsWith(root) && temporary.startsWith(output),
                    "host task paths escaped the repository or output root");
            require(List.of("rust-windows-job", "windows-job", "linux-cgroup", "linux-sandbox")
                    .contains(backend), "unsupported host task backend");
            require(id.matches("[a-z0-9][a-z0-9-]{1,63}") && timeoutSeconds >= 30
                    && memoryLimitBytes > 0L && cpuUnits > 0.0 && processLimit >= 1 && hostCpus >= 1,
                    "invalid host task limits or identity");
            require(poolFile.isAbsolute() && !poolSecret.isBlank() && !command.isEmpty(),
                    "invalid host task capability or command");
        }
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        require(value != null && !value.isBlank(), "missing " + name); return value;
    }
    private static String java() { return Path.of(System.getProperty("java.home"), "bin", "java.exe").toString(); }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
}
