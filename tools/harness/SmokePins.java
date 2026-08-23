import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

/** Reads and writes the reviewable, repository-tracked smoke qualification lock. */
final class SmokePins {
    private static final String ALGORITHM = "worldline-smoke-input-v2";
    private static final String LEGACY_ALGORITHM = "worldline-smoke-input-v1";
    private static final String HEADER = "# Worldline smoke qualification lock v2\n"
            + "schema=2\nalgorithm=" + ALGORITHM + "\n";
    private final Path path;
    private final Map<String, Entry> entries;
    private final String algorithm;

    SmokePins(Path root) throws Exception {
        this.path = root.resolve("smokes/qualification.lock");
        List<String> lines = Files.isRegularFile(path)
                ? Files.readAllLines(path, StandardCharsets.UTF_8) : List.of();
        this.algorithm = lines.stream().filter(line -> line.startsWith("algorithm="))
                .map(line -> line.substring(10)).findFirst().orElse(ALGORITHM);
        require(algorithm.equals(ALGORITHM) || algorithm.equals(LEGACY_ALGORITHM),
                "invalid smoke qualification lock algorithm");
        this.entries = read(lines);
    }

    Entry match(String id, String fingerprint) {
        Entry entry = entries.get(id);
        return entry != null && entry.fingerprint.equals(fingerprint) ? entry : null;
    }

    Entry entry(String id) { return entries.get(id); }
    boolean legacyAlgorithm() { return algorithm.equals(LEGACY_ALGORITHM); }

    void validateCatalog(List<SmokeDiscovery.Entry> smokes) {
        java.util.Set<String> ids = smokes.stream().map(entry -> entry.id)
                .collect(java.util.stream.Collectors.toSet());
        for (String id : entries.keySet()) require(ids.contains(id), "stale smoke pin: " + id);
    }

    void write(List<Entry> values) throws Exception {
        List<Entry> sorted = values.stream().sorted(java.util.Comparator.comparing(Entry::id)).toList();
        StringBuilder output = new StringBuilder(HEADER);
        for (Entry entry : sorted) {
            validate(entry);
            output.append("smoke.").append(entry.id).append(".fingerprint=")
                    .append(entry.fingerprint).append('\n');
            output.append("smoke.").append(entry.id).append(".evidence_sha256=")
                    .append(entry.evidence).append('\n');
            output.append("smoke.").append(entry.id).append(".source=")
                    .append(entry.source).append('\n');
            output.append("smoke.").append(entry.id).append(".status=passed\n");
        }
        Files.writeString(path, output.toString(), StandardCharsets.UTF_8);
    }

    private static Map<String, Entry> read(List<String> lines) throws Exception {
        Map<String, String> fingerprints = new HashMap<>(), evidence = new HashMap<>();
        Map<String, String> sources = new HashMap<>(), status = new HashMap<>();
        if (lines.isEmpty()) return Map.of();
        boolean legacy = lines.stream().anyMatch("schema=1"::equals);
        require(legacy || lines.stream().anyMatch("schema=2"::equals),
                "invalid smoke qualification lock schema");
        for (String line : lines) {
            if (line.isBlank() || line.startsWith("#") || line.matches("schema=[12]")
                    || line.equals("algorithm=" + ALGORITHM)
                    || line.equals("algorithm=" + LEGACY_ALGORITHM)) continue;
            int separator = line.indexOf('='); require(separator > 6, "invalid smoke pin row: " + line);
            String key = line.substring(0, separator), value = line.substring(separator + 1);
            require(key.startsWith("smoke."), "unknown smoke pin key: " + key);
            if (key.endsWith(".fingerprint")) put(fingerprints,
                    key.substring(6, key.length() - 12), value);
            else if (key.endsWith(".evidence_sha256")) put(evidence,
                    key.substring(6, key.length() - 16), value);
            else if (key.endsWith(".source")) put(sources,
                    key.substring(6, key.length() - 7), value);
            else if (key.endsWith(".status")) put(status,
                    key.substring(6, key.length() - 7), value);
            else throw new IllegalStateException("unknown smoke pin key: " + key);
        }
        require(fingerprints.keySet().equals(evidence.keySet())
                && fingerprints.keySet().equals(status.keySet()), "incomplete smoke pin record");
        require(legacy || fingerprints.keySet().equals(sources.keySet()),
                "incomplete smoke pin provenance");
        Map<String, Entry> result = new HashMap<>();
        for (String id : fingerprints.keySet()) {
            require("passed".equals(status.get(id)), "non-PASS smoke pin: " + id);
            Entry entry = new Entry(id, fingerprints.get(id), evidence.get(id),
                    legacy ? "executed" : sources.get(id)); validate(entry);
            result.put(id, entry);
        }
        return Map.copyOf(result);
    }

    private static void put(Map<String, String> target, String id, String value) {
        require(id.matches("[a-z0-9]+(?:-[a-z0-9]+)*") && target.putIfAbsent(id, value) == null,
                "duplicate or invalid smoke pin: " + id);
    }

    static String proof(Entry entry) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        for (String value : List.of("worldline-smoke-pin-v2", entry.id,
                entry.fingerprint, entry.evidence, entry.source)) {
            digest.update(value.getBytes(StandardCharsets.UTF_8)); digest.update((byte) 0);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static void validate(Entry entry) {
        require(entry.id.matches("[a-z0-9]+(?:-[a-z0-9]+)*")
                && entry.fingerprint.matches("[0-9a-f]{64}")
                && entry.evidence.matches("[0-9a-f]{64}")
                && (entry.source.equals("executed") || entry.source.equals("legacy-frozen")),
                "invalid smoke pin: " + entry.id);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    record Entry(String id, String fingerprint, String evidence, String source) {}
}
