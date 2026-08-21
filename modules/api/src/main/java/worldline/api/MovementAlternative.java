package worldline.api;

/** One primary movement and its single caller-supplied correction fallback. */
public final class MovementAlternative {
    private final MovementStep primary, fallback;

    public MovementAlternative(MovementStep primary, MovementStep fallback) {
        if (primary == null || fallback == null)
            throw new IllegalArgumentException("null movement alternative");
        this.primary = primary; this.fallback = fallback;
    }

    public MovementStep primary() { return primary; }
    public MovementStep fallback() { return fallback; }
}
