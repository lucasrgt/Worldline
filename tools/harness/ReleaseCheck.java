import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/** Fails closed when distributed release evidence or the public/legal boundary drifts. */
public final class ReleaseCheck {
    private static final Pattern MILESTONE = Pattern.compile("m(\\d+)-[a-z0-9-]+");
    private static final Pattern SHA256 = Pattern.compile("[0-9a-f]{64}");
    private static final Set<String> RELEASE_KEYS = Set.of(
            "id", "version", "milestone", "status", "scope", "java.release",
            "canonical.command", "client.sha256", "server.sha256", "retromcp.revision",
            "server.signature", "client.signature", "client.state.signature", "lab.signature",
            "gui.signature", "invariants.signature", "semantics.signature");

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
        Properties release = load(root.resolve("release/worldline.properties"), true);
        require(new TreeSet<>(release.stringPropertyNames()).equals(new TreeSet<>(RELEASE_KEYS)),
                "release manifest keys drifted: " + release.stringPropertyNames());
        match(release, "id", "worldline");
        match(release, "status", "go");
        match(release, "scope", "local-research");
        match(release, "canonical.command", "java tools/harness/Gate.java --smoke");
        String version = value(release, "version");
        require(version.matches("[0-9]+\\.[0-9]+\\.[0-9]+"), "invalid release version: " + version);

        Properties harness = load(root.resolve("harness.properties"), true);
        Properties clientArtifact = load(root.resolve("artifacts/minecraft-b1.7.3-client.properties"), true);
        Properties serverArtifact = load(root.resolve("artifacts/minecraft-b1.7.3-server.properties"), true);
        Properties toolchain = load(root.resolve("toolchains/retromcp.properties"), true);
        same(release, "java.release", harness, "java.release");
        same(release, "client.sha256", clientArtifact, "expected.sha256");
        same(release, "server.sha256", serverArtifact, "expected.sha256");
        same(release, "retromcp.revision", toolchain, "revision");

        List<SmokeDiscovery.Entry> entries = SmokeDiscovery.discover(root);
        for (SmokeDiscovery.Entry entry : entries) verifyDescriptor(entry, release);
        verifyCoreSignatures(release);
        verifyCurrentRelease(release, entries);
        verifyVersionedDocuments(release);
        verifyPublicTree();
        System.out.println("  release: Worldline v" + version + " "
                + value(release, "milestone").toUpperCase() + " GO");
        System.out.println("  public artifact boundary: verified");
    }

    private void verifyDescriptor(SmokeDiscovery.Entry entry, Properties release) throws IOException {
        Path directory = root.resolve("smokes").resolve(entry.id);
        Properties descriptor = load(directory.resolve("smoke.properties"), false);
        for (String key : descriptor.stringPropertyNames()) {
            String candidate = descriptor.getProperty(key).trim();
            if ((key.endsWith("signature") || key.endsWith("sha256")) && key.startsWith("expected."))
                require(SHA256.matcher(candidate).matches(), entry.id + " has invalid " + key);
        }
        compareIfPresent(descriptor, "server.jar.sha256", release, "server.sha256", entry.id);
        compareIfPresent(descriptor, "client.jar.sha256", release, "client.sha256", entry.id);
        Path map = directory.resolve("MAP.md");
        Matcher milestone = MILESTONE.matcher(entry.id);
        if (milestone.matches() && Files.isRegularFile(map))
            requireFile(root.resolve("docs/M" + milestone.group(1) + "_CYCLE.md"));
    }

    private void verifyCoreSignatures(Properties release) throws IOException {
        Properties server = descriptor("deterministic-world-tick");
        Properties client = descriptor("controlled-client-tick");
        same(release, "server.signature", server, "expected.signature");
        same(release, "client.signature", client, "expected.signature");
        same(release, "client.state.signature", client, "expected.state.signature");
        same(release, "invariants.signature", client, "expected.state.signature");
        same(release, "lab.signature", descriptor("lab-cycle"), "expected.signature");
        same(release, "gui.signature", descriptor("gui-tree"), "expected.signature");
        requireText("docs/SEMANTICS_CYCLE.md", value(release, "semantics.signature"));
        Properties profile = load(root.resolve(
                "adapters/aero-model-lib/opt-in/worldline-adaptive.properties"), true);
        match(profile, "default.enabled", "false");
        match(profile, "shipping.status", "lab-only-no-go");
    }

    private void verifyCurrentRelease(Properties release, List<SmokeDiscovery.Entry> entries)
            throws IOException {
        String id = value(release, "milestone");
        require(entries.stream().anyMatch(entry -> entry.id.equals(id)), "unknown release milestone: " + id);
        Properties descriptor = descriptor(id);
        require(SHA256.matcher(value(descriptor, "expected.signature")).matches(),
                "release milestone lacks a frozen expected.signature: " + id);
        requireFile(root.resolve("smokes").resolve(id).resolve("MAP.md"));
        Matcher matcher = MILESTONE.matcher(id);
        require(matcher.matches(), "release milestone must use mN-slug form: " + id);
        requireFile(root.resolve("docs/M" + matcher.group(1) + "_CYCLE.md"));
    }

    private void verifyVersionedDocuments(Properties release) throws IOException {
        String version = value(release, "version");
        requireText("modules/api/src/main/java/worldline/api/WorldlineVersion.java",
                "public static final String VERSION = \"" + version + "\";");
        requireText("README.md", "v" + version);
        try { new ChangelogCheck(root).execute(version); }
        catch (Exception error) { throw new IOException("changelog validation failed", error); }
        for (String relative : List.of("AGENTS.md", "docs/VISION.md", "docs/ROADMAP.md",
                "docs/ARCHITECTURE.md", "docs/FIRST_CYCLE.md", "docs/INVARIANTS.md",
                "docs/SEMANTICS.md", "docs/OPTIMIZATION_SDK.md", "optimizations/TEMPLATE.properties",
                "optimizations/catalog/README.md")) requireFile(root.resolve(relative));
    }

    private void verifyPublicTree() throws IOException {
        Set<String> excluded = new HashSet<>(Set.of(".git", ".worldline", "local"));
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                Path relative = root.relativize(path);
                if (relative.getNameCount() > 0 && excluded.contains(relative.getName(0).toString())) return;
                String name = path.getFileName().toString().toLowerCase();
                String normalized = relative.toString().replace('\\', '/').toLowerCase();
                require(!name.endsWith(".jar") && !name.endsWith(".class")
                        && !normalized.contains("minecraft/src/") && !normalized.contains("minecraft/bin/"),
                        "prohibited public artifact: " + relative);
            });
        }
    }

    private Properties descriptor(String id) throws IOException {
        return load(root.resolve("smokes").resolve(id).resolve("smoke.properties"), true);
    }

    private static Properties load(Path path, boolean required) throws IOException {
        Properties result = new Properties();
        if (!Files.isRegularFile(path)) {
            require(!required, "missing " + path);
            return result;
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            result.load(reader);
        }
        return result;
    }

    private static void compareIfPresent(Properties left, String leftKey, Properties right,
            String rightKey, String id) {
        String candidate = left.getProperty(leftKey);
        if (candidate != null) require(candidate.trim().equals(value(right, rightKey)),
                id + " " + leftKey + " drifted from " + rightKey);
    }

    private static void same(Properties left, String leftKey, Properties right, String rightKey) {
        match(left, leftKey, value(right, rightKey));
    }

    private static void match(Properties source, String key, String expected) {
        String actual = value(source, key);
        require(actual.equals(expected), key + " is " + actual + "; expected " + expected);
    }

    private static String value(Properties source, String key) {
        String result = source.getProperty(key);
        require(result != null && !result.trim().isEmpty(), "missing " + key);
        return result.trim();
    }

    private void requireText(String relative, String expected) throws IOException {
        String contents = Files.readString(root.resolve(relative), StandardCharsets.UTF_8);
        require(contents.contains(expected), relative + " does not contain " + expected);
    }

    private static void requireFile(Path path) {
        require(Files.isRegularFile(path), "missing " + path);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
