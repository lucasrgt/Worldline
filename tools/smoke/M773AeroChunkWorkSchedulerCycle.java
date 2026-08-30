import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

/** Qualifies Aero's bounded visibility, age, and debt-aware chunk scheduler. */
public final class M773AeroChunkWorkSchedulerCycle {
    private static final String ID = "m773-aero-chunk-work-scheduler";
    private static final boolean[] REFERENCE_FIRST = {true, false, false, true};
    private static final String TRACE = "v1|scene=restored-solid-576|pairs=4|"
        + "priority=visible-before-debt|debt=8|budget=1-per-frame|"
        + "stress=16-real-chunk-builders-camera-spin|drain=zero|hitch=50ms";
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID);
    private final Path build = root.resolve(".worldline/build/smoke").resolve(ID);
    private final Properties config = new Properties();

    private M773AeroChunkWorkSchedulerCycle() {}

    public static void main(String[] arguments) {
        if (arguments.length != 1 || !arguments[0].equals(ID)) {
            System.err.println("usage: M773AeroChunkWorkSchedulerCycle " + ID);
            System.exit(2);
        }
        try { new M773AeroChunkWorkSchedulerCycle().execute(); }
        catch (Exception error) {
            System.err.println("M773 chunk-work scheduler failed: " + error.getMessage());
            error.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        SmokeSupport.load(smoke.resolve("smoke.properties"), config);
        verifyDesign();
        Path checkout = root.resolve(SmokeSupport.value(config, "aero.path")).normalize();
        M773Workspace.verify(checkout, config);
        SmokeSupport.recreate(root, build);
        buildAero(checkout);
        Path template = build.resolve("template");
        runClient(checkout, template, true, "prepare");
        Path sourceWorld = template.resolve("saves/WorldlineAero");
        SmokeSupport.require(Files.isDirectory(sourceWorld), "M773 template world absent");
        List<M773Pair> pairs = new ArrayList<>();
        int smaller = 0;
        for (int index = 0; index < REFERENCE_FIRST.length; index++) {
            String[] order = REFERENCE_FIRST[index]
                ? new String[] {"reference", "candidate"}
                : new String[] {"candidate", "reference"};
            M773Artifact reference = null, candidate = null;
            for (String treatment : order) {
                String arm = "pair" + (index + 1) + "-" + treatment;
                Path game = build.resolve(arm);
                Files.createDirectories(game.resolve("saves"));
                M773Workspace.copyTree(sourceWorld, game.resolve("saves/WorldlineAero"));
                String output = runClient(checkout, game, false, arm);
                SmokeSupport.require(output.contains("[WorldlineM773] retained-complete arm=" + arm)
                    && output.contains("BUILD SUCCESSFUL"), "M773 lifecycle drift: " + arm);
                M773Artifact value = M773Artifact.read(game, arm);
                value.verify(treatment.equals("candidate"));
                if (treatment.equals("reference")) reference = value;
                else candidate = value;
            }
            SmokeSupport.require(reference != null && candidate != null,
                "M773 paired artifacts absent: " + index);
            if (candidate.maximumRebuildNanos < reference.maximumRebuildNanos) smaller++;
            pairs.add(new M773Pair(index + 1, REFERENCE_FIRST[index], reference, candidate));
        }
        SmokeSupport.require(smaller >= 3,
            "M773 rebuild maximum did not improve in a majority: " + smaller);
        M773GateResult gate = M773GateBinding.evaluate(product("profiling"), pairs,
            Long.parseLong(SmokeSupport.value(config, "hitch.threshold.nanos")),
            Long.parseLong(SmokeSupport.value(config, "allowed.regression.ppm")));
        SmokeSupport.require(gate.passes(), "M773 hitch-rate regression: " + gate.verdict());
        String signature = sha256(TRACE);
        SmokeSupport.require(signature.equals(SmokeSupport.value(config, "expected.signature")),
            "M773 semantic signature drift: " + signature);
        StringBuilder evidence = new StringBuilder("id=").append(ID).append('\n');
        for (M773Pair pair : pairs) evidence.append(pair.summary()).append('\n');
        evidence.append("rebuild.max.smaller.pairs=").append(smaller).append('\n')
            .append(gate.summary()).append("\ntrace=").append(TRACE)
            .append("\nsignature=").append(signature).append('\n');
        Files.writeString(build.resolve("evidence.txt"), evidence, StandardCharsets.UTF_8);
        System.out.println("M773 Aero chunk-work scheduler passed");
        System.out.println("WORLDLINE_M773_SIGNAL=" + SmokeSupport.value(config, "expected.signal"));
        System.out.println("WORLDLINE_M773_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M773_SIGNATURE=" + signature);
    }

    private String runClient(Path checkout, Path game, boolean prepare, String arm)
            throws Exception {
        Files.createDirectories(game);
        Path project = checkout.resolve("stationapi/test");
        String wrapper = System.getProperty("os.name").startsWith("Windows")
            ? "gradlew.bat" : "gradlew";
        List<String> command = List.of(project.resolve(wrapper).toString(), "--no-daemon",
            "runClient", "--init-script", smoke.resolve("scheduler.init.gradle").toString(),
            "-PworldlineAeroClasses=" + checkout.resolve("stationapi/build/classes/java/main"),
            "-PworldlineAeroJar=" + checkout.resolve(
                "stationapi/build/libs/aero-model-lib-3.0.0.jar"),
            "-PworldlineRunDir=" + game, "-PworldlinePrepare=" + prepare,
            "-PworldlineArm=" + arm,
            "-PworldlineFrames=" + SmokeSupport.value(config, "retained.frames"),
            "-PworldlineMinimumMillis=" + SmokeSupport.value(config, "minimum.millis"),
            "-PworldlineMetrics=" + game.resolve("metrics.properties"),
            "-PworldlineFrameArtifact=" + game.resolve("frames.txt"));
        System.out.println("[M773] start prepare=" + prepare + " arm=" + arm);
        String output = SmokeSupport.capture(project, command,
            Integer.parseInt(SmokeSupport.value(config, "child.timeout.seconds")));
        SmokeSupport.require(output.contains("[WorldlineM773] start prepare=" + prepare
            + " arm=" + arm), "M773 client launch drift: " + arm);
        if (prepare) SmokeSupport.require(
            output.contains("[WorldlineM773] template-ready machines=576 backlog=0"),
            "M773 template preparation drift");
        return output;
    }

    private void buildAero(Path checkout) throws Exception {
        Path project = checkout.resolve("stationapi");
        String wrapper = System.getProperty("os.name").startsWith("Windows")
            ? "gradlew.bat" : "gradlew";
        String output = SmokeSupport.capture(project, List.of(project.resolve(wrapper).toString(),
            "--no-daemon", "remapJar", "--rerun-tasks"),
            Integer.parseInt(SmokeSupport.value(config, "build.timeout.seconds")));
        SmokeSupport.require(output.contains("BUILD SUCCESSFUL") && Files.isRegularFile(
            project.resolve("build/libs/aero-model-lib-3.0.0.jar")), "M773 Aero build drift");
    }

    private void verifyDesign() {
        SmokeSupport.require(SmokeSupport.value(config, "pairs").equals("4")
            && SmokeSupport.value(config, "scheduler.budget").equals("1")
            && SmokeSupport.value(config, "scheduler.debt.limit").equals("8")
            && SmokeSupport.value(config, "retained.frames").equals("600")
            && SmokeSupport.value(config, "hitch.threshold.nanos").equals("50000000"),
            "M773 acquisition design drift");
    }

    private Path product(String module) { return SmokeSupport.product(root, module); }
    private static String sha256(String value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
            .digest(value.getBytes(StandardCharsets.UTF_8)));
    }
}

final class M773Workspace {
    private M773Workspace() {}
    static void verify(Path checkout, Properties config) throws Exception {
        SmokeSupport.require(Files.isDirectory(checkout.resolve(".git"))
            || Files.isRegularFile(checkout.resolve(".git")), "Aero checkout absent");
        SmokeSupport.require(git(checkout, "rev-parse", "HEAD").trim().equals(
            SmokeSupport.value(config, "aero.revision")), "Aero revision drift");
        SmokeSupport.require(git(checkout, "status", "--porcelain").isBlank(),
            "Aero checkout is dirty");
        String origin = git(checkout, "remote", "get-url", "origin").trim()
            .replace("\\", "/").toLowerCase();
        SmokeSupport.require(origin.equals(SmokeSupport.value(config, "aero.repository").toLowerCase()),
            "Aero origin drift: " + origin);
    }
    static void copyTree(Path source, Path destination) throws IOException {
        try (Stream<Path> paths = Files.walk(source)) {
            for (Path path : paths.sorted().toList()) {
                Path target = destination.resolve(source.relativize(path));
                if (Files.isDirectory(path)) Files.createDirectories(target);
                else Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.COPY_ATTRIBUTES);
            }
        }
    }
    private static String git(Path checkout, String... args) throws Exception {
        ArrayList<String> command = new ArrayList<>();
        command.add("git");
        command.addAll(List.of(args));
        return SmokeSupport.capture(checkout, command, 60);
    }
}

final class M773Artifact {
    final String arm;
    final int frames, rebuilds, visible, maxBuilds, hiddenFrame, maxBacklog, finalBacklog;
    final long maximumRebuildNanos;
    final long[] walls;

    private M773Artifact(String arm, Properties p, long[] walls) {
        this.arm = arm;
        frames = integer(p, "frames");
        rebuilds = integer(p, "total.rebuilds");
        visible = integer(p, "visible.rebuilds");
        maxBuilds = integer(p, "max.rebuilds.frame");
        hiddenFrame = integer(p, "hidden.target.frame");
        maxBacklog = integer(p, "max.backlog");
        finalBacklog = integer(p, "final.backlog");
        maximumRebuildNanos = Long.parseLong(required(p, "max.rebuild.nanos.frame"));
        this.walls = walls;
    }

    static M773Artifact read(Path game, String expected) throws Exception {
        Properties p = new Properties();
        try (java.io.Reader reader = Files.newBufferedReader(game.resolve("metrics.properties"))) {
            p.load(reader);
        }
        List<String> rows = Files.readAllLines(game.resolve("frames.txt"));
        long[] walls = new long[rows.size()];
        for (int i = 0; i < walls.length; i++) walls[i] = Long.parseLong(rows.get(i));
        M773Artifact value = new M773Artifact(required(p, "arm"), p, walls);
        SmokeSupport.require(value.arm.equals(expected) && value.frames == walls.length,
            "M773 artifact identity drift: " + expected);
        SmokeSupport.require(integer(p, "machines") == 576, "M773 machine census drift");
        return value;
    }

    void verify(boolean candidate) {
        SmokeSupport.require(frames >= 500 && rebuilds >= 100 && visible > 0
            && maxBacklog >= 8 && finalBacklog == 0 && maximumRebuildNanos > 0L,
            "M773 incomplete artifact: " + arm);
        if (candidate) SmokeSupport.require(maxBuilds == 1
            && hiddenFrame >= 9 && hiddenFrame <= 16,
            "M773 candidate scheduling invariant failed: " + summary());
        else SmokeSupport.require(maxBuilds >= 2 && hiddenFrame >= 1,
            "M773 reference contrast absent: " + summary());
    }

    String summary() {
        return arm + ":frames=" + frames + ",rebuilds=" + rebuilds
            + ",visible=" + visible + ",max.frame=" + maxBuilds
            + ",hidden.frame=" + hiddenFrame + ",max.backlog=" + maxBacklog
            + ",max.rebuild.ns=" + maximumRebuildNanos;
    }
    private static int integer(Properties p, String key) {
        return Integer.parseInt(required(p, key));
    }
    private static String required(Properties p, String key) {
        String value = p.getProperty(key);
        if (value == null || value.isBlank()) throw new IllegalStateException("missing M773 " + key);
        return value.trim();
    }
}

record M773Pair(int index, boolean referenceFirst, M773Artifact reference,
                M773Artifact candidate) {
    String summary() {
        return "pair." + index + ".order=" + (referenceFirst ? "AB" : "BA")
            + "," + reference.summary() + "," + candidate.summary();
    }
}

final class M773GateBinding {
    private M773GateBinding() {}
    static M773GateResult evaluate(Path product, List<M773Pair> source,
            long threshold, long allowance) throws Exception {
        try (URLClassLoader loader = new URLClassLoader(new URL[] {product.toUri().toURL()}, null)) {
            Class<?> census = Class.forName("worldline.profiling.FrameCensus", true, loader);
            Class<?> gate = Class.forName("worldline.profiling.HitchRateGate", true, loader);
            Method of = census.getMethod("of", String[].class, long[][].class);
            Method pair = gate.getMethod("pair", census, census, boolean.class,
                String.class, long.class);
            List<Object> pairs = new ArrayList<>();
            for (M773Pair value : source) pairs.add(pair.invoke(null,
                frame(of, value.reference().walls), frame(of, value.candidate().walls),
                value.referenceFirst(), "frame.wall.nanos", threshold));
            Object result = gate.getMethod("evaluate", List.class, long.class)
                .invoke(null, pairs, allowance);
            return new M773GateResult(number(result, "baselineRatePpm"),
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

record M773GateResult(long baseline, long candidate, long delta,
                      boolean passes, String verdict) {
    String summary() {
        return "hitch.baseline.ppm=" + baseline + ",hitch.candidate.ppm=" + candidate
            + ",hitch.delta.ppm=" + delta + ",hitch.verdict=" + verdict;
    }
}
