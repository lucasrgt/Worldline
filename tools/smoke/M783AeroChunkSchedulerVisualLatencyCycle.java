import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

/** Classifies Aero's camera-aware chunk scheduler with real visual clients. */
public final class M783AeroChunkSchedulerVisualLatencyCycle {
    private static final String ID = "m783-aero-chunk-scheduler-visual-latency";
    private static final String REVISION = "82119d67d7ae88e527f1397cd6a0def31f1697ef";
    private static final String[][] ORDERS = {
        {"pages", "prebake"}, {"prebake", "pages"},
        {"prebake", "pages"}, {"pages", "prebake"}
    };
    private static final String TRACE = "v1|scene=restored-576|pairs=4|"
        + "orders=pages-prebake+prebake-pages+prebake-pages+pages-prebake|"
        + "window=2400|reload=1200|route=4x-walk+turn+teleport+mutation+settle|"
        + "dirty=current-visible1+adjacent1+lookahead1+background1-per-eight|pages=on|"
        + "prebake=off-vs-budget1-camera3-age120-debt30|"
        + "capture=wall+allocation+chunk+visible-latency+backlog+world-resets|"
        + "gates=hitch5000ppm+fps3pct+p995pct+alloc5pct+visible-max8+p99-4|"
        + "decision=promote-or-keep-disabled";
    private static final String SIGNAL = "scene=restored-576,pairs=4,jvms=8-fresh,"
        + "window=2400,reload=midpoint,route=walk+turn+teleport+mutation+settle,"
        + "pages=on,prebake=off-vs-budget1,visual-latency=measured,backlog=drained,"
        + "world-reset=observed,hitch=classified,metrics=classified,"
        + "decision=promote-or-keep-disabled";
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID);
    private final Path build = root.resolve(".worldline/build/smoke").resolve(ID);
    private final Properties config = new Properties();

    private M783AeroChunkSchedulerVisualLatencyCycle() {}

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: M783AeroChunkSchedulerVisualLatencyCycle " + ID);
            System.exit(2);
        }
        try { new M783AeroChunkSchedulerVisualLatencyCycle().execute(); }
        catch (Exception error) {
            System.err.println("M783 chunk scheduler visual classification failed: "
                + error.getMessage());
            error.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        SmokeSupport.load(smoke.resolve("smoke.properties"), config);
        verifyDesign();
        Path aero = root.resolve(SmokeSupport.value(config, "aero.path")).normalize();
        M783VisualRuntime runtime = new M783VisualRuntime(root, smoke, config, aero);
        runtime.verifyCheckout();
        SmokeSupport.recreate(root, build);
        runtime.buildAero();
        Path template = build.resolve("template");
        runtime.runClient(template, true, "prepare");
        Path sourceWorld = template.resolve("saves/WorldlineAero");
        SmokeSupport.require(Files.isDirectory(sourceWorld), "M783 template world absent");
        List<M783VisualPair> pairs = new ArrayList<M783VisualPair>();
        for (int pair = 0; pair < ORDERS.length; pair++) {
            M783VisualArtifact pages = null, prebake = null;
            for (String treatment : ORDERS[pair]) {
                String label = "pair" + (pair + 1) + "-" + treatment;
                Path game = build.resolve(label);
                M783VisualRuntime.copyWorld(sourceWorld, game.resolve("saves/WorldlineAero"));
                runtime.runClient(game, false, treatment);
                M783VisualArtifact artifact = M783VisualArtifact.read(game, treatment);
                artifact.verify();
                if (treatment.equals("pages")) pages = artifact;
                else prebake = artifact;
            }
            pairs.add(new M783VisualPair(pair + 1,
                indexOf(ORDERS[pair], "pages") < indexOf(ORDERS[pair], "prebake"),
                pages, prebake));
        }
        M783VisualResult result = M783VisualGate.evaluate(
            SmokeSupport.product(root, "profiling"), pairs, config);
        String signature = M783VisualRuntime.sha256(TRACE);
        SmokeSupport.require(SIGNAL.equals(SmokeSupport.value(config, "expected.signal")),
            "M783 signal drift");
        SmokeSupport.require(signature.equals(SmokeSupport.value(config, "expected.signature")),
            "M783 semantic signature drift: " + signature);
        StringBuilder evidence = new StringBuilder("id=").append(ID).append('\n');
        for (M783VisualPair pair : pairs) evidence.append(pair.summary()).append('\n');
        evidence.append(result.summary()).append("\ntrace=").append(TRACE)
            .append("\nsignature=").append(signature).append('\n');
        Files.writeString(build.resolve("evidence.txt"), evidence, StandardCharsets.UTF_8);
        System.out.println("M783 Aero chunk scheduler classification passed");
        System.out.println("WORLDLINE_M783_DECISION=" + result.decision());
        System.out.println("WORLDLINE_M783_SIGNAL=" + SIGNAL);
        System.out.println("WORLDLINE_M783_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M783_SIGNATURE=" + signature);
    }

    private void verifyDesign() {
        SmokeSupport.require(SmokeSupport.value(config, "pairs").equals("4")
            && SmokeSupport.value(config, "retained.frames").equals("2400")
            && SmokeSupport.value(config, "reload.frame").equals("1200")
            && SmokeSupport.value(config, "scheduler.budget").equals("1")
            && SmokeSupport.value(config, "lookahead.radius").equals("3"),
            "M783 acquisition design drift");
    }

    private static int indexOf(String[] values, String target) {
        for (int i = 0; i < values.length; i++) if (values[i].equals(target)) return i;
        throw new IllegalArgumentException(target);
    }
}

/** Exact checkout, build, process, and restored-world boundary for M783. */
final class M783VisualRuntime {
    private final Path smoke, aero;
    private final Properties config;

    M783VisualRuntime(Path root, Path smoke, Properties config, Path aero) {
        this.smoke = smoke;
        this.config = config;
        this.aero = aero;
    }

    void verifyCheckout() throws Exception {
        SmokeSupport.require(Files.exists(aero.resolve(".git")), "M783 Aero checkout absent");
        SmokeSupport.require(git("rev-parse", "HEAD").trim().equals(
            SmokeSupport.value(config, "aero.revision")), "M783 Aero revision drift");
        SmokeSupport.require(git("status", "--porcelain").isBlank(),
            "M783 Aero checkout dirty");
        String origin = git("remote", "get-url", "origin").trim()
            .replace("\\", "/").toLowerCase();
        SmokeSupport.require(origin.equals(SmokeSupport.value(config, "aero.repository")
            .toLowerCase()), "M783 Aero origin drift: " + origin);
    }

    void buildAero() throws Exception {
        Path project = aero.resolve("stationapi");
        String output = SmokeSupport.capture(project, List.of(wrapper(project),
            "--no-daemon", "remapJar", "--rerun-tasks"), timeout("build.timeout.seconds"));
        SmokeSupport.require(output.contains("BUILD SUCCESSFUL") && Files.isRegularFile(
            project.resolve("build/libs/aero-model-lib-3.0.0.jar")),
            "M783 Aero build drift");
    }

    void runClient(Path game, boolean prepare, String arm) throws Exception {
        Files.createDirectories(game);
        Path project = aero.resolve("stationapi/test");
        List<String> command = List.of(wrapper(project), "--no-daemon", "runClient",
            "--init-script", smoke.resolve("visual.init.gradle").toString(),
            "-PworldlineAeroClasses=" + aero.resolve("stationapi/build/classes/java/main"),
            "-PworldlineAeroJar=" + aero.resolve("stationapi/build/libs/aero-model-lib-3.0.0.jar"),
            "-PworldlineRunDir=" + game, "-PworldlinePrepare=" + prepare,
            "-PworldlineArm=" + arm,
            "-PworldlineFrames=" + SmokeSupport.value(config, "retained.frames"),
            "-PworldlineReloadFrame=" + SmokeSupport.value(config, "reload.frame"),
            "-PworldlineMinimumMillis=" + SmokeSupport.value(config, "minimum.millis"),
            "-PworldlineMetrics=" + game.resolve("metrics.properties"),
            "-PworldlineFrameArtifact=" + game.resolve("frames.csv"));
        System.out.println("[M783] start prepare=" + prepare + " arm=" + arm);
        String output = SmokeSupport.capture(project, command, timeout("child.timeout.seconds"));
        SmokeSupport.require(output.contains("[WorldlineM783] start prepare=" + prepare
            + " arm=" + arm) && output.contains("BUILD SUCCESSFUL"),
            "M783 client lifecycle drift: " + arm + "\n" + output);
        String expected = prepare ? "template-ready machines=576 backlog=0"
            : "retained-complete arm=" + arm;
        SmokeSupport.require(output.contains("[WorldlineM783] " + expected),
            "M783 completion drift: " + arm + "\n" + output);
    }

    static void copyWorld(Path source, Path destination) throws IOException {
        try (Stream<Path> paths = Files.walk(source)) {
            for (Path path : paths.sorted().toList()) {
                Path target = destination.resolve(source.relativize(path));
                if (Files.isDirectory(path)) Files.createDirectories(target);
                else Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.COPY_ATTRIBUTES);
            }
        }
    }

    static String sha256(String value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
            .digest(value.getBytes(StandardCharsets.UTF_8)));
    }

    private String git(String... arguments) throws Exception {
        ArrayList<String> command = new ArrayList<String>();
        command.add("git");
        command.addAll(List.of(arguments));
        return SmokeSupport.capture(aero, command, 60);
    }

    private int timeout(String key) {
        return Integer.parseInt(SmokeSupport.value(config, key));
    }

    private static String wrapper(Path project) {
        return project.resolve(System.getProperty("os.name").startsWith("Windows")
            ? "gradlew.bat" : "gradlew").toString();
    }
}

/** One fresh M783 arm's complete retained-window artifact. */
final class M783VisualArtifact {
    final String arm;
    final int frames, machines, reloads, worldResets, finalBacklog, maxBacklog;
    final int rebuilds, maxRebuilds, built, prebake, urgent;
    final int latencySamples, latencyMaximum, latencyP99, latencyPending;
    final long allocatedBytes, rebuildMaximumNanos;
    final long[] walls;

    private M783VisualArtifact(Properties p, long[] walls) {
        arm = required(p, "arm");
        frames = integer(p, "frames");
        machines = integer(p, "machines");
        reloads = integer(p, "reloads");
        worldResets = integer(p, "world.resets");
        finalBacklog = integer(p, "final.backlog");
        maxBacklog = integer(p, "max.backlog");
        allocatedBytes = number(p, "frame.allocated.bytes");
        rebuilds = integer(p, "chunk.rebuilds");
        rebuildMaximumNanos = number(p, "chunk.rebuild.max.nanos");
        maxRebuilds = integer(p, "chunk.rebuild.max.frame");
        built = integer(p, "chunk.work.built");
        prebake = integer(p, "chunk.work.prebake");
        urgent = integer(p, "chunk.work.urgent");
        latencySamples = integer(p, "visible.latency.samples");
        latencyMaximum = integer(p, "visible.latency.maximum.frames");
        latencyP99 = integer(p, "visible.latency.p99.frames");
        latencyPending = integer(p, "visible.latency.pending");
        this.walls = walls;
    }

    static M783VisualArtifact read(Path game, String expected) throws Exception {
        Properties p = new Properties();
        try (Reader reader = Files.newBufferedReader(game.resolve("metrics.properties"))) {
            p.load(reader);
        }
        List<String> rows = Files.readAllLines(game.resolve("frames.csv"));
        long[] walls = new long[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            String[] columns = rows.get(i).split(",", -1);
            SmokeSupport.require(columns.length == 3, "M783 frame row drift");
            walls[i] = Long.parseLong(columns[1]);
        }
        M783VisualArtifact value = new M783VisualArtifact(p, walls);
        SmokeSupport.require(value.arm.equals(expected) && value.frames == walls.length,
            "M783 artifact identity drift: " + expected);
        return value;
    }

    void verify() {
        SmokeSupport.require(frames >= 2400 && machines == 576 && reloads == 1
            && worldResets >= 2 && finalBacklog == 0 && maxBacklog > 0
            && allocatedBytes > 0L && rebuilds > 0 && rebuildMaximumNanos > 0L
            && latencySamples > 0 && latencyPending == 0,
            "M783 incomplete artifact: " + summary());
        if (arm.equals("pages")) SmokeSupport.require(prebake == 0,
            "M783 baseline activated scheduler: " + summary());
        else SmokeSupport.require(built > 0 && prebake > 0 && maxRebuilds <= 1,
            "M783 candidate activation drift: " + summary());
    }

    double fps() {
        long total = 0L;
        for (long value : walls) total += value;
        return walls.length * 1_000_000_000.0D / total;
    }

    double allocationPerFrame() { return (double) allocatedBytes / frames; }

    long p99() {
        long[] sorted = walls.clone();
        Arrays.sort(sorted);
        return sorted[Math.min(sorted.length - 1,
            (int) Math.ceil(sorted.length * 0.99D) - 1)];
    }

    String summary() {
        return arm + ":frames=" + frames + ",fps=" + round(fps())
            + ",p99.ns=" + p99() + ",alloc/frame=" + round(allocationPerFrame())
            + ",chunk=" + rebuilds + "/" + built + "/" + prebake + "/" + urgent
            + ",latency=" + latencySamples + "/" + latencyP99 + "/" + latencyMaximum
            + ",backlog=" + maxBacklog + "/" + finalBacklog
            + ",resets=" + worldResets + ",reloads=" + reloads;
    }

    private static String round(double value) { return String.format("%.2f", value); }
    private static int integer(Properties p, String key) {
        return Integer.parseInt(required(p, key));
    }
    private static long number(Properties p, String key) {
        return Long.parseLong(required(p, key));
    }
    private static String required(Properties p, String key) {
        String value = p.getProperty(key);
        if (value == null || value.isBlank()) throw new IllegalStateException("missing M783 " + key);
        return value.trim();
    }
}

record M783VisualPair(int index, boolean pagesFirst, M783VisualArtifact pages,
                      M783VisualArtifact prebake) {
    String summary() {
        return "pair." + index + ".pagesFirst=" + pagesFirst + ","
            + pages.summary() + "," + prebake.summary();
    }
}

/** Binds M783 frame arrays to Worldline's neutral paired hitch-rate gate. */
final class M783VisualGate {
    private M783VisualGate() {}

    static M783VisualResult evaluate(Path product, List<M783VisualPair> source,
                                     Properties config) throws Exception {
        long threshold = Long.parseLong(SmokeSupport.value(config, "hitch.threshold.nanos"));
        long allowance = Long.parseLong(SmokeSupport.value(config, "allowed.regression.ppm"));
        Object hitch;
        try (URLClassLoader loader = new URLClassLoader(new URL[] {product.toUri().toURL()}, null)) {
            Class<?> census = Class.forName("worldline.profiling.FrameCensus", true, loader);
            Class<?> gate = Class.forName("worldline.profiling.HitchRateGate", true, loader);
            Method of = census.getMethod("of", String[].class, long[][].class);
            Method pair = gate.getMethod("pair", census, census, boolean.class,
                String.class, long.class);
            List<Object> pairs = new ArrayList<Object>();
            for (M783VisualPair value : source) pairs.add(pair.invoke(null,
                frame(of, value.pages().walls), frame(of, value.prebake().walls),
                value.pagesFirst(), "frame.wall.nanos", threshold));
            hitch = gate.getMethod("evaluate", List.class, long.class)
                .invoke(null, pairs, allowance);
        }
        double fpsRatio = ratio(source, 0);
        double p99Ratio = ratio(source, 1);
        double allocationRatio = ratio(source, 2);
        int latencyMaximum = source.stream().mapToInt(value ->
            value.prebake().latencyMaximum).max().orElseThrow();
        int latencyP99 = source.stream().mapToInt(value ->
            value.prebake().latencyP99).max().orElseThrow();
        boolean hitchPass = (Boolean) hitch.getClass().getMethod("passes").invoke(hitch);
        boolean metricsPass = fpsRatio >= Double.parseDouble(SmokeSupport.value(config,
                "fps.minimum.ratio"))
            && p99Ratio <= Double.parseDouble(SmokeSupport.value(config,
                "p99.maximum.ratio"))
            && allocationRatio <= Double.parseDouble(SmokeSupport.value(config,
                "allocation.maximum.ratio"));
        boolean latencyPass = latencyMaximum <= Integer.parseInt(SmokeSupport.value(config,
                "maximum.visible.latency.frames"))
            && latencyP99 <= Integer.parseInt(SmokeSupport.value(config,
                "maximum.visible.p99.frames"));
        return new M783VisualResult(number(hitch, "baselineRatePpm"),
            number(hitch, "candidateRatePpm"), number(hitch, "aggregateDeltaPpm"),
            hitchPass, fpsRatio, p99Ratio, allocationRatio, metricsPass,
            latencyMaximum, latencyP99, latencyPass,
            hitchPass && metricsPass && latencyPass ? "promote" : "keep-disabled");
    }

    private static double ratio(List<M783VisualPair> pairs, int metric) {
        double baseline = 0.0D, candidate = 0.0D;
        for (M783VisualPair pair : pairs) {
            baseline += value(pair.pages(), metric);
            candidate += value(pair.prebake(), metric);
        }
        return candidate / baseline;
    }

    private static double value(M783VisualArtifact artifact, int metric) {
        if (metric == 0) return artifact.fps();
        if (metric == 1) return artifact.p99();
        return artifact.allocationPerFrame();
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
}

record M783VisualResult(long baselineHitchPpm, long candidateHitchPpm,
                        long hitchDeltaPpm, boolean hitchPass, double fpsRatio,
                        double p99Ratio, double allocationRatio, boolean metricsPass,
                        int latencyMaximum, int latencyP99, boolean latencyPass,
                        String decision) {
    String summary() {
        return "decision=" + decision + ",hitch.pages.ppm=" + baselineHitchPpm
            + ",hitch.prebake.ppm=" + candidateHitchPpm + ",hitch.delta.ppm="
            + hitchDeltaPpm + ",hitch.pass=" + hitchPass + ",fps.ratio=" + round(fpsRatio)
            + ",p99.ratio=" + round(p99Ratio) + ",allocation.ratio="
            + round(allocationRatio) + ",metrics.pass=" + metricsPass
            + ",latency.max=" + latencyMaximum + ",latency.p99=" + latencyP99
            + ",latency.pass=" + latencyPass;
    }

    private static String round(double value) { return String.format("%.4f", value); }
}
