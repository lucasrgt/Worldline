import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;

/** Qualifies Aero's default smooth-light policy and single-world cache lifecycle. */
public final class M781AeroSmoothLightDefaultLifecycleCycle {
    private static final String ID = "m781-aero-smooth-light-default-lifecycle";
    private static final String REVISION = "06c0c22ce15454b45b14597332a92241fef0931e";
    private static final String[] ARMS = {"default", "false", "true", "true", "false", "default"};
    private static final String TRACE = "v1|consumer=aero-model-lib|revision=" + REVISION
            + "|compile=javac-release8|jvms=6-fresh-default+false+true+true+false+default|"
            + "default=enabled|explicit-false=disabled|explicit-true=enabled|"
            + "same-world=array-reuse|world-switch=entries1-to0+old-world-miss|oracle=exact";
    private static final String SIGNAL = "consumer=aero-model-lib,revision=" + REVISION
            + ",compile=javac-release8,jvms=6-fresh-counterbalanced,default=enabled,"
            + "explicit-false=disabled,explicit-true=enabled,same-world=array-reuse,"
            + "world-switch=entries1-to0+old-world-miss,oracle=exact";
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID);
    private final Path build = root.resolve(".worldline/smokes").resolve(ID);
    private final Properties config = new Properties();

    private M781AeroSmoothLightDefaultLifecycleCycle() {}

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: M781AeroSmoothLightDefaultLifecycleCycle " + ID);
            System.exit(2);
        }
        try { new M781AeroSmoothLightDefaultLifecycleCycle().execute(); }
        catch (Exception error) {
            System.err.println("M781 Aero smooth-light lifecycle failed: " + error.getMessage());
            error.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        SmokeSupport.load(smoke.resolve("smoke.properties"), config);
        Path aero = root.resolve(SmokeSupport.value(config, "aero.path")).normalize();
        verifyAero(aero);
        verifyInputs(aero);
        SmokeSupport.recreate(root, build);
        Path classes = build.resolve("classes");
        Files.createDirectories(classes);
        compile(aero, classes);
        List<String> observations = new ArrayList<String>();
        for (String arm : ARMS) observations.add(run(classes, arm));
        verifyObservations(observations);
        String signature = sha256(TRACE);
        SmokeSupport.require(SIGNAL.equals(SmokeSupport.value(config, "expected.signal")),
                "M781 signal drift");
        SmokeSupport.require(signature.equals(SmokeSupport.value(config, "expected.signature")),
                "M781 signature drift: " + signature);
        Files.writeString(build.resolve("evidence.txt"), "id=" + ID + "\n"
                + SIGNAL.replace(',', '\n') + "\nobservations="
                + String.join(",", observations) + "\ntrace=" + TRACE
                + "\nsignature=" + signature + "\n", StandardCharsets.UTF_8);
        System.out.println("M781 Aero smooth-light default lifecycle passed");
        System.out.println("WORLDLINE_M781_SIGNAL=" + SIGNAL);
        System.out.println("WORLDLINE_M781_TRACE=" + TRACE);
        System.out.println("WORLDLINE_M781_SIGNATURE=" + signature);
    }

    private void verifyAero(Path aero) throws Exception {
        SmokeSupport.require(Files.exists(aero.resolve(".git")), "M781 Aero checkout absent");
        SmokeSupport.require(capture(aero, "rev-parse", "HEAD").equals(REVISION),
                "M781 Aero revision drift");
        SmokeSupport.require(capture(aero, "status", "--porcelain").isEmpty(),
                "M781 Aero checkout dirty");
        String origin = capture(aero, "remote", "get-url", "origin").replace('\\', '/');
        SmokeSupport.require(origin.equalsIgnoreCase(SmokeSupport.value(config, "aero.repository")),
                "M781 Aero origin drift: " + origin);
    }

    private void verifyInputs(Path aero) throws Exception {
        Path cache = aero.resolve("core/aero/modellib/render/Aero_SmoothLightCache.java");
        Path probe = smoke.resolve("src/worldline/m781/SmoothLightLifecycleProbe.java");
        SmokeSupport.require(SmokeSupport.digest(cache, "SHA-256").equals(
                SmokeSupport.value(config, "cache.source.sha256")), "M781 cache source drift");
        SmokeSupport.require(SmokeSupport.digest(probe, "SHA-256").equals(
                SmokeSupport.value(config, "probe.sha256")), "M781 probe drift");
    }

    private void compile(Path aero, Path classes) throws Exception {
        List<String> command = new ArrayList<String>();
        command.add(javaTool("javac"));
        command.addAll(List.of("--release", "8", "-encoding", "UTF-8", "-d", classes.toString(),
                aero.resolve("core/aero/modellib/optimization/OptimizationRef.java").toString(),
                aero.resolve("core/aero/modellib/render/Aero_SmoothLightCache.java").toString(),
                smoke.resolve("src/worldline/m781/SmoothLightLifecycleProbe.java").toString()));
        SmokeSupport.capture(root, command, timeout());
    }

    private String run(Path classes, String arm) throws Exception {
        List<String> command = new ArrayList<String>();
        command.add(javaTool("java"));
        if (!"default".equals(arm)) command.add("-Daero.smoothlight.cache=" + arm);
        command.addAll(List.of("-cp", classes.toString(),
                "worldline.m781.SmoothLightLifecycleProbe", arm));
        String output = SmokeSupport.capture(root, command, timeout());
        String expected = "M781_ARM=" + arm + ";enabled=" + !"false".equals(arm)
                + ";sameWorldReuse=true;worldSwitchClears=true;entries=0";
        SmokeSupport.require(output.strip().equals(expected), "M781 arm drift: " + output);
        return output.strip();
    }

    private void verifyObservations(List<String> observations) {
        SmokeSupport.require(observations.size() == ARMS.length, "M781 arm census drift");
        for (int index = 0; index < ARMS.length; index++)
            SmokeSupport.require(observations.get(index).startsWith("M781_ARM=" + ARMS[index] + ";"),
                    "M781 arm order drift");
    }

    private String capture(Path directory, String... arguments) throws Exception {
        List<String> command = new ArrayList<String>(List.of("git"));
        command.addAll(List.of(arguments));
        return SmokeSupport.capture(directory, command, timeout()).strip();
    }

    private String javaTool(String name) {
        boolean windows = System.getProperty("os.name").startsWith("Windows");
        return Path.of(System.getProperty("java.home"), "bin", name + (windows ? ".exe" : ""))
                .toString();
    }

    private int timeout() {
        return Integer.parseInt(SmokeSupport.value(config, "child.timeout.seconds"));
    }

    private static String sha256(String value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8)));
    }
}
