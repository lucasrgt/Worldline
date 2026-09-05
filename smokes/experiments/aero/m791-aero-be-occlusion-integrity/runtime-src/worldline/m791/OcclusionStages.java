package worldline.m791;

/** Allocation-free attribution of the two production flush boundaries. */
public final class OcclusionStages {
    private static final long[] STARTED = new long[2];
    private static final long[] ELAPSED = new long[2];

    private OcclusionStages() {}

    public static void reset() {
        for (int i = 0; i < 2; i++) { STARTED[i] = 0L; ELAPSED[i] = 0L; }
    }

    public static void begin(int stage) {
        STARTED[stage] = OcclusionState.retaining() ? System.nanoTime() : 0L;
    }

    public static void end(int stage) {
        if (STARTED[stage] != 0L) ELAPSED[stage] += System.nanoTime() - STARTED[stage];
        STARTED[stage] = 0L;
    }

    public static long nanos(int stage) { return ELAPSED[stage]; }
}
