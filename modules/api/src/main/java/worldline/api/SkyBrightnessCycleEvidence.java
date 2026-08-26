package worldline.api;

import java.util.Arrays;

/** Immutable canonical clear-sky time and skylight-subtraction evidence. */
public final class SkyBrightnessCycleEvidence {
    private static final long[] CANONICAL_TIMES = {
        0L, 6000L, 12000L, 12500L, 13000L, 13500L, 14000L,
        18000L, 22000L, 22500L, 23000L, 23500L, 23999L
    };
    private static final int[] CANONICAL_SUBTRACTION = {
        0, 0, 0, 3, 6, 9, 11, 11, 11, 9, 6, 3, 0
    };
    private final long[] times;
    private final int[] subtraction;

    private SkyBrightnessCycleEvidence(long[] times, int[] subtraction) {
        this.times = times;
        this.subtraction = subtraction;
    }

    public static SkyBrightnessCycleEvidence capture(long[] times, int[] subtraction) {
        if (times == null || subtraction == null) {
            throw new IllegalArgumentException("null sky brightness cycle evidence");
        }
        if (!Arrays.equals(times, CANONICAL_TIMES)) {
            throw new IllegalStateException("canonical clear-sky times drifted");
        }
        if (!Arrays.equals(subtraction, CANONICAL_SUBTRACTION)) {
            throw new IllegalStateException("canonical skylight subtraction drifted");
        }
        return new SkyBrightnessCycleEvidence(times.clone(), subtraction.clone());
    }

    public int samples() {
        return times.length;
    }

    public long timeAt(int index) {
        return times[index];
    }

    public int skylightSubtractedAt(int index) {
        return subtraction[index];
    }

    public int[] flattened() {
        int[] result = new int[times.length * 2];
        for (int index = 0; index < times.length; index++) {
            result[index * 2] = Math.toIntExact(times[index]);
            result[index * 2 + 1] = subtraction[index];
        }
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof SkyBrightnessCycleEvidence)) {
            return false;
        }
        SkyBrightnessCycleEvidence value = (SkyBrightnessCycleEvidence) other;
        return Arrays.equals(times, value.times) && Arrays.equals(subtraction, value.subtraction);
    }

    @Override
    public int hashCode() {
        return 31 * Arrays.hashCode(times) + Arrays.hashCode(subtraction);
    }
}
