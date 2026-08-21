package worldline.test;

/** Executable body for one collected test. */
@FunctionalInterface
public interface TestBody {
    void run(TestContext context) throws Exception;
}
