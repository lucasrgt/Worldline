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

/** Qualifies Aero's high-memory flattened Cell Pages in four fresh GPU clients. */
public final class M784AeroHighMemoryCellPagesCycle {
    private static final String ID = "m784-aero-high-memory-cell-pages";
    private static final String[] RUNS = {"round1-normal", "round1-high",
        "round2-high", "round2-normal", "round3-high", "round3-normal",
        "round4-normal", "round4-high"};
    private static final String[] ARMS = {"normal", "high", "high", "normal",
        "high", "normal", "normal", "high"};
    private static final String TRACE = "v1|scene=576-static-four-towers-four-chunks+fixed-membership-brightness|"
        + "jvms=8-fresh-four-counterbalanced-pairs|route=1200-min20s-orbit+traverse+spin+teleport|"
        + "warm=480-route-frames+pages-stable|memory=normal-vs-high|"
        + "cell-pages=min1+flatten-off-vs-on+ttl600-vs1800+budget8-vs16+cap-unbounded-vs4096|"
        + "submission=one-controlled-production-submit+flush-per-frame|"
        + "captures=24-full-rgba-isolated-fixture+blank-retry<=24|"
        + "metrics=wall+p99+allocation+heap+flush+pages+display-lists+prewarm+hitches|"
        + "oracle=no-unexplained-pixels+activation+stable-cache+workdrift<=0.10+guardrails+"
        + "both-order-strata+frame-wins>=3+fps>=1.03+p99<=1.05+alloc<=1.05+flush<=0.90+"
        + "hitch<=5000ppm|decision=promote-opt-in-or-keep-candidate";
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID);
    private final Path build = root.resolve(".worldline/build/smoke").resolve(ID);
    private final Properties config = new Properties();

    private M784AeroHighMemoryCellPagesCycle() {}

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: M784AeroHighMemoryCellPagesCycle " + ID);
            System.exit(2);
        }
        try { new M784AeroHighMemoryCellPagesCycle().execute(); }
        catch (Exception error) {
            System.err.println("M784 high-memory Cell Pages failed: " + error.getMessage());
            error.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        SmokeSupport.load(smoke.resolve("smoke.properties"), config);
        verifyDesign();
        Path aero = root.resolve(SmokeSupport.value(config, "aero.path")).normalize();
        M784Runtime runtime = new M784Runtime(smoke, config, aero);
        runtime.verifyCheckout();
        SmokeSupport.recreate(root, build);
        runtime.buildAero();
        Path template = build.resolve("template");
        runtime.runClient(template, true, "prepare");
        Path sourceWorld = template.resolve("saves/WorldlineAeroHigh");
        SmokeSupport.require(Files.isDirectory(sourceWorld), "M784 template world absent");
        List<M784Artifact> artifacts = new ArrayList<M784Artifact>();
        for (int index = 0; index < RUNS.length; index++) {
            Path game = build.resolve(RUNS[index]);
            M784Runtime.copyWorld(sourceWorld, game.resolve("saves/WorldlineAeroHigh"));
            runtime.runClient(game, false, ARMS[index]);
            M784Artifact artifact = M784Artifact.read(game, ARMS[index]);
            artifact.verify();
            artifacts.add(artifact);
        }
        List<M784Pair> pairs = List.of(
            new M784Pair(1, true, artifacts.get(0), artifacts.get(1)),
            new M784Pair(2, false, artifacts.get(3), artifacts.get(2)),
            new M784Pair(3, false, artifacts.get(5), artifacts.get(4)),
            new M784Pair(4, true, artifacts.get(6), artifacts.get(7)));
        List<M784Visual> visuals = List.of(
            M784Visual.compare("round.1", artifacts.get(0), artifacts.get(1)),
            M784Visual.compare("round.2", artifacts.get(3), artifacts.get(2)),
            M784Visual.compare("round.3", artifacts.get(5), artifacts.get(4)),
            M784Visual.compare("round.4", artifacts.get(6), artifacts.get(7)),
            M784Visual.compare("repeat.normal", artifacts.get(0), artifacts.get(6)),
            M784Visual.compare("repeat.high", artifacts.get(1), artifacts.get(7)));
        M784Result result = M784Gate.evaluate(
            SmokeSupport.product(root, "profiling"), pairs, visuals, config);
        SmokeSupport.require(result.integrityPass(),
            "M784 correctness/guardrail gate failed: " + result.summary());
        String signature = M784Runtime.sha256(TRACE);
        SmokeSupport.require(signature.equals(SmokeSupport.value(config, "expected.signature")),
            "M784 semantic signature drift: " + signature);
        StringBuilder evidence = new StringBuilder("id=").append(ID).append('\n');
        for (M784Pair pair : pairs) evidence.append(pair.summary()).append('\n');
        for (M784Visual visual : visuals) evidence.append(visual.summary()).append('\n');
        evidence.append(result.summary()).append("\ntrace=").append(TRACE)
            .append("\nsignature=").append(signature).append('\n');
        Files.writeString(build.resolve("evidence.txt"), evidence, StandardCharsets.UTF_8);
        System.out.println("M784 Aero high-memory Cell Pages classification passed");
        System.out.println("WORLDLINE_M784_DECISION=" + result.decision());
        System.out.println("WORLDLINE_M784_SIGNAL=" + SmokeSupport.value(config, "expected.signal"));
        System.out.println("WORLDLINE_M784_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M784_SIGNATURE=" + signature);
    }

    private void verifyDesign() {
        SmokeSupport.require(SmokeSupport.value(config, "sessions").equals("8")
            && SmokeSupport.value(config, "retained.frames").equals("1200")
            && SmokeSupport.value(config, "minimum.millis").equals("20000")
            && SmokeSupport.value(config, "warm.frames").equals("480")
            && SmokeSupport.value(config, "checkpoints").equals("24")
            && SmokeSupport.value(config, "machines").equals("576")
            && SmokeSupport.value(config, "maximum.workload.drift.ratio").equals("0.10")
            && SmokeSupport.value(config, "maximum.blank.capture.rejections").equals("24")
            && SmokeSupport.value(config, "minimum.frame.winning.pairs").equals("3"),
            "M784 acquisition design drift");
    }
}

final class M784Runtime {
    private final Path smoke, aero;
    private final Properties config;

    M784Runtime(Path smoke, Properties config, Path aero) {
        this.smoke = smoke; this.config = config; this.aero = aero;
    }

    void verifyCheckout() throws Exception {
        SmokeSupport.require(Files.exists(aero.resolve(".git")), "M784 Aero checkout absent");
        SmokeSupport.require(git("rev-parse", "HEAD").trim().equals(
            SmokeSupport.value(config, "aero.revision")), "M784 Aero revision drift");
        SmokeSupport.require(git("status", "--porcelain").isBlank(), "M784 Aero checkout dirty");
        String origin = git("remote", "get-url", "origin").trim().replace("\\", "/");
        SmokeSupport.require(origin.equalsIgnoreCase(SmokeSupport.value(config, "aero.repository")),
            "M784 Aero origin drift: " + origin);
    }

    void buildAero() throws Exception {
        Path project = aero.resolve("stationapi");
        String output = SmokeSupport.capture(project, List.of(wrapper(project),
            "--no-daemon", "remapJar", "--rerun-tasks"), timeout("build.timeout.seconds"));
        SmokeSupport.require(output.contains("BUILD SUCCESSFUL") && Files.isRegularFile(
            project.resolve("build/libs/aero-model-lib-3.0.0.jar")), "M784 Aero build drift");
    }

    void runClient(Path game, boolean prepare, String arm) throws Exception {
        Files.createDirectories(game);
        Path project = aero.resolve("stationapi/test");
        List<String> command = List.of(wrapper(project), "--no-daemon", "runClient",
            "--init-script", smoke.resolve("high-memory.init.gradle").toString(),
            "-PworldlineAeroClasses=" + aero.resolve("stationapi/build/classes/java/main"),
            "-PworldlineAeroJar=" + aero.resolve("stationapi/build/libs/aero-model-lib-3.0.0.jar"),
            "-PworldlineRunDir=" + game, "-PworldlinePrepare=" + prepare,
            "-PworldlineArm=" + arm,
            "-PworldlineFrames=" + SmokeSupport.value(config, "retained.frames"),
            "-PworldlineMinimumMillis=" + SmokeSupport.value(config, "minimum.millis"),
            "-PworldlineWarmFrames=" + SmokeSupport.value(config, "warm.frames"),
            "-PworldlineMetrics=" + game.resolve("metrics.properties"),
            "-PworldlineFramesFile=" + game.resolve("frames.csv"),
            "-PworldlineFramesDir=" + game.resolve("visual-frames"));
        System.out.println("[M784] start prepare=" + prepare + " arm=" + arm);
        String output = SmokeSupport.capture(project, command, timeout("child.timeout.seconds"));
        SmokeSupport.require(output.contains("[WorldlineM784] start prepare=" + prepare
            + " arm=" + arm) && output.contains("BUILD SUCCESSFUL"),
            "M784 client lifecycle drift: " + arm + "\n" + output);
        String expected = prepare ? "template-ready machines=576"
            : "capture-complete arm=" + arm;
        SmokeSupport.require(output.contains("[WorldlineM784] " + expected),
            "M784 completion drift: " + arm + "\n" + output);
    }

    static void copyWorld(Path source, Path destination) throws IOException {
        Files.createDirectories(destination);
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(source)) {
            for (Path path : paths) {
                Path target = destination.resolve(path.getFileName());
                if (Files.isDirectory(path)) copyWorld(path, target);
                else Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.COPY_ATTRIBUTES);
            }
        }
    }

    static String sha256(String value) throws Exception {
        return sha256Bytes(value.getBytes(StandardCharsets.UTF_8));
    }

    static String sha256Bytes(byte[] value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value));
    }

    private String git(String... arguments) throws Exception {
        ArrayList<String> command = new ArrayList<String>();
        command.add("git"); command.addAll(List.of(arguments));
        return SmokeSupport.capture(aero, command, 60);
    }

    private int timeout(String key) { return Integer.parseInt(SmokeSupport.value(config, key)); }
    private static String wrapper(Path project) {
        return project.resolve(System.getProperty("os.name").startsWith("Windows")
            ? "gradlew.bat" : "gradlew").toString();
    }
}

final class M784Artifact {
    final Path game;
    final String arm;
    final int machines, frames, captures, blankCaptures, width, height;
    final int pageCalls, pageRebuilds, directCalls, cachedMax, compiled, expired, evicted;
    final int ttl, rebuildBudget, cacheMax, prewarmPending, displayDenied, displayFailed;
    final int displayLive, displayPeak, displayAllocated, displayMax;
    final long allocatedBytes, heapPeak, heapFinal, flushCalls, flushNanos;
    final boolean flattened, prewarmEnabled;
    final long[] walls, allocations;
    final String[] hashes;

    private M784Artifact(Path game, Properties p, long[][] rows) {
        this.game = game;
        arm = required(p, "arm"); machines = integer(p, "machines");
        frames = integer(p, "frames"); captures = integer(p, "captures");
        blankCaptures = integer(p, "captures.blank.rejected");
        width = integer(p, "width"); height = integer(p, "height");
        allocatedBytes = number(p, "frame.allocated.bytes");
        heapPeak = number(p, "heap.peak.bytes"); heapFinal = number(p, "heap.final.bytes");
        pageCalls = integer(p, "page.calls"); pageRebuilds = integer(p, "page.rebuilds");
        directCalls = integer(p, "page.direct"); cachedMax = integer(p, "page.cached.max");
        compiled = integer(p, "page.compiled"); expired = integer(p, "page.expired");
        evicted = integer(p, "page.evicted"); flattened = bool(p, "page.flattened");
        ttl = integer(p, "page.ttl.frames"); rebuildBudget = integer(p, "page.rebuild.budget");
        cacheMax = integer(p, "page.cache.max"); flushCalls = number(p, "flush.calls");
        flushNanos = number(p, "flush.nanos"); prewarmEnabled = bool(p, "prewarm.enabled");
        prewarmPending = integer(p, "prewarm.pending");
        displayLive = integer(p, "display.live"); displayPeak = integer(p, "display.peak");
        displayAllocated = integer(p, "display.allocated");
        displayDenied = integer(p, "display.denied"); displayFailed = integer(p, "display.failed");
        displayMax = integer(p, "display.max");
        walls = rows[0]; allocations = rows[1];
        hashes = new String[captures];
        for (int i = 0; i < captures; i++) hashes[i] = required(p, "checkpoint." + i + ".sha256");
    }

    static M784Artifact read(Path game, String expected) throws Exception {
        Properties p = new Properties();
        try (Reader reader = Files.newBufferedReader(game.resolve("metrics.properties"))) {
            p.load(reader);
        }
        List<String> lines = Files.readAllLines(game.resolve("frames.csv"));
        long[][] rows = new long[2][lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            String[] columns = lines.get(i).split(",", -1);
            SmokeSupport.require(columns.length == 2, "M784 frame row drift");
            rows[0][i] = Long.parseLong(columns[0]); rows[1][i] = Long.parseLong(columns[1]);
        }
        M784Artifact value = new M784Artifact(game, p, rows);
        SmokeSupport.require(value.arm.equals(expected) && value.frames == lines.size(),
            "M784 artifact identity drift: " + expected);
        return value;
    }

    void verify() throws Exception {
        boolean high = arm.equals("high");
        SmokeSupport.require(machines == 576 && frames >= 1200 && captures == 24
            && width > 0 && height > 0 && allocatedBytes > 0L && heapPeak > 0L
            && pageCalls > 0 && directCalls <= frames && cachedMax > 0
            && flushCalls > 0L && flushNanos > 0L,
            "M784 incomplete artifact: " + summary());
        SmokeSupport.require(flattened == high && ttl == (high ? 1800 : 600)
            && rebuildBudget == (high ? 16 : 8) && cacheMax == (high ? 4096 : -1)
            && prewarmEnabled == high, "M784 preset activation drift: " + summary());
        SmokeSupport.require(pageRebuilds <= frames * 2 && compiled == pageRebuilds
            && expired <= compiled && evicted == 0 && prewarmPending == 0
            && displayDenied == 0 && displayFailed == 0
            && (displayMax < 0 || displayPeak <= displayMax),
            "M784 cache/display-list guardrail drift: " + summary());
        for (int i = 0; i < captures; i++) {
            byte[] pixels = pixels(i);
            SmokeSupport.require(pixels.length == width * height * 4
                && M784Runtime.sha256Bytes(pixels).equals(hashes[i]),
                "M784 framebuffer artifact drift: " + i);
        }
    }

    byte[] pixels(int checkpoint) throws IOException {
        return Files.readAllBytes(game.resolve("visual-frames").resolve(arm)
            .resolve(String.format("checkpoint-%02d.rgba", checkpoint)));
    }

    double fps() { return walls.length * 1_000_000_000.0D / sum(walls); }
    long p99() {
        long[] values = walls.clone(); Arrays.sort(values);
        return values[Math.min(values.length - 1, (int) Math.ceil(values.length * 0.99D) - 1)];
    }
    double allocationPerFrame() { return (double) allocatedBytes / frames; }
    double flushPerCall() { return (double) flushNanos / flushCalls; }
    String summary() {
        return arm + ":frames=" + frames + ",fps=" + fmt(fps()) + ",p99.ms="
            + fmt(p99() / 1_000_000.0D) + ",alloc/frame=" + fmt(allocationPerFrame())
            + ",heap.peak.mb=" + fmt(heapPeak / 1048576.0D) + ",flush.us="
            + fmt(flushPerCall() / 1000.0D) + ",blank=" + blankCaptures + ",pages=" + pageCalls + "/"
            + pageRebuilds + "/" + directCalls + "/" + cachedMax + ",lists="
            + displayLive + "/" + displayPeak + "/" + displayAllocated;
    }

    private static long sum(long[] values) { long total = 0L; for (long v : values) total += v; return total; }
    private static String fmt(double v) { return String.format(Locale.ROOT, "%.2f", v); }
    private static boolean bool(Properties p, String k) { return Boolean.parseBoolean(required(p, k)); }
    private static int integer(Properties p, String k) { return Integer.parseInt(required(p, k)); }
    private static long number(Properties p, String k) { return Long.parseLong(required(p, k)); }
    private static String required(Properties p, String k) {
        String value = p.getProperty(k);
        if (value == null || value.isBlank()) throw new IllegalStateException("missing M784 " + k);
        return value.trim();
    }
}

record M784Pair(int index, boolean normalFirst, M784Artifact normal, M784Artifact high) {
    String summary() { return "pair." + index + ".normalFirst=" + normalFirst + ","
        + normal.summary() + "," + high.summary(); }
}

record M784Visual(String label, M784Artifact baseline, M784Artifact candidate,
                  long changedPixels, int maximumDelta, Set<Long> changedLocations) {
    static M784Visual compare(String label, M784Artifact baseline,
                              M784Artifact candidate) throws Exception {
        SmokeSupport.require(baseline.width == candidate.width && baseline.height == candidate.height
            && baseline.captures == candidate.captures, "M784 framebuffer shape diverged");
        long changed = 0L; int maximum = 0; Set<Long> locations = new HashSet<Long>();
        long framePixels = (long) baseline.width * baseline.height;
        for (int checkpoint = 0; checkpoint < baseline.captures; checkpoint++) {
            byte[] left = baseline.pixels(checkpoint), right = candidate.pixels(checkpoint);
            for (int pixel = 0; pixel < left.length; pixel += 4) {
                boolean differs = false;
                for (int channel = 0; channel < 4; channel++) {
                    int delta = Math.abs((left[pixel + channel] & 255) - (right[pixel + channel] & 255));
                    maximum = Math.max(maximum, delta); differs |= delta != 0;
                }
                if (differs) { changed++; locations.add(checkpoint * framePixels + pixel / 4); }
            }
        }
        return new M784Visual(label, baseline, candidate, changed, maximum, locations);
    }

    String summary() { return "visual." + label + ",changedPixels=" + changedPixels
        + ",maxDelta=" + maximumDelta; }
}

final class M784Gate {
    private M784Gate() {}

    static M784Result evaluate(Path product, List<M784Pair> pairs,
                               List<M784Visual> visuals, Properties config) throws Exception {
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
        double normalFps = 0, highFps = 0, normalP99 = 0, highP99 = 0;
        double normalAlloc = 0, highAlloc = 0, normalFlush = 0, highFlush = 0;
        for (M784Pair pair : pairs) {
            normalFps += pair.normal().fps(); highFps += pair.high().fps();
            normalP99 += pair.normal().p99(); highP99 += pair.high().p99();
            normalAlloc += pair.normal().allocationPerFrame(); highAlloc += pair.high().allocationPerFrame();
            normalFlush += pair.normal().flushPerCall(); highFlush += pair.high().flushPerCall();
        }
        double fpsRatio = highFps / normalFps, p99Ratio = highP99 / normalP99;
        double allocationRatio = highAlloc / normalAlloc, flushRatio = highFlush / normalFlush;
        double normalWork = 0.0D, highWork = 0.0D;
        double normalRebuild = 0.0D, highRebuild = 0.0D;
        double workTolerance = threshold(config, "maximum.workload.drift.ratio");
        boolean sameArmWork = true;
        for (int index = 1; index < pairs.size(); index++) {
            sameArmWork &= within(rate(pairs.get(0).normal()), rate(pairs.get(index).normal()), workTolerance)
                && within(rate(pairs.get(0).high()), rate(pairs.get(index).high()), workTolerance);
        }
        for (M784Pair pair : pairs) {
            normalWork += rate(pair.normal()); highWork += rate(pair.high());
            normalRebuild += rebuildRate(pair.normal()); highRebuild += rebuildRate(pair.high());
        }
        boolean workloadRepeatable = sameArmWork
            && within(normalWork, highWork, workTolerance)
            && highRebuild <= normalRebuild * 1.05D + 0.001D;
        boolean capturePass = pairs.stream().allMatch(pair ->
            pair.normal().blankCaptures <= threshold(config, "maximum.blank.capture.rejections")
                && pair.high().blankCaptures <= threshold(config, "maximum.blank.capture.rejections"));
        boolean normalFirstPass = qualifies(pairs, true, config);
        boolean highFirstPass = qualifies(pairs, false, config);
        int frameWins = 0;
        for (M784Pair pair : pairs) {
            if (pair.high().fps() >= pair.normal().fps()
                && pair.high().p99() <= pair.normal().p99()
                    * threshold(config, "p99.maximum.ratio")) {
                frameWins++;
            }
        }
        M784Hitch hitch = hitch(product, pairs, config);
        boolean performance = fpsRatio >= threshold(config, "fps.minimum.ratio")
            && p99Ratio <= threshold(config, "p99.maximum.ratio")
            && allocationRatio <= threshold(config, "allocation.maximum.ratio")
            && flushRatio <= threshold(config, "flush.maximum.ratio")
            && normalFirstPass && highFirstPass
            && frameWins >= threshold(config, "minimum.frame.winning.pairs") && hitch.passes();
        return new M784Result(visualPass, workloadRepeatable, capturePass,
            noise.size(), noisePpm, unexplained.size(),
            fpsRatio, p99Ratio, allocationRatio, flushRatio, normalFirstPass,
            highFirstPass, frameWins, hitch, performance);
    }

    private static boolean qualifies(List<M784Pair> pairs, boolean normalFirst, Properties config) {
        double normalFps = 0, highFps = 0, normalP99 = 0, highP99 = 0;
        double normalAlloc = 0, highAlloc = 0, normalFlush = 0, highFlush = 0;
        for (M784Pair pair : pairs) {
            if (pair.normalFirst() != normalFirst) {
                continue;
            }
            normalFps += pair.normal().fps();
            highFps += pair.high().fps();
            normalP99 += pair.normal().p99();
            highP99 += pair.high().p99();
            normalAlloc += pair.normal().allocationPerFrame();
            highAlloc += pair.high().allocationPerFrame();
            normalFlush += pair.normal().flushPerCall();
            highFlush += pair.high().flushPerCall();
        }
        return highFps / normalFps >= threshold(config, "fps.minimum.ratio")
            && highP99 / normalP99 <= threshold(config, "p99.maximum.ratio")
            && highAlloc / normalAlloc <= threshold(config, "allocation.maximum.ratio")
            && highFlush / normalFlush <= threshold(config, "flush.maximum.ratio");
    }

    private static M784Hitch hitch(Path product, List<M784Pair> source,
                                   Properties config) throws Exception {
        try (URLClassLoader loader = new URLClassLoader(new URL[] {product.toUri().toURL()}, null)) {
            Class<?> census = Class.forName("worldline.profiling.FrameCensus", true, loader);
            Class<?> gate = Class.forName("worldline.profiling.HitchRateGate", true, loader);
            Method of = census.getMethod("of", String[].class, long[][].class);
            Method pairMethod = gate.getMethod("pair", census, census, boolean.class,
                String.class, long.class);
            List<Object> evidence = new ArrayList<Object>();
            long threshold = Long.parseLong(SmokeSupport.value(config, "hitch.threshold.nanos"));
            for (M784Pair pair : source) evidence.add(pairMethod.invoke(null,
                frame(of, pair.normal().walls), frame(of, pair.high().walls), pair.normalFirst(),
                "frame.wall.nanos", threshold));
            Object value = gate.getMethod("evaluate", List.class, long.class).invoke(null,
                evidence, Long.parseLong(SmokeSupport.value(config, "allowed.regression.ppm")));
            return new M784Hitch(number(value, "baselineRatePpm"),
                number(value, "candidateRatePpm"), number(value, "aggregateDeltaPpm"),
                (Boolean) value.getClass().getMethod("passes").invoke(value),
                value.getClass().getMethod("verdict").invoke(value).toString());
        }
    }

    private static Object frame(Method of, long[] durations) throws Exception {
        long[][] rows = new long[durations.length][3]; long time = 1L;
        for (int i = 0; i < rows.length; i++) {
            rows[i][0] = i; rows[i][1] = time; rows[i][2] = durations[i];
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
    private static double rate(M784Artifact value) {
        return (double) value.pageCalls / value.frames;
    }
    private static double rebuildRate(M784Artifact value) {
        return (double) value.pageRebuilds / value.frames;
    }
    private static boolean within(double left, double right, double tolerance) {
        if (left == 0.0D || right == 0.0D) return left == right;
        double ratio = left / right;
        return ratio >= 1.0D - tolerance && ratio <= 1.0D + tolerance;
    }
    private static double ppm(long value, long samples) { return value * 1_000_000.0D / samples; }
}

record M784Hitch(long normalPpm, long highPpm, long deltaPpm,
                 boolean passes, String verdict) {}

record M784Result(boolean visualPass, boolean workloadRepeatable, boolean capturePass,
                  long noisePixels, double noisePpm,
                  long unexplainedPixels, double fpsRatio, double p99Ratio,
                  double allocationRatio, double flushRatio, boolean normalFirstPass,
                  boolean highFirstPass, int frameWins, M784Hitch hitch,
                  boolean performancePass) {
    boolean integrityPass() { return visualPass && workloadRepeatable && capturePass; }
    String decision() { return performancePass ? "promote-opt-in" : "keep-candidate"; }
    String summary() {
        return "visual.pass=" + visualPass + ",workload.repeatable=" + workloadRepeatable
            + ",capture.pass=" + capturePass
            + ",visual.noise.locations=" + noisePixels
            + ",visual.noise.ppm=" + String.format(Locale.ROOT, "%.3f", noisePpm)
            + ",visual.unexplained.locations=" + unexplainedPixels
            + ",fps.ratio=" + format(fpsRatio) + ",p99.ratio=" + format(p99Ratio)
            + ",allocation.ratio=" + format(allocationRatio) + ",flush.ratio=" + format(flushRatio)
            + ",order.normal-first.pass=" + normalFirstPass
            + ",order.high-first.pass=" + highFirstPass + ",frame.wins=" + frameWins
            + ",hitch.normal.ppm=" + hitch.normalPpm() + ",hitch.high.ppm=" + hitch.highPpm()
            + ",hitch.delta.ppm=" + hitch.deltaPpm() + ",hitch.verdict=" + hitch.verdict()
            + ",performance.pass=" + performancePass + ",decision=" + decision();
    }
    private static String format(double value) { return String.format(Locale.ROOT, "%.3f", value); }
}
