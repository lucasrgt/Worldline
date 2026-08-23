/** Public class-loader boundary for exceptional source-launched coordinators. */
public final class SmokeRetryBoundary {
    private SmokeRetryBoundary() { }

    public static void afterEofFailure(int attempt, int maximumRetries, Exception error)
            throws Exception {
        SmokeRetry.afterEofFailure(attempt, maximumRetries, error);
    }
}
