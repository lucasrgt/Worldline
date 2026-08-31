import java.io.IOException;
import java.io.Reader;
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

/** Proves that Aero's view culling preserves complete visible pixels. */
public final class M778AeroCullingVisualIntegrity {
    private static final String ID = "m778-aero-culling-visual-integrity";
    private static final String[] ORDERS = {"off-on", "on-off"};
    private static final String TRACE = "v9|scene=four-panels-120-static-mega+static-bedrock-enclosure+prewarmed|sessions=2|"
        + "orders=within-client-off-on+on-off|pairing=cross-session-phase-matched|"
        + "checkpoints=center8x45+near4x90|hold=20|"
        + "world=frozen-tick+time6000+clear-weather+no-entities+no-clouds|"
        + "contrast=frustum-off-vs-broad-on+be-view-off|other-culls=off|"
        + "oracle=full-rgba-exact+draw-work-reduction|captures=12-per-arm-within-client";
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID);
    private final Path build = root.resolve(".worldline/build/smoke").resolve(ID);
    private final Properties config = new Properties();

    private M778AeroCullingVisualIntegrity() {}

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: M778AeroCullingVisualIntegrity " + ID);
            System.exit(2);
        }
        try {
            new M778AeroCullingVisualIntegrity().execute();
        } catch (Exception error) {
            System.err.println("M778 culling visual integrity failed: " + error.getMessage());
            error.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        SmokeSupport.load(smoke.resolve("smoke.properties"), config);
        verifyDesign();
        Path aero = root.resolve(SmokeSupport.value(config, "aero.path")).normalize();
        M778Runtime runtime = new M778Runtime(root, smoke, config, aero);
        runtime.verifyCheckout();
        SmokeSupport.recreate(root, build);
        runtime.buildAero();
        Path template = build.resolve("template");
        runtime.runClient(template, true, "prepare");
        Path sourceWorld = template.resolve("saves/WorldlineAero");
        SmokeSupport.require(Files.isDirectory(sourceWorld), "M778 template world absent");
        List<M778Session> sessions = new ArrayList<M778Session>();
        for (int index = 0; index < ORDERS.length; index++) {
            String order = ORDERS[index];
            Path game = build.resolve("session" + (index + 1) + "-" + order);
            M778Runtime.copyWorld(sourceWorld, game.resolve("saves/WorldlineAero"));
            runtime.runClient(game, false, order);
            M778Artifact off = M778Artifact.read(game, "cull-off");
            M778Artifact on = M778Artifact.read(game, "cull-on");
            off.verify();
            on.verify();
            sessions.add(new M778Session(index + 1, off, on));
        }
        List<M778VisualPair> visualPairs = List.of(
            M778VisualPair.compare(1, sessions.get(0).off(), sessions.get(1).on()),
            M778VisualPair.compare(2, sessions.get(0).on(), sessions.get(1).off()));
        M778Result result = M778Result.evaluate(sessions, visualPairs);
        SmokeSupport.require(result.passes(), "M778 visual gate failed: " + result.summary());
        String signature = M778Runtime.sha256(TRACE);
        SmokeSupport.require(signature.equals(SmokeSupport.value(config, "expected.signature")),
            "M778 semantic signature drift: " + signature);
        StringBuilder evidence = new StringBuilder("id=").append(ID).append('\n');
        for (M778Session session : sessions) evidence.append(session.summary()).append('\n');
        for (M778VisualPair pair : visualPairs) evidence.append(pair.summary()).append('\n');
        evidence.append(result.summary()).append("\ntrace=").append(TRACE)
            .append("\nsignature=").append(signature).append('\n');
        Files.writeString(build.resolve("evidence.txt"), evidence, StandardCharsets.UTF_8);
        System.out.println("M778 Aero culling visual integrity passed");
        System.out.println("WORLDLINE_M778_SIGNAL=" + SmokeSupport.value(config, "expected.signal"));
        System.out.println("WORLDLINE_M778_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M778_SIGNATURE=" + signature);
    }

    private void verifyDesign() {
        SmokeSupport.require(SmokeSupport.value(config, "sessions").equals("2")
            && SmokeSupport.value(config, "checkpoints").equals("12")
            && SmokeSupport.value(config, "hold.frames").equals("20")
            && SmokeSupport.value(config, "machines").equals("120")
            && SmokeSupport.value(config, "model.identities").equals("1")
            && SmokeSupport.value(config, "maximum.changed.pixels").equals("0")
            && SmokeSupport.value(config, "maximum.work.ratio").equals("0.95"),
            "M778 acquisition design drift");
    }
}

/** Exact pinned checkout, build, and restored-world process boundary. */
final class M778Runtime {
    private final Path root, smoke, aero;
    private final Properties config;

    M778Runtime(Path root, Path smoke, Properties config, Path aero) {
        this.root = root;
        this.smoke = smoke;
        this.config = config;
        this.aero = aero;
    }

    void verifyCheckout() throws Exception {
        SmokeSupport.require(Files.exists(aero.resolve(".git")), "M778 Aero checkout absent");
        SmokeSupport.require(git("rev-parse", "HEAD").trim().equals(
            SmokeSupport.value(config, "aero.revision")), "M778 Aero revision drift");
        SmokeSupport.require(git("status", "--porcelain").isBlank(), "M778 Aero checkout dirty");
        String origin = git("remote", "get-url", "origin").trim().replace("\\", "/");
        SmokeSupport.require(origin.equalsIgnoreCase(SmokeSupport.value(config, "aero.repository")),
            "M778 Aero origin drift: " + origin);
    }

    void buildAero() throws Exception {
        Path project = aero.resolve("stationapi");
        String output = SmokeSupport.capture(project, List.of(wrapper(project),
            "--no-daemon", "remapJar", "--rerun-tasks"),
            Integer.parseInt(SmokeSupport.value(config, "build.timeout.seconds")));
        SmokeSupport.require(output.contains("BUILD SUCCESSFUL") && Files.isRegularFile(
            project.resolve("build/libs/aero-model-lib-3.0.0.jar")), "M778 Aero build drift");
    }

    void runClient(Path game, boolean prepare, String arm) throws Exception {
        Files.createDirectories(game);
        Path project = aero.resolve("stationapi/test");
        List<String> command = List.of(wrapper(project), "--no-daemon", "runClient",
            "--init-script", smoke.resolve("visual.init.gradle").toString(),
            "-PworldlineAeroClasses=" + aero.resolve("stationapi/build/classes/java/main"),
            "-PworldlineAeroJar=" + aero.resolve("stationapi/build/libs/aero-model-lib-3.0.0.jar"),
            "-PworldlineRunDir=" + game, "-PworldlinePrepare=" + prepare,
            "-PworldlineArm=" + arm, "-PworldlineMetrics=" + game.resolve("metrics.properties"),
            "-PworldlineFramesDir=" + game.resolve("visual-frames"));
        System.out.println("[M778] start prepare=" + prepare + " arm=" + arm);
        String output = SmokeSupport.capture(project, command,
            Integer.parseInt(SmokeSupport.value(config, "child.timeout.seconds")));
        SmokeSupport.require(output.contains("[WorldlineM778] start prepare=" + prepare
            + " arm=" + arm) && output.contains("BUILD SUCCESSFUL"),
            "M778 client lifecycle drift: " + arm);
        String expected = prepare ? "template-ready machines=120"
            : "capture-complete arm=" + arm + " checkpoints=24";
        SmokeSupport.require(output.contains("[WorldlineM778] " + expected),
            "M778 completion drift: " + arm);
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
        return sha256(value.getBytes(StandardCharsets.UTF_8));
    }

    static String sha256(byte[] value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value));
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

/** One culling arm from a same-client off/on framebuffer set. */
final class M778Artifact {
    final Path game;
    final String arm;
    final int machines, captures, width, height;
    final long atRest, listCalls, viewCulls;
    final String[] hashes;

    private M778Artifact(Path game, Properties values) {
        this.game = game;
        arm = required(values, "arm");
        machines = integer(values, "machines");
        captures = integer(values, "captures");
        width = integer(values, "width");
        height = integer(values, "height");
        hashes = new String[captures];
        long rest = 0L, lists = 0L, culls = 0L;
        for (int i = 0; i < captures; i++) {
            hashes[i] = required(values, "checkpoint." + i + ".sha256");
            rest += number(values, "checkpoint." + i + ".atrest");
            lists += number(values, "checkpoint." + i + ".listcalls");
            culls += number(values, "checkpoint." + i + ".viewculls");
        }
        atRest = rest;
        listCalls = lists;
        viewCulls = culls;
    }

    static M778Artifact read(Path game, String expected) throws Exception {
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(
                game.resolve("metrics-" + expected + ".properties"))) {
            values.load(reader);
        }
        M778Artifact result = new M778Artifact(game, values);
        SmokeSupport.require(result.arm.equals(expected), "M778 artifact identity drift");
        return result;
    }

    void verify() throws Exception {
        SmokeSupport.require(machines == 120 && captures == 12 && width > 0 && height > 0
            && atRest > 0L && listCalls > 0L
            && viewCulls == 0L,
            "M778 incomplete artifact: " + summary());
        for (int i = 0; i < captures; i++) {
            byte[] pixels = pixels(i);
            SmokeSupport.require(pixels.length == width * height * 4
                && M778Runtime.sha256(pixels).equals(hashes[i]),
                "M778 framebuffer shape drift: " + i);
        }
    }

    byte[] pixels(int checkpoint) throws IOException {
        return Files.readAllBytes(game.resolve("visual-frames")
            .resolve(arm).resolve(String.format("checkpoint-%02d.rgba", checkpoint)));
    }

    String summary() {
        return arm + ":captures=" + captures + ",size=" + width + "x" + height
            + ",work=" + atRest + "/" + listCalls + ",viewCulls=" + viewCulls;
    }

    private static int integer(Properties p, String key) { return Integer.parseInt(required(p, key)); }
    private static long number(Properties p, String key) { return Long.parseLong(required(p, key)); }
    private static String required(Properties p, String key) {
        String value = p.getProperty(key);
        if (value == null || value.isBlank()) throw new IllegalStateException("missing M778 " + key);
        return value.trim();
    }
}

/** One counterbalanced session used for draw-work comparison. */
record M778Session(int index, M778Artifact off, M778Artifact on) {
    String summary() {
        return "session." + index + "," + off.summary() + "," + on.summary();
    }
}

/** Same temporal phase compared across sessions with opposite culling states. */
record M778VisualPair(int phase, M778Artifact off, M778Artifact on,
                      long changedPixels, int maxChannelDelta) {
    static M778VisualPair compare(int phase, M778Artifact off, M778Artifact on) throws Exception {
        SmokeSupport.require(off.width == on.width && off.height == on.height,
            "M778 framebuffer dimensions diverged");
        long changed = 0L;
        int maximum = 0;
        for (int checkpoint = 0; checkpoint < off.captures; checkpoint++) {
            byte[] baseline = off.pixels(checkpoint), candidate = on.pixels(checkpoint);
            SmokeSupport.require(baseline.length == candidate.length, "M778 frame size diverged");
            for (int pixel = 0; pixel < baseline.length; pixel += 4) {
                boolean differs = false;
                for (int channel = 0; channel < 4; channel++) {
                    int delta = Math.abs((baseline[pixel + channel] & 255)
                        - (candidate[pixel + channel] & 255));
                    maximum = Math.max(maximum, delta);
                    differs |= delta != 0;
                }
                if (differs) changed++;
            }
        }
        return new M778VisualPair(phase, off, on, changed, maximum);
    }

    String summary() {
        return "visual.phase." + phase + "," + off.arm + "=" + off.game.getFileName()
            + "," + on.arm + "=" + on.game.getFileName()
            + ",changedPixels=" + changedPixels + ",maxDelta=" + maxChannelDelta;
    }
}

record M778Result(long changedPixels, int maxDelta, long offWork, long onWork,
                  double workRatio, boolean everySessionReduced) {
    static M778Result evaluate(List<M778Session> sessions, List<M778VisualPair> visualPairs) {
        long changed = 0L, off = 0L, on = 0L;
        int delta = 0;
        boolean reduced = true;
        for (M778VisualPair pair : visualPairs) {
            changed += pair.changedPixels();
            delta = Math.max(delta, pair.maxChannelDelta());
        }
        for (M778Session session : sessions) {
            off += session.off().atRest;
            on += session.on().atRest;
            reduced &= session.on().atRest < session.off().atRest;
        }
        return new M778Result(changed, delta, off, on, (double) on / off, reduced);
    }

    boolean passes() {
        return changedPixels == 0L && maxDelta == 0 && everySessionReduced && workRatio <= 0.95D;
    }

    String summary() {
        return "visual.changed.pixels=" + changedPixels + ",visual.max.delta=" + maxDelta
            + ",work.atrest=" + offWork + "->" + onWork
            + ",work.ratio=" + String.format(java.util.Locale.ROOT, "%.3f", workRatio)
            + ",work.every-session-reduced=" + everySessionReduced;
    }
}
