import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/** Enforces repository text hygiene and prevents legacy physical-line debt from growing. */
final class SourceQualityCheck {
    private static final List<String> SCOPES = List.of(
            "modules", "tools/harness", "tools/integration", "tools/smoke", "smokes", "adapters");
    private final Path root;
    private final Properties policy = new Properties();

    SourceQualityCheck(Path root) { this.root = root; }

    void execute() throws IOException {
        try (Reader reader = Files.newBufferedReader(root.resolve("quality/source-policy.properties"),
                StandardCharsets.UTF_8)) { policy.load(reader); }
        int width = integer("line.width");
        for (String scope : SCOPES) inspect(scope, width);
        inspectFlakinessDebt();
        try {
            new SmokeStatementBudget(root).execute();
            SharedSmokeSourceCheck.execute(root);
            TestSurfaceCheck.execute(root);
            DuplicatePatternCheck.execute(root);
            JdkPinCheck.execute(root);
        }
        catch (IOException error) { throw error; }
        catch (Exception error) { throw new IllegalStateException(error); }
        inspectRepositoryText();
        requireText(".editorconfig", "end_of_line = lf");
        requireText(".editorconfig", "trim_trailing_whitespace = true");
        requireText(".gitattributes", "* text=auto eol=lf");
        System.out.println("  source quality: whitespace clean; physical-line debt did not grow");
    }

    private void inspectFlakinessDebt() throws IOException {
        Pattern fixedWait = Pattern.compile(
                "sustainTicks\\([^\\)]*\\)[\\s\\S]{0,160}?require\\(");
        int fixedWaitFiles = countFiles(root.resolve("smokes"), source -> fixedWait.matcher(source).find());
        int eofRetryFiles = countFiles(root.resolve("tools/smoke"), source ->
                source.contains("Thread.sleep(5000") && source.matches("(?s).*eof\\(.*"));
        int eofHelperFiles = countFiles(root.resolve("tools/smoke"), source ->
                source.contains("private static boolean eof"));
        checkBaseline("smokes.fixed.tick.assertion.files", fixedWaitFiles);
        checkBaseline("tools/smoke.eof.retry.files", eofRetryFiles);
        checkBaseline("tools/smoke.eof.helper.files", eofHelperFiles);
        System.out.println("  flakiness debt: fixed-wait=" + fixedWaitFiles
                + "; EOF-retry=" + eofRetryFiles + "; EOF-helper=" + eofHelperFiles);
    }

    private int countFiles(Path directory, java.util.function.Predicate<String> predicate)
            throws IOException {
        int count = 0;
        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path path : paths.filter(file -> file.toString().endsWith(".java")).toList())
                if (predicate.test(Files.readString(path, StandardCharsets.UTF_8))) count++;
        }
        return count;
    }

    private void inspectRepositoryText() throws IOException {
        try (Stream<Path> paths = SmokeTrackedFiles.read(root).stream()) {
            for (Path path : paths.filter(Files::isRegularFile).filter(this::maintainedText).toList()) {
                List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
                for (int index = 0; index < lines.size(); index++) {
                    String line = lines.get(index);
                    require(!line.endsWith(" ") && !line.endsWith("\t"),
                            root.relativize(path) + ":" + (index + 1) + " has trailing whitespace");
                }
                byte[] bytes = Files.readAllBytes(path);
                require(bytes.length == 0 || bytes[bytes.length - 1] == '\n',
                        root.relativize(path) + " lacks a final newline");
            }
        }
    }

    private boolean maintainedText(Path path) {
        Path relative = root.relativize(path);
        if (relative.getNameCount() == 0) return false;
        String first = relative.getName(0).toString();
        if (first.equals(".git") || first.equals(".worldline") || first.equals("local")) return false;
        String name = path.getFileName().toString();
        return name.equals(".gitattributes") || name.equals(".editorconfig") || name.equals("AGENTS.md")
                || name.endsWith(".java") || name.endsWith(".md") || name.endsWith(".properties")
                || name.endsWith(".yml") || name.endsWith(".yaml") || name.endsWith(".json")
                || name.endsWith(".toml") || name.endsWith(".sh");
    }

    private void inspect(String scope, int width) throws IOException {
        Path directory = root.resolve(scope);
        require(Files.isDirectory(directory), "missing quality scope: " + scope);
        int longFiles = 0, longLines = 0, maximum = 0;
        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path path : paths.filter(file -> file.toString().endsWith(".java")).toList()) {
                List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
                boolean longFile = false;
                for (int index = 0; index < lines.size(); index++) {
                    String line = lines.get(index);
                    require(!line.endsWith(" ") && !line.endsWith("\t"),
                            root.relativize(path) + ":" + (index + 1) + " has trailing whitespace");
                    require(!line.startsWith("\t"),
                            root.relativize(path) + ":" + (index + 1) + " uses tab indentation");
                    maximum = Math.max(maximum, line.length());
                    if (line.length() > width) { longLines++; longFile = true; }
                }
                if (longFile) longFiles++;
                byte[] bytes = Files.readAllBytes(path);
                require(bytes.length == 0 || bytes[bytes.length - 1] == '\n',
                        root.relativize(path) + " lacks a final newline");
            }
        }
        checkBaseline(scope + ".long.files", longFiles);
        checkBaseline(scope + ".long.lines", longLines);
        checkBaseline(scope + ".max.line", maximum);
    }

    private void checkBaseline(String key, int actual) {
        int baseline = integer(key);
        require(actual == baseline, key + " changed: " + actual + " != " + baseline
                + "; improve the source and update the ratchet in the same change");
    }

    private int integer(String key) {
        String value = policy.getProperty(key);
        require(value != null, "missing source policy key: " + key);
        try { return Integer.parseInt(value.trim()); }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid " + key); }
    }

    private void requireText(String relative, String expected) throws IOException {
        String value = Files.readString(root.resolve(relative), StandardCharsets.UTF_8);
        require(value.contains(expected), relative + " must contain " + expected);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
