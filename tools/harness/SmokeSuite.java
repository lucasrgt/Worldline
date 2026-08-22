import java.nio.file.Path;

/** Canonical --smoke suite extracted so Verify can grow milestones without a packed file. */
final class SmokeSuite {
    public static void main(String[] arguments) {
        if (arguments.length != 0) {
            System.err.println("usage: java tools/harness/SmokeSuite.java");
            System.exit(2);
        }
        try { run(); }
        catch (Exception error) {
            System.err.println("smoke suite failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private SmokeSuite() {}

    static void run() throws Exception {
        Path root = Path.of("").toAbsolutePath().normalize();
        SmokeProcess process = new SmokeProcess(root);
        for (SmokeDiscovery.Entry smoke : SmokeDiscovery.discover(root)) process.run(smoke);
    }
}
