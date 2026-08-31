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

/** Qualifies blind versus adaptive prewarm across fresh counterbalanced sessions. */
public final class M777AeroAdaptivePrewarmRepeatability {
    private static final String ID = "m777-aero-adaptive-prewarm-repeatability";
    private static final String[][] ORDERS = {
        {"cold", "blind", "adaptive", "pressured"},
        {"blind", "pressured", "cold", "adaptive"},
        {"adaptive", "cold", "pressured", "blind"},
        {"pressured", "adaptive", "blind", "cold"}
    };
    private static final String TRACE = "v2|scene=four-panels-120-15-models-plus-loader-decoys|sessions=4|"
        + "orders=cold-blind-adaptive-pressured+blind-pressured-cold-adaptive+"
        + "adaptive-cold-pressured-blind+pressured-adaptive-blind-cold|"
        + "journey=first-sight60+north60+east120+south120+west120+spin120|"
        + "admission=hidden-probe4-MegaCrusher+loader-decoys|"
        + "lists=cold-vs-blind1-vs-adaptive1|"
        + "pressure=explicit-speculative-probe+0.1ms+urgent-promotion|"
        + "capture=wall+cpu+allocation+per-frame-render-work+display-lists+prewarm+admission+pressure+first-use-miss+obj-cache|"
        + "gates=fixed600+aggregate+hitch5000ppm+fps3pct+p995pct+alloc5pct+3of4+decoy4of4+drain";
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID);
    private final Path build = root.resolve(".worldline/build/smoke").resolve(ID);
    private final Properties config = new Properties();

    private M777AeroAdaptivePrewarmRepeatability() {}

    public static void main(String[] arguments) {
        if (arguments.length != 1 || !arguments[0].equals(ID)) {
            System.err.println("usage: M777AeroAdaptivePrewarmRepeatability " + ID);
            System.exit(2);
        }
        try { new M777AeroAdaptivePrewarmRepeatability().execute(); }
        catch (Exception error) {
            System.err.println("M777 adaptive prewarm matrix failed: " + error.getMessage());
            error.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        SmokeSupport.load(smoke.resolve("smoke.properties"), config);
        verifyDesign();
        Path aero = root.resolve(SmokeSupport.value(config, "aero.path")).normalize();
        M777MatrixRuntime runtime = new M777MatrixRuntime(root, smoke, config, aero);
        runtime.verifyCheckout();
        SmokeSupport.recreate(root, build);
        runtime.buildAero();
        Path template = build.resolve("template");
        runtime.runClient(template, true, "prepare");
        Path sourceWorld = template.resolve("saves/WorldlineAero");
        SmokeSupport.require(Files.isDirectory(sourceWorld), "M777 template world absent");
        List<M777MatrixSession> sessions = new ArrayList<M777MatrixSession>();
        for (int session = 0; session < ORDERS.length; session++) {
            M777MatrixArtifact cold = null, blind = null, adaptive = null, pressured = null;
            for (String treatment : ORDERS[session]) {
                Path game = build.resolve("session" + (session + 1) + "-" + treatment);
                M777MatrixRuntime.copyWorld(sourceWorld, game.resolve("saves/WorldlineAero"));
                runtime.runClient(game, false, treatment);
                M777MatrixArtifact value = M777MatrixArtifact.read(game, treatment);
                value.verify();
                if (treatment.equals("cold")) cold = value;
                else if (treatment.equals("blind")) blind = value;
                else if (treatment.equals("adaptive")) adaptive = value;
                else pressured = value;
            }
            sessions.add(new M777MatrixSession(session + 1,
                indexOf(ORDERS[session], "cold") < indexOf(ORDERS[session], "adaptive"),
                cold, blind, adaptive, pressured));
        }
        M777MatrixResult result = M777MatrixGate.evaluate(
            SmokeSupport.product(root, "profiling"), sessions,
            Long.parseLong(SmokeSupport.value(config, "hitch.threshold.nanos")),
            Long.parseLong(SmokeSupport.value(config, "allowed.regression.ppm")));
        SmokeSupport.require(result.passes(), "M777 promotion gate failed: " + result.summary());
        String signature = M777MatrixRuntime.sha256(TRACE);
        SmokeSupport.require(signature.equals(SmokeSupport.value(config, "expected.signature")),
            "M777 semantic signature drift: " + signature);
        StringBuilder evidence = new StringBuilder("id=").append(ID).append('\n');
        for (M777MatrixSession session : sessions) evidence.append(session.summary()).append('\n');
        evidence.append(result.summary()).append("\ntrace=").append(TRACE)
            .append("\nsignature=").append(signature).append('\n');
        Files.writeString(build.resolve("evidence.txt"), evidence, StandardCharsets.UTF_8);
        System.out.println("M777 Aero adaptive prewarm repeatability passed");
        System.out.println("WORLDLINE_M777_SIGNAL=" + SmokeSupport.value(config, "expected.signal"));
        System.out.println("WORLDLINE_M777_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M777_SIGNATURE=" + signature);
    }

    private void verifyDesign() {
        SmokeSupport.require(SmokeSupport.value(config, "sessions").equals("4")
            && SmokeSupport.value(config, "retained.frames").equals("600")
            && SmokeSupport.value(config, "prewarm.budget").equals("1")
            && SmokeSupport.value(config, "model.identities").equals("15")
            && SmokeSupport.value(config, "hotness.threshold").equals("4")
            && SmokeSupport.value(config, "hitch.threshold.nanos").equals("50000000")
            && SmokeSupport.value(config, "fps.minimum.ratio").equals("0.97")
            && SmokeSupport.value(config, "p99.maximum.ratio").equals("1.05")
            && SmokeSupport.value(config, "allocation.maximum.ratio").equals("1.05")
            && SmokeSupport.value(config, "metric.sessions.minimum").equals("3")
            && SmokeSupport.value(config, "decoy.sessions.minimum").equals("4"),
            "M777 acquisition design drift");
    }

    private static int indexOf(String[] values, String target) {
        for (int i = 0; i < values.length; i++) if (values[i].equals(target)) return i;
        throw new IllegalArgumentException(target);
    }
}

/** Exact checkout, build, process, and restored-world boundary for M777. */
final class M777MatrixRuntime {
    private final Path root, smoke, aero;
    private final Properties config;

    M777MatrixRuntime(Path root, Path smoke, Properties config, Path aero) {
        this.root = root;
        this.smoke = smoke;
        this.config = config;
        this.aero = aero;
    }

    void verifyCheckout() throws Exception {
        SmokeSupport.require(Files.exists(aero.resolve(".git")), "M777 Aero checkout absent");
        SmokeSupport.require(git("rev-parse", "HEAD").trim().equals(
            SmokeSupport.value(config, "aero.revision")), "M777 Aero revision drift");
        SmokeSupport.require(git("status", "--porcelain").isBlank(), "M777 Aero checkout dirty");
        String origin = git("remote", "get-url", "origin").trim().replace("\\", "/").toLowerCase();
        SmokeSupport.require(origin.equals(SmokeSupport.value(config, "aero.repository").toLowerCase()),
            "M777 Aero origin drift: " + origin);
    }

    void buildAero() throws Exception {
        Path project = aero.resolve("stationapi");
        String output = SmokeSupport.capture(project, List.of(wrapper(project),
            "--no-daemon", "remapJar", "--rerun-tasks"),
            Integer.parseInt(SmokeSupport.value(config, "build.timeout.seconds")));
        SmokeSupport.require(output.contains("BUILD SUCCESSFUL") && Files.isRegularFile(
            project.resolve("build/libs/aero-model-lib-3.0.0.jar")), "M777 Aero build drift");
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
        System.out.println("[M777] start prepare=" + prepare + " arm=" + arm);
        String output = SmokeSupport.capture(project, command,
            Integer.parseInt(SmokeSupport.value(config, "child.timeout.seconds")));
        SmokeSupport.require(output.contains("[WorldlineM777] start prepare=" + prepare
            + " arm=" + arm) && output.contains("BUILD SUCCESSFUL"),
            "M777 client lifecycle drift: " + arm);
        String expected = prepare ? "template-ready machines=120" : "retained-complete arm=" + arm;
        SmokeSupport.require(output.contains("[WorldlineM777] " + expected),
            "M777 completion drift: " + arm);
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

/** One fresh M777 treatment's retained-window artifact. */
final class M777MatrixArtifact {
    final String arm;
    final int frames, capturedFrames, machines, atRest, listCalls, fallbacks, animated;
    final int displays, displayLive, displayPeak, denied, failed;
    final int drained, urgent, queued, promoted, dropped, maxQueued, finalQueued, samples;
    final int admissionTracked, admissionAccepted, admissionRejected, admissionExpired;
    final int pressureSkips, firstUseMisses, objCacheSize;
    final long allocatedBytes, cpuNanos;
    final long[] walls, cpus;
    final int[] frameAtRest, frameListCalls;
    final int[] phases;

    private M777MatrixArtifact(Properties p, long[] walls, long[] cpus, int[] phases,
                               int[] frameAtRest, int[] frameListCalls,
                               long comparisonAllocatedBytes, long comparisonCpuNanos) {
        arm = required(p, "arm");
        frames = walls.length;
        capturedFrames = integer(p, "frames");
        machines = integer(p, "machines");
        SmokeSupport.require(number(p, "frame.allocated.bytes") > 0L,
            "M777 total allocation absent");
        SmokeSupport.require(number(p, "frame.cpu.nanos") > 0L,
            "M777 total CPU time absent");
        allocatedBytes = comparisonAllocatedBytes;
        cpuNanos = comparisonCpuNanos;
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
        admissionTracked = integer(p, "prewarm.admission.tracked");
        admissionAccepted = integer(p, "prewarm.admission.accepted");
        admissionRejected = integer(p, "prewarm.admission.rejected");
        admissionExpired = integer(p, "prewarm.admission.expired");
        pressureSkips = integer(p, "prewarm.pressure.skips");
        firstUseMisses = integer(p, "prewarm.firstuse.misses");
        objCacheSize = integer(p, "obj.cache.size");
        samples = integer(p, "render.samples");
        this.walls = walls;
        this.cpus = cpus;
        this.phases = phases;
        this.frameAtRest = frameAtRest;
        this.frameListCalls = frameListCalls;
    }

    static M777MatrixArtifact read(Path game, String expected) throws Exception {
        Properties p = new Properties();
        try (Reader reader = Files.newBufferedReader(game.resolve("metrics.properties"))) { p.load(reader); }
        List<String> rows = Files.readAllLines(game.resolve("frames.csv"));
        SmokeSupport.require(rows.size() >= 600, "M777 fixed comparison window absent");
        long[] walls = new long[600];
        long[] cpus = new long[600];
        int[] phases = new int[600];
        int[] frameAtRest = new int[600];
        int[] frameListCalls = new int[600];
        long comparisonAllocatedBytes = 0L, comparisonCpuNanos = 0L;
        for (int i = 0; i < rows.size(); i++) {
            String[] columns = rows.get(i).split(",", -1);
            SmokeSupport.require(columns.length == 6, "M777 frame row drift");
            if (i >= walls.length) continue;
            phases[i] = Integer.parseInt(columns[0]);
            walls[i] = Long.parseLong(columns[1]);
            comparisonAllocatedBytes = Math.addExact(comparisonAllocatedBytes,
                Long.parseLong(columns[2]));
            cpus[i] = Long.parseLong(columns[3]);
            comparisonCpuNanos = Math.addExact(comparisonCpuNanos, cpus[i]);
            frameAtRest[i] = Integer.parseInt(columns[4]);
            frameListCalls[i] = Integer.parseInt(columns[5]);
        }
        M777MatrixArtifact value = new M777MatrixArtifact(
            p, walls, cpus, phases, frameAtRest, frameListCalls,
            comparisonAllocatedBytes, comparisonCpuNanos);
        SmokeSupport.require(value.arm.equals(expected) && value.capturedFrames == rows.size(),
            "M777 artifact identity drift: " + expected);
        return value;
    }

    void verify() {
        SmokeSupport.require(frames == 600 && capturedFrames >= 600 && samples >= capturedFrames
            && machines == 120 && allocatedBytes > 0L && cpuNanos > 0L
            && denied == 0 && failed == 0, "M777 incomplete artifact: " + summary());
        for (int phase = 0; phase < 6; phase++)
            SmokeSupport.require(countPhase(phase) >= 50, "M777 phase absent: " + phase);
        if (arm.equals("cold")) SmokeSupport.require(displays > 0 && listCalls > 0
            && drained == 0 && admissionTracked == 0,
            "M777 cold activation drift: " + summary());
        if (arm.equals("blind")) SmokeSupport.require(displays > 0 && listCalls > 0
            && fallbacks > 0 && drained > 0 && queued > 0 && finalQueued == 0
            && admissionTracked == 0, "M777 blind activation drift: " + summary());
        if (arm.equals("adaptive")) SmokeSupport.require(displays > 0 && listCalls > 0
            && queued > 0 && (drained > 0 || firstUseMisses > 0) && finalQueued == 0
            && admissionAccepted > 0 && admissionTracked > 0,
            "M777 adaptive activation drift: " + summary());
        if (arm.equals("pressured")) SmokeSupport.require(displays > 0 && listCalls > 0
            && drained > 0 && urgent > 0 && finalQueued == 0 && admissionAccepted > 0
            && admissionTracked > 0 && pressureSkips > 0,
            "M777 pressure activation drift: " + summary());
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

    double allocationPerFrame() { return (double) allocatedBytes / frames; }
    double cpuPerFrame() { return (double) cpuNanos / frames; }
    double cpuUtilization() { return (double) cpuNanos / duration(); }

    long retainedAtRest() {
        long total = 0L;
        for (int value : frameAtRest) total += value;
        return total;
    }

    long retainedListCalls() {
        long total = 0L;
        for (int value : frameListCalls) total += value;
        return total;
    }

    long duration() {
        long total = 0L;
        for (long value : walls) total = Math.addExact(total, value);
        return total;
    }

    String summary() {
        return arm + ":frames=" + frames + "/" + capturedFrames
            + ",fps=" + round(fps()) + ",p99.ns=" + p99()
            + ",alloc/frame=" + round((double) allocatedBytes / frames)
            + ",cpu/frame=" + round(cpuPerFrame())
            + ",cpu/wall=" + round(cpuUtilization())
            + ",retained-render=" + retainedAtRest() + "/" + retainedListCalls()
            + ",atrest=" + atRest + "/" + listCalls + "/" + fallbacks
            + ",animated=" + animated + ",display=" + displays + "/" + displayPeak
            + ",prewarm=" + queued + "/" + drained + "/" + promoted + "/" + dropped
            + ",admission=" + admissionTracked + "/" + admissionAccepted + "/"
            + admissionRejected + "/" + admissionExpired + ",pressure=" + pressureSkips
            + ",first-use-miss=" + firstUseMisses + ",obj-cache=" + objCacheSize;
    }

    private int countPhase(int target) {
        int count = 0;
        for (int value : phases) if (value == target) count++;
        return count;
    }

    private static String round(double value) {
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }
    private static int integer(Properties p, String key) { return Integer.parseInt(required(p, key)); }
    private static long number(Properties p, String key) { return Long.parseLong(required(p, key)); }
    private static String required(Properties p, String key) {
        String value = p.getProperty(key);
        if (value == null || value.isBlank()) throw new IllegalStateException("missing M777 " + key);
        return value.trim();
    }
}

record M777MatrixSession(int index, boolean coldFirst, M777MatrixArtifact cold,
                         M777MatrixArtifact blind, M777MatrixArtifact adaptive,
                         M777MatrixArtifact pressured) {
    String summary() {
        return "session." + index + ".coldFirst=" + coldFirst + "," + cold.summary()
            + "," + blind.summary() + "," + adaptive.summary() + "," + pressured.summary();
    }
}

/** Binds first-sight frame arrays to Worldline's neutral paired hitch gate. */
final class M777MatrixGate {
    private M777MatrixGate() {}

    static M777MatrixResult evaluate(Path product, List<M777MatrixSession> source,
                                     long threshold, long allowance) throws Exception {
        try (URLClassLoader loader = new URLClassLoader(new URL[] {product.toUri().toURL()}, null)) {
            Class<?> census = Class.forName("worldline.profiling.FrameCensus", true, loader);
            Class<?> gate = Class.forName("worldline.profiling.HitchRateGate", true, loader);
            Method of = census.getMethod("of", String[].class, long[][].class);
            Method pair = gate.getMethod("pair", census, census, boolean.class, String.class, long.class);
            List<Object> pairs = new ArrayList<Object>();
            long coldDuration = 0L, adaptiveDuration = 0L;
            long coldAllocation = 0L, adaptiveAllocation = 0L;
            long coldCpu = 0L, adaptiveCpu = 0L;
            long[] coldFrames = new long[source.size() * 600];
            long[] adaptiveFrames = new long[source.size() * 600];
            int fpsBoundSessions = 0, p99BoundSessions = 0, allocationBoundSessions = 0;
            int decoyBenefitSessions = 0, offset = 0;
            boolean lifecyclePass = true;
            for (M777MatrixSession session : source) {
                Object value = pair.invoke(null, frame(of, session.cold().firstSight()),
                    frame(of, session.adaptive().firstSight()), session.coldFirst(),
                    "frame.wall.nanos", threshold);
                pairs.add(value);
                M777MatrixArtifact cold = session.cold(), adaptive = session.adaptive();
                if (adaptive.fps() >= cold.fps() * 0.97D) fpsBoundSessions++;
                if (adaptive.p99() <= cold.p99() * 1.05D) p99BoundSessions++;
                if (adaptive.allocationPerFrame() <= cold.allocationPerFrame() * 1.05D)
                    allocationBoundSessions++;
                if (session.blind().displays > adaptive.displays) decoyBenefitSessions++;
                lifecyclePass &= session.blind().displays > adaptive.displays
                    && adaptive.displays <= cold.displays
                    && adaptive.objCacheSize > adaptive.admissionAccepted
                    && session.pressured().pressureSkips > 0
                    && session.pressured().urgent > 0 && session.pressured().finalQueued == 0;
                coldDuration = Math.addExact(coldDuration, cold.duration());
                adaptiveDuration = Math.addExact(adaptiveDuration, adaptive.duration());
                coldAllocation = Math.addExact(coldAllocation, cold.allocatedBytes);
                adaptiveAllocation = Math.addExact(adaptiveAllocation, adaptive.allocatedBytes);
                coldCpu = Math.addExact(coldCpu, cold.cpuNanos);
                adaptiveCpu = Math.addExact(adaptiveCpu, adaptive.cpuNanos);
                System.arraycopy(cold.walls, 0, coldFrames, offset, cold.walls.length);
                System.arraycopy(adaptive.walls, 0, adaptiveFrames, offset, adaptive.walls.length);
                offset += cold.walls.length;
            }
            double fpsRatio = (double) coldDuration / adaptiveDuration;
            double p99Ratio = (double) quantile99(adaptiveFrames) / quantile99(coldFrames);
            double allocationRatio = (double) adaptiveAllocation / coldAllocation;
            double cpuRatio = (double) adaptiveCpu / coldCpu;
            boolean metricBoundsPass = fpsRatio >= 0.97D && p99Ratio <= 1.05D
                && allocationRatio <= 1.05D;
            boolean metricSessionsPass = fpsBoundSessions >= 3 && p99BoundSessions >= 3
                && allocationBoundSessions >= 3;
            Object result = gate.getMethod("evaluate", List.class, long.class)
                .invoke(null, pairs, allowance);
            return new M777MatrixResult(number(result, "baselineRatePpm"),
                number(result, "candidateRatePpm"), number(result, "aggregateDeltaPpm"),
                (Boolean) result.getClass().getMethod("passes").invoke(result),
                metricBoundsPass, metricSessionsPass, lifecyclePass,
                fpsBoundSessions, p99BoundSessions, allocationBoundSessions,
                decoyBenefitSessions, fpsRatio, p99Ratio, allocationRatio,
                cpuRatio,
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

    private static long quantile99(long[] values) {
        long[] sorted = values.clone();
        Arrays.sort(sorted);
        return sorted[(int) Math.ceil(sorted.length * 0.99D) - 1];
    }

    private static long number(Object value, String method) throws Exception {
        return ((Number) value.getClass().getMethod(method).invoke(value)).longValue();
    }
}

record M777MatrixResult(long baseline, long candidate, long delta, boolean aggregateHitchPass,
                        boolean metricBoundsPass, boolean metricSessionsPass,
                        boolean lifecyclePass, int fpsBoundSessions, int p99BoundSessions,
                        int allocationBoundSessions, int decoyBenefitSessions,
                        double fpsRatio, double p99Ratio, double allocationRatio,
                        double cpuRatio, String verdict) {
    boolean passes() {
        return aggregateHitchPass && metricBoundsPass && metricSessionsPass
            && lifecyclePass && decoyBenefitSessions == 4;
    }

    String summary() {
        return "hitch.cold.first-sight.ppm=" + baseline
            + ",hitch.adaptive.first-sight.ppm=" + candidate
            + ",hitch.delta.ppm=" + delta + ",hitch.verdict=" + verdict
            + ",metric.bounds.pass=" + metricBoundsPass
            + ",metric.sessions.pass=" + metricSessionsPass
            + ",metric.sessions=" + fpsBoundSessions + "/" + p99BoundSessions
            + "/" + allocationBoundSessions
            + ",metric.ratios=" + format(fpsRatio) + "/" + format(p99Ratio)
            + "/" + format(allocationRatio)
            + ",diagnostic.cpu.ratio=" + format(cpuRatio)
            + ",lifecycle.pass=" + lifecyclePass
            + ",decoy.benefit.sessions=" + decoyBenefitSessions;
    }

    private static String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.3f", value);
    }
}
