import java.util.Arrays;
import java.util.List;

/** Closed argument grammar and direct-tool routing for the canonical Gate. */
final class GateArguments {
    private static final String USAGE = "usage: java tools/harness/Gate.java "
            + "[--runtime|--smoke|--pin-smokes|--accept-legacy-smoke-baseline|--orchestrator|"
            + "--smoke-plan|--pinned-smoke|--train-readiness|--migrate-data-cycles|--refresh-data-cycle-pins|"
            + "--migrate-composite-cycles|--refresh-composite-cycle-pins|--migrate-telemetry-pins|"
            + "--migrate-repository-schemas|--migrate-formatting-pins|--migrate-unicode-pins|"
            + "--migrate-shared-helper-pins|--migrate-adapter-split-pins|--migrate-provider-discovery-pins|"
            + "--migrate-gui-workbench-pins|--rebalance-behavior-families|--migrate-behavior-family-pins|"
            + "--migrate-train-pins|--refresh-readme-status|--refresh-testkit-artifact-pins|"
            + "--refresh-documentation|--seal-lane-portability|--module-cache-doctor|--module-cache-gc|"
            + "--cache-doctor|--cache-gc|--cache-rebuild-drill|--executor-tooling|--executor-environment|"
            + "--executor-filesystem|--executor-server|--executor-candidate|--executor-gui|--executor-aero|"
            + "--cas-permits|--cas-shadow|--new-milestone ID|--input-graph ID|--module-input-graph MODULE|"
            + "--cache-explain ID|--input-dependents KIND:KEY|--input-diff ID@REF|--milestone ID|"
            + "--lane-differential ID|--candidate-ready ID|--candidate ID|--self-test]";

    private GateArguments() { }

    static void validate(String[] arguments) {
        boolean profile = arguments.length == 0
                || one(arguments, "--runtime", "--smoke", "--orchestrator", "--pin-smokes", "--smoke-plan",
                        "--pinned-smoke", "--train-readiness", "--executor-tooling", "--executor-environment",
                        "--executor-filesystem", "--executor-candidate", "--accept-legacy-smoke-baseline",
                        "--cas-permits", "--cas-shadow")
                || arguments.length == 1 && arguments[0].matches("--executor-(server|gui|aero)")
                || arguments.length == 1 && maintenance(arguments[0])
                || direct(arguments) != null
                || pair(arguments, "--new-milestone", "--candidate", "--candidate-ready",
                        "--lane-differential", "--milestone", "--smoke-id");
        if (!profile) throw new IllegalArgumentException(USAGE);
        if (arguments.length == 2 && query(arguments[0]) == null
                && !arguments[1].matches("[a-z0-9]+(?:-[a-z0-9]+)*"))
            throw new IllegalArgumentException("invalid milestone id: " + arguments[1]);
    }

    static DirectTool direct(String[] arguments) {
        if (Arrays.equals(arguments, new String[] {"--cas-permits"}))
            return new DirectTool("ArtifactTransportPermitQuery", null);
        if (Arrays.equals(arguments, new String[] {"--cas-shadow"}))
            return new DirectTool("CasTransportShadow", null);
        String main = arguments.length == 2 ? query(arguments[0]) : null;
        return main == null ? null : new DirectTool(main, arguments[1]);
    }

    static boolean maintenance(String value) {
        return value.startsWith("--migrate-") || value.startsWith("--refresh-")
                || value.startsWith("--rebalance-") || value.startsWith("--seal-")
                || value.startsWith("--module-cache-") || value.startsWith("--cache-");
    }

    static void selfTest() {
        validate(new String[] {"--executor-aero"}); validate(new String[] {"--cas-shadow"});
        validate(new String[] {"--input-diff", "m1@HEAD"});
        require("SmokeInputGraphQuery".equals(direct(new String[] {"--input-graph", "m1"}).main()),
                "input graph routing drift");
        expectFailure(() -> validate(new String[] {"--unknown"}));
        expectFailure(() -> validate(new String[] {"--milestone", "INVALID"}));
    }

    private static String query(String value) { return switch (value) {
        case "--input-graph" -> "SmokeInputGraphQuery"; case "--module-input-graph" -> "ModuleInputGraphQuery";
        case "--cache-explain" -> "SmokeCacheQuery"; case "--input-dependents" -> "SmokeInputDependentsQuery";
        case "--input-diff" -> "SmokeInputDiffQuery"; default -> null; }; }
    private static boolean one(String[] arguments, String... options) {
        return arguments.length == 1 && List.of(options).contains(arguments[0]);
    }
    private static boolean pair(String[] arguments, String... options) {
        return arguments.length == 2 && List.of(options).contains(arguments[0]);
    }
    private static void expectFailure(Runnable action) { try { action.run();
        throw new IllegalStateException("invalid Gate arguments were accepted");
    } catch (IllegalArgumentException expected) { } }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
    record DirectTool(String main, String operand) { }
}
