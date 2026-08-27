import java.util.List;
import java.util.Set;

/** Proves correctly anticipated duplicates are not treated as new systemic failures. */
final class WaveMetricSelfTest {
    private WaveMetricSelfTest() {
    }

    static void selfTest() {
        WaveCensus.Row row = new WaveCensus.Row("m2-duplicate",
                ",\"state\":\"REJECTED\",\"prevention_interlock\":\"pre-dispatch\"");
        RejectionRegistry.Entry entry = new RejectionRegistry.Entry(row.id(), "duplicate",
                Set.of(), "harness-process-defect", "NYA-TEST", "archive.zip", "0".repeat(64),
                false, "", "", "", "m1-canonical");
        WaveSelfImprovement.Metrics metrics = WaveSelfImprovement.Metrics.of(
                List.of(row), List.of(entry));
        require(metrics.correctlyAnticipated == 1 && metrics.equivalentBlockedByCheck == 1
                && metrics.historicalEquivalentRelaunches == 0
                && !WaveMetricPolicy.newSystemic(metrics, WaveSelfImprovement.Metrics.empty()),
                "anticipated duplicate was treated as a systemic relaunch");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
