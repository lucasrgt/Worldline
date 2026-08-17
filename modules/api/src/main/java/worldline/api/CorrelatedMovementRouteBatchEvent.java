package worldline.api;

import java.util.Objects;

/** Immutable batch-indexed view of one correlated route event. */
public final class CorrelatedMovementRouteBatchEvent {
    private final int routeIndex;
    private final CorrelatedMovementRouteEvent event;

    public CorrelatedMovementRouteBatchEvent(int routeIndex, CorrelatedMovementRouteEvent event) {
        if (routeIndex < 0 || routeIndex >= 16) throw new IllegalArgumentException("invalid route index");
        this.routeIndex = routeIndex;
        this.event = Objects.requireNonNull(event, "event");
    }

    public int routeIndex() { return routeIndex; }
    public CorrelatedMovementRouteEvent event() { return event; }
}
