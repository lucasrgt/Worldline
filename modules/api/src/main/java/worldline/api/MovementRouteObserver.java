package worldline.api;

/** Synchronous observer invoked immediately after each resolved route attempt. */
public interface MovementRouteObserver {
    MovementRouteObserver NONE = event -> { };

    void observe(MovementRouteEvent event);
}
