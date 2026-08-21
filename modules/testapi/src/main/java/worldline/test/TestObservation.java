package worldline.test;

/** Thread-confined bridge between runtime observations and structured assertions. */
public final class TestObservation {
    private static final ThreadLocal<TestDivergence> CURRENT = new ThreadLocal<>();
    private TestObservation() {}
    public static void record(long tick, String field, String role) {
        if (tick < 0 || field == null || role == null) throw new IllegalArgumentException("invalid observation");
        CURRENT.set(new TestDivergence(tick, field, role));
    }
    public static TestDivergence current() { return CURRENT.get(); }
    public static void clear() { CURRENT.remove(); }
}
