import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Emits Rust CAS permits from already authorized and tracked artifacts. */
public final class ArtifactTransportPermitQuery {
    private ArtifactTransportPermitQuery() { }

    public static void main(String[] arguments) {
        try {
            require(arguments.length == 0, "usage: Gate.java --cas-permits");
            Path root = Path.of("").toAbsolutePath().normalize();
            ArtifactTransportPolicy policy = ArtifactTransportPolicy.load(root);
            System.out.print(manifest(policy.eligibleCandidates(SmokeTrackedFiles.read(root))));
        } catch (Exception error) {
            System.err.println("CAS permit query failed: " + error.getMessage());
            System.exit(1);
        }
    }

    static void selfTest() {
        Path root = Path.of("").toAbsolutePath().normalize();
        ArtifactTransportPolicy.Candidate second = candidate("2", 20, root.resolve("two"));
        ArtifactTransportPolicy.Candidate first = candidate("1", 10, root.resolve("one"));
        String value = manifest(List.of(second, first));
        require(value.equals("{\n  \"schema\":\"worldline.cas-transport-permits.v1\",\n"
                + "  \"objects\":[\n    {\"sha256\":\"" + "1".repeat(64)
                + "\",\"size\":10},\n    {\"sha256\":\"" + "2".repeat(64)
                + "\",\"size\":20}\n  ]\n}\n"), "CAS permit manifest drifted");
        rejected(() -> manifest(List.of(first, first)), "duplicate CAS digest was emitted");
    }

    static String manifest(List<ArtifactTransportPolicy.Candidate> input) {
        List<ArtifactTransportPolicy.Candidate> candidates = new ArrayList<>(input);
        candidates.sort(Comparator.comparing(ArtifactTransportPolicy.Candidate::sha256));
        Set<String> digests = new HashSet<>();
        StringBuilder value = new StringBuilder(
                "{\n  \"schema\":\"worldline.cas-transport-permits.v1\",\n  \"objects\":[\n");
        for (int index = 0; index < candidates.size(); index++) {
            ArtifactTransportPolicy.Candidate candidate = candidates.get(index);
            require(digests.add(candidate.sha256()), "duplicate CAS permit digest");
            value.append("    {\"sha256\":\"").append(candidate.sha256())
                    .append("\",\"size\":").append(candidate.size()).append('}');
            if (index + 1 < candidates.size()) value.append(',');
            value.append('\n');
        }
        return value.append("  ]\n}\n").toString();
    }

    private static ArtifactTransportPolicy.Candidate candidate(String digit, long size, Path path) {
        return new ArtifactTransportPolicy.Candidate("fixture",
                ArtifactTransportClass.PORTABLE_BEHAVIOR_PROOF, digit.repeat(64), size, path);
    }
    private static void rejected(Runnable action, String message) {
        boolean rejected = false;
        try { action.run(); } catch (IllegalStateException expected) { rejected = true; }
        require(rejected, message);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
