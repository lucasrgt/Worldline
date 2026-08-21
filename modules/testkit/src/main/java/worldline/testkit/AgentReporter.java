package worldline.testkit;

import java.io.PrintStream;

/** Low-noise reporter optimized for coding agents and CI logs. */
public final class AgentReporter implements TestReporter {
    private final PrintStream output;
    public AgentReporter(PrintStream output) {
        if (output == null) throw new NullPointerException("output"); this.output = output;
    }
    @Override public void testFinished(TestResult result) {
        if (result.status() != TestStatus.FAILED && result.status() != TestStatus.FLAKY) return;
        output.println(result.status() + " " + result.path() + " @ " + result.location());
        if (result.errorType() != null) output.println("error=" + result.errorType() + ":"
                + String.valueOf(result.errorMessage()));
        if (result.expected() != null) output.println("expected=" + result.expected());
        if (result.received() != null) output.println("received=" + result.received());
        output.println("seed=" + result.seed());
        if (result.divergenceTick() >= 0) {
            output.println("divergence.tick=" + result.divergenceTick());
            output.println("divergence.role=" + result.divergenceRole());
            output.println("divergence.field=" + result.divergenceField());
        }
        for (java.nio.file.Path artifact : result.artifacts()) output.println("artifact=" + artifact);
    }
    @Override public void runFinished(TestRunResult result) {
        output.println("WORLDLINE_TEST=" + (result.passed() ? "PASS" : "FAIL"));
        output.println("tests=" + result.tests().size()); output.println("duration.ms=" + result.durationMillis());
        if (result.fatalError() != null) output.println("fatal=" + result.fatalError());
    }
}
