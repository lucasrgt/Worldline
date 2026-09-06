import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** Pinned checkout, SDK discovery, build, and client process boundary. */
final class M794Runtime {
  private final Path root, smoke, build, aero;
  private final Properties config;

  M794Runtime(Path root, Path smoke, Path build, Properties config, Path aero) {
    this.root = root;
    this.smoke = smoke;
    this.build = build;
    this.config = config;
    this.aero = aero;
  }

  void verifyCheckout() throws Exception {
    SmokeSupport.require(Files.exists(aero.resolve(".git")), "M794 Aero checkout absent");
    SmokeSupport.require(git("rev-parse", "HEAD").trim().equals(SmokeSupport.value(config, "aero.revision")),
        "M794 Aero revision drift");
    SmokeSupport.require(git("status", "--porcelain").isBlank(), "M794 Aero checkout dirty");
    String origin = git("remote", "get-url", "origin").trim().replace("\\", "/");
    SmokeSupport.require(
        origin.equalsIgnoreCase(SmokeSupport.value(config, "aero.repository")), "M794 Aero origin drift: " + origin);
  }

  void verifyExtension() throws Exception {
    Path output = build.resolve("extension-classes");
    Files.createDirectories(output);
    List<String> sources = new ArrayList<String>();
    sources.addAll(SmokeSupport.javaFiles(aero.resolve("worldline/extensions/ultra-journey/src/main/java")));
    sources.addAll(SmokeSupport.javaFiles(smoke.resolve("extension-probe-src")));
    sources.add(smoke.resolve("runtime-src/worldline/m794/ProductionMetricSchema.java").toString());
    List<Path> products = List.of(SmokeSupport.product(root, "api"), SmokeSupport.product(root, "extensions"),
        SmokeSupport.product(root, "profiling"));
    List<String> compile = new ArrayList<String>(List.of("javac", "--release", "8", "-encoding", "UTF-8",
        "-Xlint:all,-options", "-Werror", "-classpath", join(products), "-d", output.toString()));
    compile.addAll(sources);
    SmokeSupport.capture(root, compile, 60);
    ArrayList<Path> runtime = new ArrayList<Path>(products);
    runtime.add(0, output);
    String observed = SmokeSupport.capture(
        root, List.of("java", "-classpath", join(runtime), "worldline.m794.ExtensionProbe", aero.toString()), 60);
    SmokeSupport.require(observed.contains("WORLDLINE_M794_EXTENSION=PASS"), "M794 public extension discovery drift");
    String schema = SmokeSupport.capture(
        root, List.of("java", "-classpath", join(runtime), "worldline.m794.ProductionMetricSchema"), 60);
    SmokeSupport.require(schema.contains("WORLDLINE_M794_PROFILE_SCHEMA=PASS"), "M794 profile schema drift");
  }

  void buildAero() throws Exception {
    Path project = aero.resolve("stationapi");
    String output = SmokeSupport.capture(project,
        List.of(wrapper(project), "--no-daemon", "compileJava", "remapJar", "--rerun-tasks"),
        timeout("build.timeout.seconds"));
    SmokeSupport.require(output.contains("BUILD SUCCESSFUL")
            && Files.isRegularFile(project.resolve("build/libs/aero-model-lib-3.0.0.jar"))
            && Files.isRegularFile(project.resolve("build/classes/java/main/aero/modellib/Aero_AnimatedBatcher.class")),
        "M794 Aero build drift");
  }

  void runClient(Path game, boolean prepare, String arm) throws Exception {
    Files.createDirectories(game);
    Path project = aero.resolve("stationapi/test");
    List<String> command = List.of(wrapper(project), "--no-daemon", "runClient", "--init-script",
        smoke.resolve("production.init.gradle").toString(),
        "-PworldlineAeroClasses=" + aero.resolve("stationapi/build/classes/java/main"),
        "-PworldlineAeroJar=" + aero.resolve("stationapi/build/libs/aero-model-lib-3.0.0.jar"),
        "-PworldlineRunDir=" + game, "-PworldlinePrepare=" + prepare, "-PworldlineArm=" + arm,
        "-PworldlineMetrics=" + game.resolve("metrics.properties"),
        "-PworldlineFramesDir=" + game.resolve("visual-frames"));
    System.out.println("[M794] start prepare=" + prepare + " arm=" + arm);
    String output = SmokeSupport.capture(project, command, timeout("child.timeout.seconds"));
    Files.writeString(game.resolve("runtime-console.log"), output);
    SmokeSupport.require(output.contains("[WorldlineM794] start prepare=" + prepare + " arm=" + arm)
            && output.contains(SmokeSupport.value(config, "host.signal"))
            && output.contains("BUILD SUCCESSFUL"),
        "M794 client lifecycle drift: " + arm + "\n" + output);
    String expected = prepare ? "template-ready machines=512" : "capture-complete arm=" + arm + " checkpoints=36";
    SmokeSupport.require(
        output.contains("[WorldlineM794] " + expected), "M794 completion drift: " + arm + "\n" + output);
  }

  static void copyWorld(Path source, Path destination) throws IOException {
    Files.createDirectories(destination);
    try (DirectoryStream<Path> paths = Files.newDirectoryStream(source)) {
      for (Path path : paths) {
        Path target = destination.resolve(path.getFileName());
        if (Files.isDirectory(path))
          copyWorld(path, target);
        else
          Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
      }
    }
  }

  static String sha256(String value) throws Exception {
    return M794Digest.sha256Hex(value.getBytes(StandardCharsets.UTF_8));
  }

  private String git(String... arguments) throws Exception {
    ArrayList<String> command = new ArrayList<String>();
    command.add("git");
    command.addAll(List.of(arguments));
    return SmokeSupport.capture(aero, command, 60);
  }
  private int timeout(String key) {
    return Integer.parseInt(SmokeSupport.value(config, key));
  }
  private static String wrapper(Path project) {
    return project.resolve(System.getProperty("os.name").startsWith("Windows") ? "gradlew.bat" : "gradlew").toString();
  }
  private static String join(List<Path> paths) {
    ArrayList<String> values = new ArrayList<String>();
    for (Path path : paths) values.add(path.toString());
    return String.join(File.pathSeparator, values);
  }
}
