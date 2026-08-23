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

/** Differentially verifies the inventory UI tree against the official JAR. */
public final class GuiCycle {
  private final Path root = Paths.get("").toAbsolutePath().normalize();
  private final Path client = root.resolve(".worldline/smokes/controlled-client-tick");
  private final Path smoke = root.resolve("smokes/gui-tree");
  private final Path build = root.resolve(".worldline/smokes/gui-tree");

  public static void main(String[] arguments) {
    if (!Arrays.equals(arguments, new String[] {"gui-tree"})) {
      System.err.println("usage: java tools/smoke/GuiCycle.java gui-tree");
      System.exit(2);
    }
    try {
      new GuiCycle().execute();
    } catch (Exception error) {
      System.err.println("GUI tree cycle failed: " + error.getMessage());
      System.exit(1);
    }
  }

  private void execute() throws Exception {
    require(Files.isDirectory(client.resolve("headless-classes"))
        && Files.isDirectory(client.resolve("instrumented-client"))
        && Files.isDirectory(client.resolve("oracle-classes")),
        "run ClientCycle before GuiCycle");
    recreate(build);
    Path adapterRoot = root.resolve("adapters/b173-client");
    List<Path> libraries = jars(root.resolve("local/workspaces/b1.7.3/libraries"));
    List<Path> adapterPath = new ArrayList<Path>(Arrays.asList(
        client.resolve("headless-classes"), root.resolve("local/workspaces/b1.7.3/minecraft/bin"),
        product("api"), product("kernel"), product("reproduction"), product("trace"),
        product("mods"), product("modtest"), product("minimization"), product("fuzz"),
        product("profiling"), product("analysis"), product("testapi")));
    adapterPath.addAll(libraries);
    Path adapter = compile(adapterRoot.resolve("src/main/java"),
        build.resolve("adapter-classes"), adapterPath);
    Path subject = compile(smoke.resolve("src"), build.resolve("classes"),
        Arrays.asList(adapter, product("api"), product("trace"), product("kernel")));
    Path official = root.resolve("local/workspaces/b1.7.3/jars/minecraft.jar");
    List<Path> oraclePath = new ArrayList<Path>(Arrays.asList(client.resolve("oracle-classes"),
        client.resolve("headless-classes"), product("trace"), official));
    oraclePath.addAll(libraries);
    Path oracle = compile(smoke.resolve("oracle-src"), build.resolve("oracle-classes"), oraclePath);
    List<Path> subjectPath = new ArrayList<Path>(Arrays.asList(subject,
        client.resolve("instrumented-client"), adapter,
        client.resolve("headless-classes"), product("api"), product("trace"), product("kernel"),
        root.resolve("local/workspaces/b1.7.3/minecraft/bin"), official));
    subjectPath.addAll(libraries);
    List<Path> officialPath =
        new ArrayList<Path>(Arrays.asList(oracle, client.resolve("oracle-classes"),
            client.resolve("headless-classes"), product("trace"), official));
    officialPath.addAll(libraries);
    Outcome first = run("worldline.smoke.gui.GuiTreeSmoke", subjectPath, "instrumented-client/");
    Outcome second = run("worldline.smoke.gui.GuiTreeSmoke", subjectPath, "instrumented-client/");
    Outcome oracleFirst = run("GuiTreeOracle", officialPath, "jars/minecraft.jar");
    Outcome oracleSecond = run("GuiTreeOracle", officialPath, "jars/minecraft.jar");
    require(first.equals(second) && oracleFirst.equals(oracleSecond), "fresh processes diverged");
    require(first.equals(oracleFirst), "neutral UI tree and official JAR traces diverged");
    Properties properties = new Properties();
    try (java.io.Reader reader = Files.newBufferedReader(smoke.resolve("smoke.properties"))) {
      properties.load(reader);
    }
    require(first.signature.equals(properties.getProperty("expected.signature")),
        "GUI tree diverged from frozen signature: " + first.signature);
    Files.write(build.resolve("evidence.txt"),
        ("processes=4\nofficial.oracle=MATCH\ntrace=" + first.trace
            + "\nsignature=" + first.signature + "\n")
            .getBytes(StandardCharsets.UTF_8));
    System.out.println("GUI tree cycle passed");
    System.out.println("  processes: 4 (2 neutral UI, 2 official oracle)");
    System.out.println("  official oracle: MATCH");
    System.out.println("  signature: " + first.signature);
  }

  private Outcome run(String type, List<Path> paths, String source) throws Exception {
    String output = capture(
        Arrays.asList("java", "-Djava.awt.headless=true", "-classpath", classpath(paths), type));
    require(output.replace('\\', '/').contains(source), "wrong Minecraft class source");
    require(output.contains("WORLDLINE_GUI_API=screen,slot,click,spec"), "GUI API marker missing");
    return new Outcome(
        line(output, "WORLDLINE_GUI_TRACE="), line(output, "WORLDLINE_GUI_SIGNATURE="));
  }

  private Path compile(Path source, Path output, List<Path> dependencies) throws Exception {
    Files.createDirectories(output);
    List<String> command = new ArrayList<String>(
        Arrays.asList("javac", "-encoding", "UTF-8", "--release", "8", "-Xlint:all,-options",
            "-Werror", "-classpath", classpath(dependencies), "-d", output.toString()));
    javaFiles(source).forEach(path -> command.add(path.toString()));
    capture(command);
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

  private String capture(List<String> command) throws Exception {
    Process process =
        new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
    String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    if (process.waitFor() != 0)
      throw new IllegalStateException(command.get(0) + " failed\n" + output);
    return output;
  }

  private void recreate(Path target) throws IOException {
    if (Files.exists(target))
      try (Stream<Path> paths = Files.walk(target)) {
        for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList()))
          Files.delete(path);
      }
    Files.createDirectories(target);
  }

  private Path product(String name) {
    return root.resolve(".worldline/build/classes").resolve(name);
  }
  private String classpath(List<Path> paths) {
    return paths.stream()
        .map(Path::toString)
        .collect(Collectors.joining(System.getProperty("path.separator")));
  }
  private String line(String output, String prefix) {
    return output.lines()
        .filter(value -> value.startsWith(prefix))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("missing " + prefix))
        .substring(prefix.length());
  }
  private static void require(boolean value, String message) {
    if (!value)
      throw new IllegalStateException(message);
  }

  private static final class Outcome {
    private final String trace, signature;
    Outcome(String trace, String signature) {
      this.trace = trace;
      this.signature = signature;
    }
    @Override
    public boolean equals(Object other) {
      return other instanceof Outcome && trace.equals(((Outcome) other).trace)
          && signature.equals(((Outcome) other).signature);
    }
    @Override
    public int hashCode() {
      return 31 * trace.hashCode() + signature.hashCode();
    }
  }
}
