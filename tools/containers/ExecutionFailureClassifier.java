import java.util.Set;

/** Stable failure classes that keep executor, provider, and behavior domains distinct. */
final class ExecutionFailureClassifier {
    private static final Set<String> INFRASTRUCTURE = Set.of("oom", "memory-limit", "timeout",
            "escaped-descendant", "tool-absence", "spawn-failure", "wait-failure",
            "owner-exit", "provider-failure", "process-crash");
    private ExecutionFailureClassifier() { }

    static String classify(ExecutionTelemetry telemetry) {
        if (positive(telemetry.oomKillEvents())) return "oom";
        if (positive(telemetry.memoryLimitEvents()) && telemetry.termination().equals("crashed"))
            return "memory-limit";
        return switch (telemetry.termination()) {
            case "exited" -> "none";
            case "timed_out" -> "timeout";
            case "spawn_failed" -> "spawn-failure";
            case "wait_failed" -> "wait-failure";
            case "owner_exited" -> "owner-exit";
            case "crashed" -> "process-crash";
            default -> throw new IllegalArgumentException("unknown telemetry termination");
        };
    }

    static String explicit(Boundary boundary) {
        return switch (boundary) {
            case ESCAPED_DESCENDANT -> "escaped-descendant";
            case TOOL_ABSENCE -> "tool-absence";
            case PROVIDER_FAILURE -> "provider-failure";
            case ORACLE_MISMATCH -> "oracle-mismatch";
        };
    }

    static String domain(String failureClass) {
        if (failureClass.equals("oracle-mismatch")) return "behavioral";
        if (failureClass.equals("provider-failure")) return "provider-infrastructure";
        if (INFRASTRUCTURE.contains(failureClass)) return "executor-infrastructure";
        if (failureClass.equals("none")) return "success";
        throw new IllegalArgumentException("unknown failure class");
    }

    private static boolean positive(Long value) { return value != null && value > 0L; }
    enum Boundary { ESCAPED_DESCENDANT, TOOL_ABSENCE, PROVIDER_FAILURE, ORACLE_MISMATCH }
}
