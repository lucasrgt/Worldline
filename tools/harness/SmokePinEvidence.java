import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Properties;

/** Verifies repository-tracked, content-addressed evidence envelopes for portable pins. */
final class SmokePinEvidence {
    private static final String SCHEMA = "1";
    private final Path directory;

    SmokePinEvidence(Path root) { directory = root.resolve("smokes/qualification-evidence"); }

    String write(SmokePins.Entry entry) throws Exception {
        byte[] bytes = bytes(entry); Path path = path(entry.id());
        Files.createDirectories(path.getParent()); Files.write(path, bytes);
        return digest(bytes);
    }

    boolean verify(SmokePins.Entry entry) throws Exception {
        if (!entry.attestation().matches("[0-9a-f]{64}")) return false;
        Path path = path(entry.id()); if (!Files.isRegularFile(path)) return false;
        byte[] bytes = Files.readAllBytes(path);
        if (!digest(bytes).equals(entry.attestation())) return false;
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return values.stringPropertyNames().equals(java.util.Set.of(
                        "schema", "id", "fingerprint", "observation.sha256", "source"))
                && SCHEMA.equals(values.getProperty("schema"))
                && entry.id().equals(values.getProperty("id"))
                && entry.fingerprint().equals(values.getProperty("fingerprint"))
                && entry.evidence().equals(values.getProperty("observation.sha256"))
                && entry.source().equals(values.getProperty("source"));
    }

    void validate(java.util.List<SmokePins.Entry> entries) throws Exception {
        java.util.Set<String> expected = entries.stream().map(SmokePins.Entry::id)
                .collect(java.util.stream.Collectors.toSet());
        java.util.Set<String> actual = new java.util.HashSet<>();
        if (Files.isDirectory(directory)) try (var paths = Files.list(directory)) {
            for (Path path : paths.filter(item -> item.toString().endsWith(".proof")).toList())
                actual.add(path.getFileName().toString().replaceFirst("[.]proof$", ""));
        }
        require(actual.equals(expected), "smoke pin evidence catalog drift");
        for (SmokePins.Entry entry : entries)
            require(verify(entry), "smoke pin evidence drift: " + entry.id());
    }

    private Path path(String id) { return directory.resolve(id + ".proof"); }
    private static byte[] bytes(SmokePins.Entry entry) {
        return ("schema=" + SCHEMA + "\nid=" + entry.id() + "\nfingerprint="
                + entry.fingerprint() + "\nobservation.sha256=" + entry.evidence()
                + "\nsource=" + entry.source() + "\n").getBytes(StandardCharsets.UTF_8);
    }
    private static String digest(byte[] bytes) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(bytes)); }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
