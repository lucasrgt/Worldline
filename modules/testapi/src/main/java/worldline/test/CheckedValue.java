package worldline.test;

/** Checked value supplier used by change assertions. */
@FunctionalInterface
public interface CheckedValue<T> {
    T get() throws Exception;
}
