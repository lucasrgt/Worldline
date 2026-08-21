package worldline.testkit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Canonical machine-readable run result. */
public final class JsonReporter implements TestReporter {
    private final Path output;
    public JsonReporter(Path output) {
        this.output = OutputGuard.safe(output, "JSON report");
    }
    @Override public void runFinished(TestRunResult result) {
        StringBuilder json = new StringBuilder("{\"version\":1,\"passed\":")
                .append(result.passed()).append(",\"durationMillis\":").append(result.durationMillis())
                .append(",\"fatalError\":").append(ReportText.json(result.fatalError()))
                .append(",\"tests\":[");
        for (int index = 0; index < result.tests().size(); index++) {
            if (index > 0) json.append(','); TestResult test = result.tests().get(index);
            json.append("{\"id\":").append(ReportText.json(test.id()))
                    .append(",\"path\":").append(ReportText.json(test.path()))
                    .append(",\"status\":").append(ReportText.json(test.status().name().toLowerCase()))
                    .append(",\"durationMillis\":").append(test.durationMillis())
                    .append(",\"seed\":").append(test.seed())
                    .append(",\"attempts\":").append(test.attempts())
                    .append(",\"errorType\":").append(ReportText.json(test.errorType()))
                    .append(",\"errorMessage\":").append(ReportText.json(test.errorMessage()))
                    .append(",\"expected\":").append(ReportText.json(test.expected()))
                    .append(",\"received\":").append(ReportText.json(test.received()))
                    .append(",\"divergenceTick\":").append(test.divergenceTick())
                    .append(",\"divergenceField\":").append(ReportText.json(test.divergenceField()))
                    .append(",\"divergenceRole\":").append(ReportText.json(test.divergenceRole()))
                    .append('}');
        }
        json.append("]}\n"); write(json.toString());
    }
    private void write(String value) {
        try {
            Path parent = output.toAbsolutePath().normalize().getParent();
            if (parent != null) Files.createDirectories(parent);
            Files.write(output, value.getBytes(StandardCharsets.UTF_8));
        } catch (IOException error) { throw new IllegalStateException("JSON reporter failed", error); }
    }
}
