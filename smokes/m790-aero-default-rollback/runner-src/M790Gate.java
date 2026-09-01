import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/** Classifies bounded queue reuse without allowing frame or raster regressions. */
final class M790Gate {
    private M790Gate() {}

    static M790Result evaluate(Path product, List<M790Pair> pairs,
                               List<M790Visual> visuals, Properties config) throws Exception {
        long pixelSamples = (long) visuals.get(0).baseline().width
            * visuals.get(0).baseline().height * visuals.get(0).baseline().captures;
        Set<Long> noise = new HashSet<Long>(visuals.get(4).changedLocations());
        noise.addAll(visuals.get(5).changedLocations());
        Set<Long> unexplained = new HashSet<Long>();
        for (int index = 0; index < 4; index++) {
            unexplained.addAll(visuals.get(index).changedLocations());
        }
        unexplained.removeAll(noise);
        double noisePpm = ppm(noise.size(), pixelSamples);
        double maximumNoise = threshold(config, "maximum.raster.noise.ppm");
        boolean visualPass = unexplained.isEmpty()
            && ppm(visuals.get(4).changedPixels(), pixelSamples) <= maximumNoise
            && ppm(visuals.get(5).changedPixels(), pixelSamples) <= maximumNoise
            && noisePpm <= maximumNoise;
        Totals totals = totals(pairs);
        double fpsRatio = totals.reuseFps / totals.baselineFps;
        double p99Ratio = totals.reuseP99 / totals.baselineP99;
        double allocationRatio = totals.reuseAllocation / totals.baselineAllocation;
        double renderRatio = totals.reuseRender / totals.baselineRender;
        double queueAllocationRatio = totals.reuseQueueAllocated / totals.baselineQueueAllocated;
        boolean workloadRepeatable = workloadRepeatable(pairs,
            threshold(config, "maximum.workload.drift.ratio"));
        boolean capturePass = pairs.stream().allMatch(pair ->
            pair.baseline().blankCaptures <= threshold(config, "maximum.blank.capture.rejections")
                && pair.reuse().blankCaptures <= threshold(config,
                    "maximum.blank.capture.rejections"));
        boolean baselineFirstPass = qualifies(pairs, true, config);
        boolean reuseFirstPass = qualifies(pairs, false, config);
        int allocationWins = 0;
        for (M790Pair pair : pairs) {
            if (pair.reuse().allocationPerFrame() <= pair.baseline().allocationPerFrame()
                    * threshold(config, "allocation.maximum.ratio")) allocationWins++;
        }
        M790Hitch hitch = hitch(product, pairs, config);
        boolean performance = fpsRatio >= threshold(config, "fps.minimum.ratio")
            && p99Ratio <= threshold(config, "p99.maximum.ratio")
            && allocationRatio <= threshold(config, "allocation.maximum.ratio")
            && renderRatio <= threshold(config, "render.maximum.ratio")
            && queueAllocationRatio <= threshold(config, "queue.allocation.maximum.ratio")
            && baselineFirstPass && reuseFirstPass
            && allocationWins >= threshold(config, "minimum.allocation.winning.pairs")
            && hitch.passes();
        int size = pairs.size();
        return new M790Result(visualPass, workloadRepeatable, capturePass,
            noise.size(), noisePpm, unexplained.size(),
            totals.baselineFps / size, totals.reuseFps / size,
            totals.baselineP50 / size, totals.reuseP50 / size,
            totals.baselineP95 / size, totals.reuseP95 / size,
            totals.baselineP99 / size, totals.reuseP99 / size,
            fpsRatio, p99Ratio, allocationRatio, renderRatio, queueAllocationRatio,
            baselineFirstPass, reuseFirstPass, allocationWins, hitch, performance);
    }

    private static Totals totals(List<M790Pair> pairs) {
        Totals value = new Totals();
        for (M790Pair pair : pairs) value.add(pair);
        return value;
    }

    private static boolean workloadRepeatable(List<M790Pair> pairs, double tolerance) {
        boolean repeatable = true;
        for (int index = 1; index < pairs.size(); index++) {
            repeatable &= within(rate(pairs.get(0).baseline()), rate(pairs.get(index).baseline()), tolerance)
                && within(rate(pairs.get(0).reuse()), rate(pairs.get(index).reuse()), tolerance);
        }
        for (M790Pair pair : pairs) {
            repeatable &= within(rate(pair.baseline()), rate(pair.reuse()), tolerance)
                && within(rebuildRate(pair.baseline()), rebuildRate(pair.reuse()), tolerance);
        }
        return repeatable;
    }

    private static boolean qualifies(List<M790Pair> pairs, boolean baselineFirst,
                                      Properties config) {
        Totals totals = new Totals();
        for (M790Pair pair : pairs) {
            if (pair.baselineFirst() == baselineFirst) totals.add(pair);
        }
        return totals.reuseFps / totals.baselineFps >= threshold(config, "fps.minimum.ratio")
            && totals.reuseP99 / totals.baselineP99 <= threshold(config, "p99.maximum.ratio")
            && totals.reuseAllocation / totals.baselineAllocation
                <= threshold(config, "allocation.maximum.ratio")
            && totals.reuseRender / totals.baselineRender
                <= threshold(config, "render.maximum.ratio");
    }

    private static M790Hitch hitch(Path product, List<M790Pair> source,
                                   Properties config) throws Exception {
        try (URLClassLoader loader = new URLClassLoader(new URL[] {product.toUri().toURL()}, null)) {
            Class<?> census = Class.forName("worldline.profiling.FrameCensus", true, loader);
            Class<?> gate = Class.forName("worldline.profiling.HitchRateGate", true, loader);
            Method of = census.getMethod("of", String[].class, long[][].class);
            Method pairMethod = gate.getMethod("pair", census, census, boolean.class,
                String.class, long.class);
            List<Object> evidence = new ArrayList<Object>();
            long threshold = Long.parseLong(SmokeSupport.value(config, "hitch.threshold.nanos"));
            for (M790Pair pair : source) evidence.add(pairMethod.invoke(null,
                frame(of, pair.baseline().walls), frame(of, pair.reuse().walls), pair.baselineFirst(),
                "frame.wall.nanos", threshold));
            Object value = gate.getMethod("evaluate", List.class, long.class).invoke(null,
                evidence, Long.parseLong(SmokeSupport.value(config, "allowed.regression.ppm")));
            return new M790Hitch(number(value, "baselineRatePpm"),
                number(value, "candidateRatePpm"), number(value, "aggregateDeltaPpm"),
                (Boolean) value.getClass().getMethod("passes").invoke(value),
                value.getClass().getMethod("verdict").invoke(value).toString());
        }
    }

    private static Object frame(Method of, long[] durations) throws Exception {
        long[][] rows = new long[durations.length][3];
        long time = 1L;
        for (int index = 0; index < rows.length; index++) {
            rows[index][0] = index;
            rows[index][1] = time;
            rows[index][2] = durations[index];
            time = Math.addExact(time, Math.max(1L, durations[index]));
        }
        return of.invoke(null, new String[] {"frame.wall.nanos"}, rows);
    }

    private static long number(Object value, String method) throws Exception {
        return ((Number) value.getClass().getMethod(method).invoke(value)).longValue();
    }
    private static double threshold(Properties value, String key) {
        return Double.parseDouble(SmokeSupport.value(value, key));
    }
    private static double rate(M790Artifact value) {
        return (double) value.submittedMachines / value.frames;
    }
    private static double rebuildRate(M790Artifact value) {
        return (double) value.pageRebuilds / value.frames;
    }
    private static boolean within(double left, double right, double tolerance) {
        if (left == 0.0D || right == 0.0D) return left == right;
        double ratio = left / right;
        return ratio >= 1.0D - tolerance && ratio <= 1.0D + tolerance;
    }
    private static double ppm(long value, long samples) {
        return value * 1_000_000.0D / samples;
    }

    private static final class Totals {
        double baselineFps, reuseFps, baselineP50, reuseP50, baselineP95, reuseP95;
        double baselineP99, reuseP99, baselineAllocation, reuseAllocation;
        double baselineRender, reuseRender, baselineQueueAllocated, reuseQueueAllocated;

        void add(M790Pair pair) {
            baselineFps += pair.baseline().fps();
            reuseFps += pair.reuse().fps();
            baselineP50 += pair.baseline().p50();
            reuseP50 += pair.reuse().p50();
            baselineP95 += pair.baseline().p95();
            reuseP95 += pair.reuse().p95();
            baselineP99 += pair.baseline().p99();
            reuseP99 += pair.reuse().p99();
            baselineAllocation += pair.baseline().allocationPerFrame();
            reuseAllocation += pair.reuse().allocationPerFrame();
            baselineRender += pair.baseline().renderPerCall();
            reuseRender += pair.reuse().renderPerCall();
            baselineQueueAllocated += pair.baseline().queueAllocated;
            reuseQueueAllocated += pair.reuse().queueAllocated;
        }
    }
}
