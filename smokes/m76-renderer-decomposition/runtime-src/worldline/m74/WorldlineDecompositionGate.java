package worldline.m74;

/** Arms one cold-path treatment before the first retained M74 interval. */
public final class WorldlineDecompositionGate {
    private static boolean prepared;
    private WorldlineDecompositionGate() {}
    public static boolean prepare() {
        if (prepared || !WorldlineFrameCensus.armed) return false;
        if (WorldlineFrameCensus.running) {
            if (WorldlineFrameCensus.count != 0 || WorldlineFrameCensus.pending || WorldlineFrameCensus.sealed)
                throw new IllegalStateException("late M76 treatment setup");
            WorldlineFrameCensus.running = false; WorldlineFrameCensus.start = WorldlineFrameCensus.last = WorldlineFrameCensus.end = 0L;
        }
        prepared = true; return true;
    }
}
