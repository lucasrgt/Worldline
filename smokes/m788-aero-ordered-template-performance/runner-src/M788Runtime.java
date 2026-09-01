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
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

/** Pinned checkout, build, and counterbalanced client-process boundary. */
final class M788Runtime {
    private final Path smoke, aero;
    private final Properties config;

    M788Runtime(Path smoke, Properties config, Path aero) {
        this.smoke = smoke;
        this.config = config;
        this.aero = aero;
    }

    void verifyCheckout() throws Exception {
        SmokeSupport.require(Files.exists(aero.resolve(".git")), "M788 Aero checkout absent");
        SmokeSupport.require(git("rev-parse", "HEAD").trim().equals(
            SmokeSupport.value(config, "aero.revision")), "M788 Aero revision drift");
        SmokeSupport.require(git("status", "--porcelain").isBlank(), "M788 Aero checkout dirty");
        String origin = git("remote", "get-url", "origin").trim().replace("\\", "/");
        SmokeSupport.require(origin.equalsIgnoreCase(SmokeSupport.value(config, "aero.repository")),
            "M788 Aero origin drift: " + origin);
    }

    void buildAero() throws Exception {
        Path project = aero.resolve("stationapi");
        String output = SmokeSupport.capture(project, List.of(wrapper(project),
            "--no-daemon", "compileJava", "remapJar", "--rerun-tasks"),
            timeout("build.timeout.seconds"));
        SmokeSupport.require(output.contains("BUILD SUCCESSFUL") && Files.isRegularFile(
            project.resolve("build/libs/aero-model-lib-3.0.0.jar")) && Files.isRegularFile(
            project.resolve("build/classes/java/main/aero/modellib/Aero_BECellReplay.class")),
            "M788 Aero build drift");
    }

    void runClient(Path game, boolean prepare, String arm) throws Exception {
        Files.createDirectories(game);
        Path project = aero.resolve("stationapi/test");
        List<String> command = List.of(wrapper(project), "--no-daemon", "runClient",
            "--init-script", smoke.resolve("ordered-template.init.gradle").toString(),
            "-PworldlineAeroClasses=" + aero.resolve("stationapi/build/classes/java/main"),
            "-PworldlineAeroJar=" + aero.resolve("stationapi/build/libs/aero-model-lib-3.0.0.jar"),
            "-PworldlineRunDir=" + game, "-PworldlinePrepare=" + prepare,
            "-PworldlineArm=" + arm,
            "-PworldlineFrames=" + SmokeSupport.value(config, "retained.frames"),
            "-PworldlineMinimumMillis=" + SmokeSupport.value(config, "minimum.millis"),
            "-PworldlineWarmFrames=" + SmokeSupport.value(config, "warm.frames"),
            "-PworldlineMaxBlankCaptures="
                + SmokeSupport.value(config, "maximum.blank.capture.rejections"),
            "-PworldlineMetrics=" + game.resolve("metrics.properties"),
            "-PworldlineFramesFile=" + game.resolve("frames.csv"),
            "-PworldlineFramesDir=" + game.resolve("visual-frames"));
        System.out.println("[M788] start prepare=" + prepare + " arm=" + arm);
        String output = SmokeSupport.capture(project, command, timeout("child.timeout.seconds"));
        SmokeSupport.require(output.contains("[WorldlineM788] start prepare=" + prepare
            + " arm=" + arm) && output.contains("BUILD SUCCESSFUL"),
            "M788 client lifecycle drift: " + arm + "\n" + output);
        String expected = prepare ? "template-ready machines=576"
            : "capture-complete arm=" + arm;
        SmokeSupport.require(output.contains("[WorldlineM788] " + expected),
            "M788 completion drift: " + arm + "\n" + output);
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
        return sha256Bytes(value.getBytes(StandardCharsets.UTF_8));
    }

    static String sha256Bytes(byte[] value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value));
    }

    private String git(String... arguments) throws Exception {
        ArrayList<String> command = new ArrayList<String>();
        command.add("git");
        command.addAll(List.of(arguments));
        return SmokeSupport.capture(aero, command, 60);
    }

    private int timeout(String key) { return Integer.parseInt(SmokeSupport.value(config, key)); }
    private static String wrapper(Path project) {
        return project.resolve(System.getProperty("os.name").startsWith("Windows")
            ? "gradlew.bat" : "gradlew").toString();
    }
}
