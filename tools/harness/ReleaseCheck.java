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
        Properties m5 = load("smokes/m5-reproduction-bundle/smoke.properties");
        Properties m6 = load("smokes/m6-trace-explorer/smoke.properties");
        Properties m7 = load("smokes/m7-mod-loading/smoke.properties");
        Properties m8 = load("smokes/m8-mod-version-diff/smoke.properties");
        Properties m9 = load("smokes/m9-scenario-minimization/smoke.properties");
        Properties m10 = load("smokes/m10-native-render/smoke.properties");
        Properties m11 = load("smokes/m11-aero-attribution/smoke.properties");
        Properties lab = load("smokes/lab-cycle/smoke.properties");
        match(release, "id", "worldline");
        match(release, "version", "0.9.0");
        match(release, "milestone", "m11-aero-attribution");
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
        same(release, "m5.signature", m5, "expected.bundle.sha256");
        same(release, "m6.signature", m6, "expected.divergence.sha256");
        same(release, "m7.signature", m7, "expected.signature");
        same(release, "m8.signature", m8, "expected.signature");
        same(release, "m9.signature", m9, "expected.signature");
        same(release, "m10.signature", m10, "expected.frame.sha256");
        same(release, "m10.aero.status", m10, "aero.status");
        same(release, "m11.signature", m11, "expected.signature");
        same(release, "m11.aero.revision", m11, "aero.revision");
        same(release, "m11.aero.version", m11, "aero.version");
        same(release, "lab.signature", lab, "expected.signature");
        requireText("modules/api/src/main/java/worldline/api/WorldlineVersion.java",
                "public static final String VERSION = \"" + value(release, "version") + "\";");
        for (String file : Arrays.asList("README.md", "CHANGELOG.md", "AGENTS.md",
                "docs/VISION.md", "docs/ROADMAP.md", "docs/ARCHITECTURE.md",
                "docs/FIRST_CYCLE.md", "docs/M3_API.md", "docs/M3_CYCLE.md",
                "docs/M4_SNAPSHOT.md", "docs/M4_CYCLE.md", "smokes/controlled-client-tick/MAP.md",
                "docs/M5_BUNDLE.md", "docs/M5_CYCLE.md", "smokes/m3-domain-api/MAP.md",
                "docs/M6_TRACE.md", "docs/M6_CYCLE.md", "smokes/m4-durable-snapshot/MAP.md",
                "smokes/m5-reproduction-bundle/MAP.md", "smokes/m6-trace-explorer/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M7_MODS.md", "docs/M7_CYCLE.md",
                "smokes/m7-mod-loading/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M8_RESULTS.md", "docs/M8_CYCLE.md",
                "smokes/m8-mod-version-diff/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M9_MINIMIZATION.md", "docs/M9_CYCLE.md",
                "smokes/m9-scenario-minimization/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M10_RENDER.md", "docs/M10_CYCLE.md",
                "smokes/m10-native-render/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M11_ATTRIBUTION.md", "docs/M11_CYCLE.md",
                "smokes/m11-aero-attribution/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        verifyPublicTree();
        System.out.println("  release: Worldline v0.9.0 M11 Aero attribution GO");
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
