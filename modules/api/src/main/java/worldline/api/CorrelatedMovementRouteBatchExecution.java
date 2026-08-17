package worldline.api;

import java.util.Objects;

/** Immutable batch result bound to its exact final resolved movement event. */
public final class CorrelatedMovementRouteBatchExecution {
    private final CorrelatedMovementRouteBatchResult result;
    private final MovementRouteBatchTerminalKind terminalKind;
    private final CorrelatedMovementRouteBatchEvent terminalEvent;

    public CorrelatedMovementRouteBatchExecution(CorrelatedMovementRouteBatchResult result,
            MovementRouteBatchTerminalKind terminalKind,
            CorrelatedMovementRouteBatchEvent terminalEvent) {
        this.result = Objects.requireNonNull(result, "result");
        this.terminalKind = Objects.requireNonNull(terminalKind, "terminalKind");
        this.terminalEvent = Objects.requireNonNull(terminalEvent, "terminalEvent");
        int last = result.executions().size() - 1;
        boolean exhausted = result.termination() == MovementRouteBatchTermination.EXHAUSTED;
        if (terminalEvent.routeIndex() != last
                || terminalEvent.event() != result.finalExecution().terminalEvent()
                || exhausted != (terminalKind == MovementRouteBatchTerminalKind.EXHAUSTED))
            throw new IllegalArgumentException("terminal event does not end batch result");
    }

    public CorrelatedMovementRouteBatchResult result() { return result; }
    public MovementRouteBatchTerminalKind terminalKind() { return terminalKind; }
    public CorrelatedMovementRouteBatchEvent terminalEvent() { return terminalEvent; }
}
