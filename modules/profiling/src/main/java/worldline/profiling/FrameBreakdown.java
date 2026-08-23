package worldline.profiling;

/**
 * One complete frame split into nonexclusive causal buckets. Stage values may
 * overlap, so classification compares each bucket with the frame instead of
 * summing them.
 */
public final class FrameBreakdown {
    public enum HitchClass {
        SAVE, GC_RUNTIME, CHUNK_WORK, SUBJECT_WORK, DISPLAY_PRESENT, MIXED, UNKNOWN
    }

    private final long frameNanos, saveNanos, gcNanos, chunkNanos;
    private final long subjectNanos, displayNanos;

    private FrameBreakdown(long frameNanos, long saveNanos, long gcNanos,
            long chunkNanos, long subjectNanos, long displayNanos) {
        this.frameNanos = frameNanos; this.saveNanos = saveNanos;
        this.gcNanos = gcNanos; this.chunkNanos = chunkNanos;
        this.subjectNanos = subjectNanos; this.displayNanos = displayNanos;
    }

    public static FrameBreakdown of(long frameNanos, long saveNanos, long gcNanos,
            long chunkNanos, long subjectNanos, long displayNanos) {
        require(frameNanos > 0L, "frame duration must be positive");
        require(saveNanos >= 0L && gcNanos >= 0L && chunkNanos >= 0L
                && subjectNanos >= 0L && displayNanos >= 0L,
                "frame buckets must be nonnegative");
        return new FrameBreakdown(frameNanos, saveNanos, gcNanos,
                chunkNanos, subjectNanos, displayNanos);
    }

    /**
     * Classifies material buckets using the larger of an absolute floor and a
     * rational share of the complete frame. More than one material bucket is
     * deliberately MIXED because timings can overlap.
     */
    public HitchClass classify(long absoluteFloorNanos, int shareNumerator,
            int shareDenominator) {
        require(absoluteFloorNanos >= 0L, "negative classification floor");
        require(shareNumerator > 0 && shareDenominator > 0
                && shareNumerator <= shareDenominator, "invalid classification share");
        long threshold = Math.max(absoluteFloorNanos,
                scaledCeiling(frameNanos, shareNumerator, shareDenominator));
        HitchClass result = HitchClass.UNKNOWN;
        int material = 0;
        long[] values = {saveNanos, gcNanos, chunkNanos, subjectNanos, displayNanos};
        HitchClass[] classes = {HitchClass.SAVE, HitchClass.GC_RUNTIME,
                HitchClass.CHUNK_WORK, HitchClass.SUBJECT_WORK,
                HitchClass.DISPLAY_PRESENT};
        for (int index = 0; index < values.length; index++) {
            if (values[index] >= threshold) {
                result = classes[index]; material++;
            }
        }
        return material > 1 ? HitchClass.MIXED : result;
    }

    public long frameNanos() { return frameNanos; }
    public long saveNanos() { return saveNanos; }
    public long gcNanos() { return gcNanos; }
    public long chunkNanos() { return chunkNanos; }
    public long subjectNanos() { return subjectNanos; }
    public long displayNanos() { return displayNanos; }

    private static long scaledCeiling(long value, int numerator, int denominator) {
        long whole = value / denominator;
        long remainder = value % denominator;
        return Math.addExact(Math.multiplyExact(whole, numerator),
                (remainder * numerator + denominator - 1L) / denominator);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
