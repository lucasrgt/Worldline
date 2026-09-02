import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Rebuildable clustering over sanitized executor telemetry failures. */
public final class ExecutionFailureClusters {
    private ExecutionFailureClusters() { }

    public static void main(String[] arguments) {
        try {
            if (List.of(arguments).equals(List.of("--self-test"))) { selfTest(); return; }
            require(List.of(arguments).equals(List.of("summarize")),
                    "usage: ExecutionFailureClusters summarize|--self-test");
            Path root = Path.of(".worldline/executor-telemetry").toAbsolutePath().normalize();
            System.out.write(summaryBytes(clusters(root)));
        } catch (Exception error) {
            System.err.println("execution failure clustering failed: " + error.getMessage());
            System.exit(1);
        }
    }

    static List<Cluster> clusters(Path root) throws Exception {
        Map<String, Cluster> grouped = new LinkedHashMap<>();
        for (ExecutionTelemetrySample sample : ExecutionTelemetryHistory.samples(root)) {
            String failureClass = ExecutionFailureClassifier.classify(sample.telemetry());
            if (failureClass.equals("none")) continue;
            Cluster candidate = new Cluster(sample, failureClass, 1L);
            grouped.merge(candidate.key(), candidate,
                    (prior, ignored) -> new Cluster(prior.sample(), prior.failureClass(),
                            Math.addExact(prior.count(), 1L)));
        }
        return grouped.values().stream().sorted(Comparator.comparing(Cluster::key)).toList();
    }

    static byte[] summaryBytes(List<Cluster> clusters) {
        long samples = clusters.stream().mapToLong(Cluster::count).sum();
        StringBuilder json = new StringBuilder("{\"schema\":\"worldline.execution-failure-clusters.v1\",\"failure_count\":")
                .append(samples).append(",\"clusters\":[");
        for (int index = 0; index < clusters.size(); index++) {
            if (index > 0) json.append(','); json.append(clusters.get(index).json());
        }
        return ExecutionJson.bytes(json.append("]}").toString());
    }

    static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-failure-clusters-").toAbsolutePath().normalize();
        try {
            ExecutionTelemetry timeout = telemetry("timed_out", null, null);
            ExecutionTelemetry oom = telemetry("crashed", 1L, 1L);
            ExecutionTelemetryHistory.append(root, sample("00000000-0000-0000-0000-000000000001", timeout));
            ExecutionTelemetryHistory.append(root, sample("00000000-0000-0000-0000-000000000002", timeout));
            ExecutionTelemetryHistory.append(root, sample("00000000-0000-0000-0000-000000000003", oom));
            List<Cluster> clusters = clusters(root);
            require(clusters.size() == 2 && clusters.stream().mapToLong(Cluster::count).sum() == 3,
                    "failure cluster grouping drift");
            require(!ExecutionFailureClassifier.explicit(ExecutionFailureClassifier.Boundary.PROVIDER_FAILURE)
                    .equals(ExecutionFailureClassifier.explicit(ExecutionFailureClassifier.Boundary.ORACLE_MISMATCH)),
                    "provider and oracle failures merged");
            require(ExecutionFailureClassifier.domain("oracle-mismatch").equals("behavioral")
                    && ExecutionFailureClassifier.domain("escaped-descendant").equals("executor-infrastructure"),
                    "failure domains drifted");
            System.out.println("execution failure clustering self-test passed");
        } finally { delete(root); }
    }

    private static ExecutionTelemetrySample sample(String id, ExecutionTelemetry telemetry) {
        return ExecutionTelemetrySample.create(id, 1, "candidate", "m1-fixture", "windows",
                "amd64", "candidate-worker", telemetry);
    }
    private static ExecutionTelemetry telemetry(String termination, Long memoryEvents, Long oomKills) {
        return new ExecutionTelemetry("0.1.0", "windows_job", termination, 12, 2, 10,
                4096L, "job_commit", 1L, null, 3L, 4L, 5L, 6L, memoryEvents, oomKills,
                null, null, 7, 8, false, false);
    }

    record Cluster(ExecutionTelemetrySample sample, String failureClass, long count) {
        String key() { return String.join("\u0000", failureClass, sample.profileKey()); }
        String json() { return "{\"domain\":" + q(ExecutionFailureClassifier.domain(failureClass))
                + ",\"failure_class\":" + q(failureClass) + ",\"lane\":" + q(sample.lane())
                + ",\"workload_id\":" + q(sample.workloadId()) + ",\"executor_version\":"
                + q(sample.executorVersion()) + ",\"backend\":" + q(sample.backend())
                + ",\"os\":" + q(sample.os()) + ",\"architecture\":" + q(sample.architecture())
                + ",\"runtime_capability\":" + q(sample.runtimeCapability()) + ",\"count\":" + count + "}"; }
    }

    private static String q(String value) { return ExecutionJson.quote(value); }
    private static void delete(Path root) throws Exception {
        Path samples = root.resolve("samples");
        if (Files.isDirectory(samples)) try (var paths = Files.newDirectoryStream(samples)) {
            for (Path path : paths) Files.deleteIfExists(path);
        }
        Files.deleteIfExists(samples); Files.deleteIfExists(root);
    }
    private static void require(boolean value, String message) { ExecutionJson.require(value, message); }
}
