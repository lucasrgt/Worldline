import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;

/** Pinned checkout, build, and restored-world process boundary. */
final class M787Runtime {
    private final Path smoke, aero;
    private final Properties config;

    M787Runtime(Path smoke, Properties config, Path aero) {
        this.smoke = smoke;
        this.config = config;
        this.aero = aero;
    }

    void verifyCheckout() throws Exception {
        SmokeSupport.require(Files.exists(aero.resolve(".git")), "M787 Aero checkout absent");
        SmokeSupport.require(git("rev-parse", "HEAD").trim().equals(
            SmokeSupport.value(config, "aero.revision")), "M787 Aero revision drift");
        SmokeSupport.require(git("status", "--porcelain").isBlank(), "M787 Aero checkout dirty");
        String origin = git("remote", "get-url", "origin").trim().replace("\\", "/");
        SmokeSupport.require(origin.equalsIgnoreCase(SmokeSupport.value(config, "aero.repository")),
            "M787 Aero origin drift: " + origin);
    }

    void buildAero() throws Exception {
        Path project = aero.resolve("stationapi");
        String output = SmokeSupport.capture(project, List.of(wrapper(project),
            "--no-daemon", "compileJava", "remapJar", "--rerun-tasks"),
            Integer.parseInt(SmokeSupport.value(config, "build.timeout.seconds")));
        SmokeSupport.require(output.contains("BUILD SUCCESSFUL")
            && Files.isRegularFile(project.resolve("build/libs/aero-model-lib-3.0.0.jar"))
            && Files.isRegularFile(project.resolve(
                "build/classes/java/main/aero/modellib/Aero_BECellReplay.class")),
            "M787 Aero build drift");
    }

    void runClient(Path game, boolean prepare, String arm) throws Exception {
        Files.createDirectories(game);
        Path project = aero.resolve("stationapi/test");
        List<String> command = List.of(wrapper(project), "--no-daemon", "runClient",
            "--init-script", smoke.resolve("cold-entry.init.gradle").toString(),
            "-PworldlineAeroClasses=" + aero.resolve("stationapi/build/classes/java/main"),
            "-PworldlineAeroJar=" + aero.resolve("stationapi/build/libs/aero-model-lib-3.0.0.jar"),
            "-PworldlineRunDir=" + game, "-PworldlinePrepare=" + prepare,
            "-PworldlineArm=" + arm, "-PworldlineMetrics=" + game.resolve("metrics.properties"),
            "-PworldlineFramesDir=" + game.resolve("cold-entry-frames"));
        System.out.println("[M787] start prepare=" + prepare + " arm=" + arm);
        String output = SmokeSupport.capture(project, command,
            Integer.parseInt(SmokeSupport.value(config, "child.timeout.seconds")));
        SmokeSupport.require(output.contains("[WorldlineM787] start prepare=" + prepare
            + " arm=" + arm) && output.contains("BUILD SUCCESSFUL"),
            "M787 client lifecycle drift: " + arm);
        String expected = prepare ? "template-ready machines=576"
            : "capture-complete arm=" + arm + " frames=240 captures=40";
        SmokeSupport.require(output.contains("[WorldlineM787] " + expected),
            "M787 completion drift: " + arm);
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
        ArrayList<String> command = new ArrayList<>();
        command.add("git");
        command.addAll(List.of(arguments));
        return SmokeSupport.capture(aero, command, 60);
    }

    private static String wrapper(Path project) {
        return project.resolve(System.getProperty("os.name").startsWith("Windows")
            ? "gradlew.bat" : "gradlew").toString();
    }
}
