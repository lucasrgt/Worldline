import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Fail-closed verifier for native executor receipts and their referenced outputs. */
final class WorldlineExecutionReceipt {
    private static final List<String> RECEIPT_FIELDS = List.of("schema", "request_sha256",
            "executor_version", "backend", "exit_code", "termination", "stdout_sha256",
            "stderr_sha256", "metrics_sha256", "stdout_observed_bytes", "stderr_observed_bytes",
            "stdout_truncated", "stderr_truncated", "descendants_remaining", "error");
    private static final List<String> METRIC_FIELDS = List.of("schema", "elapsed_ms",
            "peak_memory_bytes", "memory_measurement", "peak_processes", "peak_tasks", "cpu_user_100ns",
            "cpu_kernel_100ns", "io_read_bytes", "io_write_bytes", "memory_limit_events",
            "oom_kill_events", "task_limit_events", "cpu_throttled_100ns", "stdout_observed_bytes",
            "stderr_observed_bytes", "stdout_truncated", "stderr_truncated", "telemetry_qualification");
    private static final Set<String> TERMINATIONS = Set.of("exited", "crashed", "timed_out",
            "spawn_failed", "wait_failed", "owner_exited");

    private WorldlineExecutionReceipt() { }

    static Verified verify(WorldlineExecutionPlan plan) throws Exception {
        Path path = plan.outputs().receipt();
        Map<String, Object> receipt = ExecutionJsonParser.object(Files.readString(path, StandardCharsets.UTF_8));
        ExecutionJson.exactKeys(receipt, RECEIPT_FIELDS, "execution receipt");
        require("worldline.execution-receipt.v1".equals(string(receipt, "schema")), "receipt schema drift");
        require(plan.requestSha256().equals(string(receipt, "request_sha256")), "receipt request digest drift");
        require(!string(receipt, "executor_version").isBlank(), "missing executor version");
        require(plan.isolation().backend().equals(string(receipt, "backend")), "receipt backend drift");
        String termination = string(receipt, "termination"); require(TERMINATIONS.contains(termination), "unknown termination");
        Long exitCode = nullableLong(receipt, "exit_code"); validateTermination(termination, exitCode);
        require(digest(plan.outputs().stdout()).equals(string(receipt, "stdout_sha256")), "stdout digest drift");
        require(digest(plan.outputs().stderr()).equals(string(receipt, "stderr_sha256")), "stderr digest drift");
        require(digest(plan.outputs().metrics()).equals(string(receipt, "metrics_sha256")), "metrics digest drift");
        long stdoutObserved = positive(receipt, "stdout_observed_bytes");
        long stderrObserved = positive(receipt, "stderr_observed_bytes");
        boolean stdoutTruncated = bool(receipt, "stdout_truncated");
        boolean stderrTruncated = bool(receipt, "stderr_truncated");
        validateOutput(plan.outputs().stdout(), plan.limits().stdoutBytes(), stdoutObserved, stdoutTruncated, "stdout");
        validateOutput(plan.outputs().stderr(), plan.limits().stderrBytes(), stderrObserved, stderrTruncated, "stderr");
        Long descendants = nullableLong(receipt, "descendants_remaining");
        if (!plan.isolation().backend().equals("direct")) require(Long.valueOf(0).equals(descendants),
                "contained receipt did not prove an empty process group");
        else require(descendants == null, "direct backend claimed descendant accounting");
        nullableString(receipt, "error"); verifyMetrics(plan, receipt);
        byte[] canonical = receiptBytes(receipt); require(java.util.Arrays.equals(Files.readAllBytes(path), canonical),
                "receipt is not canonically encoded");
        return new Verified(exitCode, termination, stdoutObserved, stderrObserved,
                stdoutTruncated, stderrTruncated, descendants);
    }

    private static void verifyMetrics(WorldlineExecutionPlan plan, Map<String, Object> receipt) throws Exception {
        Path path = plan.outputs().metrics(); Map<String, Object> value = ExecutionJsonParser.object(
                Files.readString(path, StandardCharsets.UTF_8));
        ExecutionJson.exactKeys(value, METRIC_FIELDS, "execution metrics");
        require("worldline.execution-metrics.v1".equals(string(value, "schema")), "metrics schema drift");
        positive(value, "elapsed_ms");
        String measurement = string(value, "memory_measurement");
        require(Set.of("unavailable", "job_commit", "cgroup_peak").contains(measurement),
                "unknown memory measurement");
        if (plan.isolation().backend().equals("windows_job")) require(measurement.equals("job_commit"),
                "Windows Job memory measurement drift");
        if (plan.isolation().backend().equals("linux_cgroup")) require(measurement.equals("cgroup_peak"),
                "Linux cgroup memory measurement drift");
        Long peakMemory = nullablePositive(value, "peak_memory_bytes");
        Long peakProcesses = nullablePositive(value, "peak_processes");
        Long peakTasks = nullablePositive(value, "peak_tasks");
        Long user = nullablePositive(value, "cpu_user_100ns");
        Long kernel = nullablePositive(value, "cpu_kernel_100ns");
        Long read = nullablePositive(value, "io_read_bytes");
        Long written = nullablePositive(value, "io_write_bytes");
        Long memoryEvents = nullablePositive(value, "memory_limit_events");
        Long oomKills = nullablePositive(value, "oom_kill_events");
        Long taskEvents = nullablePositive(value, "task_limit_events");
        Long throttled = nullablePositive(value, "cpu_throttled_100ns");
        if (!plan.isolation().backend().equals("direct")) require(peakMemory != null
                && user != null && kernel != null && read != null && written != null,
                "contained accounting is incomplete");
        if (plan.isolation().backend().equals("windows_job")) require(peakProcesses != null && peakTasks == null
                && memoryEvents == null && oomKills == null && taskEvents == null && throttled == null,
                "Windows Job process accounting drift");
        else if (plan.isolation().backend().equals("linux_cgroup")) require(peakProcesses == null && peakTasks != null
                && memoryEvents != null && oomKills != null && taskEvents != null && throttled != null,
                "Linux cgroup task accounting drift");
        else require(measurement.equals("unavailable") && peakMemory == null && peakProcesses == null
                && peakTasks == null && user == null && kernel == null && read == null && written == null
                && memoryEvents == null && oomKills == null && taskEvents == null && throttled == null,
                "direct backend claimed unavailable accounting");
        require(positive(value, "stdout_observed_bytes") == positive(receipt, "stdout_observed_bytes"),
                "stdout metrics drift");
        require(positive(value, "stderr_observed_bytes") == positive(receipt, "stderr_observed_bytes"),
                "stderr metrics drift");
        require(bool(value, "stdout_truncated") == bool(receipt, "stdout_truncated"), "stdout truncation drift");
        require(bool(value, "stderr_truncated") == bool(receipt, "stderr_truncated"), "stderr truncation drift");
        require("observational".equals(string(value, "telemetry_qualification")), "telemetry became qualifying");
        require(java.util.Arrays.equals(Files.readAllBytes(path), metricBytes(value)),
                "metrics are not canonically encoded");
    }

    private static void validateTermination(String termination, Long exitCode) {
        if (termination.equals("exited")) require(exitCode != null && exitCode >= 0, "exited receipt lacks exit code");
        else if (termination.equals("crashed")) { }
        else require(exitCode == null, "infrastructure termination contains an exit code");
    }

    private static void validateOutput(Path path, Long limit, long observed, boolean truncated, String label)
            throws Exception {
        long retained = Files.size(path); require(observed >= retained, label + " observed length underflow");
        if (!truncated) require(observed == retained, label + " length drift without truncation");
        else require(limit != null && retained == limit && observed > retained, label + " truncation proof drift");
    }

    private static byte[] receiptBytes(Map<String, Object> value) {
        String json = "{\"schema\":" + q(value, "schema") + ",\"request_sha256\":" + q(value, "request_sha256")
                + ",\"executor_version\":" + q(value, "executor_version") + ",\"backend\":" + q(value, "backend")
                + ",\"exit_code\":" + n(value.get("exit_code")) + ",\"termination\":" + q(value, "termination")
                + ",\"stdout_sha256\":" + q(value, "stdout_sha256") + ",\"stderr_sha256\":" + q(value, "stderr_sha256")
                + ",\"metrics_sha256\":" + q(value, "metrics_sha256") + ",\"stdout_observed_bytes\":" + n(value.get("stdout_observed_bytes"))
                + ",\"stderr_observed_bytes\":" + n(value.get("stderr_observed_bytes")) + ",\"stdout_truncated\":" + n(value.get("stdout_truncated"))
                + ",\"stderr_truncated\":" + n(value.get("stderr_truncated")) + ",\"descendants_remaining\":" + n(value.get("descendants_remaining"))
                + ",\"error\":" + n(value.get("error")) + "}";
        return ExecutionJson.bytes(json);
    }

    private static byte[] metricBytes(Map<String, Object> value) {
        String json = "{\"schema\":" + q(value, "schema") + ",\"elapsed_ms\":" + n(value.get("elapsed_ms"))
                + ",\"peak_memory_bytes\":" + n(value.get("peak_memory_bytes")) + ",\"memory_measurement\":" + q(value, "memory_measurement")
                + ",\"peak_processes\":" + n(value.get("peak_processes")) + ",\"peak_tasks\":" + n(value.get("peak_tasks"))
                + ",\"cpu_user_100ns\":" + n(value.get("cpu_user_100ns"))
                + ",\"cpu_kernel_100ns\":" + n(value.get("cpu_kernel_100ns")) + ",\"io_read_bytes\":" + n(value.get("io_read_bytes"))
                + ",\"io_write_bytes\":" + n(value.get("io_write_bytes")) + ",\"memory_limit_events\":" + n(value.get("memory_limit_events"))
                + ",\"oom_kill_events\":" + n(value.get("oom_kill_events")) + ",\"task_limit_events\":" + n(value.get("task_limit_events"))
                + ",\"cpu_throttled_100ns\":" + n(value.get("cpu_throttled_100ns")) + ",\"stdout_observed_bytes\":" + n(value.get("stdout_observed_bytes"))
                + ",\"stderr_observed_bytes\":" + n(value.get("stderr_observed_bytes")) + ",\"stdout_truncated\":" + n(value.get("stdout_truncated"))
                + ",\"stderr_truncated\":" + n(value.get("stderr_truncated")) + ",\"telemetry_qualification\":" + q(value, "telemetry_qualification") + "}";
        return ExecutionJson.bytes(json);
    }

    private static String digest(Path path) throws Exception { require(Files.isRegularFile(path), "missing executor output " + path); return ExecutionJson.sha256(path); }
    private static String q(Map<String, Object> value, String key) { return ExecutionJson.quote(string(value, key)); }
    private static String n(Object value) { return ExecutionJson.nullable(value); }
    private static String string(Map<String, Object> value, String key) { Object item = value.get(key); require(item instanceof String, key + " is not a string"); return (String) item; }
    private static String nullableString(Map<String, Object> value, String key) { Object item = value.get(key); require(item == null || item instanceof String, key + " is not nullable string"); return (String) item; }
    private static boolean bool(Map<String, Object> value, String key) { Object item = value.get(key); require(item instanceof Boolean, key + " is not boolean"); return (Boolean) item; }
    private static long positive(Map<String, Object> value, String key) { Long item = nullableLong(value, key); require(item != null && item >= 0, key + " is negative or null"); return item; }
    private static Long nullablePositive(Map<String, Object> value, String key) { Long item = nullableLong(value, key); require(item == null || item >= 0, key + " is negative"); return item; }
    private static Long nullableLong(Map<String, Object> value, String key) { Object item = value.get(key); require(item == null || item instanceof Long, key + " is not nullable integer"); return (Long) item; }
    private static void require(boolean value, String message) { ExecutionJson.require(value, message); }

    record Verified(Long exitCode, String termination, long stdoutObservedBytes,
            long stderrObservedBytes, boolean stdoutTruncated, boolean stderrTruncated,
            Long descendantsRemaining) { }
}
