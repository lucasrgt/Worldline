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

/** Proves broad culling is pixel-safe for animation, morph, and IK in motion. */
public final class M779AeroAnimatedVisualIntegrityCycle {
    private static final String ID = "m779-aero-animated-visual-integrity";
    private static final String[] RUNS = {"round1-off", "round1-on", "round2-on", "round2-off"};
    private static final String[] ARMS = {"cull-off", "cull-on", "cull-on", "cull-off"};
    private static final String TRACE = "v14|scene=120-dynamic-40-keyframed+40-morph+40-ik+"
        + "nonoverlap-panels+static-enclosure+prewarmed|"
        + "jvms=4-fresh-abba-off+on+on+off|route=240-frames-orbit+walk+fast-spin+teleport+near-orbit|"
        + "camera=continuous-samples+collapsed-interpolation-history|"
        + "poses=deterministic-per-frame+spatial-phase|"
        + "cache-history=per-jvm-full-route-warm1200|batch-order=stable-model-name|"
        + "cone-bounds=radius-aware-sphere|reuse=production-defaults|"
        + "captures=24-per-jvm|visibility=per-checkpoint-radius-signature|"
        + "world=frozen-tick+time6000+clear-weather+no-entities+no-clouds|"
        + "contrast=immutable-startup-frustum-off-vs-broad-on+be-view-off+production-animation-lod|"
        + "oracle=full-rgba-differential-no-unexplained+same-arm-noise10ppm+"
        + "same-arm-work-repeatable+animated-work-reduction+render-time-reduction+hitch-census";
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID);
    private final Path build = root.resolve(".worldline/build/smoke").resolve(ID);
    private final Properties config = new Properties();

    private M779AeroAnimatedVisualIntegrityCycle() {}

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: M779AeroAnimatedVisualIntegrityCycle " + ID);
            System.exit(2);
        }
        try { new M779AeroAnimatedVisualIntegrityCycle().execute(); }
        catch (Exception error) {
            System.err.println("M779 animated visual integrity failed: " + error.getMessage());
            error.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        SmokeSupport.load(smoke.resolve("smoke.properties"), config);
        verifyDesign();
        Path aero = root.resolve(SmokeSupport.value(config, "aero.path")).normalize();
        M779Runtime runtime = new M779Runtime(smoke, config, aero);
        runtime.verifyCheckout();
        SmokeSupport.recreate(root, build);
        runtime.buildAero();
        Path template = build.resolve("template");
        runtime.runClient(template, true, "prepare");
        Path sourceWorld = template.resolve("saves/WorldlineAero");
        SmokeSupport.require(Files.isDirectory(sourceWorld), "M779 template world absent");
        List<M779Artifact> artifacts = new ArrayList<M779Artifact>();
        for (int index = 0; index < RUNS.length; index++) {
            String arm = ARMS[index];
            Path game = build.resolve(RUNS[index]);
            M779Runtime.copyWorld(sourceWorld, game.resolve("saves/WorldlineAero"));
            runtime.runClient(game, false, arm);
            M779Artifact artifact = M779Artifact.read(game, arm);
            artifact.verify();
            artifacts.add(artifact);
        }
        List<M779Session> sessions = List.of(
            new M779Session(1, artifacts.get(0), artifacts.get(1)),
            new M779Session(2, artifacts.get(3), artifacts.get(2)));
        List<M779VisualPair> pairs = List.of(
            M779VisualPair.compare("round.1", artifacts.get(0), artifacts.get(1)),
            M779VisualPair.compare("round.2", artifacts.get(3), artifacts.get(2)),
            M779VisualPair.compare("repeat.off", artifacts.get(0), artifacts.get(3)),
            M779VisualPair.compare("repeat.on", artifacts.get(1), artifacts.get(2)));
        M779Result result = M779Result.evaluate(sessions, pairs);
        SmokeSupport.require(result.passes(), "M779 visual gate failed: " + result.summary());
        String signature = M779Runtime.sha256(TRACE.getBytes(StandardCharsets.UTF_8));
        SmokeSupport.require(signature.equals(SmokeSupport.value(config, "expected.signature")),
            "M779 semantic signature drift: " + signature);
        StringBuilder evidence = new StringBuilder("id=").append(ID).append('\n');
        for (M779Session session : sessions) evidence.append(session.summary()).append('\n');
        for (M779VisualPair pair : pairs) evidence.append(pair.summary()).append('\n');
        evidence.append(result.summary()).append("\ntrace=").append(TRACE)
            .append("\nsignature=").append(signature).append('\n');
        Files.writeString(build.resolve("evidence.txt"), evidence, StandardCharsets.UTF_8);
        System.out.println("M779 Aero animated visual integrity passed");
        System.out.println("WORLDLINE_M779_SIGNAL=" + SmokeSupport.value(config, "expected.signal"));
        System.out.println("WORLDLINE_M779_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M779_SIGNATURE=" + signature);
    }

    private void verifyDesign() {
        SmokeSupport.require(SmokeSupport.value(config, "sessions").equals("4")
            && SmokeSupport.value(config, "route.frames").equals("240")
            && SmokeSupport.value(config, "checkpoints").equals("24")
            && SmokeSupport.value(config, "machines").equals("120")
            && SmokeSupport.value(config, "model.families").equals("3")
            && SmokeSupport.value(config, "maximum.raster.noise.ppm").equals("10")
            && SmokeSupport.value(config, "maximum.work.ratio").equals("0.95"),
            "M779 acquisition design drift");
    }
}

final class M779Runtime {
    private final Path smoke, aero;
    private final Properties config;

    M779Runtime(Path smoke, Properties config, Path aero) {
        this.smoke = smoke;
        this.config = config;
        this.aero = aero;
    }

    void verifyCheckout() throws Exception {
        SmokeSupport.require(Files.exists(aero.resolve(".git")), "M779 Aero checkout absent");
        SmokeSupport.require(git("rev-parse", "HEAD").trim().equals(
            SmokeSupport.value(config, "aero.revision")), "M779 Aero revision drift");
        SmokeSupport.require(git("status", "--porcelain").isBlank(), "M779 Aero checkout dirty");
        String origin = git("remote", "get-url", "origin").trim().replace("\\", "/");
        SmokeSupport.require(origin.equalsIgnoreCase(SmokeSupport.value(config, "aero.repository")),
            "M779 Aero origin drift: " + origin);
    }

    void buildAero() throws Exception {
        Path project = aero.resolve("stationapi");
        String output = SmokeSupport.capture(project, List.of(wrapper(project),
            "--no-daemon", "remapJar", "--rerun-tasks"),
            Integer.parseInt(SmokeSupport.value(config, "build.timeout.seconds")));
        SmokeSupport.require(output.contains("BUILD SUCCESSFUL") && Files.isRegularFile(
            project.resolve("build/libs/aero-model-lib-3.0.0.jar")), "M779 Aero build drift");
    }

    void runClient(Path game, boolean prepare, String arm) throws Exception {
        Files.createDirectories(game);
        Path project = aero.resolve("stationapi/test");
        List<String> command = List.of(wrapper(project), "--no-daemon", "runClient",
            "--init-script", smoke.resolve("animated-visual.init.gradle").toString(),
            "-PworldlineAeroClasses=" + aero.resolve("stationapi/build/classes/java/main"),
            "-PworldlineAeroJar=" + aero.resolve("stationapi/build/libs/aero-model-lib-3.0.0.jar"),
            "-PworldlineRunDir=" + game, "-PworldlinePrepare=" + prepare,
            "-PworldlineArm=" + arm, "-PworldlineMetrics=" + game.resolve("metrics.properties"),
            "-PworldlineFramesDir=" + game.resolve("visual-frames"));
        System.out.println("[M779] start prepare=" + prepare + " arm=" + arm);
        String output = SmokeSupport.capture(project, command,
            Integer.parseInt(SmokeSupport.value(config, "child.timeout.seconds")));
        SmokeSupport.require(output.contains("[WorldlineM779] start prepare=" + prepare
            + " arm=" + arm) && output.contains("BUILD SUCCESSFUL"),
            "M779 client lifecycle drift: " + arm);
        String expected = prepare ? "template-ready machines=120 types=40/40/40"
            : "capture-complete arm=" + arm + " frames=240 captures=24";
        SmokeSupport.require(output.contains("[WorldlineM779] " + expected),
            "M779 completion drift: " + arm);
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

final class M779Artifact {
    final Path game;
    final String arm;
    final int machines, mega, morph, ik, frames, captures, width, height, hitches;
    final long animated, atRest, listCalls, viewCulls, renderNs, maxRenderNs;
    final String[] hashes;
    final long[] visibility;

    private M779Artifact(Path game, Properties values) {
        this.game = game;
        arm = required(values, "arm");
        machines = integer(values, "machines");
        mega = integer(values, "animated.mega");
        morph = integer(values, "morph.crystal");
        ik = integer(values, "turret.ik");
        frames = integer(values, "frames");
        captures = integer(values, "captures");
        width = integer(values, "width");
        height = integer(values, "height");
        renderNs = number(values, "render.ns");
        maxRenderNs = number(values, "max.render.ns");
        hitches = integer(values, "hitches.50ms");
        hashes = new String[captures];
        visibility = new long[captures];
        long animation = 0L, rest = 0L, lists = 0L, culls = 0L;
        for (int i = 0; i < captures; i++) {
            hashes[i] = required(values, "checkpoint." + i + ".sha256");
            animation += number(values, "checkpoint." + i + ".animated");
            rest += number(values, "checkpoint." + i + ".atrest");
            lists += number(values, "checkpoint." + i + ".listcalls");
            culls += number(values, "checkpoint." + i + ".viewculls");
            visibility[i] = number(values, "checkpoint." + i + ".visibility");
        }
        animated = animation;
        atRest = rest;
        listCalls = lists;
        viewCulls = culls;
    }

    static M779Artifact read(Path game, String expected) throws Exception {
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(game.resolve("metrics-" + expected + ".properties"))) {
            values.load(reader);
        }
        M779Artifact result = new M779Artifact(game, values);
        SmokeSupport.require(result.arm.equals(expected), "M779 artifact identity drift");
        return result;
    }

    void verify() throws Exception {
        SmokeSupport.require(machines == 120 && mega == 40 && morph == 40 && ik == 40
            && frames == 240 && captures == 24 && width > 0 && height > 0
            && animated > 0L && atRest > 0L && listCalls > 0L && viewCulls == 0L
            && maxRenderNs < 1_500_000_000L, "M779 incomplete artifact: " + summary());
        for (int i = 0; i < captures; i++) {
            byte[] pixels = pixels(i);
            SmokeSupport.require(pixels.length == width * height * 4,
                "M779 framebuffer shape drift: " + i);
            SmokeSupport.require(M779Runtime.sha256(pixels).equals(hashes[i]),
                "M779 framebuffer hash drift: " + i);
        }
    }

    byte[] pixels(int checkpoint) throws IOException {
        return Files.readAllBytes(game.resolve("visual-frames").resolve(arm)
            .resolve(String.format("checkpoint-%02d.rgba", checkpoint)));
    }

    String summary() {
        return arm + ":frames=" + frames + ",captures=" + captures + ",size=" + width + "x" + height
            + ",animated=" + animated + ",atRest=" + atRest + ",renderMs="
            + String.format(Locale.ROOT, "%.1f", renderNs / 1_000_000.0D)
            + ",maxMs=" + String.format(Locale.ROOT, "%.1f", maxRenderNs / 1_000_000.0D)
            + ",hitches50=" + hitches;
    }

    private static int integer(Properties p, String key) { return Integer.parseInt(required(p, key)); }
    private static long number(Properties p, String key) { return Long.parseLong(required(p, key)); }
    private static String required(Properties p, String key) {
        String value = p.getProperty(key);
        if (value == null || value.isBlank()) throw new IllegalStateException("missing M779 " + key);
        return value.trim();
    }
}

record M779Session(int index, M779Artifact off, M779Artifact on) {
    String summary() { return "session." + index + "," + off.summary() + "," + on.summary(); }
}

record M779VisualPair(String label, M779Artifact baseline, M779Artifact candidate,
                      long changedPixels, int maxChannelDelta, Set<Long> changedLocations) {
    static M779VisualPair compare(String label, M779Artifact baseline,
            M779Artifact candidate) throws Exception {
        SmokeSupport.require(baseline.width == candidate.width && baseline.height == candidate.height,
            "M779 framebuffer dimensions diverged");
        long changed = 0L;
        int maximum = 0;
        Set<Long> locations = new HashSet<Long>();
        for (int checkpoint = 0; checkpoint < baseline.captures; checkpoint++) {
            byte[] expected = baseline.pixels(checkpoint), actual = candidate.pixels(checkpoint);
            SmokeSupport.require(expected.length == actual.length, "M779 frame size diverged");
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
        return new M779VisualPair(label, baseline, candidate, changed, maximum, locations);
    }

    String summary() {
        return "visual." + label + "," + baseline.arm + "=" + baseline.game.getFileName()
            + "," + candidate.arm + "=" + candidate.game.getFileName()
            + ",changedPixels=" + changedPixels + ",maxDelta=" + maxChannelDelta;
    }
}

record M779Result(long changedPixels, int maxDelta, long noisePixels, double noisePpm,
                  long unexplainedPixels,
                  boolean everyPairBounded, boolean sameArmWorkRepeatable,
                  long offWork, long onWork,
                  double workRatio, boolean everySessionReduced,
                  long offRenderNs, long onRenderNs, double renderRatio,
                  boolean everySessionRenderReduced, int offHitches, int onHitches,
                  long maximumFrameNs) {
    static M779Result evaluate(List<M779Session> sessions, List<M779VisualPair> pairs) {
        long changed = 0L, off = 0L, on = 0L, offRender = 0L, onRender = 0L, max = 0L;
        int delta = 0, offHitches = 0, onHitches = 0;
        boolean bounded = true, reduced = true, renderReduced = true;
        long pixelSamples = (long) pairs.get(0).baseline().width
            * pairs.get(0).baseline().height * pairs.get(0).baseline().captures;
        for (M779VisualPair pair : pairs) {
            changed += pair.changedPixels();
            delta = Math.max(delta, pair.maxChannelDelta());
            bounded &= ppm(pair.changedPixels(), pixelSamples) <= 10.0D;
        }
        Set<Long> noise = new HashSet<Long>(pairs.get(2).changedLocations());
        noise.addAll(pairs.get(3).changedLocations());
        Set<Long> unexplained = new HashSet<Long>(pairs.get(0).changedLocations());
        unexplained.addAll(pairs.get(1).changedLocations());
        unexplained.removeAll(noise);
        for (M779Session session : sessions) {
            off += session.off().animated;
            on += session.on().animated;
            reduced &= session.on().animated < session.off().animated;
            offRender += session.off().renderNs;
            onRender += session.on().renderNs;
            renderReduced &= session.on().renderNs < session.off().renderNs;
            offHitches += session.off().hitches;
            onHitches += session.on().hitches;
            max = Math.max(max, Math.max(session.off().maxRenderNs, session.on().maxRenderNs));
        }
        M779Session first = sessions.get(0), second = sessions.get(1);
        boolean repeatable = sameWork(first.off(), second.off())
            && sameWork(first.on(), second.on());
        return new M779Result(changed, delta, noise.size(), ppm(noise.size(), pixelSamples),
            unexplained.size(), bounded, repeatable,
            off, on, (double) on / off, reduced,
            offRender, onRender, (double) onRender / offRender, renderReduced,
            offHitches, onHitches, max);
    }

    boolean passes() {
        return unexplainedPixels == 0L && noisePpm <= 10.0D && everyPairBounded
            && sameArmWorkRepeatable
            && everySessionReduced
            && workRatio <= 0.95D && everySessionRenderReduced && renderRatio <= 0.95D
            && onHitches <= offHitches + 10
            && maximumFrameNs < 1_500_000_000L;
    }

    String summary() {
        return "visual.changed.pixels=" + changedPixels + ",visual.max.delta=" + maxDelta
            + ",visual.noise.locations=" + noisePixels
            + ",visual.noise.ppm=" + String.format(Locale.ROOT, "%.3f", noisePpm)
            + ",visual.unexplained.locations=" + unexplainedPixels
            + ",visual.every-pair-bounded=" + everyPairBounded
            + ",work.same-arm-repeatable=" + sameArmWorkRepeatable
            + ",work.animated=" + offWork + "->" + onWork
            + ",work.ratio=" + String.format(Locale.ROOT, "%.3f", workRatio)
            + ",work.every-session-reduced=" + everySessionReduced
            + ",render.ms=" + String.format(Locale.ROOT, "%.1f->%.1f",
                offRenderNs / 1_000_000.0D, onRenderNs / 1_000_000.0D)
            + ",render.ratio=" + String.format(Locale.ROOT, "%.3f", renderRatio)
            + ",render.every-session-reduced=" + everySessionRenderReduced
            + ",hitches50=" + offHitches + "->" + onHitches
            + ",maximum.frame.ms=" + String.format(Locale.ROOT, "%.1f", maximumFrameNs / 1_000_000.0D);
    }

    private static boolean sameWork(M779Artifact left, M779Artifact right) {
        return left.animated == right.animated && left.atRest == right.atRest
            && left.listCalls == right.listCalls && left.viewCulls == right.viewCulls
            && Arrays.equals(left.visibility, right.visibility);
    }

    private static double ppm(long changed, long samples) {
        return changed * 1_000_000.0D / samples;
    }
}
