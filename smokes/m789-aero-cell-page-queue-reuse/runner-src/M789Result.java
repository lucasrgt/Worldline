import java.util.Locale;

record M789Result(boolean visualPass, boolean workloadRepeatable, boolean capturePass,
                  long noisePixels, double noisePpm, long unexplainedPixels,
                  double baselineFps, double reuseFps,
                  double baselineP50, double reuseP50,
                  double baselineP95, double reuseP95,
                  double baselineP99, double reuseP99,
                  double fpsRatio, double p99Ratio, double allocationRatio,
                  double renderRatio, double queueAllocationRatio,
                  boolean baselineFirstPass, boolean reuseFirstPass,
                  int allocationWins, M789Hitch hitch, boolean performancePass) {
    boolean integrityPass() { return visualPass && workloadRepeatable && capturePass; }

    String decision() {
        if (performancePass) return "benefit-confirmed";
        return fpsRatio >= 0.97D && p99Ratio <= 1.10D && hitch.passes()
            ? "mixed-tradeoff" : "regression-detected";
    }

    String summary() {
        return "visual.pass=" + visualPass + ",workload.repeatable=" + workloadRepeatable
            + ",capture.pass=" + capturePass + ",visual.noise.locations=" + noisePixels
            + ",visual.noise.ppm=" + format(noisePpm)
            + ",visual.unexplained.locations=" + unexplainedPixels
            + ",fps.baseline/reuse=" + format(baselineFps) + "/" + format(reuseFps)
            + ",p50.baseline/reuse.ms=" + millis(baselineP50) + "/" + millis(reuseP50)
            + ",p95.baseline/reuse.ms=" + millis(baselineP95) + "/" + millis(reuseP95)
            + ",p99.baseline/reuse.ms=" + millis(baselineP99) + "/" + millis(reuseP99)
            + ",fps.ratio=" + format(fpsRatio) + ",p99.ratio=" + format(p99Ratio)
            + ",allocation.ratio=" + format(allocationRatio)
            + ",render.ratio=" + format(renderRatio)
            + ",queue.allocation.ratio=" + format(queueAllocationRatio)
            + ",order.baseline-first.pass=" + baselineFirstPass
            + ",order.reuse-first.pass=" + reuseFirstPass
            + ",allocation.wins=" + allocationWins
            + ",hitch.baseline.ppm=" + hitch.baselinePpm()
            + ",hitch.reuse.ppm=" + hitch.reusePpm()
            + ",hitch.delta.ppm=" + hitch.deltaPpm() + ",hitch.verdict=" + hitch.verdict()
            + ",performance.pass=" + performancePass + ",decision=" + decision();
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.3f", value);
    }

    private static String millis(double nanos) { return format(nanos / 1_000_000.0D); }
}
