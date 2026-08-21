package worldline.api;

/** Synchronous decision made after one correlated route execution completes. */
public interface CorrelatedMovementRouteBatchController {
    MovementRouteDirective after(CorrelatedMovementRouteExecution execution);
}
