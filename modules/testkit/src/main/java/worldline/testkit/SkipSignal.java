package worldline.testkit;

/** Private control signal for a dynamic skip. */
final class SkipSignal extends RuntimeException {
    private static final long serialVersionUID = 1L;
    SkipSignal(String reason) { super(reason); }
}
