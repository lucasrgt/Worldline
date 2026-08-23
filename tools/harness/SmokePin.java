import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Promotes exact-current-tree smoke proofs into the tracked qualification lock. */
final class SmokePin {
    private SmokePin() {}

    public static void main(String[] arguments) {
        try { execute(Path.of("").toAbsolutePath().normalize()); }
        catch (Exception error) {
            System.err.println("smoke pinning failed: " + error.getMessage()); System.exit(1);
        }
    }

    static void execute(Path root) throws Exception {
        SmokeGitState state = SmokeGitState.read(root);
        if (!state.clean()) throw new IllegalStateException("pinning requires a clean committed worktree");
        SmokeReceiptCache cache = new SmokeReceiptCache(root);
        List<SmokePins.Entry> pins = new ArrayList<>();
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) {
            SmokePins.Entry pin = cache.availablePin(smoke); if (pin != null) pins.add(pin);
        }
        if (pins.isEmpty()) throw new IllegalStateException("no current-input PASS proofs are available to pin");
        new SmokePins(root).write(pins);
        System.out.println("  smoke qualification lock updated: " + pins.size() + " verified pins");
    }
}
