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
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;

/** Qualifies cold GL compilation, predictive prewarm, and animated LOD. */
public final class M776AeroProfilerGlPrewarmLodMatrix {
    private static final String ID = "m776-aero-profiler-gl-prewarm-lod-matrix";
    private static final String[][] ORDERS = {
        {"direct", "cold", "prewarm", "lod"},
        {"cold", "lod", "direct", "prewarm"},
        {"prewarm", "direct", "lod", "cold"},
        {"lod", "prewarm", "cold", "direct"}
    };
    private static final String TRACE = "v1|scene=four-panels-120-15-models|rounds=4|"
        + "orders=direct-cold-prewarm-lod+cold-lod-direct-prewarm+"
        + "prewarm-direct-lod-cold+lod-prewarm-cold-direct|"
        + "journey=first-sight60+north60+east120+south120+west120+spin120|"
        + "lists=off-vs-cold-vs-prewarm1|lod=off-vs-28|"
        + "capture=wall+allocation+display-lists+prewarm+at-rest+animated|"
        + "hitch=first-sight-50ms+5000ppm|claims=activation+drain+hitch-safety";
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID);
    private final Path build = root.resolve(".worldline/build/smoke").resolve(ID);
    private final Properties config = new Properties();

    private M776AeroProfilerGlPrewarmLodMatrix() {}

    public static void main(String[] arguments) {
        if (arguments.length != 1 || !arguments[0].equals(ID)) {
            System.err.println("usage: M776AeroProfilerGlPrewarmLodMatrix " + ID);
            System.exit(2);
        }
        try { new M776AeroProfilerGlPrewarmLodMatrix().execute(); }
        catch (Exception error) {
            System.err.println("M776 GL/prewarm/LOD matrix failed: " + error.getMessage());
            error.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        SmokeSupport.load(smoke.resolve("smoke.properties"), config);
        verifyDesign();
        Path aero = root.resolve(SmokeSupport.value(config, "aero.path")).normalize();
        M776MatrixRuntime runtime = new M776MatrixRuntime(root, smoke, config, aero);
        runtime.verifyCheckout();
        SmokeSupport.recreate(root, build);
        runtime.buildAero();
        Path template = build.resolve("template");
        runtime.runClient(template, true, "prepare");
        Path sourceWorld = template.resolve("saves/WorldlineAero");
        SmokeSupport.require(Files.isDirectory(sourceWorld), "M776 template world absent");
        List<M776MatrixRound> rounds = new ArrayList<M776MatrixRound>();
        for (int round = 0; round < ORDERS.length; round++) {
            M776MatrixArtifact direct = null, cold = null, prewarm = null, lod = null;
            for (String treatment : ORDERS[round]) {
                Path game = build.resolve("round" + (round + 1) + "-" + treatment);
                M776MatrixRuntime.copyWorld(sourceWorld, game.resolve("saves/WorldlineAero"));
                runtime.runClient(game, false, treatment);
                M776MatrixArtifact value = M776MatrixArtifact.read(game, treatment);
                value.verify();
                if (treatment.equals("direct")) direct = value;
                else if (treatment.equals("cold")) cold = value;
                else if (treatment.equals("prewarm")) prewarm = value;
                else lod = value;
            }
            rounds.add(new M776MatrixRound(round + 1,
                indexOf(ORDERS[round], "cold") < indexOf(ORDERS[round], "prewarm"),
                direct, cold, prewarm, lod));
        }
        M776MatrixResult result = M776MatrixGate.evaluate(
            SmokeSupport.product(root, "profiling"), rounds,
            Long.parseLong(SmokeSupport.value(config, "hitch.threshold.nanos")),
            Long.parseLong(SmokeSupport.value(config, "allowed.regression.ppm")));
        SmokeSupport.require(result.passes(), "M776 first-sight hitch regression: " + result.summary());
        String signature = M776MatrixRuntime.sha256(TRACE);
        SmokeSupport.require(signature.equals(SmokeSupport.value(config, "expected.signature")),
            "M776 semantic signature drift: " + signature);
        StringBuilder evidence = new StringBuilder("id=").append(ID).append('\n');
        for (M776MatrixRound round : rounds) evidence.append(round.summary()).append('\n');
        evidence.append(result.summary()).append("\ntrace=").append(TRACE)
            .append("\nsignature=").append(signature).append('\n');
        Files.writeString(build.resolve("evidence.txt"), evidence, StandardCharsets.UTF_8);
        System.out.println("M776 Aero GL/prewarm/LOD matrix passed");
        System.out.println("WORLDLINE_M776_SIGNAL=" + SmokeSupport.value(config, "expected.signal"));
        System.out.println("WORLDLINE_M776_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M776_SIGNATURE=" + signature);
    }

    private void verifyDesign() {
        SmokeSupport.require(SmokeSupport.value(config, "rounds").equals("4")
            && SmokeSupport.value(config, "retained.frames").equals("600")
            && SmokeSupport.value(config, "prewarm.budget").equals("1")
            && SmokeSupport.value(config, "model.identities").equals("15")
            && SmokeSupport.value(config, "hitch.threshold.nanos").equals("50000000"),
            "M776 acquisition design drift");
    }

    private static int indexOf(String[] values, String target) {
        for (int i = 0; i < values.length; i++) if (values[i].equals(target)) return i;
        throw new IllegalArgumentException(target);
    }
}

/** Exact checkout, build, process, and restored-world boundary for M776. */
final class M776MatrixRuntime {
    private final Path root, smoke, aero;
    private final Properties config;

    M776MatrixRuntime(Path root, Path smoke, Properties config, Path aero) {
        this.root = root;
        this.smoke = smoke;
        this.config = config;
        this.aero = aero;
    }

    void verifyCheckout() throws Exception {
        SmokeSupport.require(Files.exists(aero.resolve(".git")), "M776 Aero checkout absent");
        SmokeSupport.require(git("rev-parse", "HEAD").trim().equals(
            SmokeSupport.value(config, "aero.revision")), "M776 Aero revision drift");
        SmokeSupport.require(git("status", "--porcelain").isBlank(), "M776 Aero checkout dirty");
        String origin = git("remote", "get-url", "origin").trim().replace("\\", "/").toLowerCase();
        SmokeSupport.require(origin.equals(SmokeSupport.value(config, "aero.repository").toLowerCase()),
            "M776 Aero origin drift: " + origin);
    }

    void buildAero() throws Exception {
        Path project = aero.resolve("stationapi");
        String output = SmokeSupport.capture(project, List.of(wrapper(project),
            "--no-daemon", "remapJar", "--rerun-tasks"),
            Integer.parseInt(SmokeSupport.value(config, "build.timeout.seconds")));
        SmokeSupport.require(output.contains("BUILD SUCCESSFUL") && Files.isRegularFile(
            project.resolve("build/libs/aero-model-lib-3.0.0.jar")), "M776 Aero build drift");
    }

    void runClient(Path game, boolean prepare, String arm) throws Exception {
        Files.createDirectories(game);
        Path project = aero.resolve("stationapi/test");
        List<String> command = List.of(wrapper(project), "--no-daemon", "runClient",
            "--init-script", smoke.resolve("matrix.init.gradle").toString(),
            "-PworldlineAeroClasses=" + aero.resolve("stationapi/build/classes/java/main"),
            "-PworldlineAeroJar=" + aero.resolve("stationapi/build/libs/aero-model-lib-3.0.0.jar"),
            "-PworldlineRunDir=" + game, "-PworldlinePrepare=" + prepare,
            "-PworldlineArm=" + arm,
            "-PworldlineFrames=" + SmokeSupport.value(config, "retained.frames"),
            "-PworldlineMinimumMillis=" + SmokeSupport.value(config, "minimum.millis"),
            "-PworldlineMetrics=" + game.resolve("metrics.properties"),
            "-PworldlineFrameArtifact=" + game.resolve("frames.csv"));
        System.out.println("[M776] start prepare=" + prepare + " arm=" + arm);
        String output = SmokeSupport.capture(project, command,
            Integer.parseInt(SmokeSupport.value(config, "child.timeout.seconds")));
        SmokeSupport.require(output.contains("[WorldlineM776] start prepare=" + prepare
            + " arm=" + arm) && output.contains("BUILD SUCCESSFUL"),
            "M776 client lifecycle drift: " + arm);
        String expected = prepare ? "template-ready machines=120" : "retained-complete arm=" + arm;
        SmokeSupport.require(output.contains("[WorldlineM776] " + expected),
            "M776 completion drift: " + arm);
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
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
            .digest(value.getBytes(StandardCharsets.UTF_8)));
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

/** One fresh M776 treatment's retained-window artifact. */
final class M776MatrixArtifact {
    final String arm;
    final int frames, machines, atRest, listCalls, fallbacks, animated;
    final int displays, displayLive, displayPeak, denied, failed;
    final int drained, urgent, queued, promoted, dropped, maxQueued, finalQueued, samples;
    final long allocatedBytes;
    final long[] walls;
    final int[] phases;

    private M776MatrixArtifact(Properties p, long[] walls, int[] phases) {
        arm = required(p, "arm");
        frames = integer(p, "frames");
        machines = integer(p, "machines");
        allocatedBytes = number(p, "frame.allocated.bytes");
        atRest = integer(p, "atrest.renders");
        listCalls = integer(p, "atrest.list.calls");
        fallbacks = integer(p, "atrest.fallbacks");
        animated = integer(p, "animated.instances");
        displays = integer(p, "display.allocated");
        displayLive = integer(p, "display.live");
        displayPeak = integer(p, "display.peak");
        denied = integer(p, "display.denied");
        failed = integer(p, "display.failed");
        drained = integer(p, "prewarm.drained");
        urgent = integer(p, "prewarm.urgent.drained");
        queued = integer(p, "prewarm.queued.total");
        promoted = integer(p, "prewarm.promoted");
        dropped = integer(p, "prewarm.dropped");
        maxQueued = integer(p, "prewarm.max.queued");
        finalQueued = integer(p, "prewarm.final.queued");
        samples = integer(p, "render.samples");
        this.walls = walls;
        this.phases = phases;
    }

    static M776MatrixArtifact read(Path game, String expected) throws Exception {
        Properties p = new Properties();
        try (Reader reader = Files.newBufferedReader(game.resolve("metrics.properties"))) { p.load(reader); }
        List<String> rows = Files.readAllLines(game.resolve("frames.csv"));
        long[] walls = new long[rows.size()];
        int[] phases = new int[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            String[] columns = rows.get(i).split(",", -1);
            SmokeSupport.require(columns.length == 3, "M776 frame row drift");
            phases[i] = Integer.parseInt(columns[0]);
            walls[i] = Long.parseLong(columns[1]);
        }
        M776MatrixArtifact value = new M776MatrixArtifact(p, walls, phases);
        SmokeSupport.require(value.arm.equals(expected) && value.frames == walls.length,
            "M776 artifact identity drift: " + expected);
        return value;
    }

    void verify() {
        SmokeSupport.require(frames >= 500 && samples >= 500 && machines == 120 && allocatedBytes > 0L
            && denied == 0 && failed == 0, "M776 incomplete artifact: " + summary());
        for (int phase = 0; phase < 6; phase++)
            SmokeSupport.require(countPhase(phase) >= 50, "M776 phase absent: " + phase);
        if (arm.equals("direct")) SmokeSupport.require(displays == 0 && listCalls == 0
            && drained == 0 && fallbacks > 0, "M776 direct contrast drift: " + summary());
        if (arm.equals("cold")) SmokeSupport.require(displays > 0 && listCalls > 0
            && drained == 0, "M776 cold activation drift: " + summary());
        if (arm.equals("prewarm")) SmokeSupport.require(displays > 0 && listCalls > 0
            && fallbacks > 0 && drained > 0 && queued > 0 && finalQueued == 0,
            "M776 prewarm activation drift: " + summary());
        if (arm.equals("lod")) SmokeSupport.require(displays > 0 && drained > 0
            && animated > 0 && atRest > 0 && finalQueued == 0,
            "M776 LOD activation drift: " + summary());
    }

    long[] firstSight() {
        int count = countPhase(0), index = 0;
        long[] values = new long[count];
        for (int i = 0; i < walls.length; i++) if (phases[i] == 0) values[index++] = walls[i];
        return values;
    }

    double fps() {
        long total = 0L;
        for (long value : walls) total += value;
        return walls.length * 1_000_000_000.0D / total;
    }

    long p99() {
        long[] sorted = walls.clone();
        Arrays.sort(sorted);
        return sorted[Math.min(sorted.length - 1, (int) Math.ceil(sorted.length * 0.99D) - 1)];
    }

    String summary() {
        return arm + ":frames=" + frames + ",fps=" + round(fps()) + ",p99.ns=" + p99()
            + ",alloc/frame=" + round((double) allocatedBytes / frames)
            + ",atrest=" + atRest + "/" + listCalls + "/" + fallbacks
            + ",animated=" + animated + ",display=" + displays + "/" + displayPeak
            + ",prewarm=" + queued + "/" + drained + "/" + promoted + "/" + dropped;
    }

    private int countPhase(int target) {
        int count = 0;
        for (int value : phases) if (value == target) count++;
        return count;
    }

    private static String round(double value) { return String.format("%.2f", value); }
    private static int integer(Properties p, String key) { return Integer.parseInt(required(p, key)); }
    private static long number(Properties p, String key) { return Long.parseLong(required(p, key)); }
    private static String required(Properties p, String key) {
        String value = p.getProperty(key);
        if (value == null || value.isBlank()) throw new IllegalStateException("missing M776 " + key);
        return value.trim();
    }
}

record M776MatrixRound(int index, boolean coldFirst, M776MatrixArtifact direct,
                       M776MatrixArtifact cold, M776MatrixArtifact prewarm,
                       M776MatrixArtifact lod) {
    String summary() {
        return "round." + index + ".coldFirst=" + coldFirst + "," + direct.summary()
            + "," + cold.summary() + "," + prewarm.summary() + "," + lod.summary();
    }
}

/** Binds first-sight frame arrays to Worldline's neutral paired hitch gate. */
final class M776MatrixGate {
    private M776MatrixGate() {}

    static M776MatrixResult evaluate(Path product, List<M776MatrixRound> source,
                                     long threshold, long allowance) throws Exception {
        try (URLClassLoader loader = new URLClassLoader(new URL[] {product.toUri().toURL()}, null)) {
            Class<?> census = Class.forName("worldline.profiling.FrameCensus", true, loader);
            Class<?> gate = Class.forName("worldline.profiling.HitchRateGate", true, loader);
            Method of = census.getMethod("of", String[].class, long[][].class);
            Method pair = gate.getMethod("pair", census, census, boolean.class, String.class, long.class);
            List<Object> pairs = new ArrayList<Object>();
            for (M776MatrixRound round : source) pairs.add(pair.invoke(null,
                frame(of, round.cold().firstSight()), frame(of, round.prewarm().firstSight()),
                round.coldFirst(), "frame.wall.nanos", threshold));
            Object result = gate.getMethod("evaluate", List.class, long.class)
                .invoke(null, pairs, allowance);
            return new M776MatrixResult(number(result, "baselineRatePpm"),
                number(result, "candidateRatePpm"), number(result, "aggregateDeltaPpm"),
                (Boolean) result.getClass().getMethod("passes").invoke(result),
                result.getClass().getMethod("verdict").invoke(result).toString());
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
}

record M776MatrixResult(long baseline, long candidate, long delta,
                        boolean passes, String verdict) {
    String summary() {
        return "hitch.cold.first-sight.ppm=" + baseline
            + ",hitch.prewarm.first-sight.ppm=" + candidate
            + ",hitch.delta.ppm=" + delta + ",hitch.verdict=" + verdict;
    }
}
