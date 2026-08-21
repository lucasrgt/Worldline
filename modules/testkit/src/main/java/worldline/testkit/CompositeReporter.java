package worldline.testkit;

import java.util.Arrays;
import java.util.List;
import worldline.test.TestPlan;

/** Fan-out reporter for simultaneous console and machine outputs. */
public final class CompositeReporter implements TestReporter {
    private final List<TestReporter> reporters;
    public CompositeReporter(TestReporter... reporters) {
        if (reporters == null) throw new NullPointerException("reporters");
        this.reporters = Arrays.asList(reporters.clone());
    }
    @Override public void runStarted(TestPlan plan, int selected) {
        for (TestReporter reporter : reporters) reporter.runStarted(plan, selected);
    }
    @Override public void runStarted(java.util.List<TestPlan> plans, int selected) {
        for (TestReporter reporter : reporters) reporter.runStarted(plans, selected);
    }
    @Override public void fileCollected(TestPlan plan) {
        for (TestReporter reporter : reporters) reporter.fileCollected(plan);
    }
    @Override public void testQueued(String path) {
        for (TestReporter reporter : reporters) reporter.testQueued(path);
    }
    @Override public void testStarted(String path, int attempt) {
        for (TestReporter reporter : reporters) reporter.testStarted(path, attempt);
    }
    @Override public void artifactRecorded(String path, java.nio.file.Path artifact) {
        for (TestReporter reporter : reporters) reporter.artifactRecorded(path, artifact);
    }
    @Override public void testFinished(TestResult result) {
        for (TestReporter reporter : reporters) reporter.testFinished(result);
    }
    @Override public void runFinished(TestRunResult result) {
        for (TestReporter reporter : reporters) reporter.runFinished(result);
    }
    @Override public void close() {
        for (TestReporter reporter : reporters) reporter.close();
    }
}
