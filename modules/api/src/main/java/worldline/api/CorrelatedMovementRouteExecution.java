package worldline.api;

import java.util.Objects;

/** Immutable correlated route execution and its exact terminal correlated event. */
public final class CorrelatedMovementRouteExecution {
    private final Object correlation;
    private final MovementRouteExecution execution;
    private final CorrelatedMovementRouteEvent terminalEvent;

    public CorrelatedMovementRouteExecution(Object correlation, MovementRouteExecution execution,
            CorrelatedMovementRouteEvent terminalEvent) {
        this.correlation = Objects.requireNonNull(correlation, "correlation");
        this.execution = Objects.requireNonNull(execution, "execution");
        this.terminalEvent = Objects.requireNonNull(terminalEvent, "terminalEvent");
        if (terminalEvent.correlation() != correlation
                || terminalEvent.event() != execution.terminalEvent())
            throw new IllegalArgumentException("correlated terminal event does not end execution");
    }

    public Object correlation() { return correlation; }
    public MovementRouteExecution execution() { return execution; }
    public CorrelatedMovementRouteEvent terminalEvent() { return terminalEvent; }
}
