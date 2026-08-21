package worldline.test;

import java.util.Objects;

/** Assertion over a value sampled before and after an action. */
public final class ChangeExpectation<T> {
    private final CheckedValue<T> sample;

    ChangeExpectation(CheckedValue<T> sample) { this.sample = sample; }

    public void toChange(TestContext.CheckedStep action) throws Exception {
        if (action == null) throw new NullPointerException("action");
        T before = sample.get(); action.run(); T after = sample.get();
        if (Objects.deepEquals(before, after)) {
            throw new WorldlineAssertionError("value did not change", "different from " + before, after);
        }
    }

    public void toChangeFromTo(T expectedBefore, T expectedAfter,
            TestContext.CheckedStep action) throws Exception {
        if (action == null) throw new NullPointerException("action");
        T before = sample.get(); action.run(); T after = sample.get();
        if (!Objects.deepEquals(before, expectedBefore) || !Objects.deepEquals(after, expectedAfter)) {
            throw new WorldlineAssertionError("value did not change as expected",
                    expectedBefore + " -> " + expectedAfter, before + " -> " + after);
        }
    }
}
