import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Mechanically centralizes recognized one-EOF retry loops without changing their bounds. */
final class RetryMigration {
    private static final Pattern BRACED = Pattern.compile(
            "if\\s*\\(\\s*(attempt|i)\\s*==\\s*0\\s*&&\\s*eof\\(\\s*(e|error)\\s*\\)"
            + "\\s*\\)\\s*\\{[^{}]*?continue;\\s*\\}", Pattern.DOTALL);
    private static final Pattern PLAIN = Pattern.compile(
            "if\\s*\\(\\s*(attempt|i)\\s*==\\s*0\\s*&&\\s*eof\\(\\s*(e|error)\\s*\\)"
            + "\\s*\\)\\s*continue;");
    private final Path root;

    private RetryMigration(Path root) { this.root = root.toAbsolutePath().normalize(); }

    public static void main(String[] arguments) {
        try {
            require(arguments.length == 1 && (arguments[0].equals("--apply")
                    || arguments[0].equals("--finalize")),
                    "usage: RetryMigration [--apply|--finalize]");
            RetryMigration migration = new RetryMigration(Path.of(""));
            if (arguments[0].equals("--apply")) migration.apply(); else migration.finalizeWhitespace();
        } catch (Exception error) {
            System.err.println("EOF retry migration failed: " + error.getMessage()); System.exit(1);
        }
    }

    private void apply() throws Exception {
        require(stagedChanges(), "stage the migration implementation before applying the rewrite");
        List<SmokeDiscovery.Entry> smokes = SmokeDiscovery.discover(root);
        SmokeInputFingerprint fingerprints = new SmokeInputFingerprint(root);
        SmokePins previous = new SmokePins(root); List<Change> changes = new ArrayList<>();
        for (Path source : sources()) {
            String oldText = Files.readString(source, StandardCharsets.UTF_8);
            if (!legacy(oldText)) continue;
            String id = id(oldText, source); SmokeDiscovery.Entry smoke = smokes.stream()
                    .filter(entry -> entry.id.equals(id)).findFirst().orElseThrow();
            String prior = fingerprints.compute(smoke); SmokePins.Entry pin = previous.match(id, prior);
            require(pin != null, "legacy retry lacks a current pin: " + id);
            String current = transform(oldText, source); Files.writeString(source, current,
                    StandardCharsets.UTF_8); changes.add(new Change(source, id, oldText, current, prior, pin));
        }
        require(changes.size() == 33, "legacy retry census drifted: " + changes.size());
        SmokeInputFingerprint updated = new SmokeInputFingerprint(root); List<SmokePins.Entry> pins =
                new ArrayList<>(); Properties manifest = new Properties();
        manifest.setProperty("schema", "1"); manifest.setProperty("count", "33");
        manifest.setProperty("boundary_sha256", digest(
                root.resolve("tools/harness/SmokeRetryBoundary.java")));
        manifest.setProperty("support_sha256", digest(
                root.resolve("tools/harness/ExceptionalSmokeSupport.java")));
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            Change change = changes.stream().filter(value -> value.id.equals(smoke.id))
                    .findFirst().orElse(null);
            if (change == null) { pins.add(requiredEntry(previous, smoke.id)); continue; }
            String currentFingerprint = updated.compute(smoke);
            pins.add(new SmokePins.Entry(smoke.id, currentFingerprint, change.pin.evidence(),
                    "refactor-equivalent")); String stem = "retry." + smoke.id + ".";
            manifest.setProperty(stem + "source", relative(change.source));
            manifest.setProperty(stem + "prior_source_sha256", digest(change.oldText));
            manifest.setProperty(stem + "current_source_sha256", digest(change.newText));
            manifest.setProperty(stem + "prior_fingerprint", change.priorFingerprint);
            manifest.setProperty(stem + "current_fingerprint", currentFingerprint);
            manifest.setProperty(stem + "evidence_sha256", change.pin.evidence());
        }
        previous.write(pins); store(root.resolve("smokes/eof-retry-migration.lock"), manifest);
        System.out.println("EOF retry migration applied: " + changes.size() + " coordinators");
    }

    private void finalizeWhitespace() throws Exception {
        Properties manifest = load(root.resolve("smokes/eof-retry-migration.lock"));
        SmokePins previous = new SmokePins(root); List<SmokePins.Entry> pins = new ArrayList<>();
        int changed = 0;
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            String stem = "retry." + smoke.id + ".", relative = manifest.getProperty(stem + "source");
            if (relative == null) { pins.add(requiredEntry(previous, smoke.id)); continue; }
            Path source = root.resolve(relative); String text = Files.readString(source, StandardCharsets.UTF_8);
            String normalized = normalizeSupport(text.replaceAll("(?m)^[ \\t]+$", ""));
            if (!normalized.equals(text)) {
                Files.writeString(source, normalized, StandardCharsets.UTF_8); changed++;
            }
        }
        SmokeInputFingerprint fingerprints = new SmokeInputFingerprint(root);
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            String stem = "retry." + smoke.id + ".", relative = manifest.getProperty(stem + "source");
            if (relative == null) continue; Path source = root.resolve(relative);
            String fingerprint = fingerprints.compute(smoke);
            pins.add(new SmokePins.Entry(smoke.id, fingerprint,
                    manifest.getProperty(stem + "evidence_sha256"), "refactor-equivalent"));
            manifest.setProperty(stem + "current_source_sha256", digest(source));
            manifest.setProperty(stem + "current_fingerprint", fingerprint);
        }
        manifest.setProperty("support_sha256", digest(
                root.resolve("tools/harness/ExceptionalSmokeSupport.java")));
        previous.write(pins); store(root.resolve("smokes/eof-retry-migration.lock"), manifest);
        System.out.println("EOF retry whitespace finalized: " + changed + " coordinators");
    }

    private String transform(String source, Path path) {
        String transformed = replace(BRACED, source); transformed = replace(PLAIN, transformed);
        transformed = transformed.replace("Thread.sleep(5000L);", "")
                .replace("Thread.sleep(5000);", "");
        transformed = removeMethod(transformed, "private static boolean eof");
        transformed = normalizeSupport(transformed);
        transformed = transformed.replaceAll("(?m)^[ \\t]+$", "");
        require(!legacy(transformed) && occurrences(transformed, "SmokeRetryBoundary.afterEofFailure") == 1,
                "unrecognized retry shape: " + relative(path)); return transformed;
    }

    private static String replace(Pattern pattern, String source) {
        Matcher matcher = pattern.matcher(source); StringBuffer output = new StringBuffer(); int count = 0;
        while (matcher.find()) { count++; matcher.appendReplacement(output, Matcher.quoteReplacement(
                "SmokeRetryBoundary.afterEofFailure(" + matcher.group(1) + ",1,"
                        + matcher.group(2) + ");")); }
        matcher.appendTail(output); return count == 0 ? source : output.toString();
    }

    private static String normalizeSupport(String source) {
        String duplicate = "ExceptionalExceptionalSmokeSupport";
        while (source.contains(duplicate)) source = source.replace(
                duplicate, "ExceptionalSmokeSupport");
        return source.replaceAll("(?<!Exceptional)SmokeSupport\\.", "ExceptionalSmokeSupport.");
    }

    private static String removeMethod(String source, String marker) {
        int start = source.indexOf(marker); require(start >= 0, "missing legacy EOF helper");
        int open = source.indexOf('{', start), depth = 0, end = -1;
        for (int index = open; index < source.length(); index++) {
            char value = source.charAt(index); if (value == '{') depth++;
            else if (value == '}' && --depth == 0) { end = index + 1; break; }
        }
        require(open >= 0 && end > open, "malformed legacy EOF helper");
        return source.substring(0, start) + source.substring(end);
    }

    private List<Path> sources() throws Exception { try (var paths = Files.list(root.resolve("tools/smoke"))) {
        return paths.filter(path -> path.toString().endsWith(".java")).sorted().toList(); } }
    private static boolean legacy(String source) {
        return (source.contains("Thread.sleep(5000L)") || source.contains("Thread.sleep(5000)"))
                && source.matches("(?s).*eof\\(.*") && source.contains("private static boolean eof");
    }
    private static String id(String source, Path path) { Matcher matcher = Pattern.compile(
            "(?:String ID\\s*=|String ID=)\\s*\\\"([^\\\"]+)\\\"").matcher(source);
        require(matcher.find(), "missing retry smoke id: " + path); return matcher.group(1); }
    private boolean stagedChanges() throws Exception { Process process = new ProcessBuilder("git", "diff",
            "--cached", "--quiet").directory(root.toFile()).start(); return process.waitFor() == 1; }
    private String relative(Path path) { return root.relativize(path).toString().replace('\\', '/'); }
    private static int occurrences(String value, String token) { int count = 0, index = 0;
        while ((index = value.indexOf(token, index)) >= 0) { count++; index += token.length(); } return count; }
    private static SmokePins.Entry requiredEntry(SmokePins pins, String id) { SmokePins.Entry entry = pins.entry(id);
        require(entry != null, "missing unchanged pin: " + id); return entry; }
    private static String digest(String value) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
    private static String digest(Path path) throws Exception { return digest(
            Files.readString(path, StandardCharsets.UTF_8).replace("\r\n", "\n")); }
    private static Properties load(Path path) throws Exception { Properties values = new Properties();
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader); } return values; }
    private static void store(Path path, Properties values) throws Exception { StringBuilder output =
            new StringBuilder("# Worldline EOF retry migration v1\n"); for (String key : values
            .stringPropertyNames().stream().sorted(Comparator.naturalOrder()).toList())
        output.append(key).append('=').append(values.getProperty(key)).append('\n');
        Files.writeString(path, output.toString(), StandardCharsets.UTF_8); }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message); }
    private record Change(Path source, String id, String oldText, String newText,
            String priorFingerprint, SmokePins.Entry pin) { }
}
