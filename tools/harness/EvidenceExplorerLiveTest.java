import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

/** Authority separation, queue inspection, admission, and escaping fixtures. */
final class EvidenceExplorerLiveTest {
    private EvidenceExplorerLiveTest() { }

    static void selfTest() throws Exception {
        EvidenceExplorerExecutionLinks.selfTest();
        Path root = Files.createTempDirectory("worldline-evidence-live-");
        try {
            long now = System.currentTimeMillis(); Path queue = root.resolve("queue");
            Files.createDirectories(queue);
            ticket(queue, 1, ProcessHandle.current().pid(), ProcessHandle.current().info()
                    .startInstant().orElseThrow().toEpochMilli(), now - 200L);
            ticket(queue, 2, 999_999_999L, 1L, now - 500L);
            Files.writeString(queue.resolve("partial.tmp-fixture"), "partial", StandardCharsets.UTF_8);
            EvidenceExplorerLiveSnapshot.Queue observed = EvidenceExplorerLiveSnapshot.queue(
                    queue, "fixture", now);
            require(observed.waiting() == 2 && observed.live() == 1 && observed.abandoned() == 1
                    && observed.partial() == 1 && observed.oldestWaitMs() == 500L,
                    "live queue classification drift");
            Path admission = root.resolve("admission.properties");
            Files.writeString(admission, "schema=worldline.telemetry-admission.v1\nos=windows\n"
                    + "architecture=amd64\nruntime.capability=candidate-worker\nsample.count=2\n"
                    + "observed.peak.memory.bytes=100\nminimum.worker.memory.bytes=125\n"
                    + "direction=lower-only\n", StandardCharsets.UTF_8);
            require(EvidenceExplorerLiveSnapshot.admission(admission).minimumWorkerMemoryBytes() == 125L,
                    "live admission parsing drift");
            EvidenceExplorerLiveSnapshot snapshot = snapshot(observed);
            String json = EvidenceExplorerLiveFormat.json(snapshot);
            String html = EvidenceExplorerLiveFormat.html(snapshot);
            require(json.contains("\"verdict_authority\":false")
                    && json.contains("\"schema\":\"worldline.evidence-explorer-live.v4\"")
                    && json.contains("\"ephemeral_state_included\":true")
                    && json.contains("\"regression_triage_cases\":1")
                    && json.contains("\"native_campaign_failure_clusters\"")
                    && json.contains("\"native_unclassified_workload_failures\":1")
                    && json.contains("\"stdout_truncated\":false")
                    && html.contains("repeat-exact-profile")
                    && html.contains("workload-nonzero-exit")
                    && html.contains("unsafe &lt;reason&gt;") && !html.contains("unsafe <reason>"),
                    "live overlay authority or escaping drift");
            System.out.println("  evidence explorer live self-test: passed");
        } finally { SafeTreeDelete.delete(root); }
    }

    private static EvidenceExplorerLiveSnapshot snapshot(EvidenceExplorerLiveSnapshot.Queue queue) {
        List<String> baseline = digests(1), recent = digests(4);
        String material = "schema=worldline.execution-regression-triage-case.v1\n"
                + "metric=elapsed_ms\nthreshold=150\nlane=campaign\nworkload=fixture-profile\n"
                + "executor=0.1.0\nbackend=windows_job\nos=windows\narchitecture=amd64\n"
                + "runtime_capability=campaign-worker\ntermination=exited\nkind=performance-increase\n"
                + "baseline=" + String.join(",", baseline) + "\nrecent="
                + String.join(",", recent) + "\n";
        String triageJson = "{\"schema\":\"worldline.execution-regression-triage.v1\","
                + "\"verdict_authority\":false,\"admission_effect\":false,"
                + "\"automatic_execution\":false,\"metric\":\"elapsed_ms\","
                + "\"change_threshold_percent\":150,\"scope_termination\":\"exited\","
                + "\"case_count\":1,"
                + "\"performance_increase_count\":1,\"data_quality_count\":0,\"cases\":[{"
                + "\"id\":\"" + sha256(material) + "\",\"lane\":\"campaign\","
                + "\"workload_id\":\"fixture-profile\",\"executor_version\":\"0.1.0\","
                + "\"backend\":\"windows_job\",\"os\":\"windows\",\"architecture\":\"amd64\","
                + "\"runtime_capability\":\"campaign-worker\",\"kind\":\"performance-increase\","
                + "\"disposition\":\"investigation-candidate\",\"sample_count\":6,"
                + "\"baseline_median_ms\":100,\"recent_median_ms\":160,\"ratio_percent\":160,"
                + "\"baseline_sample_sha256\":" + strings(baseline)
                + ",\"recent_sample_sha256\":" + strings(recent) + ","
                + "\"suggested_next_step\":\"repeat-exact-profile\"}]}";
        String execution = "{\"schema\":\"worldline.execution-evidence-overlay.v3\","
                + "\"telemetry\":{\"schema\":\"worldline.execution-telemetry-profiles.v1\","
                + "\"sample_count\":0,\"profiles\":[]},\"failure_clusters\":{"
                + "\"schema\":\"worldline.execution-failure-clusters.v1\","
                + "\"failure_count\":0,\"clusters\":[]},\"performance_signals\":{"
                + "\"schema\":\"worldline.execution-performance-signals.v1\","
                + "\"verdict_authority\":false,\"admission_effect\":false,\"metric\":\"elapsed_ms\","
                + "\"baseline_window_samples\":3,\"recent_window_samples\":3,"
                + "\"change_threshold_percent\":150,\"signal_count\":0,\"increase_count\":0,"
                + "\"insufficient_history_count\":0,\"signals\":[]},\"regression_triage\":"
                + triageJson + "}";
        EvidenceExplorerRegressionTriage.Snapshot triage = EvidenceExplorerRegressionTriage.parse(
                MiniJson.object(triageJson));
        try {
            EvidenceExplorerRegressionTriage.parse(MiniJson.object(
                    triageJson.replace(sha256(material), "c".repeat(64))));
            throw new IllegalStateException("changed regression triage ID was accepted");
        } catch (IllegalArgumentException expected) { }
        EvidenceExplorerLiveSnapshot.Signal signal = new EvidenceExplorerLiveSnapshot.Signal(
                "fixture-profile", "0.1.0", "windows_job", "windows", "amd64", 6L, 3L, 3L,
                100L, 160L, 160L, "increase");
        EvidenceExplorerLiveSnapshot.Performance performance =
                new EvidenceExplorerLiveSnapshot.Performance("elapsed_ms", 150L,
                        1L, 1L, 0L, List.of(signal));
        EvidenceExplorerRegressionTriage.requireMatches(performance, triage);
        EvidenceExplorerLiveSnapshot.Signal mismatch = new EvidenceExplorerLiveSnapshot.Signal(
                "fixture-profile", "0.1.0", "windows_job", "windows", "amd64", 6L, 3L, 3L,
                100L, 161L, 161L, "increase");
        try {
            EvidenceExplorerRegressionTriage.requireMatches(new EvidenceExplorerLiveSnapshot.Performance(
                    "elapsed_ms", 150L, 1L, 1L, 0L, List.of(mismatch)), triage);
            throw new IllegalStateException("mismatched regression triage was accepted");
        } catch (IllegalArgumentException expected) { }
        EvidenceExplorerLiveSnapshot.Execution values = new EvidenceExplorerLiveSnapshot.Execution(
                execution, 0, 0, performance, triage);
        EvidenceExplorerLiveSnapshot.CacheDecision cache = new EvidenceExplorerLiveSnapshot.CacheDecision(
                "sample", "a".repeat(64), true, false, true, "reuse-pin",
                "unsafe <reason>", null);
        EvidenceExplorerExecutionLinks.Link link = new EvidenceExplorerExecutionLinks.Link(
                "d".repeat(64), "fixture", "e".repeat(64), 0, "windows_job", "0.1.0",
                "exited", 1L, 7L, 8L, 1L, false, false, 0L);
        EvidenceExplorerExecutionLinks.Snapshot links = new EvidenceExplorerExecutionLinks.Snapshot(
                1, 1, 1, 0, 0, 1, 0, 1, 0, 0, List.of(link));
        return new EvidenceExplorerLiveSnapshot(1L, "b".repeat(64), values, links,
                NativeCampaignFailureClusters.summarize(links),
                null, null, List.of(queue), List.of(cache));
    }

    private static void ticket(Path root, long sequence, long pid, long started, long created)
            throws Exception {
        String name = "%020d-%d-00000000-0000-0000-0000-%012d.ticket".formatted(
                sequence, pid, sequence);
        Files.writeString(root.resolve(name), "pid=" + pid + "\nprocess-start=" + started
                + "\ncreated=" + Instant.ofEpochMilli(created) + "\n", StandardCharsets.UTF_8);
    }
    private static List<String> digests(int start) {
        return java.util.stream.IntStream.range(start, start + 3).mapToObj(
                value -> "%064x".formatted(value)).toList();
    }
    private static String strings(List<String> values) {
        return "[\"" + String.join("\",\"", values) + "\"]";
    }
    private static String sha256(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(
                value.getBytes(StandardCharsets.UTF_8))); }
        catch (java.security.NoSuchAlgorithmException error) { throw new IllegalStateException(error); }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
}
