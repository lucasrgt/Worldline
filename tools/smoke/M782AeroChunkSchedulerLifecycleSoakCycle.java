import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;

/** Qualifies Aero's chunk scheduler soak, reset, and platform lifecycle boundary. */
public final class M782AeroChunkSchedulerLifecycleSoakCycle {
    private static final String ID = "m782-aero-chunk-scheduler-lifecycle-soak";
    private static final String REVISION = "31dfe0c03b0ee454bd3996cec0bb76705f52835b";
    private static final String TRACE = "v1|consumer=aero-model-lib|revision=" + REVISION
            + "|platform=stationapi-remapJar|compile=javac-release8|jvms=2-fresh|"
            + "epochs=256|arrival-frames=512|hidden-per-epoch=128|budget=1|"
            + "max-age=120|debt=30|hidden-wait<=160|transition-pending=16|"
            + "reset=states0+queue-null+invocation0|oracle=exact";
    private static final String SIGNAL = "consumer=aero-model-lib,revision=" + REVISION
            + ",platform=stationapi-remapJar,compile=javac-release8,jvms=2-fresh,"
            + "epochs=256,frames=163840,rebuilds=164096,hidden-wait=157<=160,"
            + "transitions=256,pending-before-reset=16,reset=states0+queue-null+invocation0";
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID);
    private final Path build = root.resolve(".worldline/smokes").resolve(ID);
    private final Properties config = new Properties();

    private M782AeroChunkSchedulerLifecycleSoakCycle() {}

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: M782AeroChunkSchedulerLifecycleSoakCycle " + ID);
            System.exit(2);
        }
        try { new M782AeroChunkSchedulerLifecycleSoakCycle().execute(); }
        catch (Exception error) {
            System.err.println("M782 Aero chunk scheduler soak failed: " + error.getMessage());
            error.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        SmokeSupport.load(smoke.resolve("smoke.properties"), config);
        Path aero = root.resolve(SmokeSupport.value(config, "aero.path")).normalize();
        verifyAero(aero);
        verifyInputs(aero);
        buildPlatform(aero);
        SmokeSupport.recreate(root, build);
        Path classes = build.resolve("classes");
        Files.createDirectories(classes);
        compile(aero, classes);
        String first = run(classes);
        String second = run(classes);
        SmokeSupport.require(first.equals(second), "M782 fresh-JVM repeatability drift");
        SmokeSupport.require(first.equals(SmokeSupport.value(config, "expected.observation")),
                "M782 observation drift: " + first);
        String signature = sha256(TRACE);
        SmokeSupport.require(SIGNAL.equals(SmokeSupport.value(config, "expected.signal")),
                "M782 signal drift");
        SmokeSupport.require(signature.equals(SmokeSupport.value(config, "expected.signature")),
                "M782 signature drift: " + signature);
        Files.writeString(build.resolve("evidence.txt"), "id=" + ID + "\n" + SIGNAL
                + "\nobservation=" + first + "\ntrace=" + TRACE
                + "\nsignature=" + signature + "\n", StandardCharsets.UTF_8);
        System.out.println("M782 Aero chunk scheduler lifecycle soak passed");
        System.out.println("WORLDLINE_M782_SIGNAL=" + SIGNAL);
        System.out.println("WORLDLINE_M782_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M782_SIGNATURE=" + signature);
    }

    private void verifyAero(Path aero) throws Exception {
        SmokeSupport.require(Files.exists(aero.resolve(".git")), "M782 Aero checkout absent");
        SmokeSupport.require(capture(aero, "rev-parse", "HEAD").equals(REVISION),
                "M782 Aero revision drift");
        SmokeSupport.require(capture(aero, "status", "--porcelain").isEmpty(),
                "M782 Aero checkout dirty");
        String origin = capture(aero, "remote", "get-url", "origin").replace('\\', '/');
        SmokeSupport.require(origin.equalsIgnoreCase(SmokeSupport.value(config, "aero.repository")),
                "M782 Aero origin drift: " + origin);
    }

    private void verifyInputs(Path aero) throws Exception {
        verifyHash(aero.resolve("core/aero/modellib/render/Aero_ChunkWorkScheduler.java"),
                "scheduler.source.sha256");
        verifyHash(aero.resolve("stationapi/src/main/java/aero/modellib/Aero_ChunkCompileBudget.java"),
                "budget.source.sha256");
        verifyHash(aero.resolve("stationapi/src/main/java/aero/modellib/mixin/WorldRendererChunkSchedulerMixin.java"),
                "mixin.source.sha256");
        verifyHash(smoke.resolve("src/worldline/m782/ChunkSchedulerSoakProbe.java"),
                "probe.sha256");
    }

    private void verifyHash(Path path, String key) throws Exception {
        SmokeSupport.require(SmokeSupport.digest(path, "SHA-256").equals(
                SmokeSupport.value(config, key)), "M782 input drift: " + path);
    }

    private void buildPlatform(Path aero) throws Exception {
        Path project = aero.resolve("stationapi");
        String wrapper = project.resolve(System.getProperty("os.name").startsWith("Windows")
                ? "gradlew.bat" : "gradlew").toString();
        String output = SmokeSupport.capture(project, List.of(wrapper, "--no-daemon",
                "remapJar", "--rerun-tasks"), timeout("build.timeout.seconds"));
        SmokeSupport.require(output.contains("BUILD SUCCESSFUL") && Files.isRegularFile(
                project.resolve("build/libs/aero-model-lib-3.0.0.jar")),
                "M782 StationAPI build drift");
    }

    private void compile(Path aero, Path classes) throws Exception {
        List<String> command = new ArrayList<String>();
        command.add(javaTool("javac"));
        command.addAll(List.of("--release", "8", "-encoding", "UTF-8", "-d", classes.toString(),
                aero.resolve("core/aero/modellib/optimization/OptimizationRef.java").toString(),
                aero.resolve("core/aero/modellib/render/Aero_ChunkWorkScheduler.java").toString(),
                smoke.resolve("src/worldline/m782/ChunkSchedulerSoakProbe.java").toString()));
        SmokeSupport.capture(root, command, timeout("child.timeout.seconds"));
    }

    private String run(Path classes) throws Exception {
        return SmokeSupport.capture(root, List.of(javaTool("java"), "-Xmx128m", "-cp",
                classes.toString(), "worldline.m782.ChunkSchedulerSoakProbe"),
                timeout("child.timeout.seconds")).strip();
    }

    private String capture(Path directory, String... arguments) throws Exception {
        List<String> command = new ArrayList<String>(List.of("git"));
        command.addAll(List.of(arguments));
        return SmokeSupport.capture(directory, command, 60).strip();
    }

    private String javaTool(String name) {
        boolean windows = System.getProperty("os.name").startsWith("Windows");
        return Path.of(System.getProperty("java.home"), "bin", name + (windows ? ".exe" : ""))
                .toString();
    }

    private int timeout(String key) {
        return Integer.parseInt(SmokeSupport.value(config, key));
    }

    private static String sha256(String value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8)));
    }
}
