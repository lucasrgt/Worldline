import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/** Strict Java producer for worldline.execution-plan.v1. */
final class WorldlineExecutionPlan {
    static final String SCHEMA = "worldline.execution-plan.v1";
    private final String jobId;
    private final Path workingDirectory;
    private final List<String> argv;
    private final Environment environment;
    private final Limits limits;
    private final Isolation isolation;
    private final Outputs outputs;

    WorldlineExecutionPlan(String jobId, Path workingDirectory, List<String> argv,
            Environment environment, Limits limits, Isolation isolation, Outputs outputs) {
        this.jobId = jobId; this.workingDirectory = clean(workingDirectory, "working directory");
        this.argv = List.copyOf(argv); this.environment = environment; this.limits = limits;
        this.isolation = isolation; this.outputs = outputs; validate();
    }

    byte[] canonicalBytes() {
        String json = "{\"schema\":" + q(SCHEMA) + ",\"job_id\":" + q(jobId)
                + ",\"working_directory\":" + q(text(workingDirectory))
                + ",\"argv\":" + ExecutionJson.strings(argv)
                + ",\"environment\":{" + "\"inherit\":" + ExecutionJson.strings(environment.inherit)
                + ",\"remove\":" + ExecutionJson.strings(environment.remove)
                + ",\"set\":" + ExecutionJson.stringMap(environment.set) + "}"
                + ",\"limits\":{" + "\"timeout_ms\":" + limits.timeoutMs
                + ",\"job_memory_bytes\":" + n(limits.jobMemoryBytes)
                + ",\"active_processes\":" + n(limits.activeProcesses)
                + ",\"active_tasks\":" + n(limits.activeTasks)
                + ",\"cpu_rate_percent\":" + n(limits.cpuRatePercent)
                + ",\"stdout_bytes\":" + n(limits.stdoutBytes)
                + ",\"stderr_bytes\":" + n(limits.stderrBytes) + "}"
                + ",\"isolation\":{" + "\"backend\":" + q(isolation.backend)
                + ",\"network\":" + q(isolation.network)
                + ",\"owner_pid\":" + n(isolation.ownerPid) + "}"
                + ",\"outputs\":{" + "\"root\":" + q(text(outputs.root))
                + ",\"stdout\":" + q(text(outputs.stdout))
                + ",\"stderr\":" + q(text(outputs.stderr))
                + ",\"metrics\":" + q(text(outputs.metrics))
                + ",\"receipt\":" + q(text(outputs.receipt)) + "}}";
        return ExecutionJson.bytes(json);
    }

    void write(Path path) throws Exception { ExecutionJson.writeAtomic(path, canonicalBytes()); }
    String requestSha256() throws Exception { return ExecutionJson.sha256(canonicalBytes()); }
    Limits limits() { return limits; }
    Isolation isolation() { return isolation; }
    Outputs outputs() { return outputs; }

    private void validate() {
        require(jobId != null && jobId.matches("[a-z0-9]+(?:-[a-z0-9]+)*") && jobId.length() <= 80,
                "invalid executor job id");
        require(!argv.isEmpty() && argv.stream().noneMatch(value -> value == null || value.indexOf(0) >= 0),
                "invalid executor argv");
        require(limits.timeoutMs >= 1 && limits.timeoutMs <= 86_400_000, "invalid executor timeout");
        positive(limits.jobMemoryBytes, "job memory"); range(limits.activeProcesses, 1, 1024, "active processes");
        range(limits.activeTasks, 1, 4096, "active tasks");
        range(limits.cpuRatePercent, 1, 100, "CPU rate"); positive(limits.stdoutBytes, "stdout limit");
        positive(limits.stderrBytes, "stderr limit");
        require(List.of("direct", "windows_job", "linux_cgroup").contains(isolation.backend),
                "unsupported executor backend");
        require(isolation.network.equals("inherit"), "unsupported executor network policy");
        positive(isolation.ownerPid, "owner pid");
        if (isolation.backend.equals("direct")) require(isolation.ownerPid == null
                && limits.jobMemoryBytes == null && limits.activeProcesses == null
                && limits.activeTasks == null
                && limits.cpuRatePercent == null && limits.stdoutBytes == null && limits.stderrBytes == null,
                "direct executor plan claims unsupported controls");
        else require(Path.of(argv.get(0)).isAbsolute(), "contained executor command is not absolute");
        if (isolation.backend.equals("windows_job")) require(limits.activeTasks == null,
                "Windows Job cannot enforce active tasks");
        if (isolation.backend.equals("linux_cgroup")) require(limits.activeProcesses == null,
                "Linux cgroup cannot enforce active processes");
        environment.validate(); outputs.validate(workingDirectory);
    }

    record Environment(List<String> inherit, List<String> remove, Map<String, String> set) {
        Environment { inherit = List.copyOf(inherit); remove = List.copyOf(remove);
            set = java.util.Collections.unmodifiableMap(new TreeMap<>(set)); }
        void validate() { names(inherit, "inherited"); names(remove, "removed"); names(new ArrayList<>(set.keySet()), "set");
            require(set.values().stream().noneMatch(value -> value == null || value.indexOf(0) >= 0),
                    "invalid executor environment value"); }
        private static void names(List<String> values, String label) {
            HashSet<String> unique = new HashSet<>();
            for (String value : values) require(value != null && value.matches("[A-Za-z_][A-Za-z0-9_]*")
                    && unique.add(value.toUpperCase(Locale.ROOT)), "invalid or duplicate " + label + " environment name");
        }
        static Environment empty() { return new Environment(List.of(), List.of(), Map.of()); }
    }

    record Limits(long timeoutMs, Long jobMemoryBytes, Integer activeProcesses, Integer activeTasks,
            Integer cpuRatePercent, Long stdoutBytes, Long stderrBytes) { }
    record Isolation(String backend, String network, Long ownerPid) { }
    record Outputs(Path root, Path stdout, Path stderr, Path metrics, Path receipt) {
        Outputs { root = clean(root, "output root"); stdout = clean(stdout, "stdout");
            stderr = clean(stderr, "stderr"); metrics = clean(metrics, "metrics");
            receipt = clean(receipt, "receipt"); }
        void validate(Path working) { require(!working.equals(root), "working directory equals output root");
            List<Path> values = List.of(stdout, stderr, metrics, receipt);
            require(values.stream().allMatch(path -> path.startsWith(root) && !path.equals(root)), "output escapes root");
            require(new HashSet<>(values).size() == values.size(), "duplicate executor output path"); }
    }

    private static Path clean(Path path, String label) {
        require(path != null && path.isAbsolute() && path.normalize().equals(path), label + " is not absolute and normalized");
        require(path.toString().indexOf(0) < 0, label + " contains NUL"); return path;
    }
    private static void positive(Number value, String label) { if (value != null) require(value.longValue() > 0, "invalid " + label); }
    private static void range(Number value, long min, long max, String label) { if (value != null)
        require(value.longValue() >= min && value.longValue() <= max, "invalid " + label); }
    private static String text(Path path) { return path.toString(); }
    private static String q(String value) { return ExecutionJson.quote(value); }
    private static String n(Object value) { return ExecutionJson.nullable(value); }
    private static void require(boolean value, String message) { ExecutionJson.require(value, message); }
}
