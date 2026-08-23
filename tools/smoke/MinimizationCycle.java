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

/** Qualifies automatic scenario minimization against real mod-version executions. */
public final class MinimizationCycle {
  private static final String ID = "m9-scenario-minimization";
  private final Path root = Paths.get("").toAbsolutePath().normalize();
  private final Path smoke = root.resolve("smokes").resolve(ID);
  private final Path build = root.resolve(".worldline/smokes").resolve(ID);
  private final Path client = root.resolve(".worldline/smokes/controlled-client-tick");
  private final Path m8 = root.resolve(".worldline/smokes/m8-mod-version-diff");

  public static void main(String[] arguments) {
    if (!Arrays.equals(arguments, new String[] {ID})) {
      System.err.println("usage: java tools/smoke/MinimizationCycle.java " + ID);
      System.exit(2);
    }
    try {
      new MinimizationCycle().execute();
    } catch (Exception error) {
      System.err.println("M9 minimization cycle failed: " + error.getMessage());
      System.exit(1);
    }
  }

  private void execute() throws Exception {
    Path adapter = client.resolve("adapter-classes"), v1 = m8.resolve("v1-a.jar"),
         v2 = m8.resolve("v2-a.jar");
    require(Files.isDirectory(adapter) && Files.isRegularFile(v1) && Files.isRegularFile(v2),
        "run client and M8 cycles before M9");
    recreate(build);
    Path classes = compile(smoke.resolve("src"), build.resolve("classes"),
        Arrays.asList(adapter, product("api"), product("invariants"), product("semantics"),
            product("trace"), product("mods"), product("analysis"), product("minimization")));
    List<Path> runtime = gamePath(classes, adapter);
    Path originalA = build.resolve("original-a.wlscenario"),
         minimizedA = build.resolve("minimized-a.wlscenario");
    Path originalB = build.resolve("original-b.wlscenario"),
         minimizedB = build.resolve("minimized-b.wlscenario");
    Result first = process(runtime, "worldline.smoke.m9.M9MinimizationSmoke", v1.toString(),
        v2.toString(), originalA.toString(), minimizedA.toString());
    Result second = process(runtime, "worldline.smoke.m9.M9MinimizationSmoke", v1.toString(),
        v2.toString(), originalB.toString(), minimizedB.toString());
    require(first.code == 0, "first minimization failed\n" + first.text);
    require(second.code == 0, "second minimization failed\n" + second.text);
    require(first.text.equals(second.text)
            && Arrays.equals(Files.readAllBytes(originalA), Files.readAllBytes(originalB))
            && Arrays.equals(Files.readAllBytes(minimizedA), Files.readAllBytes(minimizedB)),
        "fresh minimization processes diverged");
    require(first.text.contains("WORLDLINE_M9_MINIMIZATION=PASS")
            && first.text.contains("original.steps=9") && first.text.contains("minimized.steps=3")
            && first.text.contains("complete=true")
            && first.text.contains("steps=observe:before,tick,observe:target")
            && first.text.contains("invariant=block-conservation"),
        "minimization proof markers missing");
    Result inspect = cli("scenario", "inspect", minimizedA.toString());
    require(inspect.code == 0 && inspect.text.contains("steps=3")
            && inspect.text.contains("0=observe:before") && inspect.text.contains("1=tick")
            && inspect.text.contains("2=observe:target"),
        "minimized scenario inspection failed");
    byte[] corrupt = Files.readAllBytes(minimizedA);
    corrupt[corrupt.length - 3] ^= 1;
    Path corruptPath = build.resolve("corrupt.wlscenario");
    Files.write(corruptPath, corrupt);
    require(cli("scenario", "inspect", corruptPath.toString()).code == 1,
        "corrupt minimized scenario was accepted");
    String signature = line(first.text, "evidence.sha256=");
    Files.write(build.resolve("evidence.txt"), first.text.getBytes(StandardCharsets.UTF_8));
    Properties expected = new Properties();
    try (java.io.Reader reader = Files.newBufferedReader(smoke.resolve("smoke.properties"))) {
      expected.load(reader);
    }
    require(signature.equals(expected.getProperty("expected.signature")),
        "M9 evidence diverged: " + signature);
    System.out.println("M9 automatic scenario minimization cycle passed");
    System.out.println("  reduction: 9 -> 3 steps; one-minimal: verified");
    System.out.println("  exact divergence: tick1 target.block65, 20 -> 0");
    System.out.println("  evidence SHA-256: " + signature);
  }

  private Result cli(String... arguments) throws Exception {
    return process(
        Arrays.asList(product("cli"), product("reproduction"), product("api"),
            product("invariants"), product("semantics"), product("trace"), product("mods"),
            product("analysis"), product("modtest"), product("minimization")),
        "worldline.cli.WorldlineCli", arguments);
  }
  private List<Path> gamePath(Path scenario, Path adapter) throws IOException {
    Path workspace = root.resolve("local/workspaces/b1.7.3");
    List<Path> result = new ArrayList<>(Arrays.asList(scenario,
        client.resolve("instrumented-client"), adapter, client.resolve("headless-classes"),
        product("api"), product("invariants"), product("semantics"), product("trace"),
        product("kernel"), product("mods"), product("analysis"), product("minimization"),
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
    Result result = command(command);
    require(result.code == 0, "compilation failed\n" + result.text);
    return output;
  }
  private Result process(List<Path> paths, String type, String... arguments) throws Exception {
    List<String> command = new ArrayList<>(
        Arrays.asList("java", "-Djava.awt.headless=true", "-classpath", classpath(paths), type));
    command.addAll(Arrays.asList(arguments));
    return command(command);
  }
  private Result command(List<String> command) throws Exception {
    Process process =
        new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
    String text = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    return new Result(process.waitFor(), text);
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
  private void recreate(Path target) throws IOException {
    if (Files.exists(target))
      try (Stream<Path> paths = Files.walk(target)) {
        for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList()))
          Files.delete(path);
      }
    Files.createDirectories(target);
  }
  private static void require(boolean condition, String message) {
    if (!condition)
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
