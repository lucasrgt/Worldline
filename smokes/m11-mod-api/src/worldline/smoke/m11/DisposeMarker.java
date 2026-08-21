package worldline.smoke.m11;

/** Parent-classloader marker so the driver can observe mod disposal. */
public final class DisposeMarker {
    public static boolean marked;

    private DisposeMarker() {}

    public static void mark() { marked = true; }
}
