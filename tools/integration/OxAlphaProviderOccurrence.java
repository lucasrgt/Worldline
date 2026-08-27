import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Extracts one ordered provider failure from one bounded immutable log snapshot. */
final class OxAlphaProviderOccurrence {
    private static final int MAX_BYTES = 64 * 1024 * 1024;

    private OxAlphaProviderOccurrence() {
    }

    static Result capture(Path log, String session, String model, Path worktree,
            String adoptionMode, Instant started, Instant finished) throws Exception {
        byte[] snapshot = snapshot(log);
        List<String> all = new String(snapshot, StandardCharsets.UTF_8).lines().toList();
        List<String> errors = all.stream().filter(line -> within(line, started, finished))
                .filter(line -> providerError(line, session, model)).toList();
        require(errors.size() == 1, "provider window must contain one exact quota failure");
        String run = field(errors.get(0), "run");
        List<String> excerpt = all.stream().filter(line -> within(line, started, finished))
                .filter(line -> session.equals(field(line, "session.id"))
                        || session.equals(field(line, "id"))).toList();
        require(excerpt.stream().allMatch(line -> run.equals(field(line, "run"))),
                "provider window contains a second run for the same session");
        require(excerpt.stream().allMatch(line -> List.of("created", "loop", "process",
                "stream", "stream error").contains(field(line, "message"))),
                "provider run contains an unclassified session event");
        require(excerpt.stream().allMatch(line -> within(line, started, finished)),
                "provider run crosses the launch window");
        validateExcerpt(excerpt, session, model, worktree, adoptionMode);
        String excerptText = String.join("\n", excerpt) + "\n";
        return new Result(excerpt, sha(snapshot), sha(excerptText.getBytes(StandardCharsets.UTF_8)));
    }

    static void validateSealed(Map<String, Object> receipt, String session, String model,
            Path worktree, String adoptionMode) throws Exception {
        List<String> excerpt = MiniJson.array(receipt, "provider_excerpt").stream()
                .map(value -> {
                    require(value instanceof String, "provider excerpt row is not a string");
                    return (String) value;
                }).toList();
        String text = String.join("\n", excerpt) + "\n";
        require(sha(text.getBytes(StandardCharsets.UTF_8)).equalsIgnoreCase(
                MiniJson.string(receipt, "provider_excerpt_sha256")),
                "sealed provider excerpt SHA-256 drifted");
        validateExcerpt(excerpt, session, model, worktree, adoptionMode);
    }

    private static void validateExcerpt(List<String> excerpt, String session, String model,
            Path worktree, String adoptionMode) throws Exception {
        require(!excerpt.isEmpty(), "provider occurrence is empty");
        String run = field(excerpt.get(0), "run");
        require(run != null && excerpt.stream().allMatch(line -> run.equals(field(line, "run"))),
                "provider occurrence spans multiple runs");
        require(count(excerpt, "loop") == 1 && count(excerpt, "process") == 1
                && count(excerpt, "stream") == 1 && count(excerpt, "stream error") == 1,
                "provider occurrence cardinality drifted");
        require(excerpt.stream().filter(line -> !"created".equals(field(line, "message")))
                .allMatch(line -> session.equals(field(line, "session.id"))),
                "provider occurrence session drifted");
        String loop = only(excerpt, "loop");
        String stream = only(excerpt, "stream");
        require("0".equals(field(loop, "step"))
                && providerLine(stream, session, model)
                && providerError(only(excerpt, "stream error"), session, model),
                "provider occurrence is not a zero-event quota failure");
        int loopIndex = excerpt.indexOf(loop);
        int processIndex = excerpt.indexOf(only(excerpt, "process"));
        int streamIndex = excerpt.indexOf(stream);
        int errorIndex = excerpt.indexOf(only(excerpt, "stream error"));
        require(loopIndex < processIndex && processIndex < streamIndex && streamIndex < errorIndex,
                "provider occurrence order drifted");
        if (adoptionMode.equals("process-recovery")) {
            require(count(excerpt, "created") == 1 && excerpt.indexOf(only(excerpt, "created")) < loopIndex,
                    "process recovery did not create exactly one session in the failed launch");
            String created = only(excerpt, "created");
            require(session.equals(field(created, "id")), "created provider session drifted");
            Path directory = Path.of(field(created, "directory"))
                    .toAbsolutePath().normalize();
            Path exact = worktree.toAbsolutePath().normalize();
            require(directory.equals(exact) && directory.toRealPath().equals(exact.toRealPath()),
                    "created provider session belongs to a different worktree");
        } else {
            require(adoptionMode.equals("resume-session") && count(excerpt, "created") == 0,
                    "resume-session provider run created a replacement session");
        }
    }

    private static byte[] snapshot(Path path) throws Exception {
        require(Files.isRegularFile(path), "provider log is unavailable");
        try (InputStream input = Files.newInputStream(path);
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) >= 0) {
                if (count == 0) {
                    continue;
                }
                require(output.size() <= MAX_BYTES - count, "provider log snapshot is too large");
                output.write(buffer, 0, count);
            }
            return output.toByteArray();
        }
    }

    private static boolean within(String line, Instant started, Instant finished) {
        String timestamp = field(line, "timestamp");
        if (timestamp == null) {
            return false;
        }
        try {
            Instant value = Instant.parse(timestamp);
            return !value.isBefore(started) && !value.isAfter(finished);
        } catch (java.time.format.DateTimeParseException ignored) {
            return false;
        }
    }

    private static boolean providerError(String line, String session, String model) {
        return providerLine(line, session, model)
                && "ERROR".equals(field(line, "level"))
                && "stream error".equals(field(line, "message"))
                && line.toLowerCase(Locale.ROOT).contains("usage limit");
    }

    private static boolean providerLine(String line, String session, String model) {
        return session.equals(field(line, "session.id"))
                && model.equals(field(line, "providerID") + "/" + field(line, "modelID"));
    }

    private static long count(List<String> lines, String message) {
        return lines.stream().filter(line -> message.equals(field(line, "message"))).count();
    }

    private static String only(List<String> lines, String message) {
        return lines.stream().filter(line -> message.equals(field(line, "message")))
                .findFirst().orElseThrow();
    }

    private static String field(String line, String key) {
        return OxAlphaProviderLogMonitor.field(line, key);
    }

    private static String sha(byte[] value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value));
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    record Result(List<String> excerpt, String sourceSha, String excerptSha) {
    }
}
