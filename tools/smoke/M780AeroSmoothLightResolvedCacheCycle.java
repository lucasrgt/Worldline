import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

/** Qualifies Aero's resolved smooth-light cache in four fresh GPU clients. */
public final class M780AeroSmoothLightResolvedCacheCycle {
    private static final String ID = "m780-aero-smooth-light-resolved-cache";
    private static final String[] RUNS = {"round1-off", "round1-on", "round2-on", "round2-off"};
    private static final String[] ARMS = {"cache-off", "cache-on", "cache-on", "cache-off"};
    private static final String TRACE = "v4|scene=128-dense-smooth-grid-2048tri+four-panels+isolated-buffer|"
        + "jvms=4-fresh-abba-off+on+on+off|route=240-orbit+traverse+spin+teleport|"
        + "warm=480-route-frames|light=synthetic-grid+phase-change+100ms-convergence|"
        + "cache=immutable-startup+ttl50ms+lru1024+native-hit-miss-cold-stale-eviction-counters|"
        + "captures=24-route+2-light-diagnostics-per-jvm|world=frozen+clear-weather+no-clouds|"
        + "oracle=full-rgba+same-arm-noise10ppm+no-unexplained+light-change+sample-reduction+"
        + "render-time-reduction+hitch-census";
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID);
    private final Path build = root.resolve(".worldline/build/smoke").resolve(ID);
    private final Properties config = new Properties();

    private M780AeroSmoothLightResolvedCacheCycle() {}

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: M780AeroSmoothLightResolvedCacheCycle " + ID);
            System.exit(2);
        }
        try {
            new M780AeroSmoothLightResolvedCacheCycle().execute();
        } catch (Exception error) {
            System.err.println("M780 smooth-light cache failed: " + error.getMessage());
            error.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        SmokeSupport.load(smoke.resolve("smoke.properties"), config);
        verifyDesign();
        Path aero = root.resolve(SmokeSupport.value(config, "aero.path")).normalize();
        M780Runtime runtime = new M780Runtime(smoke, config, aero);
        runtime.verifyCheckout();
        SmokeSupport.recreate(root, build);
        runtime.buildAero();
        Path template = build.resolve("template");
        runtime.runClient(template, true, "prepare");
        Path sourceWorld = template.resolve("saves/WorldlineAeroLight");
        SmokeSupport.require(Files.isDirectory(sourceWorld), "M780 template world absent");
        List<M780Artifact> artifacts = new ArrayList<M780Artifact>();
        for (int index = 0; index < RUNS.length; index++) {
            String arm = ARMS[index];
            Path game = build.resolve(RUNS[index]);
            M780Runtime.copyWorld(sourceWorld, game.resolve("saves/WorldlineAeroLight"));
            runtime.runClient(game, false, arm);
            M780Artifact artifact = M780Artifact.read(game, arm);
            artifact.verify();
            artifacts.add(artifact);
        }
        List<M780Session> sessions = List.of(
            new M780Session(1, artifacts.get(0), artifacts.get(1)),
            new M780Session(2, artifacts.get(3), artifacts.get(2)));
        List<M780VisualPair> pairs = List.of(
            M780VisualPair.compare("round.1", artifacts.get(0), artifacts.get(1)),
            M780VisualPair.compare("round.2", artifacts.get(3), artifacts.get(2)),
            M780VisualPair.compare("repeat.off", artifacts.get(0), artifacts.get(3)),
            M780VisualPair.compare("repeat.on", artifacts.get(1), artifacts.get(2)));
        M780Result result = M780Result.evaluate(sessions, pairs);
        SmokeSupport.require(result.passes(), "M780 classification gate failed: " + result.summary());
        String signature = M780Runtime.sha256(TRACE.getBytes(StandardCharsets.UTF_8));
        SmokeSupport.require(signature.equals(SmokeSupport.value(config, "expected.signature")),
            "M780 semantic signature drift: " + signature);
        StringBuilder evidence = new StringBuilder("id=").append(ID).append('\n');
        for (M780Session session : sessions) evidence.append(session.summary()).append('\n');
        for (M780VisualPair pair : pairs) evidence.append(pair.summary()).append('\n');
        evidence.append(result.summary()).append("\ntrace=").append(TRACE)
            .append("\nsignature=").append(signature).append('\n');
        Files.writeString(build.resolve("evidence.txt"), evidence, StandardCharsets.UTF_8);
        System.out.println("M780 Aero smooth-light resolved cache passed");
        System.out.println("WORLDLINE_M780_SIGNAL=" + SmokeSupport.value(config, "expected.signal"));
        System.out.println("WORLDLINE_M780_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M780_SIGNATURE=" + signature);
    }

    private void verifyDesign() {
        SmokeSupport.require(SmokeSupport.value(config, "sessions").equals("4")
            && SmokeSupport.value(config, "route.frames").equals("240")
            && SmokeSupport.value(config, "checkpoints").equals("24")
            && SmokeSupport.value(config, "machines").equals("128")
            && SmokeSupport.value(config, "cache.ttl.ms").equals("50")
            && SmokeSupport.value(config, "maximum.raster.noise.ppm").equals("10")
            && SmokeSupport.value(config, "maximum.sample.ratio").equals("0.70")
            && SmokeSupport.value(config, "promotion.render.ratio").equals("0.95"),
            "M780 acquisition design drift");
    }
}

final class M780Runtime {
    private final Path smoke, aero;
    private final Properties config;

    M780Runtime(Path smoke, Properties config, Path aero) {
        this.smoke = smoke;
        this.config = config;
        this.aero = aero;
    }

    void verifyCheckout() throws Exception {
        SmokeSupport.require(Files.exists(aero.resolve(".git")), "M780 Aero checkout absent");
        SmokeSupport.require(git("rev-parse", "HEAD").trim().equals(
            SmokeSupport.value(config, "aero.revision")), "M780 Aero revision drift");
        SmokeSupport.require(git("status", "--porcelain").isBlank(), "M780 Aero checkout dirty");
        String origin = git("remote", "get-url", "origin").trim().replace("\\", "/");
        SmokeSupport.require(origin.equalsIgnoreCase(SmokeSupport.value(config, "aero.repository")),
            "M780 Aero origin drift: " + origin);
    }

    void buildAero() throws Exception {
        Path project = aero.resolve("stationapi");
        String output = SmokeSupport.capture(project, List.of(wrapper(project),
            "--no-daemon", "remapJar", "--rerun-tasks"),
            Integer.parseInt(SmokeSupport.value(config, "build.timeout.seconds")));
        SmokeSupport.require(output.contains("BUILD SUCCESSFUL") && Files.isRegularFile(
            project.resolve("build/libs/aero-model-lib-3.0.0.jar")), "M780 Aero build drift");
    }

    void runClient(Path game, boolean prepare, String arm) throws Exception {
        Files.createDirectories(game);
        Path project = aero.resolve("stationapi/test");
        List<String> command = List.of(wrapper(project), "--no-daemon", "runClient",
            "--init-script", smoke.resolve("smooth-light.init.gradle").toString(),
            "-PworldlineAeroClasses=" + aero.resolve("stationapi/build/classes/java/main"),
            "-PworldlineAeroJar=" + aero.resolve("stationapi/build/libs/aero-model-lib-3.0.0.jar"),
            "-PworldlineRunDir=" + game, "-PworldlinePrepare=" + prepare,
            "-PworldlineArm=" + arm, "-PworldlineMetrics=" + game.resolve("metrics.properties"),
            "-PworldlineFramesDir=" + game.resolve("visual-frames"));
        System.out.println("[M780] start prepare=" + prepare + " arm=" + arm);
        String output = SmokeSupport.capture(project, command,
            Integer.parseInt(SmokeSupport.value(config, "child.timeout.seconds")));
        SmokeSupport.require(output.contains("[WorldlineM780] start prepare=" + prepare
            + " arm=" + arm) && output.contains("BUILD SUCCESSFUL"),
            "M780 client lifecycle drift: " + arm);
        String expected = prepare ? "template-ready machines=128"
            : "capture-complete arm=" + arm + " frames=240 captures=24";
        SmokeSupport.require(output.contains("[WorldlineM780] " + expected),
            "M780 completion drift: " + arm);
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

    static String sha256(byte[] value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value));
    }

    private String git(String... arguments) throws Exception {
        ArrayList<String> command = new ArrayList<String>();
        command.add("git");
        command.addAll(List.of(arguments));
        return SmokeSupport.capture(aero, command, 60);
    }

    private static String wrapper(Path project) {
        return project.resolve(System.getProperty("os.name").startsWith("Windows")
            ? "gradlew.bat" : "gradlew").toString();
    }
}

final class M780Artifact {
    final Path game;
    final String arm;
    final int machines, frames, captures, width, height, hitches, entries;
    final long renders, samples, renderNs, maxRenderNs, hits, misses, cold, stale, size, evictions;
    final String[] hashes;
    final String beforeHash, afterHash;

    private M780Artifact(Path game, Properties values) {
        this.game = game;
        arm = required(values, "arm");
        machines = integer(values, "machines");
        frames = integer(values, "frames");
        captures = integer(values, "captures");
        width = integer(values, "width");
        height = integer(values, "height");
        renderNs = number(values, "render.ns");
        maxRenderNs = number(values, "max.render.ns");
        hitches = integer(values, "hitches.50ms");
        entries = integer(values, "cache.entries");
        hits = number(values, "cache.hits");
        misses = number(values, "cache.misses");
        cold = number(values, "cache.cold.misses");
        stale = number(values, "cache.stale.misses");
        size = number(values, "cache.size.misses");
        evictions = number(values, "cache.evictions");
        beforeHash = required(values, "light.before.sha256");
        afterHash = required(values, "light.after.sha256");
        hashes = new String[captures];
        long renderCalls = 0L, lightSamples = 0L;
        for (int i = 0; i < captures; i++) {
            hashes[i] = required(values, "checkpoint." + i + ".sha256");
            renderCalls += number(values, "checkpoint." + i + ".renders");
            lightSamples += number(values, "checkpoint." + i + ".samples");
        }
        renders = renderCalls;
        samples = lightSamples;
    }

    static M780Artifact read(Path game, String expected) throws Exception {
        Properties values = new Properties();
        Path file = game.resolve("metrics-" + expected + ".properties");
        try (Reader reader = Files.newBufferedReader(file)) { values.load(reader); }
        M780Artifact result = new M780Artifact(game, values);
        SmokeSupport.require(result.arm.equals(expected), "M780 artifact identity drift");
        return result;
    }

    void verify() throws Exception {
        boolean cache = arm.equals("cache-on");
        SmokeSupport.require(machines == 128 && frames == 240 && captures == 24
            && width > 0 && height > 0 && renders > 0L && samples > 0L
            && maxRenderNs < 1_500_000_000L && !beforeHash.equals(afterHash),
            "M780 incomplete artifact: " + summary());
        SmokeSupport.require(cache
            ? hits > 0L && misses > 0L && cold > 0L && stale > 0L
                && size == 0L && evictions == 0L && entries > 0 && entries <= 128
            : hits == 0L && misses == 0L && cold == 0L && stale == 0L
                && size == 0L && evictions == 0L && entries == 0,
            "M780 cache telemetry drift: " + summary());
        for (int i = 0; i < captures + 2; i++) {
            byte[] pixels = pixels(i);
            SmokeSupport.require(pixels.length == width * height * 4,
                "M780 framebuffer shape drift: " + i);
            String expected = i < captures ? hashes[i] : i == captures ? beforeHash : afterHash;
            SmokeSupport.require(M780Runtime.sha256(pixels).equals(expected),
                "M780 framebuffer hash drift: " + i);
        }
    }

    byte[] pixels(int checkpoint) throws IOException {
        String file = checkpoint < captures ? String.format("checkpoint-%02d.rgba", checkpoint)
            : checkpoint == captures ? "light-before.rgba" : "light-after.rgba";
        return Files.readAllBytes(game.resolve("visual-frames").resolve(arm).resolve(file));
    }

    String summary() {
        return arm + ":renders=" + renders + ",samples=" + samples + ",hits=" + hits
            + ",misses=" + misses + ",cold=" + cold + ",stale=" + stale
            + ",renderMs=" + String.format(Locale.ROOT, "%.1f", renderNs / 1_000_000.0D)
            + ",maxMs=" + String.format(Locale.ROOT, "%.1f", maxRenderNs / 1_000_000.0D)
            + ",hitches50=" + hitches;
    }

    private static int integer(Properties p, String key) { return Integer.parseInt(required(p, key)); }
    private static long number(Properties p, String key) { return Long.parseLong(required(p, key)); }
    private static String required(Properties p, String key) {
        String value = p.getProperty(key);
        if (value == null || value.isBlank()) throw new IllegalStateException("missing M780 " + key);
        return value.trim();
    }
}

record M780Session(int index, M780Artifact off, M780Artifact on) {
    String summary() { return "session." + index + "," + off.summary() + "," + on.summary(); }
}

record M780VisualPair(String label, M780Artifact baseline, M780Artifact candidate,
                      long changedPixels, int maxChannelDelta, Set<Long> changedLocations) {
    static M780VisualPair compare(String label, M780Artifact baseline,
            M780Artifact candidate) throws Exception {
        SmokeSupport.require(baseline.width == candidate.width && baseline.height == candidate.height,
            "M780 framebuffer dimensions diverged");
        long changed = 0L;
        int maximum = 0;
        Set<Long> locations = new HashSet<Long>();
        for (int checkpoint = 0; checkpoint < baseline.captures + 2; checkpoint++) {
            byte[] expected = baseline.pixels(checkpoint), actual = candidate.pixels(checkpoint);
            SmokeSupport.require(expected.length == actual.length, "M780 frame size diverged");
            for (int pixel = 0; pixel < expected.length; pixel += 4) {
                boolean differs = false;
                for (int channel = 0; channel < 4; channel++) {
                    int delta = Math.abs((expected[pixel + channel] & 255)
                        - (actual[pixel + channel] & 255));
                    maximum = Math.max(maximum, delta);
                    differs |= delta != 0;
                }
                if (differs) {
                    changed++;
                    long framePixels = (long) baseline.width * baseline.height;
                    locations.add(checkpoint * framePixels + pixel / 4);
                }
            }
        }
        return new M780VisualPair(label, baseline, candidate, changed, maximum, locations);
    }

    String summary() {
        return "visual." + label + ",changedPixels=" + changedPixels
            + ",maxDelta=" + maxChannelDelta;
    }
}

record M780Result(long noisePixels, double noisePpm, long unexplainedPixels,
                  boolean everyPairBounded, boolean rendersRepeatable,
                  long offSamples, long onSamples, double sampleRatio,
                  boolean everySessionSamplesReduced,
                  long offRenderNs, long onRenderNs, double renderRatio,
                  boolean everySessionRenderReduced, int offHitches, int onHitches,
                  long maximumFrameNs) {
    static M780Result evaluate(List<M780Session> sessions, List<M780VisualPair> pairs) {
        long offSamples = 0L, onSamples = 0L, offRender = 0L, onRender = 0L, maximum = 0L;
        int offHitches = 0, onHitches = 0;
        boolean samplesReduced = true, renderReduced = true, bounded = true;
        long pixelSamples = (long) pairs.get(0).baseline().width
            * pairs.get(0).baseline().height * (pairs.get(0).baseline().captures + 2);
        for (M780VisualPair pair : pairs) bounded &= ppm(pair.changedPixels(), pixelSamples) <= 10.0D;
        Set<Long> noise = new HashSet<Long>(pairs.get(2).changedLocations());
        noise.addAll(pairs.get(3).changedLocations());
        Set<Long> unexplained = new HashSet<Long>(pairs.get(0).changedLocations());
        unexplained.addAll(pairs.get(1).changedLocations());
        unexplained.removeAll(noise);
        for (M780Session session : sessions) {
            offSamples += session.off().samples;
            onSamples += session.on().samples;
            samplesReduced &= session.on().samples < session.off().samples;
            offRender += session.off().renderNs;
            onRender += session.on().renderNs;
            renderReduced &= session.on().renderNs < session.off().renderNs;
            offHitches += session.off().hitches;
            onHitches += session.on().hitches;
            maximum = Math.max(maximum, Math.max(session.off().maxRenderNs, session.on().maxRenderNs));
        }
        boolean repeatable = sessions.get(0).off().renders == sessions.get(1).off().renders
            && sessions.get(0).on().renders == sessions.get(1).on().renders;
        return new M780Result(noise.size(), ppm(noise.size(), pixelSamples), unexplained.size(),
            bounded, repeatable, offSamples, onSamples, (double) onSamples / offSamples,
            samplesReduced, offRender, onRender, (double) onRender / offRender,
            renderReduced, offHitches, onHitches, maximum);
    }

    boolean passes() {
        boolean visual = unexplainedPixels == 0L && noisePpm <= 10.0D && everyPairBounded;
        boolean cacheWork = rendersRepeatable && everySessionSamplesReduced && sampleRatio <= 0.70D;
        boolean promotion = everySessionRenderReduced && renderRatio <= 0.95D
            && onHitches <= offHitches + 10;
        boolean keepDisabled = !everySessionRenderReduced || renderRatio > 0.95D
            || onHitches > offHitches + 10;
        return visual && cacheWork && (promotion || keepDisabled)
            && maximumFrameNs < 1_500_000_000L;
    }

    String summary() {
        return "visual.noise.locations=" + noisePixels
            + ",visual.noise.ppm=" + String.format(Locale.ROOT, "%.3f", noisePpm)
            + ",visual.unexplained.locations=" + unexplainedPixels
            + ",visual.every-pair-bounded=" + everyPairBounded
            + ",renders.same-arm-repeatable=" + rendersRepeatable
            + ",samples=" + offSamples + "->" + onSamples
            + ",sample.ratio=" + String.format(Locale.ROOT, "%.3f", sampleRatio)
            + ",samples.every-session-reduced=" + everySessionSamplesReduced
            + ",render.ms=" + String.format(Locale.ROOT, "%.1f->%.1f",
                offRenderNs / 1_000_000.0D, onRenderNs / 1_000_000.0D)
            + ",render.ratio=" + String.format(Locale.ROOT, "%.3f", renderRatio)
            + ",render.every-session-reduced=" + everySessionRenderReduced
            + ",decision=" + (everySessionRenderReduced && renderRatio <= 0.95D
                && onHitches <= offHitches + 10 ? "promote" : "keep-disabled")
            + ",hitches50=" + offHitches + "->" + onHitches
            + ",maximum.frame.ms=" + String.format(Locale.ROOT, "%.1f", maximumFrameNs / 1_000_000.0D);
    }

    private static double ppm(long changed, long samples) {
        return changed * 1_000_000.0D / samples;
    }
}
