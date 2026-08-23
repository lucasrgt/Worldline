import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Focused paired-frame budget self-test. */
final class AeroSceneBudgetTest {
    private AeroSceneBudgetTest() { }

    static void execute() throws Exception {
        Path root = Files.createTempDirectory("worldline-aero-budget-");
        try {
            Path quality = root.resolve("quality"); Files.createDirectories(quality);
            Files.writeString(quality.resolve("aero.properties"), "schema=1\nscene.test.pairs=2\n"
                    + "scene.test.median.ratio.max=2/1\nscene.test.median.slack.nanos=10\n"
                    + "scene.test.p95.ratio.max=2/1\nscene.test.p95.slack.nanos=20\n"
                    + "scene.test.p99.ratio.max=3/1\nscene.test.p99.slack.nanos=30\n"
                    + "scene.test.max.ratio.max=4/1\nscene.test.max.slack.nanos=40\n");
            Properties descriptor = new Properties(); descriptor.setProperty("performance.scene", "test");
            descriptor.setProperty("performance.baseline", "absent");
            descriptor.setProperty("performance.treatment", "present");
            descriptor.setProperty("performance.budget", "quality/aero.properties");
            AeroSceneBudget.validateDescriptor(root, descriptor);
            String pair = "  pair %d: absent:intervalNs=10/20/30/40,x | "
                    + "present:intervalNs=30/60/120/200,x\n";
            AeroSceneBudget.validateEvidence(root, descriptor, pair.formatted(1) + pair.formatted(2));
            rejects(() -> AeroSceneBudget.validateEvidence(root, descriptor,
                    pair.formatted(1) + pair.formatted(2).replace("intervalNs=30", "intervalNs=31")));
            System.out.println("  Aero scene budget self-test: passed");
        } finally { SafeTreeDelete.delete(root); }
    }

    private static void rejects(Checked action) throws Exception {
        try { action.run(); throw new IllegalStateException("expected Aero budget rejection"); }
        catch (IllegalStateException expected) {
            if (expected.getMessage().equals("expected Aero budget rejection")) throw expected;
        }
    }
    private interface Checked { void run() throws Exception; }
}
