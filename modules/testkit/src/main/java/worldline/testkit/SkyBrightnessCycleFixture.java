package worldline.testkit;

import worldline.api.SkyBrightnessCycleEvidence;

/** Reusable TestKit fixture for exact clear-sky day-cycle observations. */
public final class SkyBrightnessCycleFixture {
    private SkyBrightnessCycleFixture() { }

    public static SkyBrightnessCycleEvidence observe(long[] times, int[] skylightSubtraction) {
        return SkyBrightnessCycleEvidence.capture(times, skylightSubtraction);
    }

    public static void compare(SkyBrightnessCycleEvidence expected,
            SkyBrightnessCycleEvidence observed) {
        if (expected == null || !expected.equals(observed)) {
            throw new IllegalStateException("sky brightness cycle evidence diverged");
        }
    }
}
