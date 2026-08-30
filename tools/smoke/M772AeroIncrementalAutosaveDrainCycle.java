import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

/** Qualifies bounded autosave work, forced drainage, persistence, and hitch rate. */
public final class M772AeroIncrementalAutosaveDrainCycle {
    private static final String ID = "m772-aero-incremental-autosave-drain";
    private static final boolean[] REFERENCE_FIRST = {true, false, false, true};
    private static final String TRACE = "v1|scene=ultra-12-chunk-3072-machine-min"
            + "|design=4-pairs-AB+BA+BA+AB|autosave=native-40-tick"
            + "|treatment=aero.world.incremental-autosave-budget-1"
            + "|mutation=12-gold-then-12-diamond-sentinels"
            + "|nonforced=fair-progress-12-unique-before-tick-540"
            + "|forced=all-dirty-to-0"
            + "|reload=12-diamond-sentinels|frames=complete"
            + "|gate=hitch-rate-no-regression+save-max-majority-smaller";
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID);
    private final Path build = root.resolve(".worldline/smokes").resolve(ID);
    private final Properties config = new Properties();

    private M772AeroIncrementalAutosaveDrainCycle() {}

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: M772AeroIncrementalAutosaveDrainCycle " + ID);
            System.exit(2);
        }
        try {
            new M772AeroIncrementalAutosaveDrainCycle().execute();
        } catch (Exception error) {
            System.err.println("M772 incremental autosave drain failed: " + error.getMessage());
            error.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        SmokeSupport.load(smoke.resolve("smoke.properties"), config);
        verifyDesign();
        Path checkout = root.resolve(SmokeSupport.value(config, "aero.path")).normalize();
        M772Workspace.verifyCheckout(checkout, config);
        SmokeSupport.recreate(root, build);
        buildAero(checkout);
        Path template = build.resolve("template");
        runClient(checkout, template, "prepare", "prepare");
        Path sourceWorld = template.resolve("saves/WorldlineAero");
        SmokeSupport.require(Files.isDirectory(sourceWorld), "M772 template world absent");
        List<M772Pair> pairs = new ArrayList<>();
        int smaller = 0;
        for (int index = 0; index < REFERENCE_FIRST.length; index++) {
            String[] order = REFERENCE_FIRST[index]
                    ? new String[] {"reference", "candidate"}
                    : new String[] {"candidate", "reference"};
            M772Artifact reference = null;
            M772Artifact candidate = null;
            for (String treatment : order) {
                String arm = "pair" + (index + 1) + "-" + treatment;
                Path game = build.resolve(arm);
                Files.createDirectories(game.resolve("saves"));
                M772Workspace.copyTree(sourceWorld, game.resolve("saves/WorldlineAero"));
                String output = runClient(checkout, game, "measure", arm);
                SmokeSupport.require(output.contains("[WorldlineM772] retained-complete arm=" + arm),
                        "M772 measured lifecycle drift: " + arm);
                String verify = runClient(checkout, game, "verify", arm);
                SmokeSupport.require(verify.contains("[WorldlineM772] persistence-pass arm=" + arm
                                + " sentinels=12"), "M772 fresh persistence drift: " + arm);
                M772Artifact artifact = M772Artifact.read(game, arm);
                artifact.verify(treatment.equals("candidate"));
                if (treatment.equals("reference")) {
                    reference = artifact;
                } else {
                    candidate = artifact;
                }
            }
            SmokeSupport.require(reference != null && candidate != null,
                    "M772 paired artifacts absent: " + index);
            if (candidate.maxSaveNanos < reference.maxSaveNanos) {
                smaller++;
            }
            pairs.add(new M772Pair(index + 1, REFERENCE_FIRST[index], reference, candidate));
        }
        SmokeSupport.require(smaller >= 3,
                "M772 save maximum did not improve in a majority: " + smaller);
        M772GateResult gate = M772GateBinding.evaluate(product("profiling"), pairs,
                Long.parseLong(SmokeSupport.value(config, "hitch.threshold.nanos")),
                Long.parseLong(SmokeSupport.value(config, "allowed.regression.ppm")));
        SmokeSupport.require(gate.passes(),
                "M772 hitch-rate regression: " + gate.verdict());
        String signature = sha256(TRACE);
        SmokeSupport.require(signature.equals(SmokeSupport.value(config, "expected.signature")),
                "M772 semantic signature drift: " + signature);
        StringBuilder evidence = new StringBuilder("id=").append(ID).append('\n');
        for (M772Pair pair : pairs) evidence.append(pair.summary()).append('\n');
        evidence.append("save.max.smaller.pairs=").append(smaller).append("\n")
                .append(gate.summary()).append("\ntrace=").append(TRACE)
                .append("\nsignature=").append(signature).append('\n');
        Files.writeString(build.resolve("evidence.txt"), evidence, StandardCharsets.UTF_8);
        System.out.println("M772 Aero incremental autosave drain passed");
        System.out.println("WORLDLINE_M772_SIGNAL=" + SmokeSupport.value(config, "expected.signal"));
        System.out.println("WORLDLINE_M772_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M772_SIGNATURE=" + signature);
    }

    private String runClient(Path checkout, Path game, String mode, String arm) throws Exception {
        Files.createDirectories(game);
        Path project = checkout.resolve("stationapi/test");
        String wrapper = System.getProperty("os.name").startsWith("Windows")
                ? "gradlew.bat" : "gradlew";
        List<String> command = List.of(project.resolve(wrapper).toString(), "--no-daemon",
                "runClient", "--init-script", smoke.resolve("autosave.init.gradle").toString(),
                "-PworldlineAeroJar=" + checkout.resolve(
                        "stationapi/build/libs/aero-model-lib-3.0.0.jar"),
                "-PworldlineRunDir=" + game, "-PworldlineMode=" + mode,
                "-PworldlineArm=" + arm,
                "-PworldlineMetrics=" + game.resolve("metrics.properties"),
                "-PworldlineFrames=" + game.resolve("frames.txt"));
        System.out.println("[M772] start mode=" + mode + " arm=" + arm);
        String output = SmokeSupport.capture(project, command,
                Integer.parseInt(SmokeSupport.value(config, "child.timeout.seconds")));
        SmokeSupport.require(output.contains("[WorldlineM772] start mode=" + mode + " arm=" + arm)
                        && output.contains("BUILD SUCCESSFUL"), "M772 client launch drift: " + arm);
        if (mode.equals("prepare")) SmokeSupport.require(
                output.contains("[WorldlineM772] template-ready targets=12"),
                "M772 template preparation drift");
        return output;
    }

    private void buildAero(Path checkout) throws Exception {
        Path project = checkout.resolve("stationapi");
        String wrapper = System.getProperty("os.name").startsWith("Windows")
                ? "gradlew.bat" : "gradlew";
        String output = SmokeSupport.capture(project, List.of(project.resolve(wrapper).toString(),
                "--no-daemon", "remapJar", "--rerun-tasks"), Integer.parseInt(
                        SmokeSupport.value(config, "build.timeout.seconds")));
        SmokeSupport.require(output.contains("BUILD SUCCESSFUL") && Files.isRegularFile(
                project.resolve("build/libs/aero-model-lib-3.0.0.jar")), "M772 Aero build drift");
    }

    private void verifyDesign() {
        SmokeSupport.require(SmokeSupport.value(config, "pairs").equals("4")
                        && SmokeSupport.value(config, "dirty.chunks").equals("12")
                        && SmokeSupport.value(config, "incremental.budget").equals("1")
                        && SmokeSupport.value(config, "hitch.threshold.nanos").equals("50000000"),
                "M772 acquisition design drift");
    }

    private Path product(String module) {
        String override = System.getenv("WORLDLINE_PRODUCT_ROOT");
        Path products = override == null || override.isBlank()
                ? root.resolve(".worldline/build/classes") : Path.of(override);
        return products.resolve(module);
    }

    private static String sha256(String value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8)));
    }
}

final class M772Workspace {
    private M772Workspace() {}

    static void verifyCheckout(Path checkout, Properties config) throws Exception {
        SmokeSupport.require(Files.isDirectory(checkout.resolve(".git"))
                        || Files.isRegularFile(checkout.resolve(".git")), "Aero checkout absent");
        SmokeSupport.require(git(checkout, "rev-parse", "HEAD").trim().equals(
                        SmokeSupport.value(config, "aero.revision")), "Aero revision drift");
        SmokeSupport.require(git(checkout, "status", "--porcelain").isBlank(),
                "Aero checkout is dirty");
        String origin = git(checkout, "remote", "get-url", "origin").trim()
                .replace("\\", "/").toLowerCase();
        SmokeSupport.require(origin.equals(
                        SmokeSupport.value(config, "aero.repository").toLowerCase()),
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

    private static String git(Path checkout, String... arguments) throws Exception {
        ArrayList<String> command = new ArrayList<>();
        command.add("git");
        command.addAll(List.of(arguments));
        return SmokeSupport.capture(checkout, command, 60);
    }
}

final class M772Artifact {
    final String arm;
    final int nonForcedCalls, maxBatch, totalWritten, uniqueChunks;
    final int forcedCalls, forcedBefore, forcedAfter;
    final long maxSaveNanos;
    final long[] frames;

    private M772Artifact(String arm, int calls, int batch, int written, int uniqueChunks,
            long maxSave,
            int forcedCalls, int forcedBefore, int forcedAfter, long[] frames) {
        this.arm = arm;
        nonForcedCalls = calls;
        maxBatch = batch;
        totalWritten = written;
        this.uniqueChunks = uniqueChunks;
        maxSaveNanos = maxSave;
        this.forcedCalls = forcedCalls;
        this.forcedBefore = forcedBefore;
        this.forcedAfter = forcedAfter;
        this.frames = frames;
    }

    static M772Artifact read(Path game, String expectedArm) throws Exception {
        Properties values = new Properties();
        try (java.io.Reader reader = Files.newBufferedReader(game.resolve("metrics.properties"),
                StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        List<String> rows = Files.readAllLines(game.resolve("frames.txt"), StandardCharsets.UTF_8);
        long[] frames = new long[rows.size()];
        for (int index = 0; index < frames.length; index++)
            frames[index] = Long.parseLong(rows.get(index));
        String arm = required(values, "arm");
        SmokeSupport.require(arm.equals(expectedArm), "M772 artifact arm drift: " + arm);
        SmokeSupport.require(frames.length == integer(values, "frames"), "M772 frame count drift");
        return new M772Artifact(arm, integer(values, "non.forced.calls"),
                integer(values, "non.forced.max.batch"),
                integer(values, "non.forced.total.written"),
                integer(values, "non.forced.unique.chunks"),
                Long.parseLong(required(values, "non.forced.max.nanos")),
                integer(values, "forced.calls"), integer(values, "forced.before"),
                integer(values, "forced.after"), frames);
    }

    void verify(boolean candidate) {
        SmokeSupport.require(nonForcedCalls >= 12 && totalWritten >= 12
                        && uniqueChunks >= 12 && frames.length >= 500,
                "M772 retained census incomplete: " + arm);
        SmokeSupport.require(forcedCalls == 1 && forcedBefore >= 12 && forcedAfter == 0,
                "M772 forced drain invariant failed: " + arm);
        SmokeSupport.require(candidate ? maxBatch == 1 : maxBatch >= 2,
                "M772 non-forced batch policy drift: " + arm + " batch=" + maxBatch);
    }

    private static int integer(Properties values, String key) {
        return Integer.parseInt(required(values, key));
    }

    private static String required(Properties values, String key) {
        String value = values.getProperty(key);
        if (value == null || value.isBlank())
            throw new IllegalStateException("missing M772 " + key);
        return value.trim();
    }
}

record M772Pair(int index, boolean referenceFirst, M772Artifact reference,
                M772Artifact candidate) {
    String summary() {
        return "pair." + index + ".order=" + (referenceFirst ? "AB" : "BA")
                + ",reference.batch=" + reference.maxBatch
                + ",candidate.batch=" + candidate.maxBatch
                + ",reference.unique=" + reference.uniqueChunks
                + ",candidate.unique=" + candidate.uniqueChunks
                + ",reference.max.save.ns=" + reference.maxSaveNanos
                + ",candidate.max.save.ns=" + candidate.maxSaveNanos;
    }
}

final class M772GateBinding {
    private M772GateBinding() {}

    static M772GateResult evaluate(Path product, List<M772Pair> source,
            long threshold, long allowance) throws Exception {
        SmokeSupport.require(Files.isDirectory(product), "M772 profiling product absent");
        try (URLClassLoader loader = new URLClassLoader(new URL[] {product.toUri().toURL()}, null)) {
            Class<?> census = Class.forName("worldline.profiling.FrameCensus", true, loader);
            Class<?> gate = Class.forName("worldline.profiling.HitchRateGate", true, loader);
            Method of = census.getMethod("of", String[].class, long[][].class);
            Method pair = gate.getMethod("pair", census, census, boolean.class,
                    String.class, long.class);
            List<Object> pairs = new ArrayList<>();
            for (M772Pair value : source) pairs.add(pair.invoke(null,
                    frame(of, value.reference().frames), frame(of, value.candidate().frames),
                    value.referenceFirst(), "frame.wall.nanos", threshold));
            Object result = gate.getMethod("evaluate", List.class, long.class)
                    .invoke(null, pairs, allowance);
            return new M772GateResult(number(result, "baselineRatePpm"),
                    number(result, "candidateRatePpm"), number(result, "aggregateDeltaPpm"),
                    result.getClass().getMethod("passes").invoke(result).equals(Boolean.TRUE),
                    result.getClass().getMethod("verdict").invoke(result).toString());
        }
    }

    private static Object frame(Method of, long[] durations) throws Exception {
        long[][] rows = new long[durations.length][3];
        long time = 1L;
        for (int index = 0; index < rows.length; index++) {
            rows[index][0] = index;
            rows[index][1] = time;
            rows[index][2] = durations[index];
            time = Math.addExact(time, Math.max(1L, durations[index]));
        }
        return of.invoke(null, new String[] {"frame.wall.nanos"}, rows);
    }

    private static long number(Object value, String method) throws Exception {
        return ((Number) value.getClass().getMethod(method).invoke(value)).longValue();
    }
}

record M772GateResult(long baselineRate, long candidateRate, long delta,
                      boolean passes, String verdict) {
    String summary() {
        return "hitch.baseline.ppm=" + baselineRate + ",hitch.candidate.ppm=" + candidateRate
                + ",hitch.delta.ppm=" + delta + ",hitch.verdict=" + verdict;
    }
}
