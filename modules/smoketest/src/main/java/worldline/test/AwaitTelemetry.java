package worldline.test;

/** Immutable deterministic counters for condition-based smoke waits. */
public final class AwaitTelemetry {
    private final long waits;
    private final long polls;
    private final long failures;

    AwaitTelemetry(long waits, long polls, long failures) {
        this.waits = waits;
        this.polls = polls;
        this.failures = failures;
    }

    public long waits() { return waits; }
    public long polls() { return polls; }
    public long failures() { return failures; }

    public String evidence() {
        return "waits=" + waits + ";polls=" + polls + ";failures=" + failures;
    }

    @Override public boolean equals(Object other) {
        if (!(other instanceof AwaitTelemetry)) return false;
        AwaitTelemetry value = (AwaitTelemetry) other;
        return waits == value.waits && polls == value.polls && failures == value.failures;
    }

    @Override public int hashCode() {
        long value = waits * 31L * 31L + polls * 31L + failures;
        return (int) (value ^ value >>> 32);
    }
}
