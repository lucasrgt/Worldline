package worldline.testkit;

import java.io.PrintStream;
import java.util.List;
import worldline.test.TestPlan;

/** Compact Vitest-inspired console tree. */
public class DefaultReporter implements TestReporter {
    protected final PrintStream output;
    protected final boolean unicode;

    public DefaultReporter(PrintStream output, boolean unicode) {
        if (output == null) throw new NullPointerException("output");
        this.output = output; this.unicode = unicode;
    }
    @Override public void runStarted(TestPlan plan, int selected) {
        output.println(" WORLDLINE TEST  v0.3.1  " + selected + " selected"); output.println();
    }
    @Override public void runStarted(List<TestPlan> plans, int selected) {
        output.println(" WORLDLINE TEST  v0.3.1  " + plans.size() + " files | "
                + selected + " selected"); output.println();
    }
    @Override public void testFinished(TestResult result) {
        output.println(" " + ReportText.symbol(result.status(), unicode) + " " + result.path()
                + duration(result));
        if (result.status() == TestStatus.FAILED) failure(result);
    }
    @Override public void runFinished(TestRunResult result) {
        output.println();
        if (result.fatalError() != null) output.println(" FATAL  " + result.fatalError());
        output.println(" Tests  " + result.count(TestStatus.FAILED) + " failed | "
                + result.count(TestStatus.PASSED) + " passed | "
                + result.count(TestStatus.FLAKY) + " flaky | "
                + result.count(TestStatus.SKIPPED) + " skipped | "
                + result.count(TestStatus.TODO) + " todo");
        output.println(" Duration  " + result.durationMillis() + "ms");
    }
    protected void failure(TestResult result) {
        if (result.errorType() != null) output.println("   " + result.errorType() + ": "
                + String.valueOf(result.errorMessage()));
        if (result.expected() != null) output.println("   Expected  " + result.expected());
        if (result.received() != null) output.println("   Received  " + result.received());
        if (result.divergenceTick() >= 0) {
            output.println("   First divergence");
            output.println("     tick  " + result.divergenceTick());
            output.println("     role  " + result.divergenceRole());
            output.println("     at    " + result.divergenceField());
        }
        output.println("   Seed  " + result.seed());
    }
    private static String duration(TestResult result) {
        return result.durationMillis() > 0 ? " " + result.durationMillis() + "ms" : "";
    }
}
