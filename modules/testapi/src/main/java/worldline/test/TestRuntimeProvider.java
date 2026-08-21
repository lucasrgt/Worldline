package worldline.test;

/** Runtime-specific factory discovered by the neutral test runner. */
public interface TestRuntimeProvider {
    String runtimeId();
    TestRuntimeSession open(TestRuntimeRequest request) throws Exception;
}
