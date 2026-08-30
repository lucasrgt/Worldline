import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

/** Runs two fresh counterbalanced sets of the historical Aero tower replay. */
public final class M768AeroHistoricalTowerReplayCycle {
    private static final String ID = "m768-aero-historical-tower-replay";
    private static final String[] FORWARD = {
        "solid-aero-save", "solid-no-aero", "solid-aero-no-save", "sparse-aero"
    };
    private static final String[] REVERSE = {
        "sparse-aero", "solid-aero-no-save", "solid-no-aero", "solid-aero-save"
    };
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID);
    private final Path build = root.resolve(".worldline/smokes").resolve(ID);
    private final Properties config = new Properties();

    private M768AeroHistoricalTowerReplayCycle() {}

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: java tools/smoke/M768AeroHistoricalTowerReplayCycle.java " + ID);
            System.exit(2);
        }
        try { new M768AeroHistoricalTowerReplayCycle().execute(); }
        catch (Exception error) {
            System.err.println("M768 historical replay failed: " + error.getMessage());
            error.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        SmokeSupport.load(smoke.resolve("smoke.properties"), config);
        verifyDesign();
        Path checkout = root.resolve(SmokeSupport.value(config, "aero.path")).normalize();
        M768AeroWorkspace.verifyCheckout(checkout, config);
        SmokeSupport.recreate(root, build);
        buildAero(checkout);
        List<M768FrameArtifact> artifacts = new ArrayList<>();
        List<String> summaries = new ArrayList<>();
        String[][] orders = {FORWARD, REVERSE};
        for (int set = 0; set < orders.length; set++) {
            M768FrameArtifact absent = null, present = null;
            M768AeroWorkspace.verifyCheckout(checkout, config);
            Path setRoot = build.resolve("set-" + (set + 1));
            Path template = setRoot.resolve("template");
            runClient(checkout, template, "solid-aero-save", true, false,
                    setRoot.resolve("template-unused.bin"));
            Path templateWorld = template.resolve("saves/WorldlineAero");
            SmokeSupport.require(Files.isDirectory(templateWorld), "M768 template world absent");
            for (String arm : orders[set]) {
                M768AeroWorkspace.verifyCheckout(checkout, config);
                Path workspace = setRoot.resolve(arm), world = workspace.resolve("saves/WorldlineAero");
                Files.createDirectories(workspace.resolve("saves"));
                M768AeroWorkspace.copyTree(templateWorld, world);
                Path artifact = workspace.resolve("frames.bin");
                String output = runClient(checkout, workspace, arm, false,
                        arm.equals("solid-aero-no-save"), artifact);
                SmokeSupport.require(output.contains("[WorldlineM768] retained-complete arm=" + arm)
                                && output.contains("BUILD SUCCESSFUL")
                                && !output.contains("HistoricalRebuildTimingMixin"),
                        "M768 client lifecycle drift for " + arm);
                M768FrameArtifact parsed = M768FrameArtifact.read(artifact, arm,
                        Long.parseLong(SmokeSupport.value(config, "minimum.millis")) * 1_000_000L);
                if (arm.equals("solid-no-aero")) absent = parsed;
                if (arm.equals("solid-aero-save")) present = parsed;
                artifacts.add(parsed);
                summaries.add("set-" + (set + 1) + "/" + parsed.summary(arm));
                Files.writeString(workspace.resolve("client-output.txt"), output, StandardCharsets.UTF_8);
                System.out.println("[M768] " + summaries.get(summaries.size() - 1));
                M768AeroWorkspace.verifyCheckout(checkout, config);
            }
            SmokeSupport.require(absent != null && present != null, "M768 paired arms absent");
            String pair = "pair " + (set + 1) + ": absent:solid-no-aero intervalNs="
                    + absent.intervalSummary() + " | present:solid-aero-save intervalNs="
                    + present.intervalSummary();
            summaries.add(pair);
            System.out.println(pair);
        }
        SmokeSupport.require(artifacts.size() == 8, "M768 arm count drift");
        String trace = "v1|scene=mega-solid-16x4x3x3-576|sets=2|order=forward+reverse|fresh-process=true"
                + "|restored-world=true|synthetic-rehydrate=exact-matrix|retained-min=600s"
                + "|fps=unlimited|pacing=off|saves=native-control"
                + "|phases=stationary+look-jump-spin+stationary|census=complete-frame-sha256"
                + "|attribution=save+gc+chunk+aero+display+mixed+unknown|cleanup=normal";
        String signature = sha256(trace);
        SmokeSupport.require(signature.equals(SmokeSupport.value(config, "expected.signature")),
                "M768 semantic signature drift: " + signature);
        String evidence = "id=" + ID + "\nsets=2\narms=8\n" + String.join("\n", summaries)
                + "\ntrace=" + trace + "\nsignature=" + signature + "\n";
        Files.writeString(build.resolve("evidence.txt"), evidence, StandardCharsets.UTF_8);
        System.out.println("M768 Aero historical tower replay passed");
        System.out.println("WORLDLINE_M768_SIGNAL="
                + SmokeSupport.value(config, "expected.signal"));
        System.out.println("WORLDLINE_M768_TRACE=" + trace);
        System.out.println("WORLDLINE_M768_SIGNATURE=" + signature);
    }

    private String runClient(Path checkout, Path game, String arm, boolean prepare,
            boolean skipSaves, Path artifact) throws Exception {
        Files.createDirectories(game);
        Path project = checkout.resolve("stationapi/test"), init = smoke.resolve("historical.init.gradle");
        String wrapper = System.getProperty("os.name").startsWith("Windows") ? "gradlew.bat" : "gradlew";
        List<String> command = new ArrayList<>(List.of(project.resolve(wrapper).toString(), "--no-daemon",
                "runClient", "--init-script", init.toString(),
                "-PworldlineAeroClasses=" + checkout.resolve("stationapi/build/classes/java/main"),
                "-PworldlineAeroJar=" + checkout.resolve("stationapi/build/libs/aero-model-lib-3.0.0.jar"),
                "-PworldlineRunDir=" + game, "-PworldlineArm=" + arm,
                "-PworldlinePrepare=" + prepare,
                "-PworldlineTicks=" + SmokeSupport.value(config, "retained.ticks"),
                "-PworldlineMinimumMillis=" + SmokeSupport.value(config, "minimum.millis"),
                "-PworldlineArtifact=" + artifact, "-PworldlineSkipSaves=" + skipSaves));
        int timeout = Integer.parseInt(SmokeSupport.value(config, "child.timeout.seconds"));
        System.out.println("[M768] start arm=" + arm + " prepare=" + prepare + " dir=" + game);
        String output = SmokeSupport.capture(project, command, timeout);
        SmokeSupport.require(output.contains("[WorldlineM768] start arm=" + arm + " prepare=" + prepare)
                        && output.contains("BUILD SUCCESSFUL"), "M768 launch drift for " + arm);
        if (prepare) SmokeSupport.require(output.contains("[WorldlineM768] template-ready machines=576"),
                "M768 template preparation drift");
        return output;
    }

    private void buildAero(Path checkout) throws Exception {
        Path project = checkout.resolve("stationapi");
        String wrapper = System.getProperty("os.name").startsWith("Windows") ? "gradlew.bat" : "gradlew";
        String output = SmokeSupport.capture(project,
                List.of(project.resolve(wrapper).toString(), "--no-daemon", "remapJar", "--rerun-tasks"),
                Integer.parseInt(SmokeSupport.value(config, "build.timeout.seconds")));
        SmokeSupport.require(output.contains("BUILD SUCCESSFUL")
                        && Files.isRegularFile(project.resolve("build/libs/aero-model-lib-3.0.0.jar")),
                "M768 Aero build drift");
    }

    private void verifyDesign() {
        SmokeSupport.require(SmokeSupport.value(config, "sets").equals("2")
                        && SmokeSupport.value(config, "arms").equals("8")
                        && SmokeSupport.value(config, "minimum.millis").equals("600000")
                        && SmokeSupport.value(config, "retained.ticks").equals("12400"),
                "M768 acquisition design drift");
    }

    private static String sha256(String value) throws Exception {
        return java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8)));
    }
}

/** Filesystem and checkout boundary for the M768 historical replay. */
final class M768AeroWorkspace {
    private M768AeroWorkspace() {}

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
        SmokeSupport.require(origin.equals(SmokeSupport.value(config, "aero.repository").toLowerCase()),
                "Aero origin drift: " + origin);
    }

    static String git(Path checkout, String... arguments) throws Exception {
        ArrayList<String> command = new ArrayList<>();
        command.add("git");
        command.addAll(List.of(arguments));
        return SmokeSupport.capture(checkout, command, 60);
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
}

/** Strict reader and post-seal classifier for one complete M768 frame census. */
final class M768FrameArtifact {
    private static final String[] SCHEMA = {
        "phase", "frame.nanos", "tick.nanos", "save.nanos", "save.skipped",
        "chunks.dirty", "chunks.written", "chunkcompile.calls",
        "chunkcompile.maxnanos", "chunkcompile.backlog", "terrain.nanos",
        "aero.prepare.nanos", "aero.enqueue.nanos", "aero.flush.nanos",
        "aero.rebuild.nanos", "entity.nanos", "display.nanos",
        "allocation.bytes", "heap.usedbytes", "gc.count", "gc.nanos",
        "aero.pages.queued", "aero.pages.calls", "aero.pages.rebuilds",
        "aero.pages.cached", "aero.displaylists.live", "aero.displaylists.peak",
        "aero.batches.queued", "aero.batches.flushed", "aero.batches.count",
        "aero.animation.accepted", "aero.animation.rejected",
        "aero.visibility.visiblechunks", "aero.visibility.recentchunks"
    };

    final Path path;
    final int count;
    final long elapsed;
    final long worstFrame;
    final int worstIndex;
    final String worstClass;
    final String sha256;
    private final long[][] rows;
    private final Map<String, Integer> index = new LinkedHashMap<>();

    private M768FrameArtifact(Path path, int count, long elapsed, long worstFrame,
            int worstIndex, String worstClass, String sha256, long[][] rows) {
        this.path = path;
        this.count = count;
        this.elapsed = elapsed;
        this.worstFrame = worstFrame;
        this.worstIndex = worstIndex;
        this.worstClass = worstClass;
        this.sha256 = sha256;
        this.rows = rows;
        for (int i = 0; i < SCHEMA.length; i++) index.put(SCHEMA[i], i);
    }

    static M768FrameArtifact read(Path path, String arm, long minimumNanos) throws Exception {
        byte[] bytes = Files.readAllBytes(path);
        SmokeSupport.require(bytes.length > 32, "M768 artifact is truncated: " + path);
        byte[] body = Arrays.copyOf(bytes, bytes.length - 32);
        byte[] sealed = Arrays.copyOfRange(bytes, bytes.length - 32, bytes.length);
        SmokeSupport.require(Arrays.equals(MessageDigest.getInstance("SHA-256").digest(body), sealed),
                "M768 artifact digest mismatch: " + path);
        long[][] rows;
        int count;
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(body))) {
            SmokeSupport.require(in.readInt() == 0x574c4643 && in.readInt() == 1,
                    "M768 artifact header drift");
            int width = in.readInt();
            SmokeSupport.require(width == SCHEMA.length, "M768 metric width drift: " + width);
            for (String expected : SCHEMA) {
                int length = in.readUnsignedByte();
                byte[] name = in.readNBytes(length);
                SmokeSupport.require(new String(name, StandardCharsets.US_ASCII).equals(expected),
                        "M768 metric schema drift");
            }
            count = in.readInt();
            SmokeSupport.require(count > 0 && count <= 2_000_000,
                    "M768 frame count drift: " + count);
            rows = new long[count][width + 2];
            long prior = -1L;
            for (int frame = 0; frame < count; frame++) {
                for (int column = 0; column < width + 2; column++) {
                    long value = in.readLong();
                    SmokeSupport.require(value >= 0L, "negative M768 value at frame " + frame);
                    rows[frame][column] = value;
                }
                SmokeSupport.require(rows[frame][0] == frame && rows[frame][1] > prior,
                        "M768 frame identity drift at " + frame);
                prior = rows[frame][1];
            }
            SmokeSupport.require(in.read() == -1, "M768 artifact trailing body bytes");
        }
        M768FrameArtifact provisional = new M768FrameArtifact(path, count, rows[count - 1][1],
                0L, 0, "UNKNOWN", SmokeSupport.digest(path, "SHA-256"), rows);
        provisional.validate(arm, minimumNanos);
        int worst = 0;
        for (int i = 1; i < count; i++) if (provisional.value(i, "frame.nanos")
                > provisional.value(worst, "frame.nanos")) worst = i;
        long frame = provisional.value(worst, "frame.nanos");
        return new M768FrameArtifact(path, count, provisional.elapsed, frame, worst,
                provisional.classify(worst, frame), provisional.sha256, rows);
    }

    private void validate(String arm, long minimumNanos) {
        SmokeSupport.require(elapsed >= minimumNanos, "M768 retained window too short");
        boolean[] phase = new boolean[4];
        for (int i = 0; i < count; i++) {
            long p = value(i, "phase");
            SmokeSupport.require(p >= 1 && p <= 3, "M768 route phase drift");
            phase[(int) p] = true;
        }
        SmokeSupport.require(phase[1] && phase[2] && phase[3], "M768 route phases incomplete");
        SmokeSupport.require(value(0, "chunkcompile.backlog") == 0L,
                "M768 retained window began with compile backlog");
        long save = sum("save.nanos"), skipped = sum("save.skipped"), written = sum("chunks.written");
        boolean suppressed = arm.equals("solid-aero-no-save");
        SmokeSupport.require(suppressed ? save == 0L && written == 0L && skipped > 0L
                        : save > 0L && skipped == 0L,
                "M768 save-control drift for " + arm + " save/written/skipped="
                        + save + "/" + written + "/" + skipped);
        boolean noAero = arm.equals("solid-no-aero");
        long aero = sum("aero.pages.queued") + sum("aero.batches.queued")
                + sum("aero.animation.accepted");
        SmokeSupport.require(noAero ? aero == 0L : aero > 0L,
                "M768 Aero-control drift for " + arm + ": " + aero);
    }

    private String classify(int row, long frame) {
        long save = value(row, "save.nanos"), gc = value(row, "gc.nanos");
        long chunk = Math.max(value(row, "chunkcompile.maxnanos"), value(row, "terrain.nanos"));
        long subject = max(row, "aero.prepare.nanos", "aero.enqueue.nanos", "aero.flush.nanos",
                "aero.rebuild.nanos", "entity.nanos");
        long display = value(row, "display.nanos");
        long threshold = Math.max(1_000_000L, (frame + 3L) / 4L);
        String[] names = {"SAVE", "GC_RUNTIME", "CHUNK_WORK", "AERO_WORK", "DISPLAY_PRESENT"};
        long[] values = {save, gc, chunk, subject, display};
        String result = "UNKNOWN";
        int material = 0;
        for (int i = 0; i < values.length; i++) if (values[i] >= threshold) {
            result = names[i];
            material++;
        }
        return material > 1 ? "MIXED" : result;
    }

    long sum(String metric) {
        long result = 0L;
        for (int i = 0; i < count; i++) result = Math.addExact(result, value(i, metric));
        return result;
    }
    private long max(int row, String... metrics) {
        long result = 0L;
        for (String metric : metrics) result = Math.max(result, value(row, metric));
        return result;
    }
    private long value(int row, String metric) { return rows[row][index.get(metric) + 2]; }
    String summary(String arm) {
        return arm + ":frames=" + count + ",elapsedNs=" + elapsed + ",worst=" + worstFrame
                + "@" + worstIndex + ",class=" + worstClass + ",sha256=" + sha256;
    }
    String intervalSummary() {
        long[] values = new long[count];
        for (int i = 0; i < count; i++) values[i] = value(i, "frame.nanos");
        Arrays.sort(values);
        return values[(count - 1) / 2] + "/" + percentile(values, 95) + "/"
                + percentile(values, 99) + "/" + values[count - 1];
    }
    private static long percentile(long[] values, int percent) {
        int index = (int) Math.min(values.length - 1,
                ((long) values.length * percent + 99L) / 100L - 1L);
        return values[index];
    }
}
