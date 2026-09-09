import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** Verifies the external project pin and prepares hash-pinned native loader SDKs. */
final class M795Inputs {
  final Path root, smoke, build, aero, java8;
  private final Properties config;
  M795Inputs(Path root, Path smoke, Path build, Properties config) throws Exception {
    this.root = root;
    this.smoke = smoke;
    this.build = build;
    this.config = config;
    aero = root.resolve(SmokeSupport.value(config, "aero.path")).normalize();
    java8 = input("WORLDLINE_JAVA8_HOME");
  }
  void prepare() throws Exception {
    verifyCheckout();
    discover();
    String output = SmokeSupport.capture(root,
        List.of("java", root.resolve("tools/integration/LegacyProfilerQualificationLauncher.java").toString(),
            "--prepare-testkit-all", input("WORLDLINE_LEGACY_BASE_WORKSPACE").toString(),
            input("WORLDLINE_LEGACY_LOADER_ARTIFACTS").toString(), java8.toString()),
        900);
    Files.writeString(build.resolve("prepare.log"), output);
    for (String loader : List.of("modloader", "forge"))
      SmokeSupport.require(output.contains("WORLDLINE_LEGACY_TESTKIT_PREPARED=" + loader), "native SDK not prepared");
  }
  Path compile(String loader) throws Exception {
    verifyCheckout();
    Path sdk = root.resolve(".worldline/runtime/legacy-testkit/workspaces").resolve(loader);
    // Remove only the generated previous probe from this newly prepared, owned workspace.
    for (String path : List.of("minecraft/bin/net/minecraft/src/mod_WorldlineTestKitProbe.class",
             "minecraft/game/mods/worldline/net/minecraft/src/mod_WorldlineTestKitProbe.class"))
      Files.deleteIfExists(sdk.resolve(path));
    String output =
        SmokeSupport.capture(aero, List.of("java", "tools/platforms/LegacyCompile.java", loader, sdk.toString()), 180);
    SmokeSupport.require(output.contains("AERO_NATIVE_BUILD=PASS loader=" + loader), "native product build failed");
    Files.writeString(build.resolve(loader + "-build.log"), output);
    Files.copy(aero.resolve(".aero/platforms").resolve(loader).resolve("build.properties"),
        build.resolve(loader + "-build.properties"));
    return sdk;
  }
  private void verifyCheckout() throws Exception {
    SmokeSupport.require(SmokeSupport.capture(aero, List.of("git", "rev-parse", "HEAD"), 60)
                             .trim()
                             .equals(SmokeSupport.value(config, "aero.revision")),
        "M795 Aero revision drift");
    SmokeSupport.require(SmokeSupport.capture(aero, List.of("git", "status", "--porcelain"), 60).isBlank(),
        "M795 Aero checkout is dirty");
    SmokeSupport.require(SmokeSupport.capture(aero, List.of("git", "remote", "get-url", "origin"), 60)
                             .trim()
                             .equalsIgnoreCase(SmokeSupport.value(config, "aero.repository")),
        "M795 Aero origin drift");
  }
  private void discover() throws Exception {
    Path output = build.resolve("extension-classes");
    Files.createDirectories(output);
    String cp = SmokeSupport.product(root, "api") + File.pathSeparator + SmokeSupport.product(root, "extensions");
    List<String> command = new ArrayList<>(List.of("javac", "--release", "8", "-Xlint:all,-options", "-Werror",
        "-encoding", "UTF-8", "-classpath", cp, "-d", output.toString()));
    command.addAll(SmokeSupport.javaFiles(aero.resolve("worldline/extensions/ultra-journey/src/main/java")));
    command.addAll(SmokeSupport.javaFiles(smoke.resolve("extension-probe-src")));
    SmokeSupport.capture(root, command, 60);
    String result = SmokeSupport.capture(root,
        List.of(
            "java", "-classpath", output + File.pathSeparator + cp, "worldline.m795.ExtensionProbe", aero.toString()),
        60);
    SmokeSupport.require(result.contains("WORLDLINE_M795_EXTENSION=PASS"), "public SDK discovery failed");
  }
  private static Path input(String name) {
    String value = System.getenv(name);
    SmokeSupport.require(value != null && !value.isBlank(), "missing native runtime input: " + name);
    return Path.of(value).toAbsolutePath().normalize();
  }
}
