import java.nio.file.Path;

/** Builds a non-authoritative local overlay beside the canonical explorer. */
public final class EvidenceExplorerLive {
    private EvidenceExplorerLive() { }

    public static void main(String[] arguments) {
        try {
            require(arguments.length == 0, "usage: EvidenceExplorerLive");
            Path root = Path.of("").toAbsolutePath().normalize();
            Path control = control();
            EvidenceExplorer.build(root);
            EvidenceExplorerLiveSnapshot snapshot = EvidenceExplorerLiveSnapshot.build(
                    root, control, System.currentTimeMillis());
            Path output = EvidenceExplorer.output(root);
            EvidenceExplorer.write(output.resolve("live.json"),
                    EvidenceExplorerLiveFormat.json(snapshot));
            EvidenceExplorer.write(output.resolve("live.html"),
                    EvidenceExplorerLiveFormat.html(snapshot));
            System.out.println("live evidence overlay built: " + output + "; telemetry="
                    + snapshot.execution().sampleCount() + "; failures="
                    + snapshot.execution().failureCount() + "; cache=" + snapshot.cache().size());
        } catch (Exception error) {
            System.err.println("live evidence overlay failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private static Path control() {
        String value = System.getenv("WORLDLINE_GATE_CONTROL");
        require(value != null && !value.isBlank(), "missing Gate control directory");
        return Path.of(value).toAbsolutePath().normalize();
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
}
