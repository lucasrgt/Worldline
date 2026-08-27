/** Applies adaptive-parallelism policy to exact wave metrics. */
final class WaveMetricPolicy {
    private WaveMetricPolicy() {
    }

    static boolean newSystemic(WaveSelfImprovement.Metrics now,
            WaveSelfImprovement.Metrics prior) {
        int harness = now.rejectionClasses.getOrDefault("harness-process-defect", 0)
                - now.correctlyAnticipated;
        int priorHarness = prior.rejectionClasses.getOrDefault("harness-process-defect", 0)
                - prior.correctlyAnticipated;
        return now.hardBlockers > 0 || now.unownedRetryable > 0
                || now.retryable > prior.retryable || harness > priorHarness;
    }
}
