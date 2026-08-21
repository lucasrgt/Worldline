package worldline.api;

import java.util.Objects;

/** Immutable route result paired with its exact terminal event and reason. */
public final class MovementRouteExecution {
    private final MovementRouteResult result;
    private final MovementRouteTermination termination;
    private final MovementRouteEvent terminalEvent;

    public MovementRouteExecution(MovementRouteResult result,
            MovementRouteTermination termination, MovementRouteEvent terminalEvent) {
        this.result = Objects.requireNonNull(result, "result");
        this.termination = Objects.requireNonNull(termination, "termination");
        this.terminalEvent = Objects.requireNonNull(terminalEvent, "terminalEvent");
        int last = result.outcomes().size() - 1;
        if (terminalEvent.outcomeIndex() != last
                || terminalEvent.outcome() != result.outcomes().get(last))
            throw new IllegalArgumentException("terminal event does not end route result");
    }

    public MovementRouteResult result() { return result; }
    public MovementRouteTermination termination() { return termination; }
    public MovementRouteEvent terminalEvent() { return terminalEvent; }
    public boolean stopped() { return termination == MovementRouteTermination.CONTROLLER_STOP; }
}
