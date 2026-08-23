import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Builds and verifies the replay, branching, GUI, and mod laboratory slice. */
public final class LabCycle {
  private final Path root = Paths.get("").toAbsolutePath().normalize();
  private final Path client = root.resolve(".worldline/smokes/controlled-client-tick");
  private final Path smoke = root.resolve("smokes/lab-cycle");
  private final Path build = root.resolve(".worldline/smokes/lab-cycle");

  public static void main(String[] arguments) {
    if (!Arrays.equals(arguments, new String[] {"lab-cycle"})) {
      System.err.println("usage: java tools/smoke/LabCycle.java lab-cycle");
      System.exit(2);
    }
    try {
      new LabCycle().execute();
    } catch (Exception error) {
      System.err.println("lab cycle failed: " + error.getMessage());
      System.exit(1);
    }
  }

  private void execute() throws Exception {
    Path adapter = client.resolve("adapter-classes");
    require(Files.isDirectory(adapter), "run ClientCycle before LabCycle");
    recreate(build);
    Path modClasses =
        compile(smoke.resolve("mod-src"), build.resolve("mod-classes"), Arrays.asList(adapter));
    Path modJar = build.resolve("probe-mod.jar");
    run(Arrays.asList(
        "jar", "--create", "--file", modJar.toString(), "-C", modClasses.toString(), "."));
    List<Path> compilePath = Arrays.asList(adapter, product("api"), product("trace"));
    Path classes = compile(smoke.resolve("src"), build.resolve("classes"), compilePath);
    List<Path> runtime =
        new ArrayList<>(Arrays.asList(classes, client.resolve("instrumented-client"), adapter,
            client.resolve("headless-classes"), product("api"), product("trace"), product("kernel"),
            root.resolve("local/workspaces/b1.7.3/minecraft/bin"),
            root.resolve("local/workspaces/b1.7.3/jars/minecraft.jar")));
    runtime.addAll(jars(root.resolve("local/workspaces/b1.7.3/libraries")));
    String first = capture(Arrays.asList("java", "-Djava.awt.headless=true", "-classpath",
        classpath(runtime), "worldline.smoke.lab.WorldlineLabSmoke", modJar.toString()));
    String second = capture(Arrays.asList("java", "-Djava.awt.headless=true", "-classpath",
        classpath(runtime), "worldline.smoke.lab.WorldlineLabSmoke", modJar.toString()));
    require(first.equals(second), "fresh lab processes produced different evidence");
    String signature = line(first, "WORLDLINE_LAB_SIGNATURE=");
    Properties properties = new Properties();
    try (java.io.Reader reader = Files.newBufferedReader(smoke.resolve("smoke.properties"))) {
      properties.load(reader);
    }
    require(properties.getProperty("expected.signature").equals(signature),
        "lab trace diverged from frozen signature: " + signature);
    require(first.contains("WORLDLINE_LAB_CAPABILITIES=snapshot,restore,replay,branch,gui,mod")
            && first.contains("WORLDLINE_MOD_SOURCE=probe-mod.jar"),
        "lab proof markers missing");
    Files.write(build.resolve("evidence.txt"), first.getBytes(StandardCharsets.UTF_8));
    System.out.println("worldline lab cycle passed");
    System.out.println("  fresh processes: 2");
    System.out.println("  signature: " + signature);
    System.out.println("  capabilities: snapshot, restore, replay, branch, gui, mod");
    System.out.println("  benchmark mod: isolated probe-mod.jar");
  }

  private Path compile(Path source, Path output, List<Path> dependencies) throws Exception {
    Files.createDirectories(output);
    List<String> command = new ArrayList<>(
        Arrays.asList("javac", "-encoding", "UTF-8", "--release", "8", "-Xlint:all,-options",
            "-Werror", "-classpath", classpath(dependencies), "-d", output.toString()));
    javaFiles(source).forEach(path -> command.add(path.toString()));
    run(command);
    return output;
  }

  private List<Path> javaFiles(Path source) throws IOException {
    try (Stream<Path> paths = Files.walk(source)) {
      return paths.filter(path -> path.toString().endsWith(".java"))
          .sorted()
          .collect(Collectors.toList());
    }
  }

  private List<Path> jars(Path source) throws IOException {
    try (Stream<Path> paths = Files.walk(source)) {
      return paths.filter(path -> path.toString().endsWith(".jar"))
          .sorted()
          .collect(Collectors.toList());
    }
  }

  private Path product(String module) {
    return root.resolve(".worldline/build/classes").resolve(module);
  }

  private String classpath(List<Path> paths) {
    return paths.stream()
        .map(Path::toString)
        .collect(Collectors.joining(System.getProperty("path.separator")));
  }

  private String capture(List<String> command) throws Exception {
    Process process =
        new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
    String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    int exit = process.waitFor();
    if (exit != 0)
      throw new IllegalStateException(command.get(0) + " exited " + exit + "\n" + output);
    return output;
  }

  private void run(List<String> command) throws Exception {
    capture(command);
  }

  private String line(String output, String prefix) {
    return output.lines()
        .filter(value -> value.startsWith(prefix))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("missing " + prefix))
        .substring(prefix.length());
  }

  private void recreate(Path target) throws IOException {
    if (Files.exists(target)) {
      try (Stream<Path> paths = Files.walk(target)) {
        for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
          Files.delete(path);
        }
      }
    }
    Files.createDirectories(target);
  }

  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
}
