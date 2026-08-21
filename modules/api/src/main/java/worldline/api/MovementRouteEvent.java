package worldline.api;

/** Immutable synchronous route-progress observation. */
public final class MovementRouteEvent {
    private final int alternativeIndex, outcomeIndex;
    private final MovementAttemptKind kind;
    private final MovementOutcome outcome;

    public MovementRouteEvent(int alternativeIndex, int outcomeIndex,
            MovementAttemptKind kind, MovementOutcome outcome) {
        if (alternativeIndex < 0 || alternativeIndex >= 32 || outcomeIndex < 0 || outcomeIndex >= 64
                || kind == null || outcome == null) throw new IllegalArgumentException("invalid route event");
        this.alternativeIndex = alternativeIndex; this.outcomeIndex = outcomeIndex;
        this.kind = kind; this.outcome = outcome;
    }

    public int alternativeIndex() { return alternativeIndex; }
    public int outcomeIndex() { return outcomeIndex; }
    public MovementAttemptKind kind() { return kind; }
    public MovementOutcome outcome() { return outcome; }
}
