package worldline.testkit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** JUnit XML reporter for CI systems. */
public final class JUnitReporter implements TestReporter {
    private final Path output;
    public JUnitReporter(Path output) {
        this.output = OutputGuard.safe(output, "JUnit report");
    }
    @Override public void runFinished(TestRunResult result) {
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<testsuite name=\"Worldline Test\" tests=\"").append(result.tests().size())
                .append("\" failures=\"").append(result.count(TestStatus.FAILED)
                        + result.count(TestStatus.INTERRUPTED) + result.count(TestStatus.FLAKY))
                .append("\" skipped=\"").append(result.count(TestStatus.SKIPPED)
                        + result.count(TestStatus.TODO)).append("\" time=\"")
                .append(result.durationMillis() / 1000.0D).append("\">\n");
        for (TestResult test : result.tests()) {
            xml.append("  <testcase classname=\"").append(ReportText.xml(test.spec()))
                    .append("\" name=\"").append(ReportText.xml(test.path())).append("\" time=\"")
                    .append(test.durationMillis() / 1000.0D).append("\"><properties><property name=\"seed\" value=\"")
                    .append(test.seed()).append("\"/></properties>");
            if (test.status() == TestStatus.FAILED || test.status() == TestStatus.INTERRUPTED
                    || test.status() == TestStatus.FLAKY) {
                xml.append("<failure type=\"").append(ReportText.xml(test.errorType())).append("\">")
                        .append(ReportText.xml(test.status() == TestStatus.FLAKY
                                ? "passed only after retry" : test.errorMessage())).append("</failure>");
            } else if (test.status() == TestStatus.SKIPPED || test.status() == TestStatus.TODO) {
                xml.append("<skipped message=\"").append(ReportText.xml(test.note())).append("\"/>");
            }
            xml.append("</testcase>\n");
        }
        xml.append("</testsuite>\n"); write(xml.toString());
    }
    private void write(String value) {
        try {
            Path parent = output.toAbsolutePath().normalize().getParent();
            if (parent != null) Files.createDirectories(parent);
            Files.write(output, value.getBytes(StandardCharsets.UTF_8));
        } catch (IOException error) { throw new IllegalStateException("JUnit reporter failed", error); }
    }
}
