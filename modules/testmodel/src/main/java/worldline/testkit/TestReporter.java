package worldline.testkit;

import java.util.List;
import worldline.test.TestPlan;

/** Event-driven output boundary; executors never print directly. */
public interface TestReporter extends AutoCloseable {
    default void runStarted(TestPlan plan, int selected) {}
    default void runStarted(List<TestPlan> plans, int selected) {
        if (!plans.isEmpty()) runStarted(plans.get(0), selected);
    }
    default void fileCollected(TestPlan plan) {}
    default void testQueued(String path) {}
    default void testStarted(String path, int attempt) {}
    default void artifactRecorded(String path, java.nio.file.Path artifact) {}
    default void testFinished(TestResult result) {}
    default void runFinished(TestRunResult result) {}
    @Override default void close() {}
}
