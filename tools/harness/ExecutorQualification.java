import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/** Gate-owned dispatch for Runtime Fabric qualification profiles. */
final class ExecutorQualification {
    private static final Set<String> FLAGS = Set.of(
            "--executor-server", "--executor-candidate", "--executor-gui", "--executor-aero");

    private ExecutorQualification() { }

    static boolean matches(String[] arguments) {
        return arguments.length == 1 && FLAGS.contains(arguments[0]);
    }

    static void execute(String flag) throws Exception {
        GateWorkMetrics.qualificationValidated();
        String main = switch (flag) {
            case "--executor-server" -> "ExecutorServerCycle";
            case "--executor-gui" -> "ExecutorGuiCycle";
            case "--executor-aero" -> "ExecutorAeroCycle";
            default -> "ExecutorCandidateCycle";
        };
        String output = ProcessCapture.require(Path.of("").toAbsolutePath().normalize(), List.of(
                "java", "tools/containers/RuntimeFabricLauncher.java", main),
                main.contains("Aero") ? 14_400 : 3_000);
        System.out.print(output);
    }
}
