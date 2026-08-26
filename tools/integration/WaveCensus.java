import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses the bounded flat candidate objects emitted by SwarmCensus and reviewed cohort reports. */
final class WaveCensus {
    private static final Pattern CANDIDATE = Pattern.compile(
            "\\{\\s*\"id\"\\s*:\\s*\"(m[0-9]+-[a-z0-9-]+)\"(.*?)\\}",
            Pattern.DOTALL);
    private WaveCensus() { }

    static Snapshot read(Path path) throws Exception {
        require(Files.isRegularFile(path), "missing wave census: " + path);
        String text = Files.readString(path, StandardCharsets.UTF_8);
        List<Row> rows = parse(text);
        require(!rows.isEmpty(), "wave census contains no candidates: " + path);
        return new Snapshot(path.toAbsolutePath().normalize(), rows, text);
    }

    static List<Row> parse(String text) {
        List<Row> rows = new ArrayList<>();
        Matcher matcher = CANDIDATE.matcher(text);
        while (matcher.find()) rows.add(new Row(matcher.group(1), matcher.group(2)));
        return List.copyOf(rows);
    }

    static void selfTest() {
        StringBuilder pretty = new StringBuilder("{\n  \"candidates\": [\n");
        for (int index = 1; index <= 25; index++) {
            pretty.append("    {\n      \"id\": \"m").append(index)
                    .append("-fixture\",\n      \"state\": \"")
                    .append(index == 25 ? "NOT_STARTED" : "QUALIFIED").append("\"\n    }")
                    .append(index == 25 ? "\n" : ",\n");
        }
        List<Row> rows = parse(pretty.append("  ]\n}\n").toString());
        require(rows.size() == 25 && "NOT_STARTED".equals(rows.get(24).state()),
                "pretty-printed 25-candidate census parsing drifted");
    }

    static String string(String body, String name, String fallback) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(name)
                + "\"\\s*:\\s*\"([^\"]*)\"").matcher(body);
        return matcher.find() ? matcher.group(1) : fallback;
    }
    static int integer(String body, String name, int fallback) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(name)
                + "\"\\s*:\\s*(-?[0-9]+)").matcher(body);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : fallback;
    }
    static double decimal(String body, String name, double fallback) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(name)
                + "\"\\s*:\\s*(-?[0-9]+(?:[.][0-9]+)?)").matcher(body);
        return matcher.find() ? Double.parseDouble(matcher.group(1)) : fallback;
    }
    static boolean bool(String body, String name, boolean fallback) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(name)
                + "\"\\s*:\\s*(true|false)").matcher(body);
        return matcher.find() ? Boolean.parseBoolean(matcher.group(1)) : fallback;
    }
    static boolean has(String body, String name) {
        return Pattern.compile("\"" + Pattern.quote(name) + "\"\\s*:").matcher(body).find();
    }
    static List<String> strings(String body, String name) {
        Matcher array = Pattern.compile("\"" + Pattern.quote(name)
                + "\"\\s*:\\s*\\[([^]]*)]", Pattern.DOTALL).matcher(body);
        if (!array.find()) return List.of();
        List<String> values = new ArrayList<>();
        Matcher item = Pattern.compile("\"([^\"]+)\"").matcher(array.group(1));
        while (item.find()) values.add(item.group(1));
        return List.copyOf(values);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    record Snapshot(Path path, List<Row> rows, String text) { }
    record Row(String id, String body) {
        String state() { return string(body, "state", "UNKNOWN"); }
        boolean processed() { return !"NOT_STARTED".equals(state()); }
        boolean qualified() { return "QUALIFIED".equals(state()); }
        boolean rejected() { return "REJECTED".equals(state()); }
        boolean firstPass() { return bool(body, "first_pass", false); }
        boolean recurrence() {
            return bool(body, "recurrence", false)
                    || integer(body, "known_scar_recurrence_count", 0) > 0;
        }
        boolean recurrenceAssessed() {
            return has(body, "recurrence") || has(body, "known_scar_recurrence_count");
        }
        int corrections() {
            int candidates = Math.max(0, integer(body, "candidate_attempts", 1) - 1);
            return Math.max(candidates, integer(body, "worker_corrections", 0))
                    + Math.max(0, integer(body, "runtime_retries", integer(body, "retries", 0)));
        }
        int candidateAttempts() { return integer(body, "candidate_attempts", 1); }
        int officialAttempts() { return integer(body, "official_attempts", qualified() ? 1 : 0); }
        boolean integrated() { return bool(body, "integrated", false); }
        boolean receiptExact() { return bool(body, "receipt_exact", qualified()); }
        double receiptSeconds() { return decimal(body, "time_to_receipt_seconds", -1); }
        int preCandidatePreventions() { return integer(body, "prevented_same_scar_failures", 0); }
        boolean objectiveInterlock() { return has(body, "prevention_interlock"); }
        List<String> recurrenceScars() { return strings(body, "recurrence_scars"); }
    }
}
