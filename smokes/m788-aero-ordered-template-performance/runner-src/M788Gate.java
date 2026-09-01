import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

/** Qualifies Aero's safe template-memory Cell Page preset in four fresh GPU clients. */
final class M788Gate {
    private M788Gate() {}

    static M788Result evaluate(Path product, List<M788Pair> pairs,
                               List<M788Visual> visuals, Properties config) throws Exception {
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
        boolean visualPass = unexplained.isEmpty()
            && ppm(visuals.get(4).changedPixels(), pixelSamples) <= 10.0D
            && ppm(visuals.get(5).changedPixels(), pixelSamples) <= 10.0D && noisePpm <= 10.0D;
        double directFps = 0, templateFps = 0, directP50 = 0, templateP50 = 0;
        double directP95 = 0, templateP95 = 0, directP99 = 0, templateP99 = 0;
        double directAlloc = 0, templateAlloc = 0, directRender = 0, templateRender = 0;
        for (M788Pair pair : pairs) {
            directFps += pair.direct().fps();
            templateFps += pair.template().fps();
            directP50 += pair.direct().p50();
            templateP50 += pair.template().p50();
            directP95 += pair.direct().p95();
            templateP95 += pair.template().p95();
            directP99 += pair.direct().p99();
            templateP99 += pair.template().p99();
            directAlloc += pair.direct().allocationPerFrame();
            templateAlloc += pair.template().allocationPerFrame();
            directRender += pair.direct().renderPerCall();
            templateRender += pair.template().renderPerCall();
        }
        double fpsRatio = templateFps / directFps, p99Ratio = templateP99 / directP99;
        double allocationRatio = templateAlloc / directAlloc;
        double renderRatio = templateRender / directRender;
        double directWork = 0.0D, templateWork = 0.0D;
        double directRebuild = 0.0D, templateRebuild = 0.0D;
        double workTolerance = threshold(config, "maximum.workload.drift.ratio");
        boolean sameArmWork = true;
        for (int index = 1; index < pairs.size(); index++) {
            sameArmWork &= within(rate(pairs.get(0).direct()), rate(pairs.get(index).direct()), workTolerance)
                && within(rate(pairs.get(0).template()), rate(pairs.get(index).template()), workTolerance);
        }
        for (M788Pair pair : pairs) {
            directWork += rate(pair.direct());
            templateWork += rate(pair.template());
            directRebuild += rebuildRate(pair.direct());
            templateRebuild += rebuildRate(pair.template());
        }
        boolean workloadRepeatable = sameArmWork
            && within(directWork, templateWork, workTolerance)
            && templateRebuild <= directRebuild * 1.05D + 0.001D;
        boolean capturePass = pairs.stream().allMatch(pair ->
            pair.direct().blankCaptures <= threshold(config, "maximum.blank.capture.rejections")
                && pair.template().blankCaptures <= threshold(config, "maximum.blank.capture.rejections"));
        boolean directFirstPass = qualifies(pairs, true, config);
        boolean templateFirstPass = qualifies(pairs, false, config);
        int frameWins = 0;
        for (M788Pair pair : pairs) {
            if (pair.template().fps() >= pair.direct().fps()
                && pair.template().p99() <= pair.direct().p99()
                    * threshold(config, "p99.maximum.ratio")) {
                frameWins++;
            }
        }
        M788Hitch hitch = hitch(product, pairs, config);
        boolean performance = fpsRatio >= threshold(config, "fps.minimum.ratio")
            && p99Ratio <= threshold(config, "p99.maximum.ratio")
            && allocationRatio <= threshold(config, "allocation.maximum.ratio")
            && renderRatio <= threshold(config, "render.maximum.ratio")
            && directFirstPass && templateFirstPass
            && frameWins >= threshold(config, "minimum.frame.winning.pairs") && hitch.passes();
        return new M788Result(visualPass, workloadRepeatable, capturePass,
            noise.size(), noisePpm, unexplained.size(),
            directFps / pairs.size(), templateFps / pairs.size(),
            directP50 / pairs.size(), templateP50 / pairs.size(),
            directP95 / pairs.size(), templateP95 / pairs.size(),
            directP99 / pairs.size(), templateP99 / pairs.size(),
            fpsRatio, p99Ratio, allocationRatio, renderRatio, directFirstPass,
            templateFirstPass, frameWins, hitch, performance);
    }

    private static boolean qualifies(List<M788Pair> pairs, boolean directFirst, Properties config) {
        double directFps = 0, templateFps = 0, directP99 = 0, templateP99 = 0;
        double directAlloc = 0, templateAlloc = 0, directRender = 0, templateRender = 0;
        for (M788Pair pair : pairs) {
            if (pair.directFirst() != directFirst) {
                continue;
            }
            directFps += pair.direct().fps();
            templateFps += pair.template().fps();
            directP99 += pair.direct().p99();
            templateP99 += pair.template().p99();
            directAlloc += pair.direct().allocationPerFrame();
            templateAlloc += pair.template().allocationPerFrame();
            directRender += pair.direct().renderPerCall();
            templateRender += pair.template().renderPerCall();
        }
        return templateFps / directFps >= threshold(config, "fps.minimum.ratio")
            && templateP99 / directP99 <= threshold(config, "p99.maximum.ratio")
            && templateAlloc / directAlloc <= threshold(config, "allocation.maximum.ratio")
            && templateRender / directRender <= threshold(config, "render.maximum.ratio");
    }

    private static M788Hitch hitch(Path product, List<M788Pair> source,
                                   Properties config) throws Exception {
        try (URLClassLoader loader = new URLClassLoader(new URL[] {product.toUri().toURL()}, null)) {
            Class<?> census = Class.forName("worldline.profiling.FrameCensus", true, loader);
            Class<?> gate = Class.forName("worldline.profiling.HitchRateGate", true, loader);
            Method of = census.getMethod("of", String[].class, long[][].class);
            Method pairMethod = gate.getMethod("pair", census, census, boolean.class,
                String.class, long.class);
            List<Object> evidence = new ArrayList<Object>();
            long threshold = Long.parseLong(SmokeSupport.value(config, "hitch.threshold.nanos"));
            for (M788Pair pair : source) evidence.add(pairMethod.invoke(null,
                frame(of, pair.direct().walls), frame(of, pair.template().walls), pair.directFirst(),
                "frame.wall.nanos", threshold));
            Object value = gate.getMethod("evaluate", List.class, long.class).invoke(null,
                evidence, Long.parseLong(SmokeSupport.value(config, "allowed.regression.ppm")));
            return new M788Hitch(number(value, "baselineRatePpm"),
                number(value, "candidateRatePpm"), number(value, "aggregateDeltaPpm"),
                (Boolean) value.getClass().getMethod("passes").invoke(value),
                value.getClass().getMethod("verdict").invoke(value).toString());
        }
    }

    private static Object frame(Method of, long[] durations) throws Exception {
        long[][] rows = new long[durations.length][3];
        long time = 1L;
        for (int i = 0; i < rows.length; i++) {
            rows[i][0] = i;
            rows[i][1] = time;
            rows[i][2] = durations[i];
            time = Math.addExact(time, Math.max(1L, durations[i]));
        }
        return of.invoke(null, new String[] {"frame.wall.nanos"}, rows);
    }

    private static long number(Object value, String method) throws Exception {
        return ((Number) value.getClass().getMethod(method).invoke(value)).longValue();
    }
    private static double threshold(Properties p, String key) {
        return Double.parseDouble(SmokeSupport.value(p, key));
    }
    private static double rate(M788Artifact value) {
        return (double) value.submittedMachines / value.frames;
    }
    private static double rebuildRate(M788Artifact value) {
        return (double) value.pageRebuilds / value.frames;
    }
    private static boolean within(double left, double right, double tolerance) {
        if (left == 0.0D || right == 0.0D) return left == right;
        double ratio = left / right;
        return ratio >= 1.0D - tolerance && ratio <= 1.0D + tolerance;
    }
    private static double ppm(long value, long samples) { return value * 1_000_000.0D / samples; }
}
