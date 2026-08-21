package worldline.test;

/** Runner-installed boundary used by snapshot matchers without a runner dependency. */
public final class SnapshotExpectation {
    private static final ThreadLocal<Handler> ACTIVE = new ThreadLocal<>();

    private SnapshotExpectation() {}

    public static void install(Handler handler) {
        if (handler == null) throw new NullPointerException("handler");
        if (ACTIVE.get() != null) throw new IllegalStateException("snapshot handler is already installed");
        ACTIVE.set(handler);
    }

    public static void clear() { ACTIVE.remove(); }

    static void match(String name, Object value) {
        Handler handler = ACTIVE.get();
        if (handler == null) throw new IllegalStateException("snapshot assertion requires the Worldline runner");
        handler.match(name, value);
    }

    @FunctionalInterface
    public interface Handler { void match(String name, Object value); }
}
