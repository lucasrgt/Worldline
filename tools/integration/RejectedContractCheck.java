import java.nio.file.Path;
import java.util.List;

/** Blocks semantic relaunches of evidence-bound rejected contracts before worker startup. */
public final class RejectedContractCheck {
    private RejectedContractCheck() { }

    public static void main(String[] arguments) {
        try {
            if (List.of(arguments).equals(List.of("--self-test"))) {
                RejectionRegistry.selfTest();
                ActiveContractRegistry.selfTest();
                System.out.println("rejected contract check self-test passed");
                return;
            }
            require(arguments.length >= 4 && "--id".equals(arguments[0])
                    && "--goal".equals(arguments[2]), usage());
            Path evidence = arguments.length == 6 && "--evidence-root".equals(arguments[4])
                    ? Path.of(arguments[5]).toAbsolutePath().normalize() : null;
            require(arguments.length == 4 || evidence != null, usage());
            Path root = Path.of("").toAbsolutePath().normalize();
            List<RejectionRegistry.Entry> entries = RejectionRegistry.load(root, evidence);
            ScarControlRegistry.load(root, entries);
            ActiveContractRegistry.requireAllowed(ActiveContractRegistry.load(root),
                    arguments[1], arguments[3]);
            RejectionRegistry.requireAllowed(entries, arguments[1], arguments[3]);
            System.out.println("rejected contract check passed: " + arguments[1]);
        } catch (Exception error) {
            System.err.println("rejected contract check failed: " + error.getMessage());
            System.exit(1);
        }
    }

    static void requireAllowed(Path root, String id, String goal) throws Exception {
        List<RejectionRegistry.Entry> entries = RejectionRegistry.load(root, null);
        ScarControlRegistry.load(root, entries);
        ActiveContractRegistry.requireAllowed(ActiveContractRegistry.load(root), id, goal);
        RejectionRegistry.requireAllowed(entries, id, goal);
    }

    private static String usage() {
        return "usage: RejectedContractCheck.java --id ID --goal TEXT [--evidence-root DIR]"
                + " | --self-test";
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
}
