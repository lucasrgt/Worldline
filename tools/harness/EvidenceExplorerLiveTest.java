import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
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
                    && json.contains("\"ephemeral_state_included\":true")
                    && html.contains("unsafe &lt;reason&gt;") && !html.contains("unsafe <reason>"),
                    "live overlay authority or escaping drift");
            System.out.println("  evidence explorer live self-test: passed");
        } finally { SafeTreeDelete.delete(root); }
    }

    private static EvidenceExplorerLiveSnapshot snapshot(EvidenceExplorerLiveSnapshot.Queue queue) {
        String execution = "{\"schema\":\"worldline.execution-evidence-overlay.v2\","
                + "\"telemetry\":{\"schema\":\"worldline.execution-telemetry-profiles.v1\","
                + "\"sample_count\":0,\"profiles\":[]},\"failure_clusters\":{"
                + "\"schema\":\"worldline.execution-failure-clusters.v1\","
                + "\"failure_count\":0,\"clusters\":[]},\"performance_signals\":{"
                + "\"schema\":\"worldline.execution-performance-signals.v1\","
                + "\"verdict_authority\":false,\"admission_effect\":false,\"metric\":\"elapsed_ms\","
                + "\"baseline_window_samples\":3,\"recent_window_samples\":3,"
                + "\"change_threshold_percent\":150,\"signal_count\":0,\"increase_count\":0,"
                + "\"insufficient_history_count\":0,\"signals\":[]}}";
        EvidenceExplorerLiveSnapshot.Execution values = new EvidenceExplorerLiveSnapshot.Execution(
                execution, 0, 0, new EvidenceExplorerLiveSnapshot.Performance(0, 0, 0, List.of()));
        EvidenceExplorerLiveSnapshot.CacheDecision cache = new EvidenceExplorerLiveSnapshot.CacheDecision(
                "sample", "a".repeat(64), true, false, true, "reuse-pin",
                "unsafe <reason>", null);
        return new EvidenceExplorerLiveSnapshot(1L, "b".repeat(64), values,
                new EvidenceExplorerExecutionLinks.Snapshot(0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, List.of()), null, null, List.of(queue), List.of(cache));
    }

    private static void ticket(Path root, long sequence, long pid, long started, long created)
            throws Exception {
        String name = "%020d-%d-00000000-0000-0000-0000-%012d.ticket".formatted(
                sequence, pid, sequence);
        Files.writeString(root.resolve(name), "pid=" + pid + "\nprocess-start=" + started
                + "\ncreated=" + Instant.ofEpochMilli(created) + "\n", StandardCharsets.UTF_8);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
}
