package worldline.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Immutable ordered correlated executions and exact batch termination. */
public final class CorrelatedMovementRouteBatchResult {
    private final List<CorrelatedMovementRouteExecution> executions;
    private final MovementRouteBatchTermination termination;

    public CorrelatedMovementRouteBatchResult(List<CorrelatedMovementRouteExecution> executions,
            MovementRouteBatchTermination termination) {
        if (executions == null || executions.isEmpty() || executions.size() > 16)
            throw new IllegalArgumentException("invalid correlated route executions");
        ArrayList<CorrelatedMovementRouteExecution> copy = new ArrayList<>(executions.size());
        for (CorrelatedMovementRouteExecution execution : executions)
            copy.add(Objects.requireNonNull(execution, "execution"));
        this.executions = Collections.unmodifiableList(copy);
        this.termination = Objects.requireNonNull(termination, "termination");
    }

    public List<CorrelatedMovementRouteExecution> executions() { return executions; }
    public MovementRouteBatchTermination termination() { return termination; }
    public CorrelatedMovementRouteExecution finalExecution() {
        return executions.get(executions.size() - 1);
    }
}
