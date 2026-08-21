package worldline.test;

/** Assertion failure with structured expected and received values. */
public final class WorldlineAssertionError extends AssertionError {
    private static final long serialVersionUID = 1L;
    private final String expected;
    private final String received;
    private final TestDivergence divergence;

    public WorldlineAssertionError(String message, Object expected, Object received) {
        super(message); this.expected = SemanticSelectors.describe(expected);
        this.received = SemanticSelectors.describe(received);
        divergence = TestObservation.current();
    }

    public String expected() { return expected; }
    public String received() { return received; }
    public TestDivergence divergence() { return divergence; }
}
