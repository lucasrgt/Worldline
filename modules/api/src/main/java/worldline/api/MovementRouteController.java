package worldline.api;

/** Caller-thread route controller invoked immediately after each resolved attempt. */
public interface MovementRouteController {
    MovementRouteController CONTINUE = event -> MovementRouteDirective.CONTINUE;

    MovementRouteDirective after(MovementRouteEvent event);
}
