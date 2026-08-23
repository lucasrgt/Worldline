import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Qualifies descriptor-driven mod loading and compatibility rejection. */
public final class ModCycle {
  private static final String ID = "m7-mod-loading";
  private final Path root = Paths.get("").toAbsolutePath().normalize();
  private final Path smoke = root.resolve("smokes").resolve(ID);
  private final Path build = root.resolve(".worldline/smokes").resolve(ID);
  private final Path client = root.resolve(".worldline/smokes/controlled-client-tick");

  public static void main(String[] arguments) {
    if (!Arrays.equals(arguments, new String[] {ID})) {
      System.err.println("usage: java tools/smoke/ModCycle.java " + ID);
      System.exit(2);
    }
    try {
      new ModCycle().execute();
    } catch (Exception error) {
      System.err.println("M7 mod cycle failed: " + error.getMessage());
      System.exit(1);
    }
  }

  private void execute() throws Exception {
    Path adapter = client.resolve("adapter-classes");
    require(Files.isDirectory(adapter), "run ClientCycle before ModCycle");
    recreate(build);
    Path modClasses =
        compile(smoke.resolve("mod-src"), build.resolve("mod-classes"), Arrays.asList(adapter));
    Path primary = modJar("primary", modClasses), secondary = modJar("secondary", modClasses);
    Path runtime = modJar("wrong-runtime", modClasses), api = modJar("wrong-api", modClasses);
    Path type = modJar("wrong-type", modClasses), malformed = modJar("malformed", modClasses);
    Path missing = build.resolve("missing-descriptor.jar");
    run(Arrays.asList(
        "jar", "--create", "--file", missing.toString(), "-C", modClasses.toString(), "."));
    Path scenario = compile(smoke.resolve("src"), build.resolve("classes"),
        Arrays.asList(adapter, product("api"), product("trace"), product("mods")));
    List<Path> game = gamePath(scenario, adapter);
    Result first = process(game, "worldline.smoke.m7.M7ModSmoke", "run", primary.toString(), "20");
    Result second = process(game, "worldline.smoke.m7.M7ModSmoke", "run", primary.toString(), "20");
    require(first.code == 0 && first.text.equals(second.text), "primary mod is not deterministic");
    Result other =
        process(game, "worldline.smoke.m7.M7ModSmoke", "run", secondary.toString(), "41");
    require(other.code == 0 && first.text.contains("mod.id=worldline.glass-probe")
            && other.text.contains("mod.id=worldline.gold-probe"),
        "descriptor entrypoints not selected");
    Result validInspect = cli(primary), runtimeInspect = cli(runtime), apiInspect = cli(api);
    require(validInspect.code == 0 && validInspect.text.contains("compatibility=COMPATIBLE")
            && validInspect.text.matches("(?s).*artifact\\.sha256=[0-9a-f]{64}.*"),
        "compatible CLI inspection failed");
    require(runtimeInspect.code == 3 && runtimeInspect.text.contains("RUNTIME_MISMATCH"),
        "runtime mismatch was not reported");
    require(apiInspect.code == 3 && apiInspect.text.contains("WORLDLINE_API_MISMATCH"),
        "API mismatch was not reported");
    Result invalidInspect = cli(malformed), missingInspect = cli(missing);
    require(invalidInspect.code == 1 && missingInspect.code == 1,
        "invalid descriptors did not fail closed");
    Result runtimeReject =
        process(game, "worldline.smoke.m7.M7ModSmoke", "reject", runtime.toString());
    Result apiReject = process(game, "worldline.smoke.m7.M7ModSmoke", "reject", api.toString());
    Result typeReject = process(game, "worldline.smoke.m7.M7ModSmoke", "reject", type.toString());
    require(runtimeReject.code == 0 && apiReject.code == 0 && typeReject.code == 0
            && runtimeReject.text.contains("REJECT=RUNTIME_MISMATCH")
            && apiReject.text.contains("REJECT=WORLDLINE_API_MISMATCH")
            && typeReject.text.contains("REJECT=ENTRYPOINT_TYPE"),
        "load rejection matrix failed");
    String report = "mods=worldline.glass-probe:worldline.benchmark.DescriptorProbeMod,"
        + "worldline.gold-probe:worldline.benchmark.SecondProbeMod"
        + "\nprimary=" + line(first.text, "WORLDLINE_M7_SIGNATURE=")
        + "\nsecondary=" + line(other.text, "WORLDLINE_M7_SIGNATURE=")
        + "\ninspection=COMPATIBLE,RUNTIME_MISMATCH,WORLDLINE_API_MISMATCH"
        + "\nrejection=RUNTIME_MISMATCH,WORLDLINE_API_MISMATCH,ENTRYPOINT_TYPE"
        + "\ninvalid=MALFORMED_DESCRIPTOR,MISSING_DESCRIPTOR\n";
    String signature = sha256(report);
    Properties expected = new Properties();
    try (java.io.Reader reader = Files.newBufferedReader(smoke.resolve("smoke.properties"))) {
      expected.load(reader);
    }
    require(signature.equals(expected.getProperty("expected.signature")),
        "M7 compatibility evidence diverged: " + signature);
    Files.write(build.resolve("evidence.txt"), report.getBytes(StandardCharsets.UTF_8));
    System.out.println("M7 mod loading cycle passed");
    System.out.println("  loaded mods: worldline.glass-probe, worldline.gold-probe");
    System.out.println("  rejected: runtime, API, entrypoint type, malformed, missing descriptor");
    System.out.println("  compatibility SHA-256: " + signature);
  }

  private Path modJar(String name, Path classes) throws Exception {
    Path jar = build.resolve(name + ".jar");
    Path descriptor = smoke.resolve("descriptors").resolve(name);
    run(Arrays.asList("jar", "--create", "--file", jar.toString(), "-C", classes.toString(), ".",
        "-C", descriptor.toString(), "META-INF/worldline-mod.properties"));
    return jar;
  }

  private Result cli(Path jar) throws Exception {
    List<Path> path = Arrays.asList(product("cli"), product("reproduction"), product("api"),
        product("invariants"), product("semantics"), product("trace"), product("mods"),
        product("analysis"));
    return process(path, "worldline.cli.WorldlineCli", "mod", "inspect", jar.toString());
  }

  private List<Path> gamePath(Path scenario, Path adapter) throws IOException {
    Path workspace = root.resolve("local/workspaces/b1.7.3");
    List<Path> result = new ArrayList<>(Arrays.asList(scenario,
        client.resolve("instrumented-client"), adapter, client.resolve("headless-classes"),
        product("api"), product("trace"), product("kernel"), product("mods"),
        workspace.resolve("minecraft/bin"), workspace.resolve("jars/minecraft.jar")));
    try (Stream<Path> paths = Files.walk(workspace.resolve("libraries"))) {
      result.addAll(paths.filter(path -> path.toString().endsWith(".jar"))
              .sorted()
              .collect(Collectors.toList()));
    }
    return result;
  }

  private Path compile(Path source, Path output, List<Path> dependencies) throws Exception {
    Files.createDirectories(output);
    List<String> command = new ArrayList<>(
        Arrays.asList("javac", "-encoding", "UTF-8", "--release", "8", "-Xlint:all,-options",
            "-Werror", "-classpath", classpath(dependencies), "-d", output.toString()));
    try (Stream<Path> paths = Files.walk(source)) {
      paths.filter(path -> path.toString().endsWith(".java"))
          .sorted()
          .forEach(path -> command.add(path.toString()));
    }
    run(command);
    return output;
  }

  private Result process(List<Path> paths, String type, String... arguments) throws Exception {
    List<String> command = new ArrayList<>(
        Arrays.asList("java", "-Djava.awt.headless=true", "-classpath", classpath(paths), type));
    command.addAll(Arrays.asList(arguments));
    Process process =
        new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
    String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    return new Result(process.waitFor(), output);
  }

  private void run(List<String> command) throws Exception {
    Process process =
        new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
    String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    int code = process.waitFor();
    if (code != 0)
      throw new IllegalStateException(command.get(0) + " failed\n" + output);
  }

  private String classpath(List<Path> paths) {
    return paths.stream()
        .map(Path::toString)
        .collect(Collectors.joining(System.getProperty("path.separator")));
  }
  private Path product(String name) {
    return root.resolve(".worldline/build/classes").resolve(name);
  }
  private String line(String text, String prefix) {
    return text.lines()
        .filter(row -> row.startsWith(prefix))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("missing " + prefix))
        .substring(prefix.length());
  }
  private String sha256(String text) throws Exception {
    byte[] hash =
        MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
    StringBuilder result = new StringBuilder();
    for (byte value : hash)
      result.append(String.format("%02x", value & 255));
    return result.toString();
  }
  private void recreate(Path target) throws IOException {
    if (Files.exists(target))
      try (Stream<Path> paths = Files.walk(target)) {
        for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList()))
          Files.delete(path);
      }
    Files.createDirectories(target);
  }
  private static void require(boolean value, String message) {
    if (!value)
      throw new IllegalStateException(message);
  }
  private static final class Result {
    final int code;
    final String text;
    Result(int code, String text) {
      this.code = code;
      this.text = text;
    }
  }
}
