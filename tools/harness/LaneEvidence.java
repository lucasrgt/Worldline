import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

/** Records and seals exact Windows/Linux evidence for reviewed GUI/tooling lanes. */
final class LaneEvidence {
    private static final Set<String> TARGETS = Set.of("gui-tree", "gui-actions", "ui-export");
    private LaneEvidence() { }

    static void record(Path root, SmokeDiscovery.Entry smoke, String head, String tree,
            Path log) throws Exception {
        if (!TARGETS.contains(smoke.id)) return;
        Properties descriptor = StrictProperties.load(root.resolve("smokes").resolve(smoke.id)
                .resolve("smoke.properties"));
        String signature = required(descriptor, "expected.signature");
        String signal = required(descriptor, "expected.signal");
        List<String> semantic = Files.readAllLines(log, StandardCharsets.UTF_8).stream()
                .map(String::trim).filter(row -> row.equals("FROZEN")
                        || row.contains(signature) || row.contains(signal)).sorted().toList();
        require(semantic.stream().anyMatch(row -> row.contains(signature))
                        && semantic.stream().anyMatch(row -> row.contains(signal)),
                "lane evidence lacks frozen semantics: " + smoke.id);
        Properties record = new Properties();
        record.setProperty("schema", "1"); record.setProperty("id", smoke.id);
        record.setProperty("platform", LaneDifferential.platform());
        record.setProperty("head", head); record.setProperty("tree", tree);
        record.setProperty("lane", SmokeLane.classify(root, smoke));
        record.setProperty("qualification.fingerprint", new SmokeInputFingerprint(root).compute(smoke));
        record.setProperty("semantic.sha256", digest(String.join("\n", semantic) + "\n"));
        record.setProperty("evidence.sha256", digest(Files.readAllBytes(log)));
        Path path = root.resolve(".worldline/lane-evidence").resolve(smoke.id)
                .resolve(LaneDifferential.platform() + ".properties");
        store(path, "Worldline exact cross-lane milestone evidence", record);
        System.out.println("  lane evidence: " + root.relativize(path).toString().replace('\\', '/'));
    }

    public static void main(String[] arguments) {
        try {
            require(List.of(arguments).equals(List.of("--seal")), "usage: LaneEvidence --seal");
            seal(Path.of("").toAbsolutePath().normalize());
        } catch (Exception error) {
            System.err.println("GUI lane evidence seal failed: " + error.getMessage()); System.exit(1);
        }
    }

    static void seal(Path root) throws Exception {
        TreeMap<String, String> lock = new TreeMap<>();
        lock.put("schema", "1"); lock.put("algorithm", "worldline-cross-lane-evidence-v1");
        for (String id : TARGETS.stream().sorted().toList()) {
            Properties windows = load(root, id, "windows"), linux = load(root, id, "linux");
            for (String key : List.of("id", "head", "tree", "qualification.fingerprint",
                    "semantic.sha256")) require(required(windows, key).equals(required(linux, key)),
                    "Windows/Linux lane evidence diverged for " + id + ": " + key);
            lock.put("smoke." + id + ".status", "portable");
            for (String platform : List.of("windows", "linux")) {
                Properties record = platform.equals("windows") ? windows : linux;
                for (String key : List.of("head", "tree", "lane", "qualification.fingerprint",
                        "semantic.sha256", "evidence.sha256"))
                    lock.put("smoke." + id + "." + platform + "." + key, required(record, key));
            }
        }
        StringBuilder text = new StringBuilder("# Reviewed Windows/Linux GUI and tooling evidence\n");
        lock.forEach((key, value) -> text.append(key).append('=').append(value).append('\n'));
        Files.writeString(root.resolve("smokes/client-lane-portability.lock"), text,
                StandardCharsets.UTF_8);
        System.out.println("GUI/tooling portability sealed across exact Windows/Linux milestone gates");
    }

    static boolean portable(Path root, String id) {
        if (!TARGETS.contains(id)) return false;
        try {
            Properties lock = StrictProperties.load(root.resolve("smokes/client-lane-portability.lock"));
            String prefix = "smoke." + id + ".";
            if (!"1".equals(lock.getProperty("schema"))
                    || !"portable".equals(lock.getProperty(prefix + "status"))) return false;
            String head = required(lock, prefix + "windows.head");
            require(head.equals(required(lock, prefix + "linux.head"))
                            && required(lock, prefix + "windows.tree").equals(
                                    required(lock, prefix + "linux.tree"))
                            && required(lock, prefix + "windows.semantic.sha256").equals(
                                    required(lock, prefix + "linux.semantic.sha256")),
                    "invalid client lane portability lock");
            Process process = new ProcessBuilder("git", "merge-base", "--is-ancestor", head, "HEAD")
                    .directory(root.toFile()).redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectErrorStream(true).start();
            return process.waitFor() == 0;
        } catch (Exception error) { return false; }
    }

    private static Properties load(Path root, String id, String platform) throws Exception {
        Properties value = StrictProperties.load(root.resolve(".worldline/lane-evidence")
                .resolve(id).resolve(platform + ".properties"));
        require("1".equals(value.getProperty("schema")) && id.equals(value.getProperty("id"))
                        && platform.equals(value.getProperty("platform")),
                "invalid lane evidence record: " + id + "/" + platform);
        return value;
    }
    private static void store(Path path, String comment, Properties values) throws Exception {
        Files.createDirectories(path.getParent()); TreeMap<String, String> sorted = new TreeMap<>();
        values.stringPropertyNames().forEach(key -> sorted.put(key, values.getProperty(key)));
        StringBuilder text = new StringBuilder("# ").append(comment).append('\n');
        sorted.forEach((key, value) -> text.append(key).append('=').append(value).append('\n'));
        Files.writeString(path, text, StandardCharsets.UTF_8);
    }
    private static String digest(String value) throws Exception { return digest(
            value.getBytes(StandardCharsets.UTF_8)); }
    private static String digest(byte[] value) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(value)); }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key); require(value != null && !value.isBlank(),
                "missing lane evidence field " + key); return value.trim();
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
