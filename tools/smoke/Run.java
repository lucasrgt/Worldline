import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Prepares and executes a deterministic smoke against mapped vanilla classes. */
public final class Run {
  private static final String TRACE_PREFIX = "WORLDLINE_SMOKE_TRACE=";
  private static final String SIGNATURE_PREFIX = "WORLDLINE_SMOKE_SIGNATURE=";

  private final Path root = Paths.get("").toAbsolutePath().normalize();
  private final Properties smoke = new Properties();
  private final String id;

  private Run(String id) {
    if (!id.matches("[a-z0-9][a-z0-9-]*"))
      throw new IllegalArgumentException("invalid smoke id");
    this.id = id;
  }

  public static void main(String[] arguments) {
    if (arguments.length != 1) {
      System.err.println("usage: java tools/smoke/Run.java <smoke-id>");
      System.exit(2);
    }
    try {
      new Run(arguments[0]).execute();
    } catch (Exception error) {
      System.err.println("smoke failed: " + error.getMessage());
      System.exit(1);
    }
  }

  private void execute() throws Exception {
    loadSmoke();
    stage("RetroMCP bootstrap", root, "java", "tools/toolchains/Bootstrap.java", "retromcp");
    stage("runtime inputs", root, "java", "tools/harness/RuntimeCheck.java", "--required");

    Path workspace = localPath(required("workspace"));
    prepareWorkspace(workspace);
    Path version = workspace.resolve("conf/version.json");
    if (!Files.isRegularFile(version)) {
      stage("RetroMCP setup b1.7.3", workspace, "java", "-jar", "RetroMCP-CLI.jar", "setup",
          required("version"));
    }
    verifyWorkspaceInputs(workspace);
    verifySymbolMap(workspace.resolve("conf/mappings.tiny"));

    Path serverClasses = workspace.resolve("minecraft_server/bin");
    Path worldClass = serverClasses.resolve("net/minecraft/src/World.class");
    if (!Files.isRegularFile(worldClass)) {
      stage("RetroMCP decompile and recompile server", workspace, "java", "-jar",
          "RetroMCP-CLI.jar", "decompile", required("side"));
    }
    verifyWorkspaceInputs(workspace);
    if (!Files.isRegularFile(worldClass)) {
      throw new IllegalStateException("RetroMCP produced no mapped World.class");
    }

    Path serverJar = workspace.resolve("jars/minecraft_server.jar");
    Path output = compileScenario("src", "classes",
        classpath(serverClasses, productClasses("api"), productClasses("trace"),
            productClasses("kernel")),
        "smoke compilation");
    verifyControlPath(output, serverClasses);
    Path oracle = compileScenario("oracle-src", "oracle-classes",
        classpath(productClasses("trace"), serverJar), "official oracle compilation");
    Outcome first = scenario(required("worldline.main"), output, productClasses("api"),
        productClasses("trace"), productClasses("kernel"), serverClasses, serverJar);
    Outcome second = scenario(required("worldline.main"), output, productClasses("api"),
        productClasses("trace"), productClasses("kernel"), serverClasses, serverJar);
    Outcome officialFirst =
        scenario(required("oracle.main"), oracle, productClasses("trace"), serverJar);
    Outcome officialSecond =
        scenario(required("oracle.main"), oracle, productClasses("trace"), serverJar);
    requireSame(first, second, "Worldline processes");
    requireSame(officialFirst, officialSecond, "official oracle processes");
    requireSame(first, officialFirst, "Worldline and official oracle");
    String expected = required("expected.signature");
    if (expected.equals("pending")) {
      System.out.println(
          id + " diagnostic passed; qualification not attempted\n  signature: " + first.signature);
      System.out.println("  trace: " + first.trace);
      return;
    }
    if (!expected.equals(first.signature)) {
      throw new IllegalStateException(
          "trace diverged from frozen signature: " + first.signature + " != " + expected);
    }

    Path evidence = writeEvidence(first);
    System.out.println(id + " smoke passed");
    System.out.println("  frozen signal: " + required("expected.signal"));
    System.out.println("  processes: 4 (2 Worldline, 2 official oracle)");
    System.out.println("  official oracle: MATCH");
    System.out.println("  signature: " + first.signature);
    System.out.println("  trace: " + first.trace);
    System.out.println("  evidence: " + root.relativize(evidence));
  }

  private void loadSmoke() throws IOException {
    Path path = root.resolve("smokes").resolve(id).resolve("smoke.properties");
    try (java.io.Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      smoke.load(reader);
    }
    if (!id.equals(required("id"))) {
      throw new IllegalStateException("smoke descriptor id mismatch");
    }
  }

  private void prepareWorkspace(Path workspace) throws IOException {
    Files.createDirectories(workspace.resolve("jars"));
    Files.copy(root.resolve("local/toolchains/retromcp-java/build/libs/RetroMCP-CLI-all.jar"),
        workspace.resolve("RetroMCP-CLI.jar"), StandardCopyOption.REPLACE_EXISTING);
    Files.copy(root.resolve("local/artifacts/minecraft-b1.7.3-client.jar"),
        workspace.resolve("jars/minecraft.jar"), StandardCopyOption.REPLACE_EXISTING);
  }

  private void verifyWorkspaceInputs(Path workspace) throws Exception {
    verifyHash(workspace.resolve("jars/minecraft_server.jar"), required("server.jar.sha256"));
    verifyHash(workspace.resolve("conf/version.json"), required("version.json.sha256"));
    verifyHash(workspace.resolve("conf/mappings.tiny"), required("mappings.tiny.sha256"));
    verifyHash(workspace.resolve("conf/exceptions.exc"), required("exceptions.exc.sha256"));

    Properties artifact = load(root.resolve("artifacts/minecraft-b1.7.3-client.properties"));
    verifyHash(workspace.resolve("jars/minecraft.jar"), artifact.getProperty("expected.sha256"));
  }

  private void verifySymbolMap(Path mappingsPath) throws IOException {
    List<String> mappings = Files.readAllLines(mappingsPath, StandardCharsets.UTF_8);
    Path symbolsPath = root.resolve("smokes").resolve(id).resolve("symbols.map");
    List<String> symbols = Files.readAllLines(symbolsPath, StandardCharsets.UTF_8);
    int verified = 0;
    for (String row : symbols) {
      if (row.isEmpty() || row.startsWith("#")) {
        continue;
      }
      String[] columns = row.split("\\t", -1);
      if (columns.length != 6) {
        throw new IllegalStateException("invalid symbols.map row: " + row);
      }
      String classRow = "c\t" + columns[0];
      int start = -1;
      for (int index = 0; index < mappings.size(); index++) {
        if (mappings.get(index).startsWith(classRow + "\t")) {
          start = index;
          break;
        }
      }
      if (start < 0) {
        throw new IllegalStateException("mapped owner is absent: " + columns[0]);
      }
      int end = start + 1;
      while (end < mappings.size() && !mappings.get(end).startsWith("c\t")) {
        end++;
      }
      String expected = columns[1].equals("c")
          ? "c\t" + columns[3] + "\t" + columns[4] + "\t" + columns[5]
          : "\t" + columns[1] + "\t" + columns[2] + "\t" + columns[3] + "\t" + columns[4] + "\t"
              + columns[5];
      if (!mappings.subList(start, end).contains(expected)) {
        throw new IllegalStateException(
            "mapped symbol is absent from owner " + columns[0] + ": " + expected);
      }
      verified++;
    }
    System.out.println("  mapped symbols: " + verified + " verified");
  }

  private Path compileScenario(
      String sourceName, String outputName, String dependencies, String label) throws Exception {
    Path output = root.resolve(".worldline/smokes").resolve(id).resolve(outputName).normalize();
    recreate(output, root.resolve(".worldline").normalize());
    List<Path> sources = javaFiles(root.resolve("smokes").resolve(id).resolve(sourceName));
    List<String> command =
        new ArrayList<>(Arrays.asList("javac", "-encoding", "UTF-8", "--release", "8",
            "-Xlint:all,-options", "-Werror", "-classpath", dependencies, "-d", output.toString()));
    sources.forEach(source -> command.add(source.toString()));
    stage(label, root, command.toArray(new String[0]));
    return output;
  }

  private Outcome scenario(String mainClass, Path... paths) throws Exception {
    String text = capture(
        root, "java", "-Djava.awt.headless=true", "-classpath", classpath(paths), mainClass);
    return new Outcome(line(text, TRACE_PREFIX), line(text, SIGNATURE_PREFIX));
  }

  private void requireSame(Outcome left, Outcome right, String label) {
    if (!left.trace.equals(right.trace) || !left.signature.equals(right.signature)) {
      throw new IllegalStateException(label + " produced different canonical traces");
    }
  }

  private void verifyControlPath(Path output, Path serverClasses) throws Exception {
    String paths = classpath(output, productClasses("api"), productClasses("trace"),
        productClasses("kernel"), serverClasses);
    String driver =
        capture(root, "javap", "-classpath", paths, "-c", "-p", required("control.driver"));
    String backend =
        capture(root, "javap", "-classpath", paths, "-c", "-p", required("control.backend"));
    if (!driver.contains("InterfaceMethod worldline/api/MinecraftRuntime.tick:()V")
        || (Boolean.parseBoolean(smoke.getProperty("control.world-tick", "true"))
            && !backend.contains("Method net/minecraft/src/World.tick:()V"))
        || (Boolean.parseBoolean(required("control.entity-tick"))
            && !backend.contains("Method net/minecraft/src/World.updateEntities:()V"))) {
      throw new IllegalStateException(
          "compiled smoke does not preserve runtime -> backend -> World.tick path");
    }
    System.out.println("  control path: MinecraftRuntime -> GameBackend -> World.tick verified");
  }

  private Path productClasses(String module) {
    String override = System.getenv("WORLDLINE_PRODUCT_ROOT");
    Path products = override == null || override.isBlank()
        ? root.resolve(".worldline/build/classes")
        : Paths.get(override).toAbsolutePath().normalize();
    if (!products.startsWith(root.resolve(".worldline").normalize())) {
      throw new IllegalStateException("smoke product root escapes .worldline");
    }
    return products.resolve(module);
  }

  private String classpath(Path... paths) {
    return Arrays.stream(paths)
        .map(Path::toString)
        .collect(Collectors.joining(System.getProperty("path.separator")));
  }

  private String line(String output, String prefix) {
    List<String> matches = Arrays.stream(output.split("\\R"))
                               .filter(value -> value.startsWith(prefix))
                               .collect(Collectors.toList());
    if (matches.size() != 1) {
      throw new IllegalStateException(
          "scenario emitted " + matches.size() + " lines for " + prefix);
    }
    return matches.get(0).substring(prefix.length());
  }

  private Path writeEvidence(Outcome outcome) throws IOException {
    Path path = root.resolve(".worldline/smokes").resolve(id).resolve("evidence.txt");
    String evidence = "id=" + id + "\nprocesses=4\nworldline.processes=2\nofficial.processes=2"
        + "\nofficial.jar.sha256=" + required("server.jar.sha256")
        + "\nofficial.oracle=MATCH\nsignature=" + outcome.signature + "\ntrace=" + outcome.trace
        + "\n";
    Files.write(path, evidence.getBytes(StandardCharsets.UTF_8));
    return path;
  }

  private void verifyHash(Path path, String expected) throws Exception {
    if (!Files.isRegularFile(path)) {
      throw new IllegalStateException("missing frozen workspace input: " + path);
    }
    String actual = sha256(path);
    if (!actual.equals(expected)) {
      throw new IllegalStateException("frozen input drift: " + path + " has SHA-256 " + actual);
    }
  }

  private String sha256(Path path) throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    try (java.io.InputStream input = Files.newInputStream(path)) {
      byte[] buffer = new byte[8192];
      int count;
      while ((count = input.read(buffer)) >= 0) {
        digest.update(buffer, 0, count);
      }
    }
    return HexFormat.of().formatHex(digest.digest());
  }

  private Properties load(Path path) throws IOException {
    Properties properties = new Properties();
    try (java.io.Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      properties.load(reader);
    }
    return properties;
  }

  private String required(String key) {
    String value = smoke.getProperty(key);
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalStateException("missing smoke property: " + key);
    }
    return value.trim();
  }

  private Path localPath(String relative) {
    Path local = root.resolve("local").normalize();
    Path path = root.resolve(relative).normalize();
    if (!path.startsWith(local) || path.equals(local)) {
      throw new IllegalStateException("smoke workspace must be inside local/");
    }
    return path;
  }

  private List<Path> javaFiles(Path source) throws IOException {
    try (Stream<Path> paths = Files.walk(source)) {
      return paths.filter(path -> path.toString().endsWith(".java"))
          .sorted()
          .collect(Collectors.toList());
    }
  }

  private void recreate(Path target, Path safeRoot) throws IOException {
    if (!target.startsWith(safeRoot) || target.equals(safeRoot)) {
      throw new IllegalStateException("unsafe generated output path: " + target);
    }
    if (Files.exists(target)) {
      try (Stream<Path> paths = Files.walk(target)) {
        for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
          Files.delete(path);
        }
      }
    }
    Files.createDirectories(target);
  }

  private void stage(String label, Path directory, String... command) throws Exception {
    capture(directory, command);
    System.out.println("  " + label + ": passed");
  }

  private String capture(Path directory, String... command) throws Exception {
    Process process =
        new ProcessBuilder(command).directory(directory.toFile()).redirectErrorStream(true).start();
    String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    int exit = process.waitFor();
    if (exit != 0) {
      throw new IllegalStateException(command[0] + " exited " + exit + "\n" + output);
    }
    return output;
  }

  private static final class Outcome {
    private final String trace;
    private final String signature;

    private Outcome(String trace, String signature) {
      this.trace = trace;
      this.signature = signature;
    }
  }
}
