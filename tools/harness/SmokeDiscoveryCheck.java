import java.nio.file.Path;
import java.util.List;

/** Executable invariant check for distributed smoke registration. */
public final class SmokeDiscoveryCheck {
    private SmokeDiscoveryCheck() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 0) throw new IllegalArgumentException("usage: SmokeDiscoveryCheck");
        Path root = Path.of("").toAbsolutePath().normalize();
        List<SmokeDiscovery.Entry> entries = SmokeDiscovery.discover(root);
        if (entries.isEmpty()) throw new IllegalStateException("no smoke milestones discovered");
        if (java.nio.file.Files.exists(root.resolve("smokes/m10-probe2"))
                || !java.nio.file.Files.isRegularFile(
                        root.resolve("docs/decisions/M10_PROBE2_NON_MILESTONE.md")))
            throw new IllegalStateException("m10-probe2 non-milestone decision drifted");
        new SmokeScheduleHistory(root).validateCatalog(entries);
        System.out.println("  smoke catalog: " + entries.size() + " distributed milestones verified");
    }
}
