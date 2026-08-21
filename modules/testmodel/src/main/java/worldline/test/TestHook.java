package worldline.test;

/** Lifecycle hook executed around one isolated test attempt. */
@FunctionalInterface
public interface TestHook {
    void run(TestContext context) throws Exception;
}
