import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

/** Qualifies a counterbalanced negative control and the neutral hitch-rate gate. */
public final class M771AeroCounterbalancedHitchRateCycle {
    private static final String ID = "m771-aero-counterbalanced-hitch-rate";
    private static final boolean[] REFERENCE_FIRST = {true, false, false, true};
    private static final String FRAME = "frame.wall.nanos";
    private static final String TRACE = "v1|scene=mega-solid-16x4x3x3-576"
            + "|design=4-pairs-AB+BA+BA+AB|arms=8-fresh-processes"
            + "|treatment=identical-negative-control|retained-min=60s-each"
            + "|wlpr=complete-frame-sha256|hitch=frame-wall>=50ms"
            + "|stats=aggregate-rate+paired-median+quartiles+sign-agreement+p99"
            + "|gate=regression-only-majority+500ppm-allowance"
            + "|route=stationary+look-spin|cleanup=normal";
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID);
    private final Path build = root.resolve(".worldline/smokes").resolve(ID);
    private final Properties config = new Properties();

    private M771AeroCounterbalancedHitchRateCycle() {}

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: M771AeroCounterbalancedHitchRateCycle " + ID);
            System.exit(2);
        }
        try { new M771AeroCounterbalancedHitchRateCycle().execute(); }
        catch (Exception error) {
            System.err.println("M771 counterbalanced hitch-rate failed: " + error.getMessage());
            error.printStackTrace(System.err); System.exit(1);
        }
    }

    private void execute() throws Exception {
        SmokeSupport.load(smoke.resolve("smoke.properties"), config);
        verifyDesign(); Path profiling = product("profiling");
        SmokeSupport.require(Files.isDirectory(profiling), "M771 profiling product absent");
        Path checkout = root.resolve(SmokeSupport.value(config, "aero.path")).normalize();
        M771Workspace.verifyCheckout(checkout, config);
        SmokeSupport.recreate(root, build); buildAero(checkout);
        Path template = build.resolve("template");
        runClient(checkout, template, true, "prepare", template.resolve("unused.wlpr"));
        Path sourceWorld = template.resolve("saves/WorldlineAero");
        SmokeSupport.require(Files.isDirectory(sourceWorld), "M771 template world absent");
        List<M771Pair> pairs = new ArrayList<>();
        long minimum = Long.parseLong(SmokeSupport.value(config, "minimum.millis"));
        long threshold = Long.parseLong(SmokeSupport.value(config, "hitch.threshold.nanos"));
        for (int index = 0; index < REFERENCE_FIRST.length; index++) {
            String[] order = REFERENCE_FIRST[index]
                    ? new String[] {"reference", "candidate"}
                    : new String[] {"candidate", "reference"};
            M771Arm reference = null, candidate = null;
            for (String treatment : order) {
                String arm = "pair" + (index + 1) + "-" + treatment;
                Path game = build.resolve(arm); Files.createDirectories(game.resolve("saves"));
                M771Workspace.copyTree(sourceWorld, game.resolve("saves/WorldlineAero"));
                Path profiler = game.resolve("hitch.wlpr");
                String output = runClient(checkout, game, false, arm, profiler);
                SmokeSupport.require(output.contains("[WorldlineM771] retained-complete arm=" + arm)
                                && output.contains("WORLDLINE_PROFILER_ARTIFACT=")
                                && output.contains("BUILD SUCCESSFUL"),
                        "M771 measured client lifecycle drift: " + arm);
                M771Arm measured = M771Artifact.read(arm, profiler, minimum, threshold);
                if (treatment.equals("reference")) reference = measured;
                else candidate = measured;
            }
            SmokeSupport.require(reference != null && candidate != null,
                    "M771 paired arms absent: " + index);
            pairs.add(new M771Pair(index + 1, REFERENCE_FIRST[index], reference, candidate));
        }
        M771GateResult gate = M771GateBinding.evaluate(profiling, pairs, FRAME, threshold,
                Long.parseLong(SmokeSupport.value(config, "allowed.regression.ppm")));
        SmokeSupport.require(gate.passes, "M771 negative control failed hitch-rate gate: "
                + gate.verdict);
        String signature = sha256(TRACE);
        SmokeSupport.require(signature.equals(SmokeSupport.value(config, "expected.signature")),
                "M771 semantic signature drift: " + signature);
        StringBuilder evidence = new StringBuilder("id=").append(ID).append('\n');
        for (M771Pair pair : pairs) evidence.append(pair.summary()).append('\n');
        evidence.append(gate.summary()).append("\ntrace=").append(TRACE)
                .append("\nsignature=").append(signature).append('\n');
        Files.writeString(build.resolve("evidence.txt"), evidence, StandardCharsets.UTF_8);
        System.out.println("M771 Aero counterbalanced hitch-rate passed");
        System.out.println("WORLDLINE_M771_SIGNAL=" + SmokeSupport.value(config, "expected.signal"));
        System.out.println("WORLDLINE_M771_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M771_SIGNATURE=" + signature);
    }

    private String runClient(Path checkout, Path game, boolean prepare, String arm,
            Path profiler) throws Exception {
        Files.createDirectories(game); Path project = checkout.resolve("stationapi/test");
        String wrapper = System.getProperty("os.name").startsWith("Windows")
                ? "gradlew.bat" : "gradlew";
        List<String> command = List.of(project.resolve(wrapper).toString(), "--no-daemon",
                "runClient", "--init-script", smoke.resolve("hitch.init.gradle").toString(),
                "-PworldlineAeroClasses=" + checkout.resolve("stationapi/build/classes/java/main"),
                "-PworldlineAeroJar=" + checkout.resolve(
                        "stationapi/build/libs/aero-model-lib-3.0.0.jar"),
                "-PworldlineRunDir=" + game, "-PworldlinePrepare=" + prepare,
                "-PworldlineArm=" + arm,
                "-PworldlineTicks=" + SmokeSupport.value(config, "retained.ticks"),
                "-PworldlineMinimumMillis=" + SmokeSupport.value(config, "minimum.millis"),
                "-PworldlineProfilerCapacity=" + SmokeSupport.value(config, "profiler.capacity"),
                "-PworldlineProfiler=" + profiler);
        System.out.println("[M771] start prepare=" + prepare + " arm=" + arm);
        String output = SmokeSupport.capture(project, command,
                Integer.parseInt(SmokeSupport.value(config, "child.timeout.seconds")));
        SmokeSupport.require(output.contains("[WorldlineM771] start prepare=" + prepare
                        + " arm=" + arm) && output.contains("BUILD SUCCESSFUL"),
                "M771 client launch drift: " + arm);
        if (prepare) SmokeSupport.require(
                output.contains("[WorldlineM771] template-ready machines=576"),
                "M771 template preparation drift");
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
                project.resolve("build/libs/aero-model-lib-3.0.0.jar")),
                "M771 Aero build drift");
    }

    private void verifyDesign() {
        SmokeSupport.require(SmokeSupport.value(config, "pairs").equals("4")
                        && SmokeSupport.value(config, "retained.ticks").equals("1200")
                        && SmokeSupport.value(config, "minimum.millis").equals("60000")
                        && SmokeSupport.value(config, "hitch.threshold.nanos").equals("50000000")
                        && SmokeSupport.value(config, "allowed.regression.ppm").equals("500"),
                "M771 acquisition design drift");
    }

    private Path product(String module) {
        String override = System.getenv("WORLDLINE_PRODUCT_ROOT");
        Path products = override == null || override.isBlank()
                ? root.resolve(".worldline/build/classes") : Path.of(override);
        return products.resolve(module);
    }

    private static String sha256(String value) throws Exception {
        return java.util.HexFormat.of().formatHex(java.security.MessageDigest
                .getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    }
}

/** Pinned checkout and restored-world filesystem boundary. */
final class M771Workspace {
    private M771Workspace() {}

    static void verifyCheckout(Path checkout, Properties config) throws Exception {
        SmokeSupport.require(Files.isDirectory(checkout.resolve(".git"))
                        || Files.isRegularFile(checkout.resolve(".git")),
                "Aero checkout absent: " + checkout);
        String head = git(checkout, "rev-parse", "HEAD").trim();
        SmokeSupport.require(head.equals(SmokeSupport.value(config, "aero.revision")),
                "Aero revision drift: " + head);
        SmokeSupport.require(git(checkout, "status", "--porcelain").isBlank(),
                "Aero checkout is dirty");
        String origin = git(checkout, "remote", "get-url", "origin").trim()
                .replace("\\", "/").toLowerCase();
        SmokeSupport.require(origin.equals(
                        SmokeSupport.value(config, "aero.repository").toLowerCase()),
                "Aero origin drift: " + origin);
    }

    static void copyTree(Path source, Path destination) throws IOException {
        SmokeSupport.require(Files.isDirectory(source), "template tree absent: " + source);
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
        ArrayList<String> command = new ArrayList<>(); command.add("git");
        command.addAll(List.of(arguments)); return SmokeSupport.capture(checkout, command, 60);
    }
}

/** Independent checksum-strict reader for one complete WLPR frame census. */
final class M771Artifact {
    private static final int RUN_MAGIC = 0x574c5052;
    private static final int CENSUS_MAGIC = 0x574c4643;
    final String arm;
    final long startEpochMillis;
    final long endEpochMillis;
    final String[] names;
    final long[][] rows;
    final long hitches;
    final long p99;
    final long maximum;
    final String sha256;

    private M771Artifact(String arm, long start, long end, String[] names, long[][] rows,
            long hitches, long p99, long maximum, String sha256) {
        this.arm = arm; this.startEpochMillis = start; this.endEpochMillis = end;
        this.names = names; this.rows = rows; this.hitches = hitches;
        this.p99 = p99; this.maximum = maximum; this.sha256 = sha256;
    }

    static M771Arm read(String arm, Path path, long minimumMillis, long threshold)
            throws Exception {
        byte[] artifact = Files.readAllBytes(path);
        DataInputStream input = verified(artifact, "WLPR envelope");
        require(input.readInt() == RUN_MAGIC && input.readInt() == 1,
                "M771 WLPR envelope header drift");
        input.readUnsignedByte(); long start = input.readLong(), end = input.readLong();
        int metrics = input.readUnsignedShort();
        require(metrics > 0 && metrics <= 256, "M771 WLPR metric count drift");
        String[] names = new String[metrics];
        for (int index = 0; index < metrics; index++) {
            names[index] = text(input); text(input); input.skipNBytes(3);
        }
        int tags = input.readUnsignedByte();
        for (int index = 0; index < tags; index++) { text(input); text(input); }
        int length = input.readInt();
        require(length > 0 && length == input.available(), "M771 WLPR census length drift");
        long[][] rows = census(names, input.readNBytes(length));
        int frame = index(names, "frame.wall.nanos");
        require(frame >= 0 && index(names, "render.world.nanos") >= 0
                        && index(names, "display.present.nanos") >= 0,
                "M771 profiler schema drift");
        require(rows.length >= 3_000, "M771 frame census too small: " + arm);
        require(end - start >= minimumMillis, "M771 retained window too short: " + arm);
        long[] walls = new long[rows.length]; long hitches = 0L;
        for (int row = 0; row < rows.length; row++) {
            walls[row] = rows[row][frame + 2]; if (walls[row] >= threshold) hitches++;
        }
        Arrays.sort(walls); int p99 = Math.max(1, (int) ((99L * walls.length + 99L) / 100L));
        String digest = java.util.HexFormat.of().formatHex(java.security.MessageDigest
                .getInstance("SHA-256").digest(artifact));
        M771Artifact value = new M771Artifact(arm, start, end, names, rows, hitches,
                walls[p99 - 1], walls[walls.length - 1], digest);
        return new M771Arm(value);
    }

    private static long[][] census(String[] schema, byte[] artifact) throws Exception {
        DataInputStream input = verified(artifact, "WLPR census");
        require(input.readInt() == CENSUS_MAGIC && input.readInt() == 1,
                "M771 WLPR census header drift");
        int metrics = input.readInt();
        require(metrics == schema.length, "M771 WLPR census width drift");
        Map<String, Integer> unique = new LinkedHashMap<>();
        for (int index = 0; index < metrics; index++) {
            int length = input.readUnsignedByte();
            String name = new String(input.readNBytes(length), StandardCharsets.US_ASCII);
            require(name.equals(schema[index]) && unique.put(name, index) == null,
                    "M771 WLPR census schema drift");
        }
        int frames = input.readInt();
        require(frames > 0 && frames <= 2_000_000, "M771 WLPR frame count drift");
        require((long) frames * (metrics + 2L) * Long.BYTES == input.available(),
                "M771 WLPR census body length drift");
        long[][] rows = new long[frames][metrics + 2]; long prior = -1L;
        for (int frame = 0; frame < frames; frame++) {
            for (int column = 0; column < metrics + 2; column++) {
                rows[frame][column] = input.readLong();
                require(rows[frame][column] >= 0L, "negative M771 WLPR value");
            }
            require(rows[frame][0] == frame && rows[frame][1] > prior,
                    "M771 WLPR frame identity drift");
            prior = rows[frame][1];
        }
        return rows;
    }

    private static DataInputStream verified(byte[] artifact, String label) throws Exception {
        require(artifact.length >= 48, label + " truncated");
        int bodyLength = artifact.length - 32;
        byte[] body = Arrays.copyOf(artifact, bodyLength);
        byte[] expected = Arrays.copyOfRange(artifact, bodyLength, artifact.length);
        byte[] actual = java.security.MessageDigest.getInstance("SHA-256").digest(body);
        require(java.security.MessageDigest.isEqual(actual, expected), label + " digest mismatch");
        return new DataInputStream(new ByteArrayInputStream(body));
    }

    private static String text(DataInputStream input) throws Exception {
        int length = input.readUnsignedShort(); require(length <= 4096, "M771 text drift");
        return new String(input.readNBytes(length), StandardCharsets.UTF_8);
    }

    private static int index(String[] names, String wanted) {
        for (int index = 0; index < names.length; index++)
            if (names[index].equals(wanted)) return index;
        return -1;
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
}

final class M771Arm {
    final M771Artifact artifact;
    M771Arm(M771Artifact artifact) { this.artifact = artifact; }
    String summary() {
        return artifact.arm + ":frames=" + artifact.rows.length + ",hitches=" + artifact.hitches
                + ",p99=" + artifact.p99 + ",max=" + artifact.maximum
                + ",wlpr=" + artifact.sha256;
    }
}

final class M771Pair {
    final int index;
    final boolean referenceFirst;
    final M771Arm reference;
    final M771Arm candidate;
    M771Pair(int index, boolean referenceFirst, M771Arm reference, M771Arm candidate) {
        this.index = index; this.referenceFirst = referenceFirst;
        this.reference = reference; this.candidate = candidate;
    }
    String summary() {
        return "pair" + index + ":order=" + (referenceFirst ? "AB" : "BA") + ","
                + reference.summary() + "," + candidate.summary();
    }
}

/** Calls the shipped neutral gate rather than duplicating its verdict logic in the smoke. */
final class M771GateBinding {
    private M771GateBinding() {}

    static M771GateResult evaluate(Path product, List<M771Pair> source, String metric,
            long threshold, long allowance) throws Exception {
        URL[] urls = {product.toUri().toURL()};
        try (URLClassLoader loader = new URLClassLoader(urls, ClassLoader.getPlatformClassLoader())) {
            Class<?> census = loader.loadClass("worldline.profiling.FrameCensus");
            Class<?> gate = loader.loadClass("worldline.profiling.HitchRateGate");
            Method of = census.getMethod("of", String[].class, long[][].class);
            Method pair = gate.getMethod("pair", census, census, boolean.class,
                    String.class, long.class);
            List<Object> pairs = new ArrayList<>();
            for (M771Pair value : source) pairs.add(pair.invoke(null,
                    frame(of, value.reference.artifact), frame(of, value.candidate.artifact),
                    value.referenceFirst, metric, threshold));
            Object result = gate.getMethod("evaluate", List.class, long.class)
                    .invoke(null, pairs, allowance);
            Class<?> type = result.getClass();
            return new M771GateResult(invoke(type, result, "baselineRatePpm"),
                    invoke(type, result, "candidateRatePpm"),
                    invoke(type, result, "aggregateDeltaPpm"),
                    invoke(type, result, "medianDeltaPpm"),
                    invoke(type, result, "lowerQuartilePpm"),
                    invoke(type, result, "upperQuartilePpm"),
                    invoke(type, result, "p99MedianDeltaNanos"),
                    (int) invoke(type, result, "positivePairs"),
                    (int) invoke(type, result, "negativePairs"),
                    result.getClass().getMethod("verdict").invoke(result).toString(),
                    (Boolean) result.getClass().getMethod("passes").invoke(result));
        }
    }

    private static Object frame(Method of, M771Artifact artifact) throws Exception {
        return of.invoke(null, (Object) artifact.names, (Object) artifact.rows);
    }

    private static long invoke(Class<?> type, Object value, String method) throws Exception {
        return ((Number) type.getMethod(method).invoke(value)).longValue();
    }
}

final class M771GateResult {
    final long baseline, candidate, aggregate, median, lower, upper, p99;
    final int positive, negative;
    final String verdict;
    final boolean passes;
    M771GateResult(long baseline, long candidate, long aggregate, long median, long lower,
            long upper, long p99, int positive, int negative, String verdict, boolean passes) {
        this.baseline = baseline; this.candidate = candidate; this.aggregate = aggregate;
        this.median = median; this.lower = lower; this.upper = upper; this.p99 = p99;
        this.positive = positive; this.negative = negative;
        this.verdict = verdict; this.passes = passes;
    }
    String summary() {
        return "gate:baseline.ppm=" + baseline + ",candidate.ppm=" + candidate
                + ",aggregate.delta.ppm=" + aggregate + ",paired.q25/median/q75="
                + lower + "/" + median + "/" + upper + ",p99.median.delta=" + p99
                + ",signs=" + positive + "/" + negative + ",verdict=" + verdict;
    }
}
