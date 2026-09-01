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
    private final Filesystem filesystem;
    private final Outputs outputs;

    WorldlineExecutionPlan(String jobId, Path workingDirectory, List<String> argv,
            Environment environment, Limits limits, Isolation isolation, Filesystem filesystem, Outputs outputs) {
        this.jobId = jobId; this.workingDirectory = clean(workingDirectory, "working directory");
        this.argv = List.copyOf(argv); this.environment = environment; this.limits = limits;
        this.isolation = isolation; this.filesystem = filesystem; this.outputs = outputs; validate();
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
                + ",\"filesystem\":{" + "\"mode\":" + q(filesystem.mode)
                + ",\"read_only\":" + ExecutionJson.strings(filesystem.readOnly.stream()
                        .map(Path::toString).toList())
                + ",\"read_write\":" + ExecutionJson.strings(filesystem.readWrite.stream()
                        .map(Path::toString).toList()) + "}"
                + ",\"outputs\":{" + "\"root\":" + q(text(outputs.root))
                + ",\"stdout\":" + q(text(outputs.stdout))
                + ",\"stderr\":" + q(text(outputs.stderr))
                + ",\"metrics\":" + q(text(outputs.metrics))
                + ",\"manifest\":" + q(text(outputs.manifest))
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
        require(List.of("inherit", "none").contains(isolation.network), "unsupported executor network policy");
        if (isolation.network.equals("none")) require(isolation.backend.equals("linux_cgroup"),
                "network isolation requires Linux cgroups");
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
        environment.validate(); filesystem.validate(workingDirectory, Path.of(argv.get(0)), outputs.root,
                isolation.backend); outputs.validate(workingDirectory);
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
    record Filesystem(String mode, List<Path> readOnly, List<Path> readWrite) {
        Filesystem { readOnly = readOnly.stream().map(path -> clean(path, "read-only path")).toList();
            readWrite = readWrite.stream().map(path -> clean(path, "read-write path")).toList(); }
        void validate(Path working, Path executable, Path output, String backend) {
            require(List.of("inherit", "landlock").contains(mode), "unsupported filesystem policy");
            if (mode.equals("inherit")) { require(readOnly.isEmpty() && readWrite.isEmpty(),
                    "inherited filesystem policy contains rules"); return; }
            require(backend.equals("linux_cgroup") && !readOnly.isEmpty() && !readWrite.isEmpty(),
                    "Landlock requires Linux cgroups and path rules");
            List<Path> readable = new ArrayList<>(readOnly); readable.addAll(readWrite);
            require(covered(working, readable) && covered(executable, readable),
                    "Landlock omits working directory or executable");
            require(covered(output, readWrite), "Landlock output root is not writable");
            require(new HashSet<>(readable).size() == readable.size(), "duplicate filesystem path rule");
        }
        private static boolean covered(Path path, List<Path> rules) {
            return rules.stream().anyMatch(path::startsWith);
        }
        static Filesystem inherit() { return new Filesystem("inherit", List.of(), List.of()); }
    }
    record Outputs(Path root, Path stdout, Path stderr, Path metrics, Path manifest, Path receipt) {
        Outputs { root = clean(root, "output root"); stdout = clean(stdout, "stdout");
            stderr = clean(stderr, "stderr"); metrics = clean(metrics, "metrics");
            manifest = clean(manifest, "manifest"); receipt = clean(receipt, "receipt"); }
        void validate(Path working) { require(!working.equals(root), "working directory equals output root");
            List<Path> values = List.of(stdout, stderr, metrics, manifest, receipt);
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
