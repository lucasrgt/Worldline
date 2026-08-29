package worldline.profiling;

import java.math.BigInteger;

/** Machine-relative A/B comparison with an explicit noise band. */
public final class ProfilerComparison {
    public enum Verdict { IMPROVEMENT, EQUIVALENT, REGRESSION }
    private final ProfilerRun baseline, candidate;

    public ProfilerComparison(ProfilerRun baseline, ProfilerRun candidate) {
        if (baseline == null || candidate == null) throw new NullPointerException("profiler comparison");
        require(baseline.mode() == candidate.mode(), "profiler capture modes differ");
        this.baseline = baseline; this.candidate = candidate;
    }

    public ProfilerComparison requireMatchingTags(String... names) {
        if (names == null) throw new NullPointerException("profiler comparison tags");
        for (String name : names) {
            String before = baseline.tag(name), after = candidate.tag(name);
            require(before == null ? after == null : before.equals(after),
                    "profiler comparison tag differs: " + name);
        }
        return this;
    }

    public Result compare(String metric, ProfilerBudgetPolicy.Statistic statistic,
            long absoluteNoise, int relativeNoisePpm) {
        require(baseline.schema().contains(metric) && candidate.schema().contains(metric),
                "comparison metric is not shared");
        require(baseline.schema().metric(baseline.schema().index(metric)).equals(
                candidate.schema().metric(candidate.schema().index(metric))),
                "comparison metric metadata differs");
        require(statistic != null && absoluteNoise >= 0L
                && relativeNoisePpm >= 0 && relativeNoisePpm <= 1_000_000,
                "invalid profiler comparison policy");
        long before = value(new ProfilerSummary(baseline), metric, statistic);
        long after = value(new ProfilerSummary(candidate), metric, statistic);
        long delta = Math.subtractExact(after, before);
        long relative = before == 0L ? (after == 0L ? 0L : 1_000_000L)
                : scaled(delta, 1_000_000L, before);
        long noise = Math.max(absoluteNoise, scaled(before, relativeNoisePpm, 1_000_000L));
        Verdict verdict = delta > noise ? Verdict.REGRESSION
                : delta < -noise ? Verdict.IMPROVEMENT : Verdict.EQUIVALENT;
        return new Result(metric, statistic, before, after, delta, relative, noise, verdict);
    }

    private static long value(ProfilerSummary summary, String metric,
            ProfilerBudgetPolicy.Statistic statistic) {
        if (statistic == ProfilerBudgetPolicy.Statistic.MEAN) return summary.mean(metric);
        if (statistic == ProfilerBudgetPolicy.Statistic.P95)
            return summary.percentile(metric, 95, 100);
        if (statistic == ProfilerBudgetPolicy.Statistic.P99)
            return summary.percentile(metric, 99, 100);
        return summary.maximum(metric);
    }

    private static long scaled(long value, long multiplier, long divisor) {
        return BigInteger.valueOf(value).multiply(BigInteger.valueOf(multiplier))
                .divide(BigInteger.valueOf(divisor)).longValueExact();
    }

    public static final class Result {
        private final String metric;
        private final ProfilerBudgetPolicy.Statistic statistic;
        private final long before, after, delta, relativePpm, noise;
        private final Verdict verdict;
        private Result(String metric, ProfilerBudgetPolicy.Statistic statistic, long before,
                long after, long delta, long relativePpm, long noise, Verdict verdict) {
            this.metric = metric; this.statistic = statistic; this.before = before;
            this.after = after; this.delta = delta; this.relativePpm = relativePpm;
            this.noise = noise; this.verdict = verdict;
        }
        public String metric() { return metric; }
        public ProfilerBudgetPolicy.Statistic statistic() { return statistic; }
        public long baseline() { return before; }
        public long candidate() { return after; }
        public long delta() { return delta; }
        public long relativePpm() { return relativePpm; }
        public long noise() { return noise; }
        public Verdict verdict() { return verdict; }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
