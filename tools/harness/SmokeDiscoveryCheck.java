import java.nio.file.Path;
import java.util.List;

/** Executable invariant check for distributed smoke registration. */
public final class SmokeDiscoveryCheck {
    private SmokeDiscoveryCheck() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 0) throw new IllegalArgumentException("usage: SmokeDiscoveryCheck");
        List<SmokeDiscovery.Entry> entries = SmokeDiscovery.discover(
                Path.of("").toAbsolutePath().normalize());
        if (entries.isEmpty()) throw new IllegalStateException("no smoke milestones discovered");
        new SmokeScheduleHistory(Path.of("").toAbsolutePath().normalize()).validateCatalog(entries);
        System.out.println("  smoke catalog: " + entries.size() + " distributed milestones verified");
    }
}
