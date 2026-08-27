import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Extracts comparable model yield metrics from immutable OpenCode JSONL. */
final class OxAlphaTelemetry {
    private static final Pattern NUMBER = Pattern.compile("\"([^\"]+)\":([0-9]+(?:[.]?[0-9]+)?)");
    private OxAlphaTelemetry() { }

    static Result read(Path jsonl) throws Exception {
        int steps = 0, tools = 0; long input = 0, output = 0, reasoning = 0;
        long cacheRead = 0, cacheWrite = 0, peak = 0; double cost = 0;
        for (String line : Files.readAllLines(jsonl, StandardCharsets.UTF_8)) {
            if (line.contains("\"type\":\"step_start\"")) steps++;
            if (line.contains("\"type\":\"tool_use\"")) tools++;
            if (!line.contains("\"type\":\"step_finish\"")) continue;
            peak = Math.max(peak, integer(line, "total"));
            input += integer(line, "input"); output += integer(line, "output");
            reasoning += integer(line, "reasoning"); cacheRead += integer(line, "read");
            cacheWrite += integer(line, "write"); cost += decimal(line, "cost");
        }
        return new Result(steps, tools, input, output, reasoning, cacheRead, cacheWrite, peak, cost);
    }

    static Set<String> sessions(Path jsonl) throws Exception {
        Set<String> sessions = new LinkedHashSet<>();
        for (String line : Files.readAllLines(jsonl, StandardCharsets.UTF_8)) {
            int key = line.indexOf("\"sessionID\":\"");
            if (key >= 0) {
                int start = key + 13, end = line.indexOf('"', start);
                if (end > start) {
                    sessions.add(line.substring(start, end));
                }
            }
        }
        return sessions;
    }

    static String receiptFields(Result value, java.time.Instant started, java.time.Instant finished) {
        return ",\n  \"wall_seconds\":"
                + java.time.Duration.between(started, finished).toMillis() / 1000.0
                + ",\n  \"steps\":" + value.steps + ",\n  \"tool_calls\":" + value.toolCalls
                + ",\n  \"tokens_input\":" + value.inputTokens
                + ",\n  \"tokens_output\":" + value.outputTokens
                + ",\n  \"tokens_reasoning\":" + value.reasoningTokens
                + ",\n  \"tokens_cache_read\":" + value.cacheReadTokens
                + ",\n  \"tokens_cache_write\":" + value.cacheWriteTokens
                + ",\n  \"context_peak_tokens\":" + value.contextPeakTokens
                + ",\n  \"cost\":" + value.cost;
    }

    static void selfTest() throws Exception {
        Path file = Files.createTempFile("worldline-ox-telemetry-", ".jsonl");
        try {
            Files.writeString(file, "{\"type\":\"step_start\"}\n{\"type\":\"tool_use\"}\n"
                    + "{\"type\":\"step_finish\",\"tokens\":{\"total\":20,\"input\":10,"
                    + "\"output\":3,\"reasoning\":2,\"cache\":{\"write\":1,\"read\":4}},"
                    + "\"cost\":0.25}\n", StandardCharsets.UTF_8);
            Result result = read(file);
            require(result.steps == 1 && result.toolCalls == 1 && result.inputTokens == 10
                    && result.outputTokens == 3 && result.reasoningTokens == 2
                    && result.cacheReadTokens == 4 && result.cacheWriteTokens == 1
                    && result.contextPeakTokens == 20 && result.cost == 0.25,
                    "OpenCode telemetry parsing drifted");
            Files.writeString(file, "{\"sessionID\":\"ses_one\"}\n"
                    + "{\"sessionID\":\"ses_two\"}\n", StandardCharsets.UTF_8);
            require(sessions(file).equals(Set.of("ses_one", "ses_two")),
                    "OpenCode stdout session census drifted");
        } finally { Files.deleteIfExists(file); }
    }

    private static long integer(String line, String key) { return (long) decimal(line, key); }
    private static double decimal(String line, String key) {
        Matcher matcher = NUMBER.matcher(line);
        while (matcher.find()) if (matcher.group(1).equals(key)) {
            return Double.parseDouble(matcher.group(2));
        }
        return 0;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    record Result(int steps, int toolCalls, long inputTokens, long outputTokens,
            long reasoningTokens, long cacheReadTokens, long cacheWriteTokens,
            long contextPeakTokens, double cost) { }
}
