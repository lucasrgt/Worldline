package worldline.api;

/** Explicit route behavior after a server-authoritative movement correction. */
public enum RouteCorrectionPolicy {
    CONTINUE,
    STOP_ON_CORRECTION
}
