import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

/** Runs the external Java example suite through the verified b1.7.3 provider. */
public final class TestKitCycle {
  private static final String ID = "testkit-cycle";
  private static final String SIGNAL = "specs=11,tests=31,runtime=fresh-serial";
  private final Path root = Paths.get("").toAbsolutePath().normalize();
  private TestKitCycle() {
  }
  public static void main(String[] arguments) {
    if (!Arrays.equals(arguments, new String[] {ID})) {
      System.err.println("usage: java tools/smoke/TestKitCycle.java " + ID);
      System.exit(2);
    }
    try {
      new TestKitCycle().execute();
    } catch (Exception failure) {
      System.err.println("TestKit cycle failed: " + failure.getMessage());
      System.exit(1);
    }
  }
  private void execute() throws Exception {
    Path build = root.resolve(".worldline/smokes/testkit-cycle");
    SmokeSupport.recreate(root, build);
    Path classes = root.resolve(".worldline/build/classes");
    Path client = root.resolve(".worldline/smokes/controlled-client-tick");
    Properties smoke = properties(root.resolve("smokes/controlled-client-tick/smoke.properties"));
    Path workspace = root.resolve(smoke.getProperty("workspace")).normalize();
    List<Path> libraries = jarFiles(workspace.resolve("libraries"));
    Path adapter = build.resolve("adapter-classes");
    Files.createDirectories(adapter);
    List<Path> adapterPath = paths(client.resolve("headless-classes"),
        workspace.resolve("minecraft/bin"), classes.resolve("api"), classes.resolve("kernel"),
        classes.resolve("reproduction"), classes.resolve("trace"), classes.resolve("mods"),
        classes.resolve("modtest"), classes.resolve("minimization"), classes.resolve("fuzz"),
        classes.resolve("profiling"), classes.resolve("analysis"), classes.resolve("testapi"));
    adapterPath.addAll(libraries);
    List<String> adapterCompile = new ArrayList<>(Arrays.asList("javac", "-encoding", "UTF-8",
        "--release", "8", "-Xlint:all,-options", "-Werror", "-classpath", join(adapterPath),
        "-d", adapter.toString()));
    for (Path source : javaFiles(root.resolve("adapters/b173-client/src/main/java")))
      adapterCompile.add(source.toString());
    run(adapterCompile, false);
    Path spec = build.resolve("spec-classes");
    Files.createDirectories(spec);
    List<String> compile = new ArrayList<>(Arrays.asList("javac", "-encoding", "UTF-8", "--release",
        "8", "-Xlint:all,-options", "-Werror", "-classpath",
        join(paths(
            classes.resolve("api"), classes.resolve("testmodel"), classes.resolve("testapi"))),
        "-d", spec.toString()));
    for (Path source : javaFiles(root.resolve("examples/testkit/src/test/java")))
      compile.add(source.toString());
    run(compile, false);
    List<Path> runtime = paths(spec, classes.resolve("optimization"), classes.resolve("api"),
        classes.resolve("invariants"), classes.resolve("semantics"), classes.resolve("trace"),
        classes.resolve("kernel"), classes.resolve("reproduction"), classes.resolve("mods"),
        classes.resolve("analysis"), classes.resolve("modtest"), classes.resolve("minimization"),
        classes.resolve("testmodel"), classes.resolve("testapi"), classes.resolve("testkit"),
        classes.resolve("cli"), adapter,
        client.resolve("instrumented-client"), client.resolve("headless-classes"),
        workspace.resolve("minecraft/bin"), workspace.resolve("jars/minecraft.jar"));
    runtime.addAll(libraries);
    List<String> command = new ArrayList<>(Arrays.asList("java", "-classpath", join(runtime),
        "worldline.cli.WorldlineCli", "test", "run", spec.toString(),
        "--provider=worldline.b173.B173TestRuntimeProvider", "--world=" + build.resolve("world"),
        "--artifacts=" + build.resolve("results"), "--snapshots=" + build.resolve("snapshots"),
        "--runtime-lock=" + build.resolve("official-runtime.lock"), "--update-snapshots",
        "--reporter=agent"));
    String output = run(command, true);
    require(output.contains("WORLDLINE_TEST=PASS") && output.contains("tests=31"),
        "external example suite did not pass all 31 collected tests");
    Properties descriptor = properties(root.resolve("smokes/testkit-cycle/smoke.properties"));
    String signature = HexFormat.of().formatHex(
        MessageDigest.getInstance("SHA-256").digest(SIGNAL.getBytes(StandardCharsets.UTF_8)));
    require(SIGNAL.equals(descriptor.getProperty("expected.signal")), "TestKit signal drifted");
    require(signature.equals(descriptor.getProperty("expected.signature")),
        "TestKit signature drifted");
    System.out.println("TestKit cycle passed");
    System.out.println("  external Java 8 specs: 11 files, 31 tests");
    System.out.println("  runtime sessions: fresh and serial under the exclusive lock");
    System.out.println("  signal: " + SIGNAL);
    System.out.println("  signature: " + signature);
  }
  private String run(List<String> command, boolean capture) throws Exception {
    ProcessBuilder builder = new ProcessBuilder(command).directory(root.toFile());
    if (!capture)
      builder.inheritIO();
    else
      builder.redirectErrorStream(true);
    Process process = builder.start();
    String output =
        capture ? new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8) : "";
    int status = process.waitFor();
    if (capture)
      System.out.print(output);
    require(status == 0, "command failed: " + command.get(0));
    return output;
  }
  private static List<Path> javaFiles(Path root) throws Exception {
    List<Path> values = new ArrayList<>();
    try (Stream<Path> stream = Files.walk(root)) {
      stream.filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".java"))
          .sorted(Comparator.naturalOrder())
          .limit(101)
          .forEach(values::add);
    }
    require(!values.isEmpty() && values.size() <= 100, "invalid example source count");
    return values;
  }
  private static List<Path> jarFiles(Path root) throws Exception {
    List<Path> values = new ArrayList<>();
    try (Stream<Path> stream = Files.walk(root)) {
      stream.filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".jar"))
          .sorted(Comparator.naturalOrder())
          .limit(101)
          .forEach(values::add);
    }
    require(!values.isEmpty() && values.size() <= 100, "invalid runtime library count");
    return values;
  }
  private static Properties properties(Path path) throws Exception {
    Properties value = new Properties();
    try (java.io.Reader reader = Files.newBufferedReader(path)) {
      value.load(reader);
    }
    return value;
  }
  private static List<Path> paths(Path... values) {
    List<Path> result = new ArrayList<>();
    for (Path value : values) {
      require(Files.exists(value), "missing TestKit smoke dependency: " + value);
      result.add(value);
    }
    return result;
  }
  private static String join(List<Path> paths) {
    List<String> values = new ArrayList<>();
    for (Path path : paths)
      values.add(path.toString());
    return String.join(File.pathSeparator, values);
  }
  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
}
