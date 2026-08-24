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

/** Records and seals the permanent Windows/Linux official headless seed matrix. */
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
            System.err.println("lane differential seal failed: " + error.getMessage());
            System.exit(1);
        }
    }

    static void execute(String id) throws Exception {
        Path root = Path.of("").toAbsolutePath().normalize();
        SmokeGitState state = SmokeGitState.read(root);
        require(state.clean(), "lane differential requires a clean commit");
        LaneMatrixContract matrix = LaneMatrixContract.load(root);
        require(matrix.id().equals(id),
                "lane differential must execute the permanent matrix: " + matrix.id());
        SmokeDiscovery.Entry smoke = SmokeDiscovery.require(root, id);
        require(SmokeLane.classify(root, smoke).equals(SmokeLane.SERVER),
                "lane differential requires a headless server smoke");
        Properties descriptor = load(root.resolve("smokes").resolve(id).resolve("smoke.properties"));
        matrix.validateDescriptor(descriptor);
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
        String expected = required(descriptor, "expected.signature");
        String signal = required(descriptor, "expected.signal");
        List<String> semantic = semanticRows(Files.readAllLines(log, StandardCharsets.UTF_8), signal);
        require(expected.matches("[0-9a-f]{64}")
                        && semantic.contains("signature: " + expected)
                        && semantic.contains("signal: " + signal),
                "lane differential lacks the frozen matrix semantics");
        Properties record = new Properties();
        String platform = platform();
        record.setProperty("schema", "2");
        record.setProperty("platform", platform);
        record.setProperty("id", id);
        record.setProperty("head", state.head());
        record.setProperty("tree", state.tree());
        record.setProperty("duration.ms", Long.toString(duration));
        record.setProperty("runtime.fingerprint", runtimeFingerprint);
        record.setProperty("qualification.fingerprint",
                new SmokeInputFingerprint(root).compute(smoke));
        record.setProperty("semantic.sha256", digest(String.join("\n", semantic) + "\n"));
        record.setProperty("evidence.sha256", digest(Files.readAllBytes(log)));
        matrix.write(record);
        Path path = root.resolve(".worldline/lane-differential").resolve(platform + ".properties");
        Files.createDirectories(path.getParent());
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            record.store(writer, "Worldline official headless seed-matrix differential");
        }
        System.out.println("WORLDLINE_LANE_DIFFERENTIAL=PASS;platform=" + platform + ";id=" + id
                + ";seeds=" + matrix.seedCount() + ";chunks=" + matrix.chunkCount()
                + ";cases=" + matrix.caseCount());
    }

    static void seal(Path root) throws Exception {
        LaneMatrixContract matrix = LaneMatrixContract.load(root);
        Properties windows = load(root.resolve(".worldline/lane-differential/windows.properties"));
        Properties linux = load(root.resolve(".worldline/lane-differential/linux.properties"));
        for (Properties record : List.of(windows, linux)) {
            require("2".equals(record.getProperty("schema"))
                            && matrix.id().equals(record.getProperty("id")),
                    "invalid lane matrix record");
            matrix.validateRecord(record);
        }
        require("windows".equals(windows.getProperty("platform"))
                        && "linux".equals(linux.getProperty("platform"))
                        && equal(windows, linux, "id")
                        && equal(windows, linux, "head")
                        && equal(windows, linux, "tree")
                        && equal(windows, linux, "qualification.fingerprint")
                        && equal(windows, linux, "semantic.sha256")
                        && equal(windows, linux, "matrix.contract.sha256"),
                "Windows/Linux headless matrix semantics diverged");
        Properties lock = new Properties();
        lock.setProperty("schema", "2");
        lock.setProperty("lane", "server-headless");
        lock.setProperty("status", "portable");
        lock.setProperty("id", matrix.id());
        matrix.write(lock);
        for (String platform : List.of("windows", "linux")) {
            Properties record = platform.equals("windows") ? windows : linux;
            for (String key : List.of("head", "tree", "runtime.fingerprint",
                    "qualification.fingerprint", "semantic.sha256", "evidence.sha256"))
                lock.setProperty(platform + "." + key, required(record, key));
        }
        store(root.resolve("smokes/lane-portability.lock"), lock);
        System.out.println("server-headless seed-matrix portability sealed across Windows/Linux");
    }

    static synchronized boolean portable(Path root) {
        root = root.toAbsolutePath().normalize();
        if (root.equals(cachedRoot) && cachedPortable != null) return cachedPortable;
        boolean result = false;
        try {
            Properties lock = load(root.resolve("smokes/lane-portability.lock"));
            String head = lock.getProperty("windows.head", "");
            Process ancestor = new ProcessBuilder("git", "merge-base", "--is-ancestor", head, "HEAD")
                    .directory(root.toFile()).redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
            result = commonLock(lock, head) && ancestor.waitFor() == 0;
            if (result && "2".equals(lock.getProperty("schema"))) {
                LaneMatrixContract matrix = LaneMatrixContract.load(root);
                matrix.validateRecord(lock);
                result = matrix.id().equals(lock.getProperty("id"))
                        && lock.getProperty("windows.qualification.fingerprint", "")
                                .matches("[0-9a-f]{64}")
                        && equal(lock, "windows.qualification.fingerprint",
                                "linux.qualification.fingerprint");
            }
        } catch (Exception error) {
            result = false;
        }
        cachedRoot = root;
        cachedPortable = result;
        return result;
    }

    private static boolean commonLock(Properties lock, String head) {
        String schema = lock.getProperty("schema", "");
        return ("1".equals(schema) || "2".equals(schema))
                && "server-headless".equals(lock.getProperty("lane"))
                && "portable".equals(lock.getProperty("status"))
                && head.matches("[0-9a-f]{40,64}")
                && head.equals(lock.getProperty("linux.head"))
                && equal(lock, "windows.tree", "linux.tree")
                && lock.getProperty("windows.semantic.sha256", "").matches("[0-9a-f]{64}")
                && equal(lock, "windows.semantic.sha256", "linux.semantic.sha256");
    }

    static boolean portableQualification(Path root, SmokeDiscovery.Entry smoke) throws Exception {
        return portable(root) && SmokeLane.SERVER.equals(SmokeLane.classify(root, smoke));
    }

    static void selfTest() throws Exception {
        String signature = "a".repeat(64);
        String signal = "seeds=2,chunks=2,cases=4,matrix=" + "b".repeat(64);
        List<String> rows = semanticRows(List.of("milestone data-driven cycle passed", signal,
                "  trace: v1|stable", "  signature: " + signature, "FROZEN",
                "WORLDLINE_AWAIT_TELEMETRY=id=x;waits=1", "WORLDLINE_DIRECT=" + signature), signal);
        require(rows.equals(List.of("FROZEN", "WORLDLINE_DIRECT=" + signature,
                "signal: " + signal, "signature: " + signature, "trace: v1|stable")),
                "lane semantic normalization drifted");
        Properties matrix = new Properties();
        matrix.setProperty("schema", "1");
        matrix.setProperty("id", "m999-matrix");
        matrix.setProperty("seed.count", "2");
        matrix.setProperty("seed.1", "1");
        matrix.setProperty("seed.2", "2");
        matrix.setProperty("chunk.count", "2");
        matrix.setProperty("chunk.1", "0:0");
        matrix.setProperty("chunk.2", "1:-1");
        matrix.setProperty("case.count", "4");
        LaneMatrixContract parsed = LaneMatrixContract.parse(matrix);
        require(parsed.caseCount() == parsed.seedCount() * parsed.chunkCount()
                        && parsed.contract().matches("[0-9a-f]{64}"),
                "lane matrix contract drifted");
        System.out.println("  lane differential self-test: passed");
    }

    private static List<String> semanticRows(List<String> lines, String signal) {
        return lines.stream().map(String::trim)
                .map(row -> row.equals(signal) || row.endsWith("=" + signal) ? "signal: " + signal : row)
                .filter(row -> row.equals("FROZEN") || row.startsWith("trace: ")
                        || row.startsWith("signature: ") || row.startsWith("signal: ")
                        || row.startsWith("WORLDLINE_")
                        && !row.startsWith("WORLDLINE_AWAIT_TELEMETRY=")
                        && !row.startsWith("WORLDLINE_FLAKE_"))
                .sorted().toList();
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

    static String platform() {
        return System.getProperty("os.name", "").toLowerCase().contains("win") ? "windows" : "linux";
    }

    private static boolean equal(Properties left, Properties right, String key) {
        return required(left, key).equals(required(right, key));
    }

    private static boolean equal(Properties values, String left, String right) {
        return required(values, left).equals(required(values, right));
    }

    private static Properties load(Path path) throws Exception {
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return values;
    }

    private static String required(Properties values, String key) {
        String value = values.getProperty(key, "").trim();
        require(!value.isEmpty(), "missing lane differential field: " + key);
        return value;
    }

    private static String digest(String value) throws Exception {
        return digest(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String digest(byte[] value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value));
    }

    private static void store(Path path, Properties values) throws Exception {
        TreeMap<String, String> sorted = new TreeMap<>();
        for (String key : values.stringPropertyNames()) sorted.put(key, values.getProperty(key));
        StringBuilder output = new StringBuilder("# Worldline Windows/Linux headless differential v")
                .append(required(values, "schema")).append('\n');
        for (var row : sorted.entrySet())
            output.append(row.getKey()).append('=').append(row.getValue()).append('\n');
        Files.writeString(path, output.toString(), StandardCharsets.UTF_8);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

}
