import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
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

/** Qualifies asynchronous GPU timing, forced GPU drain, and present wait. */
public final class M770AeroGpuPresentAttributionCycle {
    private static final String ID = "m770-aero-gpu-present-attribution";
    private static final String[] ARMS = {
        "query-async-off", "finish-off", "finish-vsync"
    };
    private static final String TRACE = "v1|scene=mega-solid-16x4x3x3-576"
            + "|arms=query-async-off+finish-off+finish-vsync|retained-min=60s-each"
            + "|wlpr=complete-frame|gpu=arb-timer-query-frame-map"
            + "|finish=pre-display-update|display=swap+events+vblank|vsync=off+off+on"
            + "|query=nonblocking-ring-64|route=stationary+look-spin"
            + "|observer=fresh-process-per-arm|cleanup=normal";
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID);
    private final Path build = root.resolve(".worldline/smokes").resolve(ID);
    private final Properties config = new Properties();

    private M770AeroGpuPresentAttributionCycle() {}

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: M770AeroGpuPresentAttributionCycle " + ID);
            System.exit(2);
        }
        try {
            new M770AeroGpuPresentAttributionCycle().execute();
        } catch (Exception error) {
            System.err.println("M770 GPU present attribution failed: " + error.getMessage());
            error.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        SmokeSupport.load(smoke.resolve("smoke.properties"), config);
        verifyDesign();
        SmokeSupport.require(Files.isDirectory(product("profiling")),
                "M770 profiling product absent");
        Path checkout = root.resolve(SmokeSupport.value(config, "aero.path")).normalize();
        M770Workspace.verifyCheckout(checkout, config);
        SmokeSupport.recreate(root, build);
        buildAero(checkout);
        Path template = build.resolve("template");
        runClient(checkout, template, true, "prepare", template.resolve("unused.wlpr"),
                template.resolve("unused.gpuq"));
        Path sourceWorld = template.resolve("saves/WorldlineAero");
        SmokeSupport.require(Files.isDirectory(sourceWorld), "M770 template world absent");
        List<M770ArmResult> results = new ArrayList<>();
        for (String arm : ARMS) {
            Path game = build.resolve(arm);
            Files.createDirectories(game.resolve("saves"));
            M770Workspace.copyTree(sourceWorld, game.resolve("saves/WorldlineAero"));
            Path profiler = game.resolve("present.wlpr");
            Path queries = game.resolve("present.gpuq");
            String output = runClient(checkout, game, false, arm, profiler, queries);
            SmokeSupport.require(output.contains("[WorldlineM770] retained-complete arm=" + arm)
                            && output.contains("WORLDLINE_PROFILER_ARTIFACT=")
                            && output.contains("[WorldlineM770] gpu-query-sealed arm=" + arm)
                            && output.contains("BUILD SUCCESSFUL"),
                    "M770 measured client lifecycle drift: " + arm);
            results.add(M770Analyzer.analyze(arm, profiler, queries,
                    Long.parseLong(SmokeSupport.value(config, "minimum.millis")),
                    Integer.parseInt(SmokeSupport.value(config, "minimum.frames"))));
        }
        M770Analyzer.compare(results);
        String signature = sha256(TRACE);
        SmokeSupport.require(signature.equals(SmokeSupport.value(config, "expected.signature")),
                "M770 semantic signature drift: " + signature);
        StringBuilder evidence = new StringBuilder("id=").append(ID).append('\n');
        for (M770ArmResult result : results) evidence.append(result.summary()).append('\n');
        evidence.append("trace=").append(TRACE).append("\nsignature=").append(signature).append('\n');
        Files.writeString(build.resolve("evidence.txt"), evidence, StandardCharsets.UTF_8);
        System.out.println("M770 Aero GPU present attribution passed");
        System.out.println("WORLDLINE_M770_SIGNAL="
                + SmokeSupport.value(config, "expected.signal"));
        System.out.println("WORLDLINE_M770_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M770_SIGNATURE=" + signature);
    }

    private String runClient(Path checkout, Path game, boolean prepare, String arm,
            Path profiler, Path queries) throws Exception {
        Files.createDirectories(game);
        Path project = checkout.resolve("stationapi/test");
        Path init = smoke.resolve("present.init.gradle");
        String wrapper = System.getProperty("os.name").startsWith("Windows")
                ? "gradlew.bat" : "gradlew";
        List<String> command = List.of(project.resolve(wrapper).toString(), "--no-daemon",
                "runClient", "--init-script", init.toString(),
                "-PworldlineAeroClasses=" + checkout.resolve("stationapi/build/classes/java/main"),
                "-PworldlineAeroJar=" + checkout.resolve(
                        "stationapi/build/libs/aero-model-lib-3.0.0.jar"),
                "-PworldlineRunDir=" + game,
                "-PworldlinePrepare=" + prepare,
                "-PworldlineArm=" + arm,
                "-PworldlineTicks=" + SmokeSupport.value(config, "retained.ticks"),
                "-PworldlineMinimumMillis=" + SmokeSupport.value(config, "minimum.millis"),
                "-PworldlineProfilerCapacity=" + SmokeSupport.value(config, "profiler.capacity"),
                "-PworldlineQueryRing=" + SmokeSupport.value(config, "query.ring"),
                "-PworldlineProfiler=" + profiler,
                "-PworldlineQuery=" + queries);
        System.out.println("[M770] start prepare=" + prepare + " arm=" + arm);
        String output = SmokeSupport.capture(project, command,
                Integer.parseInt(SmokeSupport.value(config, "child.timeout.seconds")));
        SmokeSupport.require(output.contains("[WorldlineM770] start prepare=" + prepare
                        + " arm=" + arm) && output.contains("BUILD SUCCESSFUL"),
                "M770 client launch drift: " + arm);
        if (prepare) {
            SmokeSupport.require(output.contains("[WorldlineM770] template-ready machines=576"),
                    "M770 template preparation drift");
        }
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
                "M770 Aero build drift");
    }

    private void verifyDesign() {
        SmokeSupport.require(SmokeSupport.value(config, "retained.ticks").equals("1200")
                        && SmokeSupport.value(config, "minimum.millis").equals("60000")
                        && SmokeSupport.value(config, "query.ring").equals("64"),
                "M770 acquisition design drift");
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
final class M770Workspace {
    private M770Workspace() {}

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
        ArrayList<String> command = new ArrayList<>();
        command.add("git");
        command.addAll(List.of(arguments));
        return SmokeSupport.capture(checkout, command, 60);
    }
}

/** Strictly verifies all three WLPR and GPU-query artifacts. */
final class M770Analyzer {
    private static final String FRAME = "frame.wall.nanos";
    private static final String DISPLAY = "display.present.nanos";
    private static final String GPU_WAIT = "gpu.wait.nanos";

    private M770Analyzer() {}

    static M770ArmResult analyze(String arm, Path profiler, Path queries, long minimumMillis,
            int minimumFrames) throws Exception {
        M770ProfilerArtifact census = M770ProfilerArtifact.read(profiler);
        M770GpuArtifact gpu = M770GpuArtifact.read(queries, arm, census.frames());
        for (String metric : new String[] {FRAME, DISPLAY, GPU_WAIT, "render.world.nanos"}) {
            SmokeSupport.require(census.contains(metric), "M770 metric absent: " + metric);
        }
        SmokeSupport.require(census.frames() >= minimumFrames,
                "M770 frame census too small: " + arm);
        SmokeSupport.require(census.endEpochMillis() - census.startEpochMillis() >= minimumMillis,
                "M770 retained window too short: " + arm);
        SmokeSupport.require(gpu.results * 10L >= census.frames() * 9L,
                "M770 GPU query coverage below 90%: " + arm);
        SmokeSupport.require(gpu.skipped * 20L <= census.frames(),
                "M770 GPU query ring skipped more than 5%: " + arm);
        long gpuWait = census.sum(GPU_WAIT);
        if (arm.equals("query-async-off")) {
            SmokeSupport.require(gpuWait == 0L, "M770 async arm forced GPU synchronization");
        } else {
            SmokeSupport.require(gpuWait > 0L, "M770 finish arm observed no GPU wait: " + arm);
        }
        return new M770ArmResult(arm, census.frames(), gpu.results, gpu.skipped,
                census.percentile(FRAME, 50), census.percentile(DISPLAY, 50),
                census.percentile(DISPLAY, 95), gpu.percentile(50), gpu.percentile(95),
                gpuWait, SmokeSupport.digest(profiler, "SHA-256"),
                SmokeSupport.digest(queries, "SHA-256"));
    }

    static void compare(List<M770ArmResult> values) {
        SmokeSupport.require(values.size() == 3, "M770 arm count drift");
        M770ArmResult off = values.get(1);
        M770ArmResult vsync = values.get(2);
        SmokeSupport.require(off.arm().equals("finish-off")
                        && vsync.arm().equals("finish-vsync"),
                "M770 arm order drift");
        SmokeSupport.require(vsync.displayMedian() >= 1_000_000L,
                "M770 VSync arm observed no material present wait");
        SmokeSupport.require(vsync.displayMedian() > off.displayMedian(),
                "M770 VSync did not increase Display.update median");
    }
}

/** Independent checksum-strict reader for the canonical WLPR v1 envelope and census. */
final class M770ProfilerArtifact {
    private static final int RUN_MAGIC = 0x574c5052;
    private static final int CENSUS_MAGIC = 0x574c4643;
    private final long startEpochMillis;
    private final long endEpochMillis;
    private final Map<String, Integer> indexes;
    private final long[][] rows;

    private M770ProfilerArtifact(long startEpochMillis, long endEpochMillis,
            Map<String, Integer> indexes, long[][] rows) {
        this.startEpochMillis = startEpochMillis;
        this.endEpochMillis = endEpochMillis;
        this.indexes = indexes;
        this.rows = rows;
    }

    static M770ProfilerArtifact read(Path path) throws Exception {
        byte[] artifact = Files.readAllBytes(path);
        DataInputStream input = verified(artifact, "WLPR envelope");
        require(input.readInt() == RUN_MAGIC && input.readInt() == 1,
                "WLPR envelope header drift");
        input.readUnsignedByte();
        long start = input.readLong();
        long end = input.readLong();
        int metrics = input.readUnsignedShort();
        require(metrics > 0 && metrics <= 256, "WLPR metric count drift");
        String[] names = new String[metrics];
        for (int index = 0; index < metrics; index++) {
            names[index] = text(input);
            text(input);
            input.skipNBytes(3);
        }
        int tags = input.readUnsignedByte();
        for (int index = 0; index < tags; index++) {
            text(input);
            text(input);
        }
        int censusLength = input.readInt();
        require(censusLength > 0 && censusLength == input.available(),
                "WLPR census length drift");
        return census(start, end, names, input.readNBytes(censusLength));
    }

    private static M770ProfilerArtifact census(long start, long end, String[] schema,
            byte[] artifact) throws Exception {
        DataInputStream input = verified(artifact, "WLPR census");
        require(input.readInt() == CENSUS_MAGIC && input.readInt() == 1,
                "WLPR census header drift");
        int metrics = input.readInt();
        require(metrics == schema.length, "WLPR census width drift");
        Map<String, Integer> indexes = new LinkedHashMap<>();
        for (int index = 0; index < metrics; index++) {
            int length = input.readUnsignedByte();
            String name = new String(input.readNBytes(length), StandardCharsets.US_ASCII);
            require(name.equals(schema[index]) && indexes.put(name, index) == null,
                    "WLPR census schema drift");
        }
        int frames = input.readInt();
        require(frames > 0 && frames <= 2_000_000, "WLPR frame count drift");
        require((long) frames * (metrics + 2L) * Long.BYTES == input.available(),
                "WLPR census body length drift");
        long[][] rows = new long[frames][metrics + 2];
        long priorTime = -1L;
        for (int frame = 0; frame < frames; frame++) {
            for (int column = 0; column < metrics + 2; column++) {
                rows[frame][column] = input.readLong();
                require(rows[frame][column] >= 0L, "negative WLPR value");
            }
            require(rows[frame][0] == frame && rows[frame][1] > priorTime,
                    "WLPR frame identity drift");
            priorTime = rows[frame][1];
        }
        return new M770ProfilerArtifact(start, end, indexes, rows);
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
        int length = input.readUnsignedShort();
        require(length <= 4096, "WLPR text length drift");
        return new String(input.readNBytes(length), StandardCharsets.UTF_8);
    }

    int frames() {
        return rows.length;
    }

    boolean contains(String metric) {
        return indexes.containsKey(metric);
    }

    long value(int frame, String metric) {
        Integer index = indexes.get(metric);
        require(index != null, "unknown WLPR metric: " + metric);
        return rows[frame][index + 2];
    }

    long sum(String metric) {
        long result = 0L;
        for (int frame = 0; frame < rows.length; frame++) {
            result = Math.addExact(result, value(frame, metric));
        }
        return result;
    }

    long percentile(String metric, int percent) {
        long[] values = new long[rows.length];
        for (int frame = 0; frame < rows.length; frame++) values[frame] = value(frame, metric);
        Arrays.sort(values);
        int index = (int) Math.min(values.length - 1,
                ((long) values.length * percent + 99L) / 100L - 1L);
        return values[index];
    }

    long startEpochMillis() {
        return startEpochMillis;
    }

    long endEpochMillis() {
        return endEpochMillis;
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
}

/** Strict reader for the frame-keyed GPU timer-query artifact. */
final class M770GpuArtifact {
    private static final int MAGIC = 0x574c4751;
    final long skipped;
    final int results;
    private final long[] nanos;

    private M770GpuArtifact(long skipped, int results, long[] nanos) {
        this.skipped = skipped;
        this.results = results;
        this.nanos = nanos;
    }

    static M770GpuArtifact read(Path path, String arm, int frames) throws Exception {
        byte[] artifact = Files.readAllBytes(path);
        SmokeSupport.require(artifact.length >= 64, "M770 GPU artifact truncated");
        int bodyLength = artifact.length - 32;
        byte[] body = Arrays.copyOf(artifact, bodyLength);
        byte[] expected = Arrays.copyOfRange(artifact, bodyLength, artifact.length);
        byte[] actual = java.security.MessageDigest.getInstance("SHA-256").digest(body);
        SmokeSupport.require(java.security.MessageDigest.isEqual(actual, expected),
                "M770 GPU artifact digest mismatch");
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(body))) {
            SmokeSupport.require(input.readInt() == MAGIC && input.readInt() == 1,
                    "M770 GPU artifact header drift");
            int armLength = input.readUnsignedByte();
            String observedArm = new String(input.readNBytes(armLength), StandardCharsets.US_ASCII);
            SmokeSupport.require(observedArm.equals(arm), "M770 GPU arm drift");
            int ring = input.readInt();
            long issued = input.readLong();
            long skipped = input.readLong();
            int results = input.readInt();
            SmokeSupport.require(ring == 64 && issued == results && results > 0
                            && results <= frames && input.available() == results * 16,
                    "M770 GPU query census drift");
            long[] nanos = new long[results];
            boolean[] sequences = new boolean[frames];
            for (int index = 0; index < results; index++) {
                long sequence = input.readLong();
                nanos[index] = input.readLong();
                SmokeSupport.require(sequence >= 0L && sequence < frames
                                && !sequences[(int) sequence] && nanos[index] >= 0L,
                        "M770 GPU query row drift");
                sequences[(int) sequence] = true;
            }
            return new M770GpuArtifact(skipped, results, nanos);
        }
    }

    long percentile(int percent) {
        long[] values = nanos.clone();
        Arrays.sort(values);
        int index = (int) Math.min(values.length - 1,
                ((long) values.length * percent + 99L) / 100L - 1L);
        return values[index];
    }
}

record M770ArmResult(String arm, int frames, int gpuQueries, long skipped,
        long frameMedian, long displayMedian, long displayP95, long gpuMedian,
        long gpuP95, long gpuWaitTotal, String wlprSha256, String gpuSha256) {
    String summary() {
        return arm + ":frames=" + frames + ",queries=" + gpuQueries + ",skipped=" + skipped
                + ",frame.p50=" + frameMedian + ",display.p50/p95=" + displayMedian + "/"
                + displayP95 + ",gpu.p50/p95=" + gpuMedian + "/" + gpuP95
                + ",gpu.wait.total=" + gpuWaitTotal + ",wlpr=" + wlprSha256
                + ",gpuq=" + gpuSha256;
    }
}
