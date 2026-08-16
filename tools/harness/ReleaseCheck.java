import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

/** Fails closed when release metadata or the public/legal boundary drifts. */
public final class ReleaseCheck {
    private final Path root = Paths.get("").toAbsolutePath().normalize();

    public static void main(String[] arguments) {
        if (arguments.length != 0) {
            System.err.println("usage: java tools/harness/ReleaseCheck.java");
            System.exit(2);
        }
        try { new ReleaseCheck().execute(); }
        catch (Exception error) {
            System.err.println("release check failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        Properties release = load("release/worldline.properties");
        Properties harness = load("harness.properties");
        Properties artifact = load("artifacts/minecraft-b1.7.3-client.properties");
        Properties toolchain = load("toolchains/retromcp.properties");
        Properties server = load("smokes/deterministic-world-tick/smoke.properties");
        Properties client = load("smokes/controlled-client-tick/smoke.properties");
        Properties m3 = load("smokes/m3-domain-api/smoke.properties");
        Properties m4 = load("smokes/m4-durable-snapshot/smoke.properties");
        Properties lab = load("smokes/lab-cycle/smoke.properties");
        match(release, "id", "worldline");
        match(release, "version", "0.2.0");
        match(release, "milestone", "m4-durable-snapshot");
        match(release, "status", "go");
        match(release, "scope", "local-research");
        match(release, "canonical.command", "java tools/harness/Verify.java --smoke");
        same(release, "java.release", harness, "java.release");
        same(release, "client.sha256", artifact, "expected.sha256");
        same(release, "retromcp.revision", toolchain, "revision");
        same(release, "server.signature", server, "expected.signature");
        same(release, "client.signature", client, "expected.signature");
        same(release, "client.state.signature", client, "expected.state.signature");
        same(release, "m3.signature", m3, "expected.signature");
        same(release, "m4.signature", m4, "expected.snapshot.sha256");
        same(release, "lab.signature", lab, "expected.signature");
        requireText("modules/api/src/main/java/worldline/api/WorldlineVersion.java",
                "public static final String VERSION = \"" + value(release, "version") + "\";");
        for (String file : Arrays.asList("README.md", "CHANGELOG.md", "AGENTS.md",
                "docs/VISION.md", "docs/ROADMAP.md", "docs/ARCHITECTURE.md",
                "docs/FIRST_CYCLE.md", "docs/M3_API.md", "docs/M3_CYCLE.md",
                "docs/M4_SNAPSHOT.md", "docs/M4_CYCLE.md", "smokes/controlled-client-tick/MAP.md",
                "smokes/m3-domain-api/MAP.md", "smokes/m4-durable-snapshot/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        verifyPublicTree();
        System.out.println("  release: Worldline v0.2.0 M4 durable snapshot GO");
        System.out.println("  public artifact boundary: verified");
    }

    private void verifyPublicTree() throws IOException {
        Set<String> excluded = new HashSet<>(Arrays.asList(".git", ".worldline", "local"));
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                Path relative = root.relativize(path);
                if (relative.getNameCount() > 0 && excluded.contains(relative.getName(0).toString())) return;
                String name = path.getFileName().toString().toLowerCase();
                String normalized = relative.toString().replace('\\', '/').toLowerCase();
                if (name.endsWith(".jar") || name.endsWith(".class")
                        || normalized.contains("minecraft/src/")
                        || normalized.contains("minecraft/bin/")) {
                    throw new IllegalStateException("prohibited public artifact: " + relative);
                }
            });
        }
    }

    private Properties load(String relative) throws IOException {
        Properties result = new Properties();
        try (java.io.Reader reader = Files.newBufferedReader(root.resolve(relative), StandardCharsets.UTF_8)) {
            result.load(reader);
        }
        return result;
    }

    private void same(Properties left, String leftKey, Properties right, String rightKey) {
        match(left, leftKey, value(right, rightKey));
    }

    private void match(Properties source, String key, String expected) {
        String actual = value(source, key);
        if (!actual.equals(expected)) {
            throw new IllegalStateException(key + " is " + actual + "; expected " + expected);
        }
    }

    private String value(Properties source, String key) {
        String result = source.getProperty(key);
        if (result == null || result.trim().isEmpty()) throw new IllegalStateException("missing " + key);
        return result.trim();
    }

    private void requireText(String relative, String expected) throws IOException {
        String value = new String(Files.readAllBytes(root.resolve(relative)), StandardCharsets.UTF_8);
        if (!value.contains(expected)) throw new IllegalStateException(relative + " does not declare release version");
    }
}
