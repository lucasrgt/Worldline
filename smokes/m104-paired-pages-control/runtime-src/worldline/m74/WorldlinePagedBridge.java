package worldline.m74;

/** Server-safe package bridge from the client recorder to the frozen M74 state. */
public final class WorldlinePagedBridge {
    private WorldlinePagedBridge() {}
    public static boolean armed() { return WorldlineFrameCensus.armed; }
    public static boolean running() { return WorldlineFrameCensus.running; }
    public static boolean sealed() { return WorldlineFrameCensus.sealed; }
    public static int count() { return WorldlineFrameCensus.count; }
    public static long elapsed() { return WorldlineFrameCensus.elapsed(); }
    public static void align() { if (WorldlineFrameCensus.running) { if (WorldlineFrameCensus.count != 0 || WorldlineFrameCensus.pending || WorldlineFrameCensus.sealed)
            throw new IllegalStateException("late M104 paging setup"); WorldlineFrameCensus.running = false;
        WorldlineFrameCensus.start = WorldlineFrameCensus.last = WorldlineFrameCensus.end = 0L; } }
}
