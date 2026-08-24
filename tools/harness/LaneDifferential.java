import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;

/** Records and seals one Windows/Linux official headless semantic differential. */
final class LaneDifferential {
    private static Path cachedRoot;
    private static Boolean cachedPortable;
    private LaneDifferential() { }
    public static void main(String[] arguments) {
        try {
            require(List.of(arguments).equals(List.of("--seal")),
                    "usage: LaneDifferential --seal");
            seal(Path.of("").toAbsolutePath().normalize());
        } catch (Exception error) {
            System.err.println("lane differential seal failed: " + error.getMessage()); System.exit(1);
        }
    }
    static void execute(String id) throws Exception {
        Path root = Path.of("").toAbsolutePath().normalize();
        SmokeGitState state = SmokeGitState.read(root); require(state.clean(),
                "lane differential requires a clean commit");
        SmokeDiscovery.Entry smoke = SmokeDiscovery.require(root, id);
        require(lane(root, smoke).equals("server-headless"),
                "lane differential requires a headless server smoke");
        SmokeObservationCache observations = new SmokeObservationCache(root);
        String runtimeFingerprint = observations.fingerprint(smoke);
        SmokeObservationCache.Observation restored = observations.restore(smoke, runtimeFingerprint);
        long duration;
        if (restored == null) {
            duration = SmokeExecution.run(root, smoke, prepareProducts(root));
            observations.observed(smoke, runtimeFingerprint, duration);
        } else {
            duration = restored.duration();
            System.out.println("  lane observation restored: " + id);
        }
        Path log = root.resolve(".worldline/smoke-logs").resolve(id + ".log");
        Properties descriptor = load(root.resolve("smokes").resolve(id).resolve("smoke.properties"));
        String expected = descriptor.getProperty("expected.signature", "");
        List<String> semantic = semanticRows(Files.readAllLines(log, StandardCharsets.UTF_8));
        require(expected.matches("[0-9a-f]{64}")
                        && semantic.stream().anyMatch(row -> row.endsWith("=" + expected)
                                || row.equals("signature: " + expected)),
                "lane differential lacks the frozen signature");
        Properties record = new Properties(); String platform = platform();
        record.setProperty("schema", "1"); record.setProperty("platform", platform);
        record.setProperty("id", id); record.setProperty("head", state.head());
        record.setProperty("tree", state.tree()); record.setProperty("duration.ms", Long.toString(duration));
        record.setProperty("runtime.fingerprint", runtimeFingerprint);
        record.setProperty("semantic.sha256", digest(String.join("\n", semantic) + "\n"));
        record.setProperty("evidence.sha256", digest(Files.readAllBytes(log)));
        Path path = root.resolve(".worldline/lane-differential").resolve(platform + ".properties");
        Files.createDirectories(path.getParent());
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            record.store(writer, "Worldline official headless lane differential");
        }
        System.out.println("WORLDLINE_LANE_DIFFERENTIAL=PASS;platform=" + platform + ";id=" + id);
    }
    static void seal(Path root) throws Exception {
        Properties windows = load(root.resolve(".worldline/lane-differential/windows.properties"));
        Properties linux = load(root.resolve(".worldline/lane-differential/linux.properties"));
        for (Properties record : List.of(windows, linux)) require("1".equals(record.getProperty("schema"))
                && record.getProperty("id", "").matches("[a-z0-9]+(?:-[a-z0-9]+)*"),
                "invalid lane differential record");
        require("windows".equals(windows.getProperty("platform"))
                        && "linux".equals(linux.getProperty("platform"))
                        && windows.getProperty("id").equals(linux.getProperty("id"))
                        && windows.getProperty("head").equals(linux.getProperty("head"))
                        && windows.getProperty("tree").equals(linux.getProperty("tree"))
                        && windows.getProperty("semantic.sha256").equals(
                                linux.getProperty("semantic.sha256")),
                "Windows/Linux headless semantics diverged");
        Properties lock = new Properties(); lock.setProperty("schema", "1");
        lock.setProperty("lane", "server-headless"); lock.setProperty("status", "portable");
        lock.setProperty("id", windows.getProperty("id"));
        for (String platform : List.of("windows", "linux")) {
            Properties record = platform.equals("windows") ? windows : linux;
            for (String key : List.of("head", "tree", "runtime.fingerprint", "semantic.sha256",
                    "evidence.sha256")) lock.setProperty(platform + "." + key, record.getProperty(key));
        }
        store(root.resolve("smokes/lane-portability.lock"), lock);
        System.out.println("server-headless portability sealed across Windows/Linux");
    }
    static synchronized boolean portable(Path root) {
        root = root.toAbsolutePath().normalize();
        if (root.equals(cachedRoot) && cachedPortable != null) return cachedPortable;
        boolean result = false;
        try { Properties lock = load(root.resolve("smokes/lane-portability.lock"));
            String head = lock.getProperty("windows.head", "");
            Process ancestor = new ProcessBuilder("git", "merge-base", "--is-ancestor", head, "HEAD")
                    .directory(root.toFile()).redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
            result = "1".equals(lock.getProperty("schema"))
                    && "server-headless".equals(lock.getProperty("lane"))
                    && "portable".equals(lock.getProperty("status"))
                    && head.matches("[0-9a-f]{40,64}")
                    && head.equals(lock.getProperty("linux.head"))
                    && lock.getProperty("windows.tree", "").equals(lock.getProperty("linux.tree"))
                    && lock.getProperty("windows.semantic.sha256", "").matches("[0-9a-f]{64}")
                    && lock.getProperty("windows.semantic.sha256").equals(
                            lock.getProperty("linux.semantic.sha256"))
                    && ancestor.waitFor() == 0;
        } catch (Exception error) { result = false; }
        cachedRoot = root; cachedPortable = result; return result;
    }
    static boolean portableQualification(Path root, SmokeDiscovery.Entry smoke) throws Exception {
        return portable(root) && "server-headless".equals(lane(root, smoke));
    }
    static void selfTest() throws Exception {
        String signature = "a".repeat(64);
        List<String> rows = semanticRows(List.of("milestone data-driven cycle passed",
                "  trace: v1|stable", "  signature: " + signature, "FROZEN",
                "WORLDLINE_AWAIT_TELEMETRY=id=x;waits=1", "WORLDLINE_DIRECT=" + signature));
        require(rows.equals(List.of("FROZEN", "WORLDLINE_DIRECT=" + signature,
                "signature: " + signature, "trace: v1|stable")),
                "lane semantic normalization drifted");
        System.out.println("  lane differential self-test: passed");
    }
    private static List<String> semanticRows(List<String> lines) {
        return lines.stream().map(String::trim).filter(row -> row.equals("FROZEN")
                || row.startsWith("trace: ") || row.startsWith("signature: ")
                || row.startsWith("WORLDLINE_")
                && !row.startsWith("WORLDLINE_AWAIT_TELEMETRY=")
                && !row.startsWith("WORLDLINE_FLAKE_")).sorted().toList();
    }
    private static Path prepareProducts(Path root) throws Exception {
        Properties config = load(root.resolve("harness.properties"));
        List<String> modules = java.util.Arrays.stream(config.getProperty("modules", "").split(","))
                .map(String::trim).filter(value -> !value.isEmpty()).toList();
        require(!modules.isEmpty(), "lane differential has no configured modules");
        Path build = root.resolve(".worldline/lane-differential/build");
        new ModuleBuild(root, build, config, modules).compileAll();
        return build.resolve("classes");
    }
    private static String lane(Path root, SmokeDiscovery.Entry smoke) throws Exception {
        Properties values = load(root.resolve("smokes").resolve(smoke.id).resolve("smoke.properties"));
        if ("tooling-cycle".equals(values.getProperty("qualification.proof"))) return "tooling";
        String source = Files.readString(root.resolve(smoke.runner));
        return source.contains("minecraft-b1.7.3-client.properties") || source.contains("aero-model-lib")
                || source.contains("runClient") || source.contains("WORLDLINE_AERO")
                ? "windows-client-gui" : "server-headless";
    }
    static String platform() { return System.getProperty("os.name", "").toLowerCase().contains("win")
            ? "windows" : "linux"; }
    private static Properties load(Path path) throws Exception { Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader); } return values; }
    private static String digest(String value) throws Exception {
        return digest(value.getBytes(StandardCharsets.UTF_8)); }
    private static String digest(byte[] value) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(value)); }
    private static void store(Path path, Properties values) throws Exception {
        TreeMap<String, String> sorted = new TreeMap<>();
        for (String key : values.stringPropertyNames()) sorted.put(key, values.getProperty(key));
        StringBuilder output = new StringBuilder("# Worldline Windows/Linux headless differential v1\n");
        for (var row : sorted.entrySet()) output.append(row.getKey()).append('=').append(row.getValue()).append('\n');
        Files.writeString(path, output.toString(), StandardCharsets.UTF_8); }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message); }
}
