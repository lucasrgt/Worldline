import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;

/** Qualifies one real WLPR plus JFR frame-level runtime timeline. */
public final class M769AeroUnifiedRuntimeTimelineCycle {
    private static final String ID = "m769-aero-unified-runtime-timeline";
    private static final String TRACE = "v1|scene=mega-solid-16x4x3x3-576|arm=solid-aero-save"
            + "|retained-min=180s|wlpr=complete-frame|jfr=profile+anchors"
            + "|join=monotonic-epoch|events=gc+safepoint+allocation+file-io"
            + "|aero=save+enqueue+flush+pages|route=stationary+look-jump-spin+stationary"
            + "|observer=single-process|cleanup=normal";
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID);
    private final Path build = root.resolve(".worldline/smokes").resolve(ID);
    private final Properties config = new Properties();

    private M769AeroUnifiedRuntimeTimelineCycle() {}

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: M769AeroUnifiedRuntimeTimelineCycle " + ID);
            System.exit(2);
        }
        try {
            new M769AeroUnifiedRuntimeTimelineCycle().execute();
        } catch (Exception error) {
            System.err.println("M769 unified runtime timeline failed: " + error.getMessage());
            error.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        SmokeSupport.load(smoke.resolve("smoke.properties"), config);
        verifyDesign();
        SmokeSupport.require(Files.isDirectory(product("profiling")),
                "M769 profiling product absent");
        Path checkout = root.resolve(SmokeSupport.value(config, "aero.path")).normalize();
        M769Workspace.verifyCheckout(checkout, config);
        SmokeSupport.recreate(root, build);
        buildAero(checkout);
        Path template = build.resolve("template");
        runClient(checkout, template, true, template.resolve("unused.wlpr"),
                template.resolve("unused.jfr"));
        Path sourceWorld = template.resolve("saves/WorldlineAero");
        SmokeSupport.require(Files.isDirectory(sourceWorld), "M769 template world absent");
        Path measured = build.resolve("measured");
        Files.createDirectories(measured.resolve("saves"));
        M769Workspace.copyTree(sourceWorld, measured.resolve("saves/WorldlineAero"));
        Path profiler = measured.resolve("timeline.wlpr");
        Path jfr = measured.resolve("timeline.jfr");
        String output = runClient(checkout, measured, false, profiler, jfr);
        long retainedTicks = lifecycleValue(output, "ticks=");
        long retainedMillis = lifecycleValue(output, "millis=");
        SmokeSupport.require(retainedTicks >= Long.parseLong(
                            SmokeSupport.value(config, "retained.ticks"))
                        && retainedMillis >= Long.parseLong(
                            SmokeSupport.value(config, "minimum.millis"))
                        && output.contains("WORLDLINE_PROFILER_ARTIFACT=")
                        && output.contains("[WorldlineM769] jfr-sealed bytes=")
                        && output.contains("BUILD SUCCESSFUL"),
                "M769 measured client lifecycle drift");
        M769TimelineResult result = M769TimelineAnalyzer.analyze(profiler, jfr,
                Long.parseLong(SmokeSupport.value(config, "minimum.millis")));
        String signature = sha256(TRACE);
        SmokeSupport.require(signature.equals(SmokeSupport.value(config, "expected.signature")),
                "M769 semantic signature drift: " + signature);
        String evidence = "id=" + ID + "\n" + result.summary() + "\ntrace=" + TRACE
                + "\nsignature=" + signature + "\n";
        Files.writeString(build.resolve("evidence.txt"), evidence, StandardCharsets.UTF_8);
        System.out.println("M769 Aero unified runtime timeline passed");
        System.out.println("WORLDLINE_M769_SIGNAL="
                + SmokeSupport.value(config, "expected.signal"));
        System.out.println("WORLDLINE_M769_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M769_SIGNATURE=" + signature);
    }

    private static long lifecycleValue(String output, String key) {
        int line = output.indexOf("[WorldlineM769] retained-complete ");
        int start = line < 0 ? -1 : output.indexOf(key, line);
        if (start < 0) return -1L;
        start += key.length();
        int end = start;
        while (end < output.length() && Character.isDigit(output.charAt(end))) end++;
        try { return Long.parseLong(output.substring(start, end)); }
        catch (NumberFormatException error) { return -1L; }
    }

    private String runClient(Path checkout, Path game, boolean prepare, Path profiler,
            Path jfr) throws Exception {
        Files.createDirectories(game);
        Path project = checkout.resolve("stationapi/test");
        Path init = smoke.resolve("timeline.init.gradle");
        String wrapper = System.getProperty("os.name").startsWith("Windows")
                ? "gradlew.bat" : "gradlew";
        List<String> command = List.of(project.resolve(wrapper).toString(), "--no-daemon",
                "runClient", "--init-script", init.toString(),
                "-PworldlineAeroClasses=" + checkout.resolve("stationapi/build/classes/java/main"),
                "-PworldlineAeroJar=" + checkout.resolve(
                        "stationapi/build/libs/aero-model-lib-3.0.0.jar"),
                "-PworldlineRunDir=" + game,
                "-PworldlinePrepare=" + prepare,
                "-PworldlineTicks=" + SmokeSupport.value(config, "retained.ticks"),
                "-PworldlineMinimumMillis=" + SmokeSupport.value(config, "minimum.millis"),
                "-PworldlineProfilerCapacity=" + SmokeSupport.value(config, "profiler.capacity"),
                "-PworldlineAnchorInterval=" + SmokeSupport.value(config,
                        "anchor.interval.frames"),
                "-PworldlineProfiler=" + profiler,
                "-PworldlineJfr=" + jfr);
        System.out.println("[M769] start prepare=" + prepare + " dir=" + game);
        String output = SmokeSupport.capture(project, command,
                Integer.parseInt(SmokeSupport.value(config, "child.timeout.seconds")));
        SmokeSupport.require(output.contains("[WorldlineM769] start prepare=" + prepare)
                        && output.contains("BUILD SUCCESSFUL"),
                "M769 client launch drift");
        if (prepare) {
            SmokeSupport.require(output.contains("[WorldlineM769] template-ready machines=576"),
                    "M769 template preparation drift");
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
                "M769 Aero build drift");
    }

    private void verifyDesign() {
        SmokeSupport.require(SmokeSupport.value(config, "retained.ticks").equals("3600")
                        && SmokeSupport.value(config, "minimum.millis").equals("180000")
                        && SmokeSupport.value(config, "anchor.interval.frames").equals("128"),
                "M769 acquisition design drift");
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
final class M769Workspace {
    private M769Workspace() {}

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

/** Joins JFR events to canonical WLPR frame rows through custom clock anchors. */
final class M769TimelineAnalyzer {
    private static final String FRAME = "frame.wall.nanos";
    private static final String SAVE = "mod.aero.worldsave.nanos";
    private static final String ENQUEUE = "mod.aero.enqueue.nanos";
    private static final String FLUSH = "mod.aero.flush.nanos";

    private M769TimelineAnalyzer() {}

    static M769TimelineResult analyze(Path profiler, Path jfr, long minimumMillis)
            throws Exception {
        SmokeSupport.require(Files.isRegularFile(profiler) && Files.size(profiler) > 0L,
                "M769 WLPR artifact absent");
        SmokeSupport.require(Files.isRegularFile(jfr) && Files.size(jfr) > 0L,
                "M769 JFR artifact absent");
        M769ProfilerArtifact census = M769ProfilerArtifact.read(profiler);
        requireMetrics(census, FRAME, "jvm.gc.pause.nanos", "jvm.gc.collections",
                "jvm.heap.used.bytes", "chunk.compile.nanos", "chunk.rebuild.nanos",
                "display.present.nanos", SAVE, ENQUEUE, FLUSH, "mod.aero.pages.queued");
        SmokeSupport.require(census.frames() >= 10_000, "M769 frame census too small");
        SmokeSupport.require(census.endEpochMillis() - census.startEpochMillis() >= minimumMillis,
                "M769 WLPR window too short");
        List<M769Anchor> anchors = anchors(jfr);
        SmokeSupport.require(anchors.size() >= 2, "M769 JFR anchors absent");
        anchors.sort(Comparator.comparingLong(M769Anchor::sequence));
        SmokeSupport.require(anchors.get(0).sequence() <= 0L
                        && anchors.get(anchors.size() - 1).sequence() >= census.frames() - 1L,
                "M769 anchor coverage incomplete");
        long offset = medianOffset(anchors);
        M769EventTotals totals = events(jfr, census, offset);
        long save = sum(census, SAVE);
        SmokeSupport.require(save > 0L, "M769 observed no native save work");
        SmokeSupport.require(totals.gcEvents > 0L && totals.joinedGcEvents > 0L,
                "M769 observed no joined GC evidence");
        SmokeSupport.require(totals.safepointEvents > 0L, "M769 safepoint events absent");
        SmokeSupport.require(totals.allocationBytes > 0L, "M769 allocation samples absent");
        SmokeSupport.require(totals.fileEvents > 0L && totals.fileBytes > 0L,
                "M769 file I/O events absent");
        SmokeSupport.require(totals.joinedEvents > 0L, "M769 JFR events did not join frames");
        int worst = worst(census);
        String classification = classify(census, totals, worst);
        return new M769TimelineResult(census.frames(), anchors.size(), totals.gcEvents,
                totals.safepointEvents, totals.allocationEvents, totals.fileEvents,
                totals.joinedEvents, totals.allocationBytes, totals.fileBytes, worst,
                census.value(worst, FRAME), classification,
                SmokeSupport.digest(profiler, "SHA-256"), SmokeSupport.digest(jfr, "SHA-256"));
    }

    private static List<M769Anchor> anchors(Path jfr) throws Exception {
        List<M769Anchor> result = new ArrayList<>();
        try (RecordingFile recording = new RecordingFile(jfr)) {
            while (recording.hasMoreEvents()) {
                RecordedEvent event = recording.readEvent();
                if (!event.getEventType().getName().equals("worldline.FrameAnchor")) continue;
                result.add(new M769Anchor(event.getLong("sequence"),
                        event.getLong("monotonicNanos"), epochNanos(event.getStartTime())));
            }
        }
        return result;
    }

    private static long medianOffset(List<M769Anchor> anchors) {
        long[] offsets = new long[anchors.size()];
        for (int index = 0; index < offsets.length; index++) {
            offsets[index] = anchors.get(index).epochNanos()
                    - anchors.get(index).monotonicNanos();
        }
        Arrays.sort(offsets);
        long median = offsets[(offsets.length - 1) / 2];
        SmokeSupport.require(offsets[offsets.length - 1] - offsets[0] < 50_000_000L,
                "M769 clock-anchor spread exceeded 50ms");
        return median;
    }

    private static M769EventTotals events(Path jfr, M769ProfilerArtifact census, long offset)
            throws Exception {
        M769EventTotals totals = new M769EventTotals(census.frames());
        try (RecordingFile recording = new RecordingFile(jfr)) {
            while (recording.hasMoreEvents()) {
                RecordedEvent event = recording.readEvent();
                String name = event.getEventType().getName();
                if (name.equals("worldline.FrameAnchor")) continue;
                int frame = frame(census, epochNanos(event.getStartTime()) - offset);
                if (name.equals("jdk.GarbageCollection") || name.equals("jdk.GCPhasePause")) {
                    totals.gcEvents++;
                    if (frame >= 0) {
                        totals.joinedGcEvents++;
                        totals.gcNanos[frame] = Math.max(
                                totals.gcNanos[frame], event.getDuration().toNanos());
                    }
                } else if (name.startsWith("jdk.Safepoint")) {
                    totals.safepointEvents++;
                    if (frame >= 0) totals.safepointNanos[frame] = Math.max(
                            totals.safepointNanos[frame], event.getDuration().toNanos());
                } else if (name.equals("jdk.ObjectAllocationSample")) {
                    totals.allocationEvents++;
                    long bytes = field(event, "weight", field(event, "objectSize", 0L));
                    totals.allocationBytes = Math.addExact(totals.allocationBytes, bytes);
                    if (frame >= 0) totals.allocationByFrame[frame] = Math.addExact(
                            totals.allocationByFrame[frame], bytes);
                } else if (name.equals("jdk.FileRead") || name.equals("jdk.FileWrite")) {
                    totals.fileEvents++;
                    long bytes = name.endsWith("Read") ? field(event, "bytesRead", 0L)
                            : field(event, "bytesWritten", 0L);
                    totals.fileBytes = Math.addExact(totals.fileBytes, bytes);
                    if (frame >= 0) {
                        totals.ioNanos[frame] = Math.addExact(totals.ioNanos[frame],
                                event.getDuration().toNanos());
                        totals.ioBytes[frame] = Math.addExact(totals.ioBytes[frame], bytes);
                    }
                } else continue;
                if (frame >= 0) totals.joinedEvents++;
            }
        }
        return totals;
    }

    private static int frame(M769ProfilerArtifact census, long monotonic) {
        int low = 0;
        int high = census.frames() - 1;
        while (low <= high) {
            int middle = (low + high) >>> 1;
            if (census.monotonicNanos(middle) <= monotonic) low = middle + 1;
            else high = middle - 1;
        }
        if (high < 0) return -1;
        long end = census.monotonicNanos(high) + census.value(high, FRAME);
        return monotonic <= end ? high : -1;
    }

    private static int worst(M769ProfilerArtifact census) {
        int result = 0;
        for (int index = 1; index < census.frames(); index++) {
            if (census.value(index, FRAME) > census.value(result, FRAME)) result = index;
        }
        return result;
    }

    private static String classify(M769ProfilerArtifact census, M769EventTotals totals, int frame) {
        long wall = census.value(frame, FRAME);
        long threshold = Math.max(1_000_000L, (wall + 3L) / 4L);
        String[] names = {"SAVE", "GC", "SAFEPOINT", "FILE_IO", "AERO", "DISPLAY", "CHUNK"};
        long[] values = {census.value(frame, SAVE), totals.gcNanos[frame],
                totals.safepointNanos[frame], totals.ioNanos[frame],
                census.value(frame, ENQUEUE) + census.value(frame, FLUSH),
                census.value(frame, "display.present.nanos"),
                Math.max(census.value(frame, "chunk.compile.nanos"),
                        census.value(frame, "chunk.rebuild.nanos"))};
        String result = "UNKNOWN";
        int material = 0;
        for (int index = 0; index < values.length; index++) {
            if (values[index] < threshold) continue;
            result = names[index];
            material++;
        }
        return material > 1 ? "MIXED" : result;
    }

    private static long sum(M769ProfilerArtifact census, String metric) {
        long result = 0L;
        for (int frame = 0; frame < census.frames(); frame++) {
            result = Math.addExact(result, census.value(frame, metric));
        }
        return result;
    }

    private static void requireMetrics(M769ProfilerArtifact run, String... metrics) {
        for (String metric : metrics) {
            SmokeSupport.require(run.contains(metric), "M769 metric absent: " + metric);
        }
    }

    private static long field(RecordedEvent event, String name, long fallback) {
        try {
            return event.getLong(name);
        } catch (IllegalArgumentException absent) {
            return fallback;
        }
    }

    private static long epochNanos(Instant instant) {
        return Math.addExact(Math.multiplyExact(instant.getEpochSecond(), 1_000_000_000L),
                instant.getNano());
    }
}

/** Independent checksum-strict reader for the canonical WLPR v1 envelope and census. */
final class M769ProfilerArtifact {
    private static final int RUN_MAGIC = 0x574c5052;
    private static final int CENSUS_MAGIC = 0x574c4643;
    private final long startEpochMillis;
    private final long endEpochMillis;
    private final Map<String, Integer> indexes;
    private final long[][] rows;

    private M769ProfilerArtifact(long startEpochMillis, long endEpochMillis,
            Map<String, Integer> indexes, long[][] rows) {
        this.startEpochMillis = startEpochMillis;
        this.endEpochMillis = endEpochMillis;
        this.indexes = indexes;
        this.rows = rows;
    }

    static M769ProfilerArtifact read(Path path) throws Exception {
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
        byte[] census = input.readNBytes(censusLength);
        return census(start, end, names, census);
    }

    private static M769ProfilerArtifact census(long start, long end, String[] schema,
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
        return new M769ProfilerArtifact(start, end, indexes, rows);
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

    long monotonicNanos(int frame) {
        return rows[frame][1];
    }

    long value(int frame, String metric) {
        Integer index = indexes.get(metric);
        require(index != null, "unknown WLPR metric: " + metric);
        return rows[frame][index + 2];
    }

    boolean contains(String metric) {
        return indexes.containsKey(metric);
    }

    long startEpochMillis() {
        return startEpochMillis;
    }

    long endEpochMillis() {
        return endEpochMillis;
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new IllegalArgumentException(message);
        }
    }
}

record M769Anchor(long sequence, long monotonicNanos, long epochNanos) {}

final class M769EventTotals {
    final long[] gcNanos;
    final long[] safepointNanos;
    final long[] allocationByFrame;
    final long[] ioNanos;
    final long[] ioBytes;
    long gcEvents;
    long joinedGcEvents;
    long safepointEvents;
    long allocationEvents;
    long fileEvents;
    long joinedEvents;
    long allocationBytes;
    long fileBytes;

    M769EventTotals(int frames) {
        gcNanos = new long[frames];
        safepointNanos = new long[frames];
        allocationByFrame = new long[frames];
        ioNanos = new long[frames];
        ioBytes = new long[frames];
    }
}

record M769TimelineResult(int frames, int anchors, long gcEvents, long safepointEvents,
        long allocationEvents, long fileEvents, long joinedEvents, long allocationBytes,
        long fileBytes, int worstFrame, long worstNanos, String classification,
        String profilerSha256, String jfrSha256) {
    String summary() {
        return "frames=" + frames + "\nanchors=" + anchors + "\ngc.events=" + gcEvents
                + "\nsafepoint.events=" + safepointEvents + "\nallocation.events="
                + allocationEvents + "\nfile.events=" + fileEvents + "\njoined.events="
                + joinedEvents + "\nallocation.bytes=" + allocationBytes + "\nfile.bytes="
                + fileBytes + "\nworst.frame=" + worstFrame + "\nworst.nanos=" + worstNanos
                + "\nworst.class=" + classification + "\nwlpr.sha256=" + profilerSha256
                + "\njfr.sha256=" + jfrSha256;
    }
}
