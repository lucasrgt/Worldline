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

/** Qualifies page caching and camera-aware pre-bake in one restored scene. */
public final class M775AeroProfilerPageCachePrebakeMatrixCycle {
    private static final String ID = "m775-aero-profiler-page-cache-prebake-matrix";
    private static final String[][] ORDERS = {
        {"direct", "pages", "prebake"}, {"prebake", "pages", "direct"},
        {"pages", "direct", "prebake"}, {"prebake", "direct", "pages"}
    };
    private static final String TRACE = "v1|scene=restored-576|rounds=4|"
        + "orders=direct-pages-prebake+prebake-pages-direct+pages-direct-prebake+"
        + "prebake-direct-pages|journey=entry80+walk120+turn120+teleport130+drain150|"
        + "mutation=remove150+restore180|pages=off-vs-on|"
        + "prebake=off-vs-camera3-budget1|capture=wall+allocation+page+chunk+backlog|"
        + "hitch=50ms+5000ppm|claims=activation+drain+hitch-safety";
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID);
    private final Path build = root.resolve(".worldline/build/smoke").resolve(ID);
    private final Properties config = new Properties();

    private M775AeroProfilerPageCachePrebakeMatrixCycle() {}

    public static void main(String[] arguments) {
        if (arguments.length != 1 || !arguments[0].equals(ID)) {
            System.err.println("usage: M775AeroProfilerPageCachePrebakeMatrixCycle " + ID);
            System.exit(2);
        }
        try { new M775AeroProfilerPageCachePrebakeMatrixCycle().execute(); }
        catch (Exception error) {
            System.err.println("M775 page/cache/pre-bake matrix failed: " + error.getMessage());
            error.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        SmokeSupport.load(smoke.resolve("smoke.properties"), config);
        verifyDesign();
        Path aero = root.resolve(SmokeSupport.value(config, "aero.path")).normalize();
        M775MatrixRuntime runtime = new M775MatrixRuntime(root, smoke, build, config, aero);
        runtime.verifyCheckout();
        SmokeSupport.recreate(root, build);
        runtime.buildAero();
        Path template = build.resolve("template");
        runtime.runClient(template, true, "prepare");
        Path sourceWorld = template.resolve("saves/WorldlineAero");
        SmokeSupport.require(Files.isDirectory(sourceWorld), "M775 template world absent");
        List<M775MatrixRound> rounds = new ArrayList<M775MatrixRound>();
        for (int round = 0; round < ORDERS.length; round++) {
            M775MatrixArtifact direct = null, pages = null, prebake = null;
            for (String treatment : ORDERS[round]) {
                String arm = "round" + (round + 1) + "-" + treatment;
                Path game = build.resolve(arm);
                M775MatrixRuntime.copyWorld(sourceWorld, game.resolve("saves/WorldlineAero"));
                runtime.runClient(game, false, treatment);
                M775MatrixArtifact value = M775MatrixArtifact.read(game, treatment);
                value.verify();
                if (treatment.equals("direct")) direct = value;
                else if (treatment.equals("pages")) pages = value;
                else prebake = value;
            }
            rounds.add(new M775MatrixRound(round + 1,
                indexOf(ORDERS[round], "pages") < indexOf(ORDERS[round], "prebake"),
                direct, pages, prebake));
        }
        M775MatrixResult result = M775MatrixGate.evaluate(
            SmokeSupport.product(root, "profiling"), rounds,
            Long.parseLong(SmokeSupport.value(config, "hitch.threshold.nanos")),
            Long.parseLong(SmokeSupport.value(config, "allowed.regression.ppm")));
        SmokeSupport.require(result.passes(), "M775 hitch regression: " + result.summary());
        String signature = M775MatrixRuntime.sha256(TRACE);
        SmokeSupport.require(signature.equals(SmokeSupport.value(config, "expected.signature")),
            "M775 semantic signature drift: " + signature);
        StringBuilder evidence = new StringBuilder("id=").append(ID).append('\n');
        for (M775MatrixRound round : rounds) evidence.append(round.summary()).append('\n');
        evidence.append(result.summary()).append("\ntrace=").append(TRACE)
            .append("\nsignature=").append(signature).append('\n');
        Files.writeString(build.resolve("evidence.txt"), evidence, StandardCharsets.UTF_8);
        System.out.println("M775 Aero page/cache/pre-bake matrix passed");
        System.out.println("WORLDLINE_M775_SIGNAL=" + SmokeSupport.value(config, "expected.signal"));
        System.out.println("WORLDLINE_M775_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M775_SIGNATURE=" + signature);
    }

    private void verifyDesign() {
        SmokeSupport.require(SmokeSupport.value(config, "rounds").equals("4")
            && SmokeSupport.value(config, "retained.frames").equals("600")
            && SmokeSupport.value(config, "scheduler.budget").equals("1")
            && SmokeSupport.value(config, "lookahead.radius").equals("3")
            && SmokeSupport.value(config, "hitch.threshold.nanos").equals("50000000"),
            "M775 acquisition design drift");
    }

    private static int indexOf(String[] values, String target) {
        for (int i = 0; i < values.length; i++) if (values[i].equals(target)) return i;
        throw new IllegalArgumentException(target);
    }
}

/** Exact checkout, build, process, and restored-world boundary for M775. */
final class M775MatrixRuntime {
    private final Path smoke, aero;
    private final Properties config;

    M775MatrixRuntime(Path root, Path smoke, Path build, Properties config, Path aero) {
        this.smoke = smoke;
        this.config = config;
        this.aero = aero;
    }

    void verifyCheckout() throws Exception {
        SmokeSupport.require(Files.isDirectory(aero.resolve(".git"))
            || Files.isRegularFile(aero.resolve(".git")), "M775 Aero checkout absent");
        SmokeSupport.require(git("rev-parse", "HEAD").trim().equals(
            SmokeSupport.value(config, "aero.revision")), "M775 Aero revision drift");
        SmokeSupport.require(git("status", "--porcelain").isBlank(), "M775 Aero checkout dirty");
        String origin = git("remote", "get-url", "origin").trim()
            .replace("\\", "/").toLowerCase();
        SmokeSupport.require(origin.equals(SmokeSupport.value(config, "aero.repository").toLowerCase()),
            "M775 Aero origin drift: " + origin);
    }

    void buildAero() throws Exception {
        Path project = aero.resolve("stationapi");
        String output = SmokeSupport.capture(project, List.of(wrapper(project),
            "--no-daemon", "remapJar", "--rerun-tasks"),
            Integer.parseInt(SmokeSupport.value(config, "build.timeout.seconds")));
        SmokeSupport.require(output.contains("BUILD SUCCESSFUL") && Files.isRegularFile(
            project.resolve("build/libs/aero-model-lib-3.0.0.jar")), "M775 Aero build drift");
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
        System.out.println("[M775] start prepare=" + prepare + " arm=" + arm);
        String output = SmokeSupport.capture(project, command,
            Integer.parseInt(SmokeSupport.value(config, "child.timeout.seconds")));
        SmokeSupport.require(output.contains("[WorldlineM775] start prepare=" + prepare
            + " arm=" + arm) && output.contains("BUILD SUCCESSFUL"),
            "M775 client lifecycle drift: " + arm);
        String expected = prepare ? "template-ready machines=576 backlog=0"
            : "retained-complete arm=" + arm;
        SmokeSupport.require(output.contains("[WorldlineM775] " + expected),
            "M775 completion drift: " + arm);
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

    private static String wrapper(Path project) {
        return project.resolve(System.getProperty("os.name").startsWith("Windows")
            ? "gradlew.bat" : "gradlew").toString();
    }
}

/** One fresh M775 arm's complete retained-window artifact. */
final class M775MatrixArtifact {
    final String arm;
    final int frames, machines, finalBacklog, maxBacklog;
    final int chunkRebuilds, chunkMaxFrame, chunkBuilt, chunkPrebake;
    final int pageCalls, pageRebuilds, pageDirect, pageCachedMax;
    final long allocatedBytes, chunkMaximumNanos;
    final long[] walls;
    final int[] phases;

    private M775MatrixArtifact(Properties p, long[] walls, int[] phases) {
        arm = required(p, "arm");
        frames = integer(p, "frames");
        machines = integer(p, "machines");
        finalBacklog = integer(p, "final.backlog");
        maxBacklog = integer(p, "max.backlog");
        allocatedBytes = number(p, "frame.allocated.bytes");
        chunkRebuilds = integer(p, "chunk.rebuilds");
        chunkMaximumNanos = number(p, "chunk.rebuild.max.nanos");
        chunkMaxFrame = integer(p, "chunk.rebuild.max.frame");
        chunkBuilt = integer(p, "chunk.work.built");
        chunkPrebake = integer(p, "chunk.work.prebake");
        pageCalls = integer(p, "page.calls");
        pageRebuilds = integer(p, "page.rebuilds");
        pageDirect = integer(p, "page.direct");
        pageCachedMax = integer(p, "page.cached.max");
        this.walls = walls;
        this.phases = phases;
    }

    static M775MatrixArtifact read(Path game, String expected) throws Exception {
        Properties p = new Properties();
        try (Reader reader = Files.newBufferedReader(game.resolve("metrics.properties"))) {
            p.load(reader);
        }
        List<String> rows = Files.readAllLines(game.resolve("frames.csv"));
        long[] walls = new long[rows.size()];
        int[] phases = new int[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            String[] columns = rows.get(i).split(",", -1);
            SmokeSupport.require(columns.length == 3, "M775 frame row drift");
            phases[i] = Integer.parseInt(columns[0]);
            walls[i] = Long.parseLong(columns[1]);
        }
        M775MatrixArtifact value = new M775MatrixArtifact(p, walls, phases);
        SmokeSupport.require(value.arm.equals(expected) && value.frames == walls.length,
            "M775 artifact identity drift: " + expected);
        return value;
    }

    void verify() {
        SmokeSupport.require(frames >= 500 && machines == 576 && finalBacklog == 0
            && maxBacklog > 0 && allocatedBytes > 0L && chunkRebuilds > 0
            && chunkMaximumNanos > 0L, "M775 incomplete artifact: " + summary());
        for (int phase = 0; phase < 5; phase++)
            SmokeSupport.require(countPhase(phase) >= 60, "M775 phase absent: " + phase);
        if (arm.equals("direct")) SmokeSupport.require(pageCalls == 0 && pageCachedMax == 0
            && chunkPrebake == 0, "M775 direct contrast drift: " + summary());
        if (arm.equals("pages")) SmokeSupport.require(pageCalls > 0 && pageCachedMax > 0
            && pageRebuilds > 0 && chunkPrebake == 0,
            "M775 page activation drift: " + summary());
        if (arm.equals("prebake")) SmokeSupport.require(pageCalls > 0 && pageCachedMax > 0
            && pageRebuilds > 0 && chunkBuilt > 0 && chunkPrebake > 0 && chunkMaxFrame <= 1,
            "M775 pre-bake activation drift: " + summary());
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
        return sorted[Math.min(sorted.length - 1, (int) Math.ceil(sorted.length * 0.99D) - 1)];
    }

    String summary() {
        return arm + ":frames=" + frames + ",fps=" + round(fps())
            + ",p99.ns=" + p99() + ",alloc/frame=" + round(allocationPerFrame())
            + ",chunk=" + chunkRebuilds + "/" + chunkBuilt + "/" + chunkPrebake
            + ",pages=" + pageCalls + "/" + pageRebuilds + "/" + pageCachedMax
            + ",backlog=" + maxBacklog + "/" + finalBacklog;
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
        if (value == null || value.isBlank()) throw new IllegalStateException("missing M775 " + key);
        return value.trim();
    }
}

record M775MatrixRound(int index, boolean pagesFirst, M775MatrixArtifact direct,
                       M775MatrixArtifact pages, M775MatrixArtifact prebake) {
    String summary() {
        return "round." + index + ".pagesFirst=" + pagesFirst + ","
            + direct.summary() + "," + pages.summary() + "," + prebake.summary();
    }
}

/** Binds M775 frame arrays to Worldline's neutral paired hitch-rate gate. */
final class M775MatrixGate {
    private M775MatrixGate() {}

    static M775MatrixResult evaluate(Path product, List<M775MatrixRound> source,
                                     long threshold, long allowance) throws Exception {
        try (URLClassLoader loader = new URLClassLoader(new URL[] {product.toUri().toURL()}, null)) {
            Class<?> census = Class.forName("worldline.profiling.FrameCensus", true, loader);
            Class<?> gate = Class.forName("worldline.profiling.HitchRateGate", true, loader);
            Method of = census.getMethod("of", String[].class, long[][].class);
            Method pair = gate.getMethod("pair", census, census, boolean.class,
                String.class, long.class);
            List<Object> pairs = new ArrayList<Object>();
            for (M775MatrixRound round : source) pairs.add(pair.invoke(null,
                frame(of, round.pages().walls), frame(of, round.prebake().walls),
                round.pagesFirst(), "frame.wall.nanos", threshold));
            Object result = gate.getMethod("evaluate", List.class, long.class)
                .invoke(null, pairs, allowance);
            return new M775MatrixResult(number(result, "baselineRatePpm"),
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

record M775MatrixResult(long baseline, long candidate, long delta,
                        boolean passes, String verdict) {
    String summary() {
        return "hitch.pages.ppm=" + baseline + ",hitch.prebake.ppm=" + candidate
            + ",hitch.delta.ppm=" + delta + ",hitch.verdict=" + verdict;
    }
}
