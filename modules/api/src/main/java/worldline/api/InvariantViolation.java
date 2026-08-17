package worldline.api;

/** Fail-closed signal that a named invariant no longer holds. */
public final class InvariantViolation extends IllegalStateException {
    private static final long serialVersionUID = 1L;
    private final String invariant;

    public InvariantViolation(String invariant, String detail) {
        super(name(invariant) + ": " + detail(detail));
        this.invariant = name(invariant);
    }

    public String invariant() {
        return invariant;
    }

    private static String name(String invariant) {
        if (invariant == null || invariant.isEmpty()) throw new IllegalArgumentException("invariant name");
        return invariant;
    }

    private static String detail(String detail) {
        if (detail == null || detail.isEmpty()) throw new IllegalArgumentException("invariant detail");
        return detail;
    }
}
