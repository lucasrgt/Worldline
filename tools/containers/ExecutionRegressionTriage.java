import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Builds non-authoritative investigation candidates from performance signals. */
final class ExecutionRegressionTriage {
    private ExecutionRegressionTriage() { }

    static Summary summarize(ExecutionPerformanceSignals.Summary signals) throws Exception {
        List<Case> cases = new ArrayList<>();
        for (ExecutionPerformanceSignals.Signal signal : signals.signals()) {
            String kind = switch (signal.classification()) {
                case "increase" -> "performance-increase";
                case "zero-baseline" -> "baseline-data-quality";
                default -> null;
            };
            if (kind != null) cases.add(aCase(signals.policy(), signal, kind));
        }
        cases.sort(Comparator.comparing(Case::id));
        return new Summary(signals.policy(), List.copyOf(cases));
    }

    static byte[] summaryBytes(Summary summary) {
        StringBuilder json = new StringBuilder("{\"schema\":\"worldline.execution-regression-triage.v1\"")
                .append(",\"verdict_authority\":false,\"admission_effect\":false")
                .append(",\"automatic_execution\":false,\"metric\":")
                .append(q(summary.policy().metric())).append(",\"change_threshold_percent\":")
                .append(summary.policy().thresholdPercent()).append(",\"scope_termination\":")
                .append(q(summary.policy().termination())).append(",\"case_count\":")
                .append(summary.cases().size()).append(",\"performance_increase_count\":")
                .append(summary.count("performance-increase")).append(",\"data_quality_count\":")
                .append(summary.count("baseline-data-quality")).append(",\"cases\":[");
        for (int index = 0; index < summary.cases().size(); index++) {
            if (index > 0) json.append(','); json.append(summary.cases().get(index).json());
        }
        return ExecutionJson.bytes(json.append("]}").toString());
    }

    private static Case aCase(ExecutionRegressionPolicy policy,
            ExecutionPerformanceSignals.Signal signal, String kind) throws Exception {
        ExecutionTelemetrySample identity = signal.identity();
        String next = kind.equals("performance-increase")
                ? "repeat-exact-profile" : "inspect-profile-history";
        String material = "schema=worldline.execution-regression-triage-case.v1\n"
                + "metric=" + policy.metric() + "\nthreshold=" + policy.thresholdPercent()
                + "\nlane=" + identity.lane() + "\nworkload=" + identity.workloadId()
                + "\nexecutor=" + identity.executorVersion() + "\nbackend=" + identity.backend()
                + "\nos=" + identity.os() + "\narchitecture=" + identity.architecture()
                + "\nruntime_capability=" + identity.runtimeCapability()
                + "\ntermination=" + policy.termination() + "\nkind=" + kind
                + "\nbaseline=" + String.join(",", signal.baselineSampleSha256())
                + "\nrecent=" + String.join(",", signal.recentSampleSha256()) + "\n";
        String id = ExecutionJson.sha256(material.getBytes(StandardCharsets.UTF_8));
        return new Case(id, identity, kind, signal.sampleCount(), signal.baselineMedian(),
                signal.recentMedian(), signal.ratioPercent(), signal.baselineSampleSha256(),
                signal.recentSampleSha256(), next);
    }

    record Summary(ExecutionRegressionPolicy policy, List<Case> cases) {
        long count(String kind) { return cases.stream().filter(value -> kind.equals(value.kind())).count(); }
    }
    record Case(String id, ExecutionTelemetrySample identity, String kind, int sampleCount,
            Long baselineMedian, Long recentMedian, Long ratioPercent,
            List<String> baselineSampleSha256, List<String> recentSampleSha256,
            String suggestedNextStep) {
        String json() { return "{\"id\":" + q(id) + ",\"lane\":" + q(identity.lane())
                + ",\"workload_id\":" + q(identity.workloadId()) + ",\"executor_version\":"
                + q(identity.executorVersion()) + ",\"backend\":" + q(identity.backend())
                + ",\"os\":" + q(identity.os()) + ",\"architecture\":"
                + q(identity.architecture()) + ",\"runtime_capability\":"
                + q(identity.runtimeCapability()) + ",\"kind\":" + q(kind)
                + ",\"disposition\":\"investigation-candidate\",\"sample_count\":" + sampleCount
                + ",\"baseline_median_ms\":" + n(baselineMedian) + ",\"recent_median_ms\":"
                + n(recentMedian) + ",\"ratio_percent\":" + n(ratioPercent)
                + ",\"baseline_sample_sha256\":" + ExecutionJson.strings(baselineSampleSha256)
                + ",\"recent_sample_sha256\":" + ExecutionJson.strings(recentSampleSha256)
                + ",\"suggested_next_step\":" + q(suggestedNextStep) + "}"; }
    }

    static void selfTest() throws Exception {
        ExecutionRegressionPolicy policy = new ExecutionRegressionPolicy("campaign",
                "campaign-worker", "exited", "elapsed_ms", 3, 3, 150);
        ExecutionTelemetrySample identity = sample();
        List<String> baseline = digests(1), recent = digests(4);
        ExecutionPerformanceSignals.Signal increase = new ExecutionPerformanceSignals.Signal(
                identity, 6, 3, 3, 100L, 160L, 160L, "increase", baseline, recent);
        ExecutionPerformanceSignals.Signal zero = new ExecutionPerformanceSignals.Signal(
                identity, 6, 3, 3, 0L, 1L, null, "zero-baseline", baseline, recent);
        ExecutionPerformanceSignals.Signal steady = new ExecutionPerformanceSignals.Signal(
                identity, 6, 3, 3, 100L, 110L, 110L, "within-band", baseline, recent);
        Summary value = summarize(new ExecutionPerformanceSignals.Summary(
                policy, List.of(steady, zero, increase)));
        String first = new String(summaryBytes(value), StandardCharsets.UTF_8);
        String second = new String(summaryBytes(summarize(new ExecutionPerformanceSignals.Summary(
                policy, List.of(increase, steady, zero)))), StandardCharsets.UTF_8);
        require(value.cases().size() == 2 && value.count("performance-increase") == 1
                && value.count("baseline-data-quality") == 1 && first.equals(second),
                "regression triage selection or determinism drifted");
        require(first.contains("\"verdict_authority\":false")
                && first.contains("\"admission_effect\":false")
                && first.contains("\"automatic_execution\":false")
                && !first.contains("argv") && !first.contains("working_directory"),
                "regression triage authority or sanitization drifted");
        System.out.println("execution regression triage self-test passed");
    }

    private static List<String> digests(int start) {
        return java.util.stream.IntStream.range(start, start + 3).mapToObj(
                value -> "%064x".formatted(value)).toList();
    }
    private static ExecutionTelemetrySample sample() {
        ExecutionTelemetry telemetry = new ExecutionTelemetry("0.1.0", "windows_job", "exited",
                1L, 0L, 1L, 1L, "job_commit", 1L, null, null, null, null, null,
                null, null, null, null, 0L, 0L, false, false);
        return ExecutionTelemetrySample.create("00000000-0000-0000-0000-000000000001", 1L,
                "campaign", "fixture-profile", "windows", "amd64", "campaign-worker", telemetry);
    }
    private static String q(String value) { return ExecutionJson.quote(value); }
    private static String n(Long value) { return ExecutionJson.nullable(value); }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
}
