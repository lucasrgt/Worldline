import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Local content-addressed history and rebuildable executor telemetry profiles. */
public final class ExecutionTelemetryHistory {
    private static final int MAX_SAMPLES = 10_000;
    private static final long MAX_SAMPLE_BYTES = 8_192L;

    private ExecutionTelemetryHistory() { }

    public static void main(String[] arguments) {
        try {
            if (List.of(arguments).equals(List.of("--self-test"))) { selfTest(); return; }
            require(List.of(arguments).equals(List.of("summarize")),
                    "usage: ExecutionTelemetryHistory summarize|--self-test");
            Path root = Path.of(".worldline/executor-telemetry").toAbsolutePath().normalize();
            System.out.write(summaryBytes(profiles(root)));
        } catch (Exception error) {
            System.err.println("execution telemetry history failed: " + error.getMessage());
            System.exit(1);
        }
    }

    static String record(Path root, String lane, String workloadId, String capability,
            ExecutionTelemetry telemetry) throws Exception {
        String digest = append(root, ExecutionTelemetrySample.live(lane, workloadId, capability, telemetry));
        byte[] admission = ExecutionTelemetryAdmission.recommendation(profiles(root),
                ExecutionTelemetrySample.hostToken(System.getProperty("os.name")),
                ExecutionTelemetrySample.hostToken(System.getProperty("os.arch")));
        if (admission != null) ExecutionJson.writeAtomic(root.resolve("admission.properties"), admission);
        return digest;
    }

    static String append(Path root, ExecutionTelemetrySample sample) throws Exception {
        require(root.isAbsolute() && root.normalize().equals(root), "telemetry root is not normalized");
        byte[] bytes = sample.bytes(); String digest = ExecutionJson.sha256(bytes);
        Path samples = root.resolve("samples"), target = samples.resolve(digest + ".json");
        Files.createDirectories(samples);
        if (Files.exists(target, LinkOption.NOFOLLOW_LINKS))
            require(Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS)
                    && java.util.Arrays.equals(Files.readAllBytes(target), bytes),
                    "telemetry object collision");
        else ExecutionJson.writeAtomic(target, bytes);
        return digest;
    }

    static List<Profile> profiles(Path root) throws Exception {
        Map<String, Accumulator> grouped = new LinkedHashMap<>();
        for (ExecutionTelemetrySample sample : samples(root))
            grouped.computeIfAbsent(sample.profileKey(), ignored -> new Accumulator(sample)).add(sample);
        return grouped.values().stream().map(Accumulator::profile)
                .sorted(Comparator.comparing(Profile::key)).toList();
    }

    static List<ExecutionTelemetrySample> samples(Path root) throws Exception {
        Path samples = root.resolve("samples"); if (!Files.exists(samples)) return List.of();
        require(Files.isDirectory(samples, LinkOption.NOFOLLOW_LINKS)
                && !Files.isSymbolicLink(samples), "invalid telemetry sample directory");
        List<ExecutionTelemetrySample> result = new ArrayList<>(); int count = 0;
        List<Path> paths = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(samples)) {
            for (Path path : stream) paths.add(path);
        }
        paths.sort(Comparator.comparing(path -> path.getFileName().toString()));
        for (Path path : paths) {
            require(++count <= MAX_SAMPLES, "telemetry history exceeds sample budget");
            String name = path.getFileName().toString();
            require(name.matches("[0-9a-f]{64}\\.json")
                    && Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)
                    && !Files.isSymbolicLink(path), "invalid telemetry sample object");
            require(Files.size(path) <= MAX_SAMPLE_BYTES, "telemetry sample exceeds size budget");
            byte[] bytes = Files.readAllBytes(path);
            require(name.equals(ExecutionJson.sha256(bytes) + ".json"), "telemetry object digest drift");
            result.add(ExecutionTelemetrySample.parse(bytes));
        }
        return List.copyOf(result);
    }

    static byte[] summaryBytes(List<Profile> profiles) {
        long samples = profiles.stream().mapToLong(Profile::count).sum();
        StringBuilder json = new StringBuilder("{\"schema\":\"worldline.execution-telemetry-profiles.v1\",\"sample_count\":")
                .append(samples).append(",\"profiles\":[");
        for (int index = 0; index < profiles.size(); index++) {
            if (index > 0) json.append(','); json.append(profiles.get(index).json());
        }
        return ExecutionJson.bytes(json.append("]}").toString());
    }

    static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-telemetry-history-").toAbsolutePath().normalize();
        try {
            ExecutionTelemetry telemetry = new ExecutionTelemetry("0.1.0", "windows_job", "exited",
                    12, 2, 10, 4096L, "job_commit", 1L, null, 3L, 4L, 5L, 6L,
                    null, null, null, null, 7, 8, false, false);
            append(root, ExecutionTelemetrySample.create("00000000-0000-0000-0000-000000000001",
                    1, "tooling", "gate-self-test", "windows", "amd64", "structural", telemetry));
            append(root, ExecutionTelemetrySample.create("00000000-0000-0000-0000-000000000002",
                    2, "tooling", "gate-self-test", "windows", "amd64", "structural", telemetry));
            append(root, ExecutionTelemetrySample.create("00000000-0000-0000-0000-000000000003",
                    3, "tooling", "other", "windows", "amd64", "structural", telemetry));
            List<Profile> profiles = profiles(root);
            require(profiles.size() == 2 && profiles.stream().mapToLong(Profile::count).sum() == 3,
                    "telemetry profile grouping drift");
            Profile repeated = profiles.stream().filter(value -> value.count() == 2).findFirst().orElseThrow();
            require(Long.valueOf(6).equals(repeated.cpuUserTotal()) && repeated.stdoutTotal() == 14,
                    "telemetry profile aggregation drift");
            require(repeated.peakTasksMax() == null && repeated.memoryEventsTotal() == null,
                    "unavailable telemetry did not remain absent");
            byte[] none = ExecutionTelemetryAdmission.recommendation(profiles, "windows", "amd64");
            require(none == null, "non-candidate telemetry influenced admission");
            ExecutionTelemetry candidate = new ExecutionTelemetry("0.1.0", "windows_job", "exited",
                    12, 2, 10, 3L << 30, "job_commit", 1L, null, 3L, 4L, 5L, 6L,
                    null, null, null, null, 7, 8, false, false);
            append(root, ExecutionTelemetrySample.create("00000000-0000-0000-0000-000000000004",
                    4, "candidate", "m1-fixture", "windows", "amd64", "candidate-worker", candidate));
            String admission = new String(ExecutionTelemetryAdmission.recommendation(
                    profiles(root), "windows", "amd64"), java.nio.charset.StandardCharsets.UTF_8);
            require(admission.contains("minimum.worker.memory.bytes=4026531840")
                    && admission.contains("direction=lower-only"), "telemetry admission drift");
            String summary = new String(summaryBytes(profiles), java.nio.charset.StandardCharsets.UTF_8);
            require(summary.contains("\"sample_count\":3") && !summary.contains("argv")
                    && !summary.contains("working_directory"), "telemetry summary sanitization drift");
            Path first; try (var paths = Files.list(root.resolve("samples"))) {
                first = paths.findFirst().orElseThrow();
            }
            Files.writeString(first, "{}\n"); expectFailure(() -> profiles(root));
            System.out.println("execution telemetry history self-test passed");
        } finally { delete(root); }
    }

    record Profile(ExecutionTelemetrySample sample, long count, long elapsedTotal,
            long elapsedMin, long elapsedMax, long bootstrapTotal, long runtimeTotal,
            Long peakMemoryMax, Long peakProcessesMax, Long peakTasksMax, Long cpuUserTotal,
            Long cpuKernelTotal, Long ioReadTotal, Long ioWriteTotal, Long memoryEventsTotal,
            Long oomKillsTotal, Long taskEventsTotal, Long throttledTotal, long stdoutTotal,
            long stderrTotal, long truncatedSamples) {
        String key() { return sample.profileKey(); }
        String json() { ExecutionTelemetry t = sample.telemetry(); return "{\"lane\":" + q(sample.lane())
                + ",\"workload_id\":" + q(sample.workloadId()) + ",\"executor_version\":"
                + q(sample.executorVersion()) + ",\"backend\":" + q(sample.backend()) + ",\"os\":"
                + q(sample.os()) + ",\"architecture\":" + q(sample.architecture())
                + ",\"runtime_capability\":" + q(sample.runtimeCapability()) + ",\"termination\":"
                + q(t.termination()) + ",\"count\":" + count + ",\"elapsed_total_ms\":" + elapsedTotal
                + ",\"elapsed_min_ms\":" + elapsedMin + ",\"elapsed_max_ms\":" + elapsedMax
                + ",\"bootstrap_total_ms\":" + bootstrapTotal + ",\"runtime_total_ms\":"
                + runtimeTotal + ",\"peak_memory_max_bytes\":" + n(peakMemoryMax)
                + ",\"memory_measurement\":" + q(t.memoryMeasurement()) + ",\"peak_processes_max\":"
                + n(peakProcessesMax) + ",\"peak_tasks_max\":" + n(peakTasksMax)
                + ",\"cpu_user_total_100ns\":" + n(cpuUserTotal) + ",\"cpu_kernel_total_100ns\":"
                + n(cpuKernelTotal) + ",\"io_read_total_bytes\":" + n(ioReadTotal)
                + ",\"io_write_total_bytes\":" + n(ioWriteTotal) + ",\"memory_limit_events_total\":"
                + n(memoryEventsTotal) + ",\"oom_kill_events_total\":" + n(oomKillsTotal)
                + ",\"task_limit_events_total\":" + n(taskEventsTotal)
                + ",\"cpu_throttled_total_100ns\":" + n(throttledTotal)
                + ",\"stdout_total_bytes\":" + stdoutTotal + ",\"stderr_total_bytes\":"
                + stderrTotal + ",\"truncated_samples\":" + truncatedSamples + "}"; }
    }

    private static final class Accumulator {
        private final ExecutionTelemetrySample sample; private long count, elapsedTotal;
        private long elapsedMin = Long.MAX_VALUE, elapsedMax, bootstrapTotal, runtimeTotal;
        private Long peakMemoryMax, peakProcessesMax, peakTasksMax, cpuUserTotal, cpuKernelTotal;
        private Long ioReadTotal, ioWriteTotal, memoryEventsTotal, oomKillsTotal, taskEventsTotal;
        private Long throttledTotal; private long stdoutTotal, stderrTotal, truncatedSamples;
        Accumulator(ExecutionTelemetrySample sample) { this.sample = sample; }
        void add(ExecutionTelemetrySample value) { ExecutionTelemetry t = value.telemetry(); count++;
            elapsedTotal = Math.addExact(elapsedTotal, t.elapsedMs());
            bootstrapTotal = Math.addExact(bootstrapTotal, t.bootstrapMs());
            runtimeTotal = Math.addExact(runtimeTotal, t.runtimeMs());
            stdoutTotal = Math.addExact(stdoutTotal, t.stdoutObservedBytes());
            stderrTotal = Math.addExact(stderrTotal, t.stderrObservedBytes());
            if (t.stdoutTruncated() || t.stderrTruncated()) truncatedSamples++;
            elapsedMin = Math.min(elapsedMin, t.elapsedMs()); elapsedMax = Math.max(elapsedMax, t.elapsedMs());
            peakMemoryMax = max(peakMemoryMax, t.peakMemoryBytes());
            peakProcessesMax = max(peakProcessesMax, t.peakProcesses());
            peakTasksMax = max(peakTasksMax, t.peakTasks());
            cpuUserTotal = add(cpuUserTotal, t.cpuUser100ns());
            cpuKernelTotal = add(cpuKernelTotal, t.cpuKernel100ns());
            ioReadTotal = add(ioReadTotal, t.ioReadBytes()); ioWriteTotal = add(ioWriteTotal, t.ioWriteBytes());
            memoryEventsTotal = add(memoryEventsTotal, t.memoryLimitEvents());
            oomKillsTotal = add(oomKillsTotal, t.oomKillEvents());
            taskEventsTotal = add(taskEventsTotal, t.taskLimitEvents());
            throttledTotal = add(throttledTotal, t.cpuThrottled100ns()); }
        Profile profile() { return new Profile(sample, count, elapsedTotal, elapsedMin, elapsedMax,
                bootstrapTotal, runtimeTotal, peakMemoryMax, peakProcessesMax, peakTasksMax,
                cpuUserTotal, cpuKernelTotal, ioReadTotal, ioWriteTotal, memoryEventsTotal,
                oomKillsTotal, taskEventsTotal, throttledTotal, stdoutTotal, stderrTotal,
                truncatedSamples); }
        private static Long add(Long total, Long value) {
            if (value == null) return total;
            if (total == null) return value;
            return Math.addExact(total, value);
        }
        private static Long max(Long current, Long value) {
            if (value == null) return current;
            if (current == null) return value;
            return Math.max(current, value);
        }
    }

    private static String q(String value) { return ExecutionJson.quote(value); }
    private static String n(Long value) { return ExecutionJson.nullable(value); }
    private static void delete(Path root) throws Exception { try (var paths = Files.walk(root)) {
        for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path); } }
    private static void expectFailure(Checked action) throws Exception { try { action.run();
        throw new IllegalStateException("invalid telemetry history was accepted"); }
        catch (IllegalArgumentException expected) { } }
    private static void require(boolean value, String message) { ExecutionJson.require(value, message); }
    @FunctionalInterface private interface Checked { void run() throws Exception; }
}
