import java.util.Locale;

record M788Result(boolean visualPass, boolean workloadRepeatable, boolean capturePass,
                  long noisePixels, double noisePpm,
                  long unexplainedPixels, double directFps, double templateFps,
                  double directP50, double templateP50, double directP95, double templateP95,
                  double directP99, double templateP99, double fpsRatio, double p99Ratio,
                  double allocationRatio, double renderRatio, boolean directFirstPass,
                  boolean templateFirstPass, int frameWins, M788Hitch hitch,
                  boolean performancePass) {
    boolean integrityPass() { return visualPass && workloadRepeatable && capturePass; }

    String decision() {
        if (performancePass) return "benefit-confirmed";
        return fpsRatio >= 0.97D && p99Ratio <= 1.10D && hitch.passes()
            ? "mixed-tradeoff" : "regression-detected";
    }

    String summary() {
        return "visual.pass=" + visualPass + ",workload.repeatable=" + workloadRepeatable
            + ",capture.pass=" + capturePass
            + ",visual.noise.locations=" + noisePixels
            + ",visual.noise.ppm=" + String.format(Locale.ROOT, "%.3f", noisePpm)
            + ",visual.unexplained.locations=" + unexplainedPixels
            + ",fps.direct/template=" + format(directFps) + "/" + format(templateFps)
            + ",p50.direct/template.ms=" + millis(directP50) + "/" + millis(templateP50)
            + ",p95.direct/template.ms=" + millis(directP95) + "/" + millis(templateP95)
            + ",p99.direct/template.ms=" + millis(directP99) + "/" + millis(templateP99)
            + ",fps.ratio=" + format(fpsRatio) + ",p99.ratio=" + format(p99Ratio)
            + ",allocation.ratio=" + format(allocationRatio)
            + ",render.ratio=" + format(renderRatio)
            + ",order.direct-first.pass=" + directFirstPass
            + ",order.template-first.pass=" + templateFirstPass + ",frame.wins=" + frameWins
            + ",hitch.direct.ppm=" + hitch.directPpm()
            + ",hitch.template.ppm=" + hitch.templatePpm()
            + ",hitch.delta.ppm=" + hitch.deltaPpm() + ",hitch.verdict=" + hitch.verdict()
            + ",performance.pass=" + performancePass + ",decision=" + decision();
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.3f", value);
    }

    private static String millis(double nanos) { return format(nanos / 1_000_000.0D); }
}
