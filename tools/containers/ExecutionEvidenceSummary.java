import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Combines validated observational executor profiles and failure clusters. */
public final class ExecutionEvidenceSummary {
    private ExecutionEvidenceSummary() { }

    public static void main(String[] arguments) {
        try {
            require(arguments.length == 0, "usage: ExecutionEvidenceSummary");
            Path repository = Path.of("").toAbsolutePath().normalize();
            Path root = repository.resolve(".worldline/executor-telemetry");
            System.out.print(summary(repository, root));
        } catch (Exception error) {
            System.err.println("execution evidence summary failed: " + error.getMessage());
            System.exit(1);
        }
    }

    static String summary(Path repository, Path root) throws Exception {
        String telemetry = new String(ExecutionTelemetryHistory.summaryBytes(
                ExecutionTelemetryHistory.profiles(root)), StandardCharsets.UTF_8).trim();
        String failures = new String(ExecutionFailureClusters.summaryBytes(
                ExecutionFailureClusters.clusters(root)), StandardCharsets.UTF_8).trim();
        String performance = new String(ExecutionPerformanceSignals.summaryBytes(
                ExecutionPerformanceSignals.summarize(root,
                        ExecutionRegressionPolicy.load(repository))), StandardCharsets.UTF_8).trim();
        return "{\"schema\":\"worldline.execution-evidence-overlay.v2\",\"telemetry\":"
                + telemetry + ",\"failure_clusters\":" + failures
                + ",\"performance_signals\":" + performance + "}\n";
    }

    static void selfTest() throws Exception {
        ExecutionPerformanceSignals.selfTest();
        Path root = Files.createTempDirectory("worldline-execution-evidence-")
                .toAbsolutePath().normalize();
        try {
            Files.createDirectories(root.resolve("tools/containers"));
            Files.copy(Path.of("tools/containers/execution-regression.properties"),
                    root.resolve("tools/containers/execution-regression.properties"));
            Path telemetryRoot = root.resolve("telemetry");
            ExecutionTelemetry passed = telemetry("exited"), failed = telemetry("timed_out");
            ExecutionTelemetryHistory.append(telemetryRoot, ExecutionTelemetrySample.create(
                    "00000000-0000-0000-0000-000000000001", 1L, "tooling", "fixture",
                    "windows", "amd64", "structural", passed));
            ExecutionTelemetryHistory.append(telemetryRoot, ExecutionTelemetrySample.create(
                    "00000000-0000-0000-0000-000000000002", 2L, "tooling", "fixture",
                    "windows", "amd64", "structural", failed));
            String value = summary(root, telemetryRoot);
            require(value.contains("\"sample_count\":2")
                    && value.contains("\"failure_count\":1")
                    && value.contains("\"performance_signals\":{")
                    && value.contains("\"verdict_authority\":false"),
                    "execution evidence counts or authority drifted");
            System.out.println("execution evidence summary self-test passed");
        } finally {
            try (var paths = Files.walk(root)) {
                for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList())
                    Files.deleteIfExists(path);
            }
        }
    }

    private static ExecutionTelemetry telemetry(String termination) {
        return new ExecutionTelemetry("0.1.0", "windows_job", termination, 12, 2, 10,
                4_096L, "job_commit", 1L, null, 3L, 4L, 5L, 6L, null, null,
                null, null, 7L, 8L, false, false);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
}
