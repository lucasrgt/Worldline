package worldline.b173;

/** Static hook called only by the generated, locally instrumented client class. */
public final class B173ClockHooks {
    private static volatile B173VirtualClock clock;

    private B173ClockHooks() {}

    public static long currentTimeMillis() {
        B173VirtualClock current = clock;
        return current == null ? System.currentTimeMillis() : current.millis();
    }

    static void install(B173VirtualClock value) { clock = value; }

    static void clear(B173VirtualClock value) {
        if (clock == value) clock = null;
    }
}
