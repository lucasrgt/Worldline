package worldline.test;

import java.util.Objects;

/** Fluent fail-closed assertions used by Worldline specs. */
public final class Expectation<T> {
    private final T actual;
    Expectation(T actual) { this.actual = actual; }

    public void toEqual(T expected) {
        if (!Objects.deepEquals(actual, expected)) fail("values are not equal", expected, actual);
    }
    public void notToEqual(T expected) {
        if (Objects.deepEquals(actual, expected)) fail("values are equal", "not " + expected, actual);
    }
    public void toBeTrue() { if (!Boolean.TRUE.equals(actual)) fail("value is not true", true, actual); }
    public void toBeFalse() { if (!Boolean.FALSE.equals(actual)) fail("value is not false", false, actual); }
    public void toBeNull() { if (actual != null) fail("value is not null", null, actual); }
    public void notToBeNull() { if (actual == null) fail("value is null", "non-null", null); }
    public void toContain(String part) {
        if (!(actual instanceof String) || part == null || !((String) actual).contains(part))
            fail("string does not contain expected text", part, actual);
    }
    public void toBeGreaterThan(long expected) {
        if (!(actual instanceof Number) || ((Number) actual).longValue() <= expected)
            fail("number is not greater", "> " + expected, actual);
    }
    public void toMatchSnapshot(String name) { SnapshotExpectation.match(name, actual); }

    private static void fail(String message, Object expected, Object actual) {
        throw new WorldlineAssertionError(message, expected, actual);
    }
}
