package worldline.api;

/** Observable lifecycle states; they do not imply vanilla equivalence. */
public enum RuntimeState {
    NEW,
    HEADLESS_BOOTED,
    WORLD_LOADED,
    CLOSED
}
