import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Ensures every discovered smoke has a computable, milestone-specific input identity. */
public final class SmokeFingerprintCheck {
    private SmokeFingerprintCheck() {}

    public static void main(String[] arguments) {
        try { execute(Path.of("").toAbsolutePath().normalize()); }
        catch (Exception error) {
            System.err.println("smoke fingerprint check failed: " + error.getMessage()); System.exit(1);
        }
    }

    static void execute(Path root) throws Exception {
        List<SmokeDiscovery.Entry> smokes = SmokeDiscovery.discover(root);
        SmokeInputFingerprint fingerprints = new SmokeInputFingerprint(root);
        Set<String> values = new HashSet<>();
        for (SmokeDiscovery.Entry smoke : smokes) {
            String value = fingerprints.compute(smoke);
            if (!value.matches("[0-9a-f]{64}"))
                throw new IllegalStateException("invalid smoke fingerprint: " + smoke.id);
            if (!values.add(value)) throw new IllegalStateException(
                    "smokes share an input fingerprint: " + smoke.id);
        }
        System.out.println("  smoke fingerprints: " + smokes.size() + " complete and distinct");
    }
}
