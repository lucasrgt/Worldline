package worldline.testkit;

import java.io.PrintStream;

/** Dense status stream for large tables. */
public final class DotReporter implements TestReporter {
    private final PrintStream output; private final boolean unicode;
    public DotReporter(PrintStream output, boolean unicode) {
        if (output == null) throw new NullPointerException("output");
        this.output = output; this.unicode = unicode;
    }
    @Override public void testFinished(TestResult result) {
        output.print(ReportText.symbol(result.status(), unicode)); output.flush();
    }
    @Override public void runFinished(TestRunResult result) {
        output.println(); output.println(result.passed() ? "PASS" : "FAIL");
    }
}
