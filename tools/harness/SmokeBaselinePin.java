import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** Explicitly migrates reviewed pre-cache frozen signatures into portable pins. */
final class SmokeBaselinePin {
    private SmokeBaselinePin() {}

    public static void main(String[] arguments) {
        try { execute(Path.of("").toAbsolutePath().normalize()); }
        catch (Exception error) {
            System.err.println("legacy smoke baseline failed: " + error.getMessage());
            System.exit(1);
        }
    }

    static void execute(Path root) throws Exception {
        SmokeGitState state = SmokeGitState.read(root);
        require(state.clean(), "legacy smoke baseline requires a clean committed worktree");
        Properties coverage = load(root.resolve("behavior/coverage.properties"));
        require("0".equals(coverage.getProperty("pending.expected")),
                "legacy smoke baseline requires pending.expected=0");
        SmokeReceiptCache cache = new SmokeReceiptCache(root);
        List<SmokePins.Entry> pins = new ArrayList<>();
        int executed = 0, legacy = 0;
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            SmokePins.Entry pin = cache.availablePin(smoke);
            if (pin != null) { pins.add(pin); executed++; continue; }
            Properties descriptor = load(root.resolve("smokes").resolve(smoke.id)
                    .resolve("smoke.properties"));
            String signature = descriptor.getProperty("expected.signature", "").trim();
            require(signature.matches("[0-9a-f]{64}"),
                    "missing frozen expected.signature: " + smoke.id);
            pins.add(new SmokePins.Entry(smoke.id, cache.fingerprint(smoke),
                    signature, "legacy-frozen"));
            legacy++;
        }
        new SmokePins(root).write(pins);
        System.out.println("  smoke legacy baseline accepted: " + pins.size()
                + " pins (executed=" + executed + ", legacy-frozen=" + legacy + ")");
    }

    private static Properties load(Path path) throws Exception {
        require(Files.isRegularFile(path), "missing baseline input: " + path);
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return values;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
