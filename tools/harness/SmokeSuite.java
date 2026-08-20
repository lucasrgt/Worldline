import java.util.Arrays;
import java.util.List;

/** Canonical --smoke suite extracted so Verify can grow milestones without a packed file. */
final class SmokeSuite {
    public static void main(String[] arguments) {
        if (arguments.length != 0) {
            System.err.println("usage: java tools/harness/SmokeSuite.java");
            System.exit(2);
        }
        try { run(SmokeSuite::execute); }
        catch (Exception error) {
            System.err.println("smoke suite failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private static void execute(List<String> command) throws Exception {
        Process process = new ProcessBuilder(command).inheritIO().start();
        if (process.waitFor() != 0) throw new IllegalStateException(command.get(1) + " failed");
    }

    interface Run { void run(List<String> command) throws Exception; }

    // SMOKES live in SmokeCatalog so this file stays under the harness cap.

    private SmokeSuite() {}

    static void run(Run run) throws Exception {
        for (String[] smoke : SmokeCatalog.SMOKES) run.run(Arrays.asList("java", smoke[0], smoke[1]));
    }
}
