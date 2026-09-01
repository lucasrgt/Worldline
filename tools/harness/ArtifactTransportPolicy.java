import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/** Fail-closed policy boundary for artifacts proposed to a future remote CAS. */
final class ArtifactTransportPolicy {
    private static final String SCHEMA = "worldline.artifact-transport.v1";
    private static final String PROOF_VALIDATOR = "smoke-pin-envelope-v1";
    private static final Set<String> REQUIRED = Set.of(
            "tracked-smoke-pin-envelope", "shared-module-cache", "shared-test-cache",
            "verification-stage-cache", "smoke-proof-cache", "runtime-observation-cache",
            "diagnostic-bundle", "official-runtime", "mapped-runtime");
    private static final Set<String> PROOF_KEYS = Set.of(
            "schema", "id", "fingerprint", "observation.sha256", "source");
    private final Path repository;
    private final Map<String, ArtifactTransportFamily> families;

    private ArtifactTransportPolicy(Path repository,
            Map<String, ArtifactTransportFamily> families) {
        this.repository = repository; this.families = Map.copyOf(families);
    }

    static ArtifactTransportPolicy load(Path repository) throws Exception {
        Path root = repository.toAbsolutePath().normalize();
        Properties values = StrictProperties.load(root.resolve("quality/artifact-transport.properties"));
        require(SCHEMA.equals(values.getProperty("schema")), "invalid artifact transport schema");
        int count = integer(values, "family.count");
        require(count > 0 && count <= 64, "invalid artifact transport family count");
        Set<String> expected = new HashSet<>(Set.of("schema", "family.count"));
        Map<String, ArtifactTransportFamily> families = new HashMap<>();
        for (int index = 0; index < count; index++) {
            String prefix = "family." + index + ".";
            for (String field : Set.of("id", "class", "root", "suffix", "max.bytes", "validator"))
                expected.add(prefix + field);
            ArtifactTransportFamily family = family(values, prefix);
            require(families.putIfAbsent(family.id(), family) == null,
                    "duplicate artifact transport family: " + family.id());
        }
        require(values.stringPropertyNames().equals(expected), "unknown artifact transport policy key");
        require(families.keySet().containsAll(REQUIRED), "required artifact transport family absent");
        return new ArtifactTransportPolicy(root, families);
    }

    static void check(Path repository) throws Exception {
        ArtifactTransportPolicy policy = load(repository);
        ArtifactTransportFamily proof = policy.required("tracked-smoke-pin-envelope");
        Path directory = policy.repository.resolve(proof.root());
        require(Files.isDirectory(directory), "tracked smoke evidence directory absent");
        try (var paths = Files.list(directory)) {
            for (Path path : paths.filter(item -> item.toString().endsWith(proof.suffix())).toList())
                policy.authorize(proof.id(), path);
        }
    }

    Candidate authorize(String familyId, Path path) throws Exception {
        ArtifactTransportFamily family = required(familyId);
        require(family.classification().remotelyEligible(),
                "artifact family is not remotely eligible: " + familyId);
        Path declaredRoot = repository.resolve(family.root()).normalize();
        Path candidate = path.toAbsolutePath().normalize();
        require(candidate.startsWith(declaredRoot), "artifact escaped its declared family root");
        require(Files.isRegularFile(candidate, LinkOption.NOFOLLOW_LINKS)
                && !Files.isSymbolicLink(candidate), "artifact candidate is not a regular file");
        Path repositoryRoot = repository.toRealPath();
        Path realRoot = declaredRoot.toRealPath(), real = candidate.toRealPath();
        require(realRoot.startsWith(repositoryRoot), "artifact family root resolved outside repository");
        require(real.startsWith(realRoot), "artifact resolved outside its declared family root");
        require(real.getFileName().toString().endsWith(family.suffix()),
                "artifact suffix is not allowed for family: " + familyId);
        long size = Files.size(real);
        require(size > 0 && size <= family.maxBytes(), "artifact candidate size is not allowed");
        validate(family.validator(), real);
        return new Candidate(family.id(), family.classification(), digest(real), size, candidate);
    }

    List<Candidate> eligibleCandidates(Set<Path> tracked) throws Exception {
        List<ArtifactTransportFamily> eligible = families.values().stream()
                .filter(family -> family.classification().remotelyEligible())
                .sorted(java.util.Comparator.comparing(ArtifactTransportFamily::id)).toList();
        java.util.ArrayList<Candidate> candidates = new java.util.ArrayList<>();
        for (ArtifactTransportFamily family : eligible) {
            Path directory = repository.resolve(family.root()).normalize();
            try (var paths = Files.list(directory)) {
                for (Path path : paths.filter(item -> item.toString().endsWith(family.suffix()))
                        .sorted().toList()) {
                    Path normalized = path.toAbsolutePath().normalize();
                    require(tracked.contains(normalized), "transport candidate is not tracked: "
                            + repository.relativize(normalized));
                    candidates.add(authorize(family.id(), normalized));
                }
            }
        }
        return List.copyOf(candidates);
    }

    private ArtifactTransportFamily required(String id) {
        ArtifactTransportFamily family = families.get(id);
        require(family != null, "undeclared artifact transport family: " + id);
        return family;
    }

    private static ArtifactTransportFamily family(Properties values, String prefix) {
        String id = required(values, prefix + "id");
        require(id.matches("[a-z0-9]+(?:-[a-z0-9]+)*"), "invalid artifact family id: " + id);
        ArtifactTransportClass classification = ArtifactTransportClass.parse(
                required(values, prefix + "class"));
        String root = required(values, prefix + "root");
        String suffix = required(values, prefix + "suffix");
        long maxBytes = number(values, prefix + "max.bytes");
        String validator = required(values, prefix + "validator");
        if (classification.remotelyEligible()) {
            Path relative = Path.of(root).normalize();
            require(!relative.isAbsolute() && !relative.startsWith("..") && !root.equals("-"),
                    "remotely eligible family has invalid root: " + id);
            require(suffix.startsWith(".") && suffix.length() > 1,
                    "remotely eligible family has invalid suffix: " + id);
            require(maxBytes > 0 && maxBytes <= 1_048_576, "invalid artifact size limit: " + id);
            require(PROOF_VALIDATOR.equals(validator), "unknown artifact validator: " + validator);
        } else require(root.equals("-") && suffix.equals("-") && maxBytes == 0
                && validator.equals("none"), "restricted artifact family exposes transport fields: " + id);
        return new ArtifactTransportFamily(id, classification, root, suffix, maxBytes, validator);
    }

    private static void validate(String validator, Path path) throws Exception {
        require(PROOF_VALIDATOR.equals(validator), "artifact validator is not implemented");
        String text = Files.readString(path, StandardCharsets.UTF_8);
        Properties values = StrictProperties.load(path);
        require(values.stringPropertyNames().equals(PROOF_KEYS), "invalid smoke pin envelope keys");
        String id = required(values, "id"), fingerprint = required(values, "fingerprint");
        String observation = required(values, "observation.sha256"), source = required(values, "source");
        require("1".equals(values.getProperty("schema"))
                && id.matches("[a-z0-9]+(?:-[a-z0-9]+)*")
                && fingerprint.matches("[0-9a-f]{64}") && observation.matches("[0-9a-f]{64}")
                && Set.of("executed", "legacy-frozen", "refactor-equivalent").contains(source),
                "invalid smoke pin envelope values");
        String canonical = "schema=1\nid=" + id + "\nfingerprint=" + fingerprint
                + "\nobservation.sha256=" + observation + "\nsource=" + source + "\n";
        require(text.equals(canonical), "noncanonical smoke pin envelope");
    }

    private static String digest(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[16 * 1024]; int count;
            while ((count = input.read(buffer)) >= 0) if (count > 0) digest.update(buffer, 0, count);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static int integer(Properties values, String key) {
        long value = number(values, key);
        require(value <= Integer.MAX_VALUE, "integer artifact policy value is too large: " + key);
        return (int) value;
    }
    private static long number(Properties values, String key) {
        try { return Long.parseLong(required(values, key)); }
        catch (NumberFormatException error) { throw new IllegalStateException(
                "invalid numeric artifact policy value: " + key, error); }
    }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key);
        require(value != null && !value.isBlank(), "missing artifact policy value: " + key);
        return value;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    record Candidate(String family, ArtifactTransportClass classification,
            String sha256, long size, Path path) { }
}
