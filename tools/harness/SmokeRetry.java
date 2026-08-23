import java.util.concurrent.atomic.AtomicLong;

/** One bounded, observable retry for the known official-runtime EOF failure mode. */
final class SmokeRetry {
    private static final AtomicLong ATTEMPTS = new AtomicLong();
    private static final AtomicLong RETRIES = new AtomicLong();
    private static final AtomicLong FAILURES = new AtomicLong();

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

    static String telemetry() {
        return "attempts=" + ATTEMPTS.get() + ";retries=" + RETRIES.get()
                + ";failures=" + FAILURES.get();
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
