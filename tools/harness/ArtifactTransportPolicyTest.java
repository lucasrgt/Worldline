import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Positive and hostile fixtures for fail-closed artifact classification. */
final class ArtifactTransportPolicyTest {
    private ArtifactTransportPolicyTest() { }

    static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-artifact-policy-");
        try {
            Files.createDirectories(root.resolve("quality"));
            Files.writeString(root.resolve("quality/artifact-transport.properties"), policy(),
                    StandardCharsets.UTF_8);
            Path directory = root.resolve("smokes/qualification-evidence");
            Files.createDirectories(directory);
            Path proof = directory.resolve("fixture.proof");
            Files.writeString(proof, proof("fixture"), StandardCharsets.UTF_8);
            ArtifactTransportPolicy policy = ArtifactTransportPolicy.load(root);
            ArtifactTransportPolicy.Candidate candidate = policy.authorize(
                    "tracked-smoke-pin-envelope", proof);
            require(candidate.classification() == ArtifactTransportClass.PORTABLE_BEHAVIOR_PROOF
                    && candidate.sha256().matches("[0-9a-f]{64}") && candidate.size() > 0,
                    "valid portable proof was not classified");
            rejected(() -> policy.eligibleCandidates(java.util.Set.of()),
                    "untracked portable proof received a transport permit");
            rejected(() -> policy.authorize("official-runtime", proof),
                    "official runtime family was transportable");
            rejected(() -> policy.authorize("unknown-family", proof),
                    "unknown family was transportable");
            Path escaped = root.resolve("escaped.proof");
            Files.writeString(escaped, proof("escaped"), StandardCharsets.UTF_8);
            rejected(() -> policy.authorize("tracked-smoke-pin-envelope", escaped),
                    "path escape was transportable");
            Files.write(proof, new byte[] {'P', 'K', 3, 4, 0, 1, 2, 3});
            rejected(() -> policy.authorize("tracked-smoke-pin-envelope", proof),
                    "renamed binary payload was transportable");
            Files.writeString(root.resolve("quality/artifact-transport.properties"),
                    policy() + "unexpected=true\n", StandardCharsets.UTF_8);
            rejected(() -> ArtifactTransportPolicy.load(root),
                    "unknown policy field was accepted");
            ArtifactTransportPermitQuery.selfTest();
            CasTransportShadow.selfTest();
        } finally { SafeTreeDelete.delete(root); }
        System.out.println("  artifact transport policy self-test: passed");
    }

    private static String policy() {
        String[] ids = {"tracked-smoke-pin-envelope", "shared-module-cache", "shared-test-cache",
                "verification-stage-cache", "smoke-proof-cache", "runtime-observation-cache",
                "diagnostic-bundle", "official-runtime", "mapped-runtime"};
        StringBuilder value = new StringBuilder("schema=worldline.artifact-transport.v1\nfamily.count=9\n");
        for (int index = 0; index < ids.length; index++) {
            String prefix = "family." + index + ".";
            value.append(prefix).append("id=").append(ids[index]).append('\n');
            if (index == 0) value.append(prefix).append("class=portable-behavior-proof\n")
                    .append(prefix).append("root=smokes/qualification-evidence\n")
                    .append(prefix).append("suffix=.proof\n")
                    .append(prefix).append("max.bytes=4096\n")
                    .append(prefix).append("validator=smoke-pin-envelope-v1\n");
            else value.append(prefix).append("class=")
                    .append(index >= 7 ? "official-or-derived-forbidden" : "local-private")
                    .append('\n').append(prefix).append("root=-\n")
                    .append(prefix).append("suffix=-\n").append(prefix).append("max.bytes=0\n")
                    .append(prefix).append("validator=none\n");
        }
        return value.toString();
    }

    private static String proof(String id) {
        return "schema=1\nid=" + id + "\nfingerprint=" + "1".repeat(64)
                + "\nobservation.sha256=" + "2".repeat(64) + "\nsource=executed\n";
    }

    private static void rejected(Action action, String message) throws Exception {
        boolean rejected = false;
        try { action.run(); } catch (IllegalStateException expected) { rejected = true; }
        require(rejected, message);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    @FunctionalInterface private interface Action { void run() throws Exception; }
}
