package worldline.testkit;

import java.io.PrintStream;

/** Console reporter that includes attempts, source, and artifacts. */
public final class VerboseReporter extends DefaultReporter {
    public VerboseReporter(PrintStream output, boolean unicode) { super(output, unicode); }
    @Override public void testFinished(TestResult result) {
        super.testFinished(result);
        output.println("   at " + result.location() + " | attempts " + result.attempts()
                + " | seed " + result.seed());
        for (java.nio.file.Path artifact : result.artifacts()) output.println("   artifact " + artifact);
    }
}
