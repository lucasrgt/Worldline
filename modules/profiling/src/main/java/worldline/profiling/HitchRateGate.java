package worldline.profiling;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Counterbalanced paired hitch-rate analysis over complete frame censuses. */
public final class HitchRateGate {
    public enum Verdict { IMPROVEMENT, EQUIVALENT, REGRESSION }

    private HitchRateGate() {}

    public static Pair pair(FrameCensus baseline, FrameCensus candidate,
            boolean baselineFirst, String metric, long hitchThresholdNanos) {
        require(metric != null && hitchThresholdNanos > 0L, "invalid hitch policy");
        return new Pair(sample(baseline, metric, hitchThresholdNanos),
                sample(candidate, metric, hitchThresholdNanos), baselineFirst,
                metric, hitchThresholdNanos);
    }

    public static Result evaluate(List<Pair> source, long allowedRegressionPpm) {
        require(source != null && source.size() >= 4 && source.size() % 2 == 0,
                "paired hitch gate requires an even set of at least four pairs");
        require(allowedRegressionPpm >= 0L && allowedRegressionPpm <= 1_000_000L,
                "invalid hitch regression allowance");
        List<Pair> pairs = Collections.unmodifiableList(new ArrayList<Pair>(source));
        Pair first = pairs.get(0); int baselineFirst = 0, positive = 0, negative = 0;
        long baselineFrames = 0L, candidateFrames = 0L;
        long baselineHitches = 0L, candidateHitches = 0L;
        long[] rateDeltas = new long[pairs.size()], p99Deltas = new long[pairs.size()];
        for (int index = 0; index < pairs.size(); index++) {
            Pair pair = pairs.get(index);
            require(pair.metric.equals(first.metric) && pair.threshold == first.threshold,
                    "paired hitch policy drift");
            if (pair.baselineFirst) baselineFirst++;
            baselineFrames = Math.addExact(baselineFrames, pair.baseline.frames);
            candidateFrames = Math.addExact(candidateFrames, pair.candidate.frames);
            baselineHitches = Math.addExact(baselineHitches, pair.baseline.hitches);
            candidateHitches = Math.addExact(candidateHitches, pair.candidate.hitches);
            rateDeltas[index] = pair.rateDeltaPpm();
            p99Deltas[index] = pair.p99DeltaNanos();
            if (rateDeltas[index] > 0L) positive++;
            if (rateDeltas[index] < 0L) negative++;
        }
        require(baselineFirst * 2 == pairs.size(), "paired hitch order is not counterbalanced");
        long before = rate(baselineHitches, baselineFrames);
        long after = rate(candidateHitches, candidateFrames);
        long aggregateDelta = Math.subtractExact(after, before);
        long median = quantile(rateDeltas, 2, 4), lower = quantile(rateDeltas, 1, 4);
        long upper = quantile(rateDeltas, 3, 4), p99Median = quantile(p99Deltas, 2, 4);
        boolean regression = aggregateDelta > allowedRegressionPpm
                && median > allowedRegressionPpm && positive * 2 > pairs.size();
        boolean improvement = aggregateDelta < -allowedRegressionPpm
                && median < -allowedRegressionPpm && negative * 2 > pairs.size();
        Verdict verdict = regression ? Verdict.REGRESSION
                : improvement ? Verdict.IMPROVEMENT : Verdict.EQUIVALENT;
        return new Result(pairs, before, after, aggregateDelta, median, lower, upper,
                p99Median, positive, negative, allowedRegressionPpm, verdict);
    }

    private static Sample sample(FrameCensus census, String metric, long threshold) {
        if (census == null) throw new NullPointerException("frame census");
        require(census.frames() >= 2, "hitch sample requires two complete frames");
        long[] values = new long[census.frames()]; long hitches = 0L;
        for (int frame = 0; frame < values.length; frame++) {
            values[frame] = census.value(frame, metric);
            if (values[frame] >= threshold) hitches++;
        }
        Arrays.sort(values);
        long duration = census.monotonicNanos(census.frames() - 1) - census.monotonicNanos(0);
        require(duration > 0L, "hitch sample duration is not positive");
        int p99 = Math.max(1, (int) ((99L * values.length + 99L) / 100L));
        return new Sample(census.frames(), hitches, rate(hitches, census.frames()),
                values[p99 - 1], duration);
    }

    private static long rate(long hitches, long frames) {
        require(hitches >= 0L && frames > 0L && hitches <= frames, "invalid hitch sample");
        return BigInteger.valueOf(hitches).multiply(BigInteger.valueOf(1_000_000L))
                .divide(BigInteger.valueOf(frames)).longValueExact();
    }

    private static long quantile(long[] source, int numerator, int denominator) {
        long[] values = source.clone(); Arrays.sort(values);
        int rank = (int) (((long) numerator * values.length + denominator - 1L) / denominator);
        return values[Math.max(1, rank) - 1];
    }

    public static final class Pair {
        private final Sample baseline, candidate;
        private final boolean baselineFirst;
        private final String metric;
        private final long threshold;
        private Pair(Sample baseline, Sample candidate, boolean baselineFirst,
                String metric, long threshold) {
            this.baseline = baseline; this.candidate = candidate;
            this.baselineFirst = baselineFirst; this.metric = metric; this.threshold = threshold;
        }
        public Sample baseline() { return baseline; }
        public Sample candidate() { return candidate; }
        public boolean baselineFirst() { return baselineFirst; }
        public long rateDeltaPpm() { return candidate.ratePpm - baseline.ratePpm; }
        public long p99DeltaNanos() { return candidate.p99Nanos - baseline.p99Nanos; }
    }

    public static final class Sample {
        private final long frames, hitches, ratePpm, p99Nanos, durationNanos;
        private Sample(long frames, long hitches, long ratePpm, long p99Nanos,
                long durationNanos) {
            this.frames = frames; this.hitches = hitches; this.ratePpm = ratePpm;
            this.p99Nanos = p99Nanos; this.durationNanos = durationNanos;
        }
        public long frames() { return frames; }
        public long hitches() { return hitches; }
        public long ratePpm() { return ratePpm; }
        public long p99Nanos() { return p99Nanos; }
        public long durationNanos() { return durationNanos; }
    }

    public static final class Result {
        private final List<Pair> pairs;
        private final long baselineRate, candidateRate, aggregateDelta, medianDelta;
        private final long lowerQuartile, upperQuartile, p99MedianDelta, allowance;
        private final int positivePairs, negativePairs;
        private final Verdict verdict;
        private Result(List<Pair> pairs, long baselineRate, long candidateRate,
                long aggregateDelta, long medianDelta, long lowerQuartile, long upperQuartile,
                long p99MedianDelta, int positivePairs, int negativePairs, long allowance,
                Verdict verdict) {
            this.pairs = pairs; this.baselineRate = baselineRate;
            this.candidateRate = candidateRate; this.aggregateDelta = aggregateDelta;
            this.medianDelta = medianDelta; this.lowerQuartile = lowerQuartile;
            this.upperQuartile = upperQuartile; this.p99MedianDelta = p99MedianDelta;
            this.positivePairs = positivePairs; this.negativePairs = negativePairs;
            this.allowance = allowance; this.verdict = verdict;
        }
        public List<Pair> pairs() { return pairs; }
        public long baselineRatePpm() { return baselineRate; }
        public long candidateRatePpm() { return candidateRate; }
        public long aggregateDeltaPpm() { return aggregateDelta; }
        public long medianDeltaPpm() { return medianDelta; }
        public long lowerQuartilePpm() { return lowerQuartile; }
        public long upperQuartilePpm() { return upperQuartile; }
        public long p99MedianDeltaNanos() { return p99MedianDelta; }
        public int positivePairs() { return positivePairs; }
        public int negativePairs() { return negativePairs; }
        public long allowedRegressionPpm() { return allowance; }
        public Verdict verdict() { return verdict; }
        public boolean passes() { return verdict != Verdict.REGRESSION; }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
