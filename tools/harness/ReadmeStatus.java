import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/** Generates and verifies the README release/evidence status from canonical repository inputs. */
public final class ReadmeStatus {
    private static final String START = "<!-- worldline-status:start -->";
    private static final String END = "<!-- worldline-status:end -->";
    private final Path root;

    ReadmeStatus(Path root) { this.root = root; }

    public static void main(String[] arguments) {
        try {
            if (List.of(arguments).equals(List.of("--self-test"))) { selfTest(); return; }
            require(arguments.length == 1 && List.of("update", "check").contains(arguments[0]), usage());
            ReadmeStatus status = new ReadmeStatus(Path.of("").toAbsolutePath().normalize());
            if (arguments[0].equals("update")) status.update(); else status.check();
        } catch (Exception error) {
            System.err.println("README status failed: " + error.getMessage());
            System.exit(1);
        }
    }

    void update() throws Exception {
        Path readme = root.resolve("README.md");
        String original = normalize(Files.readString(readme, StandardCharsets.UTF_8));
        String generated = generated(original);
        Files.writeString(readme, generated, StandardCharsets.UTF_8);
        System.out.println("README status updated");
    }

    void check() throws Exception {
        String actual = normalize(Files.readString(root.resolve("README.md"), StandardCharsets.UTF_8));
        require(actual.equals(generated(actual)),
                "generated status or release badge drifted; run java tools/harness/ReadmeStatus.java update");
        System.out.println("  README status: generated block verified");
    }

    private String generated(String readme) throws Exception {
        Properties release = load(root.resolve("release/worldline.properties"));
        Properties coverage = load(root.resolve("behavior/coverage.properties"));
        String version = required(release, "version"), status = required(release, "status").toUpperCase(Locale.ROOT);
        String milestone = required(release, "milestone");
        int manifests = manifests(), pending = Integer.parseInt(required(coverage, "pending.expected"));
        long pins = Files.readAllLines(root.resolve("smokes/qualification.lock"), StandardCharsets.UTF_8)
                .stream().filter(line -> line.endsWith(".status=passed")).count();
        require(pending >= 0 && pending <= manifests && pins <= manifests, "invalid README status counts");
        String block = START + "\n| Release | Milestone | Behavior contracts | Portable smoke proofs |\n"
                + "| --- | --- | --- | --- |\n| v" + version + " " + status + " | `" + milestone + "` | "
                + (manifests - pending) + "/" + manifests + " complete | " + pins + "/" + manifests
                + " pinned |\n" + END;
        String result = badge(readme, version, status);
        int start = result.indexOf(START), end = result.indexOf(END);
        if (start >= 0 || end >= 0) {
            require(start >= 0 && end > start && result.indexOf(START, start + 1) < 0
                    && result.indexOf(END, end + 1) < 0, "invalid generated README status markers");
            result = result.substring(0, start) + block + result.substring(end + END.length());
        } else {
            String anchor = "The current release is declared in";
            int at = result.indexOf(anchor); require(at >= 0, "README status insertion anchor is missing");
            result = result.substring(0, at) + block + "\n\n" + result.substring(at);
        }
        return result;
    }

    private static String badge(String readme, String version, String status) {
        StringBuilder result = new StringBuilder(); boolean replaced = false;
        for (String line : readme.split("\n", -1)) {
            if (line.contains("img.shields.io/badge/release-")) {
                require(!replaced, "multiple README release badges"); replaced = true;
                String color = status.equals("GO") ? "2EA44F" : "CB8B2C";
                line = "  <img src=\"https://img.shields.io/badge/release-v" + version + "%20" + status
                        + "-" + color + "?style=flat-square\" alt=\"Worldline v" + version + " " + status + "\">";
            }
            result.append(line).append('\n');
        }
        require(replaced, "README release badge is missing");
        return result.substring(0, result.length() - 1);
    }

    private int manifests() throws Exception {
        try (var paths = Files.list(root.resolve("smokes"))) {
            return (int) paths.filter(Files::isDirectory)
                    .filter(path -> Files.isRegularFile(path.resolve("smoke.properties"))).count();
        }
    }

    static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-readme-status-");
        try {
            Files.createDirectories(root.resolve("release"));
            Files.createDirectories(root.resolve("behavior"));
            Files.createDirectories(root.resolve("smokes/m1-one"));
            Files.createDirectories(root.resolve("smokes/m2-two"));
            Files.writeString(root.resolve("release/worldline.properties"),
                    "version=1.2.3\nstatus=go\nmilestone=m2-two\n");
            Files.writeString(root.resolve("behavior/coverage.properties"), "pending.expected=0\n");
            Files.writeString(root.resolve("smokes/m1-one/smoke.properties"), "id=m1-one\n");
            Files.writeString(root.resolve("smokes/m2-two/smoke.properties"), "id=m2-two\n");
            Files.writeString(root.resolve("smokes/qualification.lock"), "smoke.m1.status=passed\n");
            Files.writeString(root.resolve("README.md"), "<img src=\"https://img.shields.io/badge/release-old\">\n\n"
                    + "The current release is declared in metadata.\n");
            ReadmeStatus generated = new ReadmeStatus(root); generated.update(); generated.check();
            String text = Files.readString(root.resolve("README.md"));
            require(text.contains("v1.2.3%20GO") && text.contains("2/2 complete")
                    && text.contains("1/2 pinned"), "README status rendering drifted");
            Files.writeString(root.resolve("README.md"), text.replace("2/2 complete", "1/2 complete"));
            boolean rejected = false;
            try { generated.check(); } catch (IllegalStateException expected) { rejected = true; }
            require(rejected, "manual README status edit was accepted");
            System.out.println("README status self-test passed");
        } finally { delete(root); }
    }

    private static void delete(Path root) throws Exception {
        if (!Files.exists(root)) return;
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attributes)
                    throws java.io.IOException { Files.delete(file); return FileVisitResult.CONTINUE; }
            @Override public FileVisitResult postVisitDirectory(Path directory, java.io.IOException error)
                    throws java.io.IOException {
                if (error != null) throw error; Files.delete(directory); return FileVisitResult.CONTINUE;
            }
        });
    }

    private static Properties load(Path path) throws Exception {
        Properties values = new Properties();
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { values.load(reader); }
        return values;
    }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key);
        require(value != null && !value.isBlank(), "missing " + key); return value.trim();
    }
    private static String normalize(String value) { return value.replace("\r\n", "\n"); }
    private static String usage() { return "usage: ReadmeStatus.java update|check"; }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
