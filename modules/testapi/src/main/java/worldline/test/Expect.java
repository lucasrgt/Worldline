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
}
