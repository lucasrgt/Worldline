package worldline.testkit;

import worldline.test.WorldlineAssertionError;

/** Internal result of one fresh-runtime attempt. */
final class AttemptOutcome {
    final Throwable failure;
    final String skipReason;
    final TestExecutionContext context;
    final long durationMillis;

    AttemptOutcome(Throwable failure, String skipReason, TestExecutionContext context, long durationMillis) {
        this.failure = failure; this.skipReason = skipReason; this.context = context;
        this.durationMillis = durationMillis;
    }
    String expected() {
        return failure instanceof WorldlineAssertionError
                ? ((WorldlineAssertionError) failure).expected() : null;
    }
    String received() {
        return failure instanceof WorldlineAssertionError
                ? ((WorldlineAssertionError) failure).received() : null;
    }
}
