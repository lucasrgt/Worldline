import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/** Reads and writes the reviewable, repository-tracked smoke qualification lock. */
final class SmokePins {
    private static final String ALGORITHM = "worldline-smoke-input-v6-tokens";
    private static final java.util.Set<String> LEGACY_ALGORITHMS = java.util.Set.of(
            "worldline-smoke-input-v1", "worldline-smoke-input-v2", "worldline-smoke-input-v3",
            "worldline-smoke-input-v4", "worldline-smoke-input-v5-portable");
    private static final String HEADER = "# Worldline smoke qualification lock v5\n"
            + "schema=5\nalgorithm=" + ALGORITHM + "\n";
    private final Path path;
    private final Map<String, Entry> entries;
    private final String algorithm;
    private final SmokePinEvidence evidence;
    private final Map<String, List<String>> comments;

    SmokePins(Path root) throws Exception {
        this.path = root.resolve("smokes/qualification.lock");
        this.evidence = new SmokePinEvidence(root);
        List<String> lines = Files.isRegularFile(path)
                ? Files.readAllLines(path, StandardCharsets.UTF_8) : List.of();
        this.algorithm = lines.stream().filter(line -> line.startsWith("algorithm="))
                .map(line -> line.substring(10)).findFirst().orElse(ALGORITHM);
        require(algorithm.equals(ALGORITHM) || LEGACY_ALGORITHMS.contains(algorithm),
                "invalid smoke qualification lock algorithm");
        this.entries = read(lines);
        this.comments = comments(lines);
    }

    Entry match(String id, String fingerprint) {
        Entry entry = entries.get(id);
        return entry != null && entry.fingerprint.equals(fingerprint) ? entry : null;
    }
    Entry migrationMatch(SmokeDiscovery.Entry smoke, String fingerprint) throws Exception {
        return LaneDifferential.portableQualification(path.getParent().getParent(), smoke)
                ? entry(smoke.id) : match(smoke.id, fingerprint);
    }
    Migration migrate(SmokeDiscovery.Entry smoke, String fingerprint, Entry baseline,
            Properties predecessor, TrainPinHistory history, String stem) {
        Entry current = match(smoke.id, fingerprint), carried = entry(smoke.id);
        String evidence = current != null ? current.evidence
                : carried == null ? baseline.evidence : carried.evidence;
        String prior = evidence.equals(predecessor.getProperty(stem + "evidence_sha256"))
                ? predecessor.getProperty(stem + "current_fingerprint") : baseline.fingerprint;
        if (evidence.equals(predecessor.getProperty(stem + "evidence_sha256")))
            prior = history.inherited(stem, evidence,
                    predecessor.getProperty(stem + "prior_fingerprint", prior));
        Entry updated = current != null ? current : new Entry(smoke.id, fingerprint, evidence,
                fingerprint.equals(baseline.fingerprint) ? baseline.source : "refactor-equivalent");
        return new Migration(updated, prior);
    }
    Entry verifiedMatch(String id, String fingerprint) throws Exception {
        Entry entry = match(id, fingerprint);
        return entry != null && evidence.verify(entry) ? entry : null;
    }

    Entry entry(String id) { return entries.get(id); }
    List<Entry> entries() { return List.copyOf(entries.values()); }
    boolean legacyAlgorithm() { return !algorithm.equals(ALGORITHM); }

    void validateCatalog(List<SmokeDiscovery.Entry> smokes) {
        java.util.Set<String> ids = smokes.stream().map(entry -> entry.id)
                .collect(java.util.stream.Collectors.toSet());
        for (String id : entries.keySet()) require(ids.contains(id), "stale smoke pin: " + id);
    }
    void validateEvidence() throws Exception { evidence.validate(entries()); }

    void write(List<Entry> values) throws Exception {
        List<Entry> sorted = values.stream().sorted(java.util.Comparator.comparing(Entry::id)).toList();
        StringBuilder output = new StringBuilder(HEADER);
        for (Entry entry : sorted) {
            String attestation = evidence.write(entry);
            entry = new Entry(entry.id, entry.fingerprint, entry.evidence, attestation, entry.source);
            validate(entry);
            for (String comment : comments.getOrDefault(entry.id, List.of()))
                output.append(comment).append('\n');
            output.append("smoke.").append(entry.id).append(".fingerprint=")
                    .append(entry.fingerprint).append('\n');
            output.append("smoke.").append(entry.id).append(".observation_sha256=")
                    .append(entry.evidence).append('\n');
            output.append("smoke.").append(entry.id).append(".evidence_sha256=")
                    .append(entry.attestation).append('\n');
            output.append("smoke.").append(entry.id).append(".source=")
                    .append(entry.source).append('\n');
            output.append("smoke.").append(entry.id).append(".status=passed\n");
        }
        Files.writeString(path, output.toString(), StandardCharsets.UTF_8);
    }

    private static Map<String, Entry> read(List<String> lines) throws Exception {
        Map<String, String> fingerprints = new HashMap<>(), observations = new HashMap<>();
        Map<String, String> evidence = new HashMap<>();
        Map<String, String> sources = new HashMap<>(), status = new HashMap<>();
        if (lines.isEmpty()) return Map.of();
        boolean legacy = lines.stream().anyMatch("schema=1"::equals);
        boolean sealed = lines.stream().anyMatch("schema=5"::equals);
        require(legacy || lines.stream().anyMatch(line -> line.matches("schema=[2345]")),
                "invalid smoke qualification lock schema");
        for (String line : lines) {
            if (line.isBlank() || line.startsWith("#") || line.matches("schema=[12345]")
                    || line.equals("algorithm=" + ALGORITHM)
                    || LEGACY_ALGORITHMS.stream().anyMatch(
                            value -> line.equals("algorithm=" + value))) continue;
            int separator = line.indexOf('='); require(separator > 6, "invalid smoke pin row: " + line);
            String key = line.substring(0, separator), value = line.substring(separator + 1);
            require(key.startsWith("smoke."), "unknown smoke pin key: " + key);
            if (key.endsWith(".fingerprint")) put(fingerprints,
                    key.substring(6, key.length() - 12), value);
            else if (key.endsWith(".observation_sha256")) put(observations,
                    key.substring(6, key.length() - 19), value);
            else if (key.endsWith(".evidence_sha256")) put(evidence,
                    key.substring(6, key.length() - 16), value);
            else if (key.endsWith(".source")) put(sources,
                    key.substring(6, key.length() - 7), value);
            else if (key.endsWith(".status")) put(status,
                    key.substring(6, key.length() - 7), value);
            else throw new IllegalStateException("unknown smoke pin key: " + key);
        }
        if (!sealed) { observations.putAll(evidence); evidence.clear(); }
        require(fingerprints.keySet().equals(observations.keySet())
                && fingerprints.keySet().equals(status.keySet()), "incomplete smoke pin record");
        require(!sealed || fingerprints.keySet().equals(evidence.keySet()),
                "incomplete sealed smoke evidence record");
        require(legacy || fingerprints.keySet().equals(sources.keySet()),
                "incomplete smoke pin provenance");
        Map<String, Entry> result = new HashMap<>();
        for (String id : fingerprints.keySet()) {
            require("passed".equals(status.get(id)), "non-PASS smoke pin: " + id);
            Entry entry = new Entry(id, fingerprints.get(id), observations.get(id),
                    evidence.getOrDefault(id, ""),
                    legacy ? "executed" : sources.get(id)); validate(entry);
            result.put(id, entry);
        }
        return Map.copyOf(result);
    }

    private static void put(Map<String, String> target, String id, String value) {
        require(id.matches("[a-z0-9]+(?:-[a-z0-9]+)*") && target.putIfAbsent(id, value) == null,
                "duplicate or invalid smoke pin: " + id);
    }

    String proof(Entry entry) throws Exception {
        require(evidence.verify(entry), "unverified smoke pin evidence: " + entry.id);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        for (String value : List.of("worldline-smoke-pin-v3", entry.id,
                entry.fingerprint, entry.evidence, entry.attestation, entry.source)) {
            digest.update(value.getBytes(StandardCharsets.UTF_8)); digest.update((byte) 0);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static void validate(Entry entry) {
        require(entry.id.matches("[a-z0-9]+(?:-[a-z0-9]+)*")
                && entry.fingerprint.matches("[0-9a-f]{64}")
                && entry.evidence.matches("[0-9a-f]{64}")
                && (entry.attestation.isEmpty() || entry.attestation.matches("[0-9a-f]{64}"))
                && (entry.source.equals("executed") || entry.source.equals("legacy-frozen")
                        || entry.source.equals("refactor-equivalent")),
                "invalid smoke pin: " + entry.id);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static Map<String, List<String>> comments(List<String> lines) {
        Map<String, List<String>> result = new HashMap<>(); List<String> pending = new ArrayList<>();
        for (String line : lines) {
            if (line.startsWith("#") && !line.startsWith("# Worldline")) { pending.add(line); continue; }
            if (line.matches("smoke[.][a-z0-9-]+[.]fingerprint=.*")) {
                String id = line.substring(6, line.indexOf(".fingerprint="));
                if (!pending.isEmpty()) result.put(id, List.copyOf(pending)); pending.clear();
            }
        }
        return Map.copyOf(result);
    }

    record Entry(String id, String fingerprint, String evidence, String attestation, String source) {
        Entry(String id, String fingerprint, String evidence, String source) {
            this(id, fingerprint, evidence, "", source);
        }
    }
    record Migration(Entry entry, String prior) { }
}
