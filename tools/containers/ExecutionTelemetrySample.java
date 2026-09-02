import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** Closed, sanitized sample schema for local executor history. */
record ExecutionTelemetrySample(String sampleId, long observedAtEpochMs, String lane,
        String workloadId, String executorVersion, String backend, String os,
        String architecture, String runtimeCapability, ExecutionTelemetry telemetry) {
    private static final String SCHEMA = "worldline.execution-telemetry-sample.v1";
    private static final List<String> FIELDS = List.of("schema", "sample_id",
            "observed_at_epoch_ms", "lane", "workload_id", "executor_version", "backend",
            "os", "architecture", "runtime_capability", "termination", "elapsed_ms",
            "bootstrap_ms", "runtime_ms", "peak_memory_bytes", "memory_measurement",
            "peak_processes", "peak_tasks", "cpu_user_100ns", "cpu_kernel_100ns",
            "io_read_bytes", "io_write_bytes", "memory_limit_events", "oom_kill_events",
            "task_limit_events", "cpu_throttled_100ns", "stdout_observed_bytes",
            "stderr_observed_bytes", "stdout_truncated", "stderr_truncated",
            "telemetry_qualification");

    static ExecutionTelemetrySample live(String lane, String workloadId,
            String runtimeCapability, ExecutionTelemetry telemetry) {
        return create(UUID.randomUUID().toString(), System.currentTimeMillis(), lane, workloadId,
                normalizeHost(System.getProperty("os.name")),
                normalizeHost(System.getProperty("os.arch")), runtimeCapability, telemetry);
    }

    static ExecutionTelemetrySample create(String sampleId, long observedAtEpochMs, String lane,
            String workloadId, String os, String architecture, String runtimeCapability,
            ExecutionTelemetry telemetry) {
        require(sampleId != null && UUID.fromString(sampleId).toString().equals(sampleId),
                "invalid telemetry sample ID");
        require(observedAtEpochMs >= 0L, "negative telemetry observation time");
        for (String value : List.of(lane, workloadId, os, architecture, runtimeCapability)) token(value);
        require(telemetry != null, "missing executor telemetry");
        token(telemetry.executorVersion()); token(telemetry.backend()); token(telemetry.termination());
        require(telemetry.bootstrapMs() <= telemetry.elapsedMs(), "bootstrap exceeds elapsed duration");
        long remainder = telemetry.elapsedMs() - telemetry.bootstrapMs();
        require(telemetry.runtimeMs() == remainder || (telemetry.runtimeMs() < Long.MAX_VALUE
                && telemetry.runtimeMs() + 1L == remainder), "phase duration partition drift");
        return new ExecutionTelemetrySample(sampleId, observedAtEpochMs, lane, workloadId,
                telemetry.executorVersion(), telemetry.backend(), os, architecture,
                runtimeCapability, telemetry);
    }

    static ExecutionTelemetrySample parse(byte[] bytes) {
        Map<String, Object> value = ExecutionJsonParser.object(
                new String(bytes, StandardCharsets.UTF_8));
        ExecutionJson.exactKeys(value, FIELDS, "execution telemetry sample");
        require(SCHEMA.equals(text(value, "schema")), "telemetry sample schema drift");
        ExecutionTelemetry telemetry = new ExecutionTelemetry(text(value, "executor_version"),
                text(value, "backend"), text(value, "termination"), number(value, "elapsed_ms"),
                number(value, "bootstrap_ms"), number(value, "runtime_ms"),
                nullable(value, "peak_memory_bytes"), text(value, "memory_measurement"),
                nullable(value, "peak_processes"), nullable(value, "peak_tasks"),
                nullable(value, "cpu_user_100ns"), nullable(value, "cpu_kernel_100ns"),
                nullable(value, "io_read_bytes"), nullable(value, "io_write_bytes"),
                nullable(value, "memory_limit_events"), nullable(value, "oom_kill_events"),
                nullable(value, "task_limit_events"), nullable(value, "cpu_throttled_100ns"),
                number(value, "stdout_observed_bytes"), number(value, "stderr_observed_bytes"),
                bool(value, "stdout_truncated"), bool(value, "stderr_truncated"));
        ExecutionTelemetrySample sample = create(text(value, "sample_id"),
                number(value, "observed_at_epoch_ms"), text(value, "lane"),
                text(value, "workload_id"), text(value, "os"), text(value, "architecture"),
                text(value, "runtime_capability"), telemetry);
        require("observational".equals(text(value, "telemetry_qualification")),
                "historical telemetry became qualifying");
        require(java.util.Arrays.equals(bytes, sample.bytes()), "telemetry sample is not canonical");
        return sample;
    }

    byte[] bytes() {
        ExecutionTelemetry t = telemetry;
        String json = "{\"schema\":" + q(SCHEMA) + ",\"sample_id\":" + q(sampleId)
                + ",\"observed_at_epoch_ms\":" + observedAtEpochMs + ",\"lane\":" + q(lane)
                + ",\"workload_id\":" + q(workloadId) + ",\"executor_version\":" + q(executorVersion)
                + ",\"backend\":" + q(backend) + ",\"os\":" + q(os) + ",\"architecture\":"
                + q(architecture) + ",\"runtime_capability\":" + q(runtimeCapability)
                + ",\"termination\":" + q(t.termination()) + ",\"elapsed_ms\":" + t.elapsedMs()
                + ",\"bootstrap_ms\":" + t.bootstrapMs() + ",\"runtime_ms\":" + t.runtimeMs()
                + ",\"peak_memory_bytes\":" + n(t.peakMemoryBytes()) + ",\"memory_measurement\":"
                + q(t.memoryMeasurement()) + ",\"peak_processes\":" + n(t.peakProcesses())
                + ",\"peak_tasks\":" + n(t.peakTasks()) + ",\"cpu_user_100ns\":" + n(t.cpuUser100ns())
                + ",\"cpu_kernel_100ns\":" + n(t.cpuKernel100ns()) + ",\"io_read_bytes\":" + n(t.ioReadBytes())
                + ",\"io_write_bytes\":" + n(t.ioWriteBytes()) + ",\"memory_limit_events\":"
                + n(t.memoryLimitEvents()) + ",\"oom_kill_events\":" + n(t.oomKillEvents())
                + ",\"task_limit_events\":" + n(t.taskLimitEvents()) + ",\"cpu_throttled_100ns\":"
                + n(t.cpuThrottled100ns()) + ",\"stdout_observed_bytes\":" + t.stdoutObservedBytes()
                + ",\"stderr_observed_bytes\":" + t.stderrObservedBytes() + ",\"stdout_truncated\":"
                + t.stdoutTruncated() + ",\"stderr_truncated\":" + t.stderrTruncated()
                + ",\"telemetry_qualification\":\"observational\"}";
        return ExecutionJson.bytes(json);
    }

    String profileKey() {
        return String.join("\u0000", lane, workloadId, executorVersion, backend, os,
                architecture, runtimeCapability, telemetry.termination());
    }

    private static String normalizeHost(String value) {
        String normalized = value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
        token(normalized); return normalized;
    }
    static String hostToken(String value) { return normalizeHost(value); }
    private static void token(String value) { require(value != null && value.matches("[a-z0-9][a-z0-9._-]{0,62}"),
            "invalid telemetry dimension"); }
    private static String q(String value) { return ExecutionJson.quote(value); }
    private static String n(Long value) { return ExecutionJson.nullable(value); }
    private static String text(Map<String, Object> value, String key) { Object item = value.get(key);
        require(item instanceof String, key + " is not a string"); return (String) item; }
    private static long number(Map<String, Object> value, String key) { Long item = nullable(value, key);
        require(item != null, key + " is null"); return item; }
    private static Long nullable(Map<String, Object> value, String key) { Object item = value.get(key);
        require(item == null || item instanceof Long, key + " is not an integer");
        if (item != null) require((Long) item >= 0L, key + " is negative"); return (Long) item; }
    private static boolean bool(Map<String, Object> value, String key) { Object item = value.get(key);
        require(item instanceof Boolean, key + " is not boolean"); return (Boolean) item; }
    private static void require(boolean value, String message) { ExecutionJson.require(value, message); }
}
