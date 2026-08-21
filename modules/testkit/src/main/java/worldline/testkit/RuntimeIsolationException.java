package worldline.testkit;

/** Fatal signal that a timed-out official attempt retained its execution thread. */
final class RuntimeIsolationException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    RuntimeIsolationException(String message) { super(message); }
}
