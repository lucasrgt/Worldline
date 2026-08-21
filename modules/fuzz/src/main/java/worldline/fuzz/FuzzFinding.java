package worldline.fuzz;

import worldline.minimization.Scenario;

/** One discovered behavioral disagreement between two fuzzing executions. */
public final class FuzzFinding {
    /** Divergence compares distinct subjects; nondeterminism reruns one subject. */
    public enum Kind { DIVERGENCE, NONDETERMINISM }

    private final Kind kind;
    private final String left, right;
    private final Scenario original;
    private final Scenario minimized;

    FuzzFinding(Kind kind, String left, String right, Scenario original,
            Scenario minimized) {
        this.kind = kind; this.left = left; this.right = right;
        this.original = original; this.minimized = minimized;
    }

    public Kind kind() { return kind; }
    public String leftSubject() { return left; }
    public String rightSubject() { return right; }

    /** The full plan scenario that first exposed the disagreement. */
    public Scenario original() { return original; }

    /** Shrunk reproducer when the predicate was deterministic; null otherwise. */
    public Scenario minimized() { return minimized; }
}
