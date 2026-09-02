package worldline.test;

import worldline.api.WorldlineEvidence;

/** Static assertion entrypoint. */
public final class Expect {
    private Expect() {}
    public static BehaviorExpectation expect(WorldlineEvidence actual) {
        return new BehaviorExpectation(actual);
    }
    public static <T> Expectation<T> expect(T actual) { return new Expectation<>(actual); }
    public static <T> ChangeExpectation<T> expect(CheckedValue<T> sample) {
        if (sample == null) throw new NullPointerException("sample");
        return new ChangeExpectation<>(sample);
    }
    public static void expectThrown(Class<? extends RuntimeException> type, Runnable action) {
        if (type == null || action == null) throw new NullPointerException("thrown expectation");
        try {
            action.run();
        } catch (RuntimeException error) {
            if (type.isInstance(error)) return;
            throw new WorldlineAssertionError("unexpected exception type", type, error.getClass());
        }
        throw new WorldlineAssertionError("expected exception was not thrown", type, null);
    }
}
