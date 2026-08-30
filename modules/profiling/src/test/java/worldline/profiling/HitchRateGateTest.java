package worldline.profiling;

import java.util.Arrays;

/** Deterministic coverage for counterbalancing, rates, quartiles, and verdicts. */
final class HitchRateGateTest {
    private HitchRateGateTest() {}

    static void run() {
        HitchRateGate.Pair first = pair(1, 4, true);
        HitchRateGate.Pair second = pair(1, 5, false);
        HitchRateGate.Pair third = pair(2, 6, false);
        HitchRateGate.Pair fourth = pair(2, 7, true);
        HitchRateGate.Result regression = HitchRateGate.evaluate(
                Arrays.asList(first, second, third, fourth), 10_000L);
        require(regression.verdict() == HitchRateGate.Verdict.REGRESSION
                && !regression.passes() && regression.positivePairs() == 4
                && regression.aggregateDeltaPpm() > regression.allowedRegressionPpm()
                && regression.lowerQuartilePpm() <= regression.medianDeltaPpm()
                && regression.medianDeltaPpm() <= regression.upperQuartilePpm(),
                "hitch regression classification drifted");
        HitchRateGate.Result equivalent = HitchRateGate.evaluate(Arrays.asList(
                pair(2, 2, true), pair(3, 3, false), pair(1, 1, false), pair(0, 0, true)), 0L);
        require(equivalent.verdict() == HitchRateGate.Verdict.EQUIVALENT
                && equivalent.passes() && equivalent.aggregateDeltaPpm() == 0L,
                "hitch equivalence classification drifted");
        rejects(() -> HitchRateGate.evaluate(Arrays.asList(
                pair(0, 0, true), pair(0, 0, true), pair(0, 0, false), pair(0, 0, true)), 0L));
    }

    private static HitchRateGate.Pair pair(int baselineHitches, int candidateHitches,
            boolean baselineFirst) {
        return HitchRateGate.pair(census(baselineHitches), census(candidateHitches),
                baselineFirst, WorldlineProfilerMetrics.FRAME_WALL, 50L);
    }

    private static FrameCensus census(int hitches) {
        long[][] rows = new long[100][3];
        for (int frame = 0; frame < rows.length; frame++) {
            rows[frame][0] = frame; rows[frame][1] = 1_000L + frame * 100L;
            rows[frame][2] = frame < hitches ? 50L : 10L;
        }
        return FrameCensus.of(new String[] {WorldlineProfilerMetrics.FRAME_WALL}, rows);
    }

    private static void rejects(Runnable action) {
        boolean rejected = false;
        try { action.run(); } catch (IllegalArgumentException expected) { rejected = true; }
        require(rejected, "invalid hitch gate input was accepted");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
