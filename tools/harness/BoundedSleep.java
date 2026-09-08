/** Central interruption-preserving sleep boundary for harness polling and retry backoff. */
final class BoundedSleep {
    private static final long MAXIMUM_MILLIS = 60_000L;

    private BoundedSleep() { }

    static void millis(long duration) throws InterruptedException {
        if (duration < 0L || duration > MAXIMUM_MILLIS)
            throw new IllegalArgumentException("sleep duration must be between 0 and 60000ms");
        Thread.sleep(duration);
    }
}
