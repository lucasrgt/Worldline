import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Deterministic stage timing report for repository verification. */
final class VerifyReport {
    private final Path root, path;
    private final String profile;
    private final Instant started = Instant.now();
    private final long startNanos = System.nanoTime();
    private final List<Stage> stages = new ArrayList<>();

    VerifyReport(Path root, String profile) {
        this.root = root; this.path = root.resolve(".worldline/reports/verify.json"); this.profile = profile;
    }

    void step(String name, Checked action) throws Exception {
        value(name, () -> { action.run(); return null; });
    }

    <T> T value(String name, Value<T> action) throws Exception {
        long start = System.nanoTime();
        try {
            T result = action.run(); stages.add(new Stage(name, elapsed(start), "passed")); return result;
        } catch (Exception error) {
            stages.add(new Stage(name, elapsed(start), "failed")); throw error;
        }
    }

    void finish(String status, Throwable error) {
        try {
            long totalMillis = elapsed(startNanos);
            String latency = error == null ? GateLatencyCheck.enforce(root, totalMillis) : "failed";
            Files.createDirectories(path.getParent());
            StringBuilder json = new StringBuilder("{\n  \"schema\": 2,\n  \"profile\": \"")
                    .append(escape(profile)).append("\",\n  \"status\": \"").append(status)
                    .append("\",\n  \"started\": \"").append(started).append("\",\n  \"elapsed_ms\": ")
                    .append(totalMillis).append(",\n  \"latency_class\": \"")
                    .append(latency).append("\",\n  \"stages\": [\n");
            for (int index = 0; index < stages.size(); index++) {
                Stage stage = stages.get(index);
                json.append("    {\"name\": \"").append(escape(stage.name)).append("\", \"status\": \"")
                        .append(stage.status).append("\", \"elapsed_ms\": ").append(stage.millis).append('}');
                if (index + 1 < stages.size()) json.append(','); json.append('\n');
            }
            json.append("  ],\n  \"error\": ");
            if (error == null) json.append("null,\n  \"stack_trace\": null\n");
            else json.append('"').append(escape(error.getMessage() == null
                    ? error.getClass().getName() : error.getMessage())).append("\",\n  \"stack_trace\": \"")
                    .append(escape(stackTrace(error))).append("\"\n");
            json.append("}\n");
            Files.writeString(path, json.toString(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("  report: " + path.getParent().getParent().relativize(path));
        } catch (IOException reportError) {
            if (error != null) error.addSuppressed(reportError);
            else throw new IllegalStateException("could not write verify report", reportError);
        }
    }

    private static long elapsed(long start) { return (System.nanoTime() - start) / 1_000_000L; }
    private static String stackTrace(Throwable error) {
        StringWriter value = new StringWriter(); error.printStackTrace(new PrintWriter(value));
        return value.toString();
    }
    private static String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n");
    }

    interface Checked { void run() throws Exception; }
    interface Value<T> { T run() throws Exception; }
    private static final class Stage {
        final String name, status; final long millis;
        Stage(String name, long millis, String status) {
            this.name = name; this.millis = millis; this.status = status;
        }
    }
}
