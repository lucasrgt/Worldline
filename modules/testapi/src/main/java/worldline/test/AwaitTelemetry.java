package worldline.test;

/** Immutable deterministic counters for condition-based smoke waits. */
public final class AwaitTelemetry {
    private final long waits;
    private final long polls;
    private final long failures;
    private final long observedTicks;

    public AwaitTelemetry(long waits, long polls, long failures, long observedTicks) {
        this.waits = waits;
        this.polls = polls;
        this.failures = failures;
        this.observedTicks = observedTicks;
    }

    public long waits() { return waits; }
    public long polls() { return polls; }
    public long failures() { return failures; }
    public long observedTicks() { return observedTicks; }

    public String evidence() {
        return "waits=" + waits + ";polls=" + polls + ";failures=" + failures
                + ";observed-ticks=" + observedTicks;
    }

    @Override public boolean equals(Object other) {
        if (!(other instanceof AwaitTelemetry)) return false;
        AwaitTelemetry value = (AwaitTelemetry) other;
        return waits == value.waits && polls == value.polls && failures == value.failures
                && observedTicks == value.observedTicks;
    }

    @Override public int hashCode() {
        long value = ((waits * 31L + polls) * 31L + failures) * 31L + observedTicks;
        return (int) (value ^ value >>> 32);
    }
}
