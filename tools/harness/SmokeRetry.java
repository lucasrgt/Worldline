import java.util.concurrent.atomic.AtomicLong;

/** One bounded, observable retry for the known official-runtime EOF failure mode. */
final class SmokeRetry {
    private static final AtomicLong ATTEMPTS = new AtomicLong();
    private static final AtomicLong RETRIES = new AtomicLong();
    private static final AtomicLong FAILURES = new AtomicLong();
    private static final AtomicLong POLICY_CALLS = new AtomicLong();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.println(
                "WORLDLINE_FLAKE_TELEMETRY=" + telemetry()), "worldline-flake-telemetry"));
    }

    private SmokeRetry() {}

    @FunctionalInterface
    interface Attempt<T> { T run(int attempt) throws Exception; }

    static <T> T onceOnEof(String smokeId, Attempt<T> action) throws Exception {
        if (smokeId == null || smokeId.isBlank() || action == null)
            throw new IllegalArgumentException("invalid smoke retry request");
        Exception last = null;
        for (int attempt = 1; attempt <= 2; attempt++) {
            ATTEMPTS.incrementAndGet();
            try { return action.run(attempt); }
            catch (Exception error) {
                last = error;
                if (attempt == 1 && SmokeSupport.eof(error)) {
                    RETRIES.incrementAndGet();
                    System.out.println("WORLDLINE_FLAKE_RETRY=id=" + smokeId
                            + ";cause=eof;next-attempt=2");
                    long backoff = backoffMillis();
                    if (backoff > 0L) Thread.sleep(backoff);
                    continue;
                }
                FAILURES.incrementAndGet(); throw error;
            }
        }
        FAILURES.incrementAndGet(); throw last;
    }

    /** Owns the retry decision for legacy loops while preserving their cleanup placement. */
    static void afterEofFailure(int attempt, int maximumRetries, Exception error) throws Exception {
        afterEofFailure(attempt, maximumRetries, error, backoffMillis());
    }

    static void afterEofFailure(int attempt, int maximumRetries, Exception error,
            long backoffMillis) throws Exception {
        if (attempt < 0 || maximumRetries < 0 || backoffMillis < 0L || error == null)
            throw new IllegalArgumentException("invalid EOF retry decision");
        POLICY_CALLS.incrementAndGet();
        if (attempt >= maximumRetries || !SmokeSupport.eof(error)) {
            FAILURES.incrementAndGet(); throw error;
        }
        RETRIES.incrementAndGet();
        System.out.println("WORLDLINE_FLAKE_RETRY=caller=" + caller()
                + ";cause=eof;next-attempt=" + (attempt + 2));
        if (backoffMillis > 0L) Thread.sleep(backoffMillis);
    }

    static String telemetry() {
        return "attempts=" + ATTEMPTS.get() + ";retries=" + RETRIES.get()
                + ";failures=" + FAILURES.get() + ";policy-calls=" + POLICY_CALLS.get();
    }

    private static String caller() {
        return StackWalker.getInstance().walk(frames -> frames
                .map(StackWalker.StackFrame::getClassName)
                .filter(name -> !name.equals(SmokeRetry.class.getName()))
                .findFirst().orElse("unknown"));
    }

    private static long backoffMillis() {
        String raw = System.getenv("WORLDLINE_SMOKE_EOF_BACKOFF_MILLIS");
        if (raw == null || raw.isBlank()) return 5_000L;
        try {
            long value = Long.parseLong(raw);
            if (value < 0L || value > 60_000L) throw new NumberFormatException();
            return value;
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException(
                    "WORLDLINE_SMOKE_EOF_BACKOFF_MILLIS must be 0..60000");
        }
    }
}
