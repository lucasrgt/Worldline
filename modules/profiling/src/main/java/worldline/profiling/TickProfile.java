package worldline.profiling;

import java.util.Arrays;

/**
 * Immutable per-tick timing samples from one profiled scenario execution.
 * Aggregates are deterministic functions of the samples; wall-clock values
 * themselves are machine-relative and never frozen evidence.
 */
public final class TickProfile {
    private final long[] tickNanos;
    private final long[] modNanos;

    private TickProfile(long[] tickNanos, long[] modNanos) {
        this.tickNanos = tickNanos; this.modNanos = modNanos;
    }

    public static TickProfile of(long[] tickNanos, long[] modNanos) {
        if (tickNanos == null || modNanos == null) throw new NullPointerException("samples");
        require(tickNanos.length == modNanos.length, "sample width mismatch");
        require(tickNanos.length > 0, "profile requires samples");
        for (int index = 0; index < tickNanos.length; index++) {
            require(tickNanos[index] > 0, "nonpositive tick sample");
            require(modNanos[index] >= 0, "negative mod sample");
            require(modNanos[index] <= tickNanos[index], "mod time exceeds tick time");
        }
        return new TickProfile(tickNanos.clone(), modNanos.clone());
    }

    public int ticks() { return tickNanos.length; }
    public long tickNanos(int index) { return tickNanos[index]; }
    public long modNanos(int index) { return modNanos[index]; }

    public long total() { return sum(tickNanos); }

    public long modTotal() { return sum(modNanos); }

    /** Mod share of total tick time, in percent rounded down to the integer grid. */
    public long modSharePercent() { return modTotal() * 100L / total(); }

    public long mean() { return total() / ticks(); }

    public long min() { return sorted()[0]; }

    public long max() { return sorted()[sorted().length - 1]; }

    /** Nearest-rank median over the sorted samples. */
    public long median() { return rank(0.50D); }

    /** Nearest-rank 95th percentile over the sorted samples. */
    public long p95() { return rank(0.95D); }

    private long[] cachedSorted;

    private long[] sorted() {
        if (cachedSorted == null) {
            cachedSorted = tickNanos.clone();
            Arrays.sort(cachedSorted);
        }
        return cachedSorted;
    }

    private long rank(double fraction) {
        int index = (int) Math.ceil(fraction * ticks());
        if (index < 1) index = 1;
        if (index > ticks()) index = ticks();
        return sorted()[index - 1];
    }

    private static long sum(long[] values) {
        long total = 0L;
        for (long value : values) total += value;
        return total;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
