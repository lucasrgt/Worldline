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
    public void toBeGreaterThanOrEqual(long expected) {
        if (!(actual instanceof Number) || ((Number) actual).longValue() < expected)
            fail("number is not greater or equal", ">= " + expected, actual);
    }
    public void toBeLessThan(long expected) {
        if (!(actual instanceof Number) || ((Number) actual).longValue() >= expected)
            fail("number is not less", "< " + expected, actual);
    }
    public void toBeLessThanOrEqual(long expected) {
        if (!(actual instanceof Number) || ((Number) actual).longValue() > expected)
            fail("number is not less or equal", "<= " + expected, actual);
    }
    public void toHaveSize(int expected) {
        Integer size = sizeOf(actual);
        if (size == null || size.intValue() != expected)
            fail("value does not have expected size", expected, size);
    }
    public void toBeEmpty() {
        Integer size = sizeOf(actual);
        if (size == null || size.intValue() != 0)
            fail("value is not empty", 0, size);
    }
    public void toStartWith(String prefix) {
        if (!(actual instanceof String) || prefix == null || !((String) actual).startsWith(prefix))
            fail("string does not start with expected text", prefix, actual);
    }
    public void toEndWith(String suffix) {
        if (!(actual instanceof String) || suffix == null || !((String) actual).endsWith(suffix))
            fail("string does not end with expected text", suffix, actual);
    }
    public void notToContain(String part) {
        if (!(actual instanceof String) || part == null || ((String) actual).contains(part))
            fail("string contains forbidden text", "not containing " + part, actual);
    }
    public void toBeInstanceOf(Class<?> type) {
        if (type == null || actual == null || !type.isInstance(actual))
            fail("value is not an instance of expected type", type, actual);
    }
    public void toMatchSnapshot(String name) { SnapshotExpectation.match(name, actual); }

    private static Integer sizeOf(Object value) {
        if (value instanceof java.util.Collection) return ((java.util.Collection<?>) value).size();
        if (value instanceof java.util.Map) return ((java.util.Map<?, ?>) value).size();
        if (value instanceof Object[]) return ((Object[]) value).length;
        if (value instanceof String) return ((String) value).length();
        return null;
    }

    private static void fail(String message, Object expected, Object actual) {
        throw new WorldlineAssertionError(message, expected, actual);
    }
}
