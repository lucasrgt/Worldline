package worldline.api;

/** Synchronous batch-wide decision made at one correlated route event. */
public interface CorrelatedMovementRouteBatchEventController {
    MovementRouteDirective after(CorrelatedMovementRouteBatchEvent event);
}
