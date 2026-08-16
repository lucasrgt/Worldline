package worldline.b173;

/** Mutable deterministic millisecond clock advanced by controlled ticks. */
public final class B173VirtualClock {
    private long millis;

    B173VirtualClock(long initialMillis) { millis = initialMillis; }

    public synchronized long millis() { return millis; }

    public synchronized void advance(long deltaMillis) {
        if (deltaMillis < 0L) throw new IllegalArgumentException("clock delta must not be negative");
        millis = Math.addExact(millis, deltaMillis);
    }

    public synchronized void set(long value) { millis = value; }
}
