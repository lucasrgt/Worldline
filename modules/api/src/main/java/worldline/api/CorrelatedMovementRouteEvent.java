package worldline.api;

import java.util.Objects;

/** Immutable route event paired with an uninterpreted caller-owned reference. */
public final class CorrelatedMovementRouteEvent {
    private final Object correlation;
    private final MovementRouteEvent event;

    public CorrelatedMovementRouteEvent(Object correlation, MovementRouteEvent event) {
        this.correlation = Objects.requireNonNull(correlation, "correlation");
        this.event = Objects.requireNonNull(event, "event");
    }

    public Object correlation() { return correlation; }
    public MovementRouteEvent event() { return event; }
}
