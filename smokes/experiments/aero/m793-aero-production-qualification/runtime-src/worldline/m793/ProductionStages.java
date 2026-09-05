package worldline.m793;

/** Allocation-free attribution of the two production flush boundaries. */
public final class ProductionStages {
    private static final long[] STARTED = new long[7];
    private static final long[] ELAPSED = new long[7];
    private static final long[] ALLOCATED = new long[7], BYTES = new long[7];

    private ProductionStages() {}

    public static void reset() {
        for (int i = 0; i < 7; i++) { STARTED[i] = 0L; ELAPSED[i] = 0L; BYTES[i] = 0L; }
    }

    public static void begin(int stage) {
        STARTED[stage] = ProductionState.retaining() ? System.nanoTime() : 0L;
        if (STARTED[stage] != 0L) ALLOCATED[stage] = ProductionProfile.allocationNow();
    }

    public static void end(int stage) {
        if (STARTED[stage] != 0L) {
            ELAPSED[stage] += System.nanoTime() - STARTED[stage];
            BYTES[stage] += ProductionProfile.allocationNow() - ALLOCATED[stage];
        }
        STARTED[stage] = 0L;
    }

    public static long nanos(int stage) { return ELAPSED[stage]; }
    public static long bytes(int stage) { return BYTES[stage]; }
}
