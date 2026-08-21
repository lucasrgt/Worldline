package worldline.test;

/** Parameterized test body. */
@FunctionalInterface
public interface EachBody<T> {
    void run(TestContext context, T value) throws Exception;
}
