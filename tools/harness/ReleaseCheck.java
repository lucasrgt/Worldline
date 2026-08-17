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
        Properties serverArtifact = load("artifacts/minecraft-b1.7.3-server.properties");
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
        Properties m12 = load("smokes/m12-aero-reproduction/smoke.properties");
        Properties m13 = load("smokes/m13-aero-differential/smoke.properties");
        Properties m14 = load("smokes/m14-chunk-backlog/smoke.properties");
        Properties m15 = load("smokes/m15-chunk-contract/smoke.properties");
        Properties m16 = load("smokes/m16-adaptive-chunks/smoke.properties");
        Properties m17 = load("smokes/m17-scheduler-hardening/smoke.properties");
        Properties m17Profile = load("adapters/aero-model-lib/opt-in/worldline-adaptive.properties");
        Properties m18 = load("smokes/m18-save-attribution/smoke.properties");
        Properties m19 = load("smokes/m19-forced-autosave/smoke.properties");
        Properties m20 = load("smokes/m20-server-bootstrap/smoke.properties");
        Properties m21 = load("smokes/m21-server-control/smoke.properties");
        Properties m22 = load("smokes/m22-multiplayer-wire/smoke.properties");
        Properties m23 = load("smokes/m23-player-persistence/smoke.properties");
        Properties lab = load("smokes/lab-cycle/smoke.properties");
        Properties gui = load("smokes/gui-tree/smoke.properties");
        match(release, "id", "worldline");
        match(release, "version", "1.11.0");
        match(release, "milestone", "m23-player-persistence");
        match(release, "status", "go");
        match(release, "scope", "local-research");
        match(release, "canonical.command", "java tools/harness/Verify.java --smoke");
        same(release, "java.release", harness, "java.release");
        same(release, "client.sha256", artifact, "expected.sha256");
        same(release, "server.sha256", serverArtifact, "expected.sha256");
        same(release, "retromcp.revision", toolchain, "revision");
        same(release, "server.signature", server, "expected.signature");
        same(release, "client.signature", client, "expected.signature");
        same(release, "client.state.signature", client, "expected.state.signature");
        same(release, "m2.signature", client, "expected.state.signature");
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
        same(release, "m12.signature", m12, "expected.signature");
        same(release, "m12.aero.revision", m12, "aero.revision");
        same(release, "m12.seed", m12, "seed");
        same(release, "m13.signature", m13, "expected.signature");
        same(release, "m13.aero.revision", m13, "aero.revision");
        same(release, "m13.seed", m13, "seed");
        same(release, "m14.signature", m14, "expected.signature");
        same(release, "m14.aero.revision", m14, "aero.revision");
        same(release, "m14.seed", m14, "seed");
        same(release, "m15.signature", m15, "expected.signature");
        same(release, "m15.aero.revision", m15, "aero.revision");
        same(release, "m15.seed", m15, "seed");
        same(release, "m16.signature", m16, "expected.signature");
        same(release, "m16.aero.revision", m16, "aero.revision");
        same(release, "m16.seed", m16, "seed");
        same(release, "m17.signature", m17, "expected.signature");
        same(release, "m17.aero.revision", m17, "aero.revision");
        same(release, "m17.seed", m17, "seed");
        match(m17Profile, "default.enabled", "false");
        match(m17Profile, "shipping.status", "lab-only-no-go");
        same(release, "m18.signature", m18, "expected.signature");
        same(release, "m18.aero.revision", m18, "aero.revision");
        same(release, "m18.seed", m18, "seed");
        same(release, "m19.signature", m19, "expected.signature");
        same(release, "m19.aero.revision", m19, "aero.revision");
        same(release, "m19.seed", m19, "seed");
        same(release, "m20.signature", m20, "expected.signature");
        same(release, "server.sha256", m20, "server.jar.sha256");
        same(release, "m21.signature", m21, "expected.signature");
        same(release, "server.sha256", m21, "server.jar.sha256");
        same(release, "m22.signature", m22, "expected.signature");
        same(release, "server.sha256", m22, "server.jar.sha256");
        same(release, "m23.signature", m23, "expected.signature");
        same(release, "server.sha256", m23, "server.jar.sha256");
        same(release, "lab.signature", lab, "expected.signature");
        same(release, "gui.signature", gui, "expected.signature");
        same(release, "invariants.signature", client, "expected.state.signature");
        match(release, "semantics.signature",
                "b4d1f4fdf968f785cc5c94b2400d5f4ad4966f8f7b042d0fd2372d24e9dadf88");
        requireText("modules/api/src/main/java/worldline/api/WorldlineVersion.java",
                "public static final String VERSION = \"" + value(release, "version") + "\";");
        requireText("docs/SEMANTICS_CYCLE.md", value(release, "semantics.signature"));
        for (String file : Arrays.asList("README.md", "CHANGELOG.md", "AGENTS.md",
                "docs/VISION.md", "docs/ROADMAP.md", "docs/ARCHITECTURE.md",
                "optimizations/TEMPLATE.properties",
                "docs/FIRST_CYCLE.md", "docs/M2_RUNTIME.md", "docs/M2_CYCLE.md",
                "docs/M3_API.md", "docs/M3_CYCLE.md",
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
                "smokes/m9-scenario-minimization/MAP.md", "docs/GUI_TREE.md", "docs/GUI_CYCLE.md",
                "smokes/gui-tree/MAP.md", "docs/INVARIANTS.md", "docs/INVARIANTS_CYCLE.md",
                "docs/SEMANTICS.md", "docs/SEMANTICS_CYCLE.md",
                "docs/OPTIMIZATION_SDK.md",
                "optimizations/catalog/README.md")) {
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
        for (String file : Arrays.asList("docs/M12_CAPTURE.md", "docs/M12_CYCLE.md",
                "smokes/m12-aero-reproduction/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M13_DIFFERENTIAL.md", "docs/M13_CYCLE.md",
                "smokes/m13-aero-differential/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M14_CHUNK_BACKLOG.md", "docs/M14_CYCLE.md",
                "smokes/m14-chunk-backlog/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M15_CHUNK_CONTRACT.md", "docs/M15_CYCLE.md",
                "smokes/m15-chunk-contract/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M16_ADAPTIVE_CHUNKS.md", "docs/M16_CYCLE.md",
                "smokes/m16-adaptive-chunks/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M17_SCHEDULER_HARDENING.md", "docs/M17_CYCLE.md",
                "smokes/m17-scheduler-hardening/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M18_SAVE_ATTRIBUTION.md", "docs/M18_CYCLE.md",
                "smokes/m18-save-attribution/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M19_FORCED_AUTOSAVE.md", "docs/M19_CYCLE.md",
                "smokes/m19-forced-autosave/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("artifacts/minecraft-b1.7.3-server.properties",
                "docs/M20_SERVER_BOOTSTRAP.md", "docs/M20_CYCLE.md",
                "smokes/m20-server-bootstrap/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M21_SERVER_CONTROL.md", "docs/M21_CYCLE.md",
                "smokes/m21-server-control/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M22_MULTIPLAYER_WIRE.md", "docs/M22_CYCLE.md",
                "smokes/m22-multiplayer-wire/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        for (String file : Arrays.asList("docs/M23_PLAYER_PERSISTENCE.md", "docs/M23_CYCLE.md",
                "smokes/m23-player-persistence/MAP.md")) {
            if (!Files.isRegularFile(root.resolve(file))) throw new IllegalStateException("missing " + file);
        }
        verifyPublicTree();
        System.out.println("  release: Worldline v1.11.0 M23 player persistence GO");
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
