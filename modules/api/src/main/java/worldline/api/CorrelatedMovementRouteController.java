package worldline.api;

/** Synchronous controller that receives caller-owned correlation by identity. */
public interface CorrelatedMovementRouteController {
    MovementRouteDirective after(CorrelatedMovementRouteEvent event);
}
