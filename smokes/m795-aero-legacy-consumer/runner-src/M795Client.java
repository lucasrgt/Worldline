import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** Launches the original native loader with one original Aero consumer mod. */
final class M795Client {
  private final M795Inputs inputs;
  private final Path sdk, classes;
  private final String loader, classpath;
  M795Client(M795Inputs inputs, Path sdk, String loader) throws Exception {
    this.inputs = inputs;
    this.sdk = sdk;
    this.loader = loader;
    classes = inputs.build.resolve(loader + "-consumer-classes");
    Files.createDirectories(classes);
    Properties manifest = new Properties();
    SmokeSupport.load(sdk.resolve("worldline-testkit.properties"), manifest);
    List<String> cp =
        new ArrayList<>(List.of(classes.toString(), inputs.aero.resolve(".aero/platforms/" + loader).toString()));
    int count = Integer.parseInt(SmokeSupport.value(manifest, "classpath.count"));
    for (int i = 1; i <= count; i++) {
      Path path = sdk.resolve(SmokeSupport.value(manifest, "classpath." + i)).normalize();
      SmokeSupport.require(path.startsWith(sdk) && Files.exists(path), "native classpath escaped or missing");
      cp.add(path.toString());
    }
    classpath = String.join(File.pathSeparator, cp);
    List<String> compile = new ArrayList<>(List.of("javac", "--release", "8", "-Xlint:all,-options", "-Werror",
        "-encoding", "UTF-8", "-classpath", classpath, "-d", classes.toString()));
    compile.addAll(SmokeSupport.javaFiles(inputs.smoke.resolve("runtime-src")));
    SmokeSupport.capture(inputs.root, compile, 120);
    Path target = sdk.resolve("minecraft/game/mods/aero-consumer/net/minecraft/src/mod_WorldlineAeroProbe.class");
    Files.createDirectories(target.getParent());
    Files.copy(classes.resolve("net/minecraft/src/mod_WorldlineAeroProbe.class"), target);
  }
  Path run(int session) throws Exception {
    Path output = inputs.build.resolve(loader + "-" + session);
    Files.createDirectories(output);
    String suffix = System.getProperty("os.name").startsWith("Windows") ? ".exe" : "";
    List<String> command = new ArrayList<>(List.of(inputs.java8.resolve("bin/java" + suffix).toString(), "-Xms256M",
        "-Xmx512M", "-Djava.library.path=" + sdk.resolve("libraries/natives"),
        "-Dworldline.aero.legacy.output=" + output, "-Daero.atRestLists=true", "-Daero.frustumcull=false",
        "-Dworldline.aero.legacy.loader=" + loader, "-Daero.spikelog=false", "-Dminecraft.launcher.brand=Worldline",
        "-Dminecraft.launcher.version=aero-consumer-v1", "-cp", classpath, "org.mcphackers.launchwrapper.Launch",
        "--username", "Worldline", "--uuid", "-", "--session", "-", "--version", "b1.7.3", "--gameDir",
        sdk.resolve("minecraft/game").toString(), "--assetsDir", sdk.resolve("minecraft/game/assets").toString(),
        "--assetIndex", "b1.7", "--accessToken", "-", "--userProperties", "{}", "--userType", "legacy", "--versionType",
        "release", "--skinProxy", "pre-b1.9-pre4"));
    System.out.println("[M795] native loader=" + loader + " session=" + session);
    String result = SmokeSupport.capture(sdk, command, 180);
    Files.writeString(output.resolve("runtime.log"), result);
    SmokeSupport.require(result.contains("WORLDLINE_AERO_LEGACY_BOOT=ModLoader Beta 1.7.3")
            && result.contains("WORLDLINE_AERO_LEGACY_PASS=draw,list-cache,dispose,animation,native-nbt,exact-rgba")
            && !result.contains("Failed to load mod"),
        "native Aero consumer did not complete");
    if (loader.equals("forge"))
      SmokeSupport.require(result.contains("WORLDLINE_AERO_FORGE_ACTIVE=1.0.6"), "native Forge did not activate");
    return output;
  }
}
