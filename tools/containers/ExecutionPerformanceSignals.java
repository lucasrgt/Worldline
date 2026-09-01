import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Detects observational campaign performance changes from sanitized local history. */
final class ExecutionPerformanceSignals {
    private ExecutionPerformanceSignals() { }

    static Summary summarize(Path telemetryRoot, ExecutionRegressionPolicy policy) throws Exception {
        Map<String, List<ExecutionTelemetrySample>> grouped = new TreeMap<>();
        for (ExecutionTelemetrySample sample : ExecutionTelemetryHistory.samples(telemetryRoot))
            if (policy.accepts(sample)) grouped.computeIfAbsent(sample.profileKey(),
                    ignored -> new ArrayList<>()).add(sample);
        List<Signal> signals = new ArrayList<>();
        Comparator<ExecutionTelemetrySample> order = Comparator.comparingLong(
                ExecutionTelemetrySample::observedAtEpochMs).thenComparing(
                        ExecutionTelemetrySample::sampleId);
        for (List<ExecutionTelemetrySample> samples : grouped.values()) {
            samples.sort(order); signals.add(signal(List.copyOf(samples), policy));
        }
        return new Summary(policy, List.copyOf(signals));
    }

    static byte[] summaryBytes(Summary summary) {
        StringBuilder json = new StringBuilder("{\"schema\":\"worldline.execution-performance-signals.v1\"")
                .append(",\"verdict_authority\":false,\"admission_effect\":false,\"metric\":")
                .append(q(summary.policy().metric())).append(",\"baseline_window_samples\":")
                .append(summary.policy().baselineWindow()).append(",\"recent_window_samples\":")
                .append(summary.policy().recentWindow()).append(",\"change_threshold_percent\":")
                .append(summary.policy().thresholdPercent()).append(",\"signal_count\":")
                .append(summary.signals().size()).append(",\"increase_count\":")
                .append(summary.count("increase")).append(",\"insufficient_history_count\":")
                .append(summary.count("insufficient-history")).append(",\"signals\":[");
        for (int index = 0; index < summary.signals().size(); index++) {
            if (index > 0) json.append(','); json.append(summary.signals().get(index).json());
        }
        return ExecutionJson.bytes(json.append("]}").toString());
    }

    private static Signal signal(List<ExecutionTelemetrySample> samples,
            ExecutionRegressionPolicy policy) throws Exception {
        ExecutionTelemetrySample identity = samples.get(0); int count = samples.size();
        int recentCount = Math.min(policy.recentWindow(), count);
        int baselineCount = Math.min(policy.baselineWindow(), count - recentCount);
        int split = count - recentCount;
        List<String> baselineIds = ids(samples.subList(split - baselineCount, split));
        List<String> recentIds = ids(samples.subList(split, count));
        if (baselineCount < policy.baselineWindow() || recentCount < policy.recentWindow())
            return new Signal(identity, count, baselineCount, recentCount, null, null, null,
                    "insufficient-history", baselineIds, recentIds);
        split = count - policy.recentWindow();
        long baseline = median(samples.subList(split - policy.baselineWindow(), split));
        long recent = median(samples.subList(split, count));
        if (baseline == 0L) return new Signal(identity, count, baselineCount, recentCount,
                baseline, recent, null, "zero-baseline", baselineIds, recentIds);
        long ratio = BigInteger.valueOf(recent).multiply(BigInteger.valueOf(100L))
                .divide(BigInteger.valueOf(baseline)).min(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
        BigInteger threshold = BigInteger.valueOf(policy.thresholdPercent());
        BigInteger baselineValue = BigInteger.valueOf(baseline), recentValue = BigInteger.valueOf(recent);
        String classification = recentValue.multiply(BigInteger.valueOf(100L))
                .compareTo(baselineValue.multiply(threshold)) >= 0 ? "increase"
                : baselineValue.multiply(BigInteger.valueOf(100L))
                        .compareTo(recentValue.multiply(threshold)) >= 0 ? "decrease" : "within-band";
        return new Signal(identity, count, baselineCount, recentCount, baseline, recent,
                ratio, classification, baselineIds, recentIds);
    }

    private static long median(List<ExecutionTelemetrySample> samples) {
        List<Long> values = samples.stream().map(value -> value.telemetry().elapsedMs()).sorted().toList();
        return values.get(values.size() / 2);
    }
    private static List<String> ids(List<ExecutionTelemetrySample> samples) throws Exception {
        List<String> result = new ArrayList<>();
        for (ExecutionTelemetrySample sample : samples)
            result.add(ExecutionJson.sha256(sample.bytes()));
        return List.copyOf(result);
    }
    private static String q(String value) { return ExecutionJson.quote(value); }
    private static String n(Long value) { return ExecutionJson.nullable(value); }

    record Summary(ExecutionRegressionPolicy policy, List<Signal> signals) {
        long count(String classification) {
            return signals.stream().filter(value -> classification.equals(value.classification())).count();
        }
    }
    record Signal(ExecutionTelemetrySample identity, int sampleCount, int baselineCount,
            int recentCount, Long baselineMedian, Long recentMedian, Long ratioPercent,
            String classification, List<String> baselineSampleSha256,
            List<String> recentSampleSha256) {
        String json() { return "{\"lane\":" + q(identity.lane()) + ",\"workload_id\":"
                + q(identity.workloadId()) + ",\"executor_version\":" + q(identity.executorVersion())
                + ",\"backend\":" + q(identity.backend()) + ",\"os\":" + q(identity.os())
                + ",\"architecture\":" + q(identity.architecture())
                + ",\"runtime_capability\":" + q(identity.runtimeCapability())
                + ",\"sample_count\":" + sampleCount + ",\"baseline_count\":" + baselineCount
                + ",\"recent_count\":" + recentCount + ",\"baseline_median_ms\":"
                + n(baselineMedian) + ",\"recent_median_ms\":" + n(recentMedian)
                + ",\"ratio_percent\":" + n(ratioPercent) + ",\"classification\":"
                + q(classification) + "}"; }
    }

    static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-performance-signals-").toAbsolutePath().normalize();
        try {
            ExecutionRegressionPolicy policy = new ExecutionRegressionPolicy("campaign",
                    "campaign-worker", "exited", "elapsed_ms", 3, 3, 150);
            add(root, "increase", 1, 100, 100, 100, 160, 150, 170);
            add(root, "decrease", 20, 150, 160, 170, 100, 100, 100);
            add(root, "steady", 40, 100, 110, 120, 120, 130, 140);
            add(root, "short", 60, 100, 200);
            append(root, 80, "tooling", "ignored", "structural", "exited", 999);
            append(root, 81, "campaign", "ignored-failure", "campaign-worker", "timed_out", 999);
            Summary summary = summarize(root, policy);
            String json = new String(summaryBytes(summary), java.nio.charset.StandardCharsets.UTF_8);
            require(summary.signals().size() == 4 && summary.count("increase") == 1
                    && summary.count("decrease") == 1 && summary.count("within-band") == 1
                    && summary.count("insufficient-history") == 1, "performance signal classification drift");
            require(json.contains("\"verdict_authority\":false")
                    && json.contains("\"admission_effect\":false")
                    && !json.contains("working_directory") && !json.contains("argv"),
                    "performance signal authority or sanitization drift");
            System.out.println("execution performance signals self-test passed");
        } finally { delete(root); }
    }

    private static void add(Path root, String workload, long start, long... elapsed) throws Exception {
        for (int index = elapsed.length - 1; index >= 0; index--)
            append(root, start + index, "campaign", workload, "campaign-worker", "exited", elapsed[index]);
    }
    private static void append(Path root, long id, String lane, String workload, String capability,
            String termination, long elapsed) throws Exception {
        ExecutionTelemetry telemetry = new ExecutionTelemetry("0.1.0", "windows_job", termination,
                elapsed, 1L, elapsed - 1L, 4_096L, "job_commit", 2L, null, null, null, null,
                null, null, null, null, null, 7L, 8L, false, false);
        ExecutionTelemetryHistory.append(root, ExecutionTelemetrySample.create(
                "00000000-0000-0000-0000-%012d".formatted(id), id, lane, workload,
                "windows", "amd64", capability, telemetry));
    }
    private static void delete(Path root) throws Exception { try (var paths = Files.walk(root)) {
        for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path); } }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
}
