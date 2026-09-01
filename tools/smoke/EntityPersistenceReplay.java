import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/** Qualifies the concrete Beta 1.7.3 EntityList NBT persistence matrix twice. */
public final class EntityPersistenceReplay {
  private static final String ID = "b173-entity-persistence-envelope-cycle";
  private final Path root = Path.of("").toAbsolutePath().normalize();
  private final Path build = root.resolve(".worldline/smokes").resolve(ID);

  public static void main(String[] arguments) {
    if (!Arrays.equals(arguments, new String[] {ID})) {
      System.err.println("usage: java tools/smoke/EntityPersistenceReplay.java " + ID);
      System.exit(2);
    }
    try { new EntityPersistenceReplay().execute(); }
    catch (Exception error) {
      System.err.println("entity persistence cycle failed: " + error.getMessage());
      System.exit(1);
    }
  }

  private void execute() throws Exception {
    SmokeSupport.recreate(root, build);
    String classpath = compile();
    capture(build.resolve("first"), classpath);
    capture(build.resolve("second"), classpath);
    Path first = build.resolve("first/entity-persistence.wlevidence");
    Path second = build.resolve("second/entity-persistence.wlevidence");
    byte[] evidence = Files.readAllBytes(first);
    require(Arrays.equals(evidence, Files.readAllBytes(second)),
        "fresh entity persistence captures diverged");
    String canonical = new String(evidence, StandardCharsets.UTF_8);
    Outcome outcome = smoke(first, classpath);
    String signal = outcome.signal, trace = outcome.trace, signature = outcome.signature;
    Properties descriptor = new Properties();
    try (java.io.Reader reader = Files.newBufferedReader(
            root.resolve("smokes").resolve(ID).resolve("smoke.properties"))) {
      descriptor.load(reader);
    }
    require(signal.equals(descriptor.getProperty("expected.signal")), "frozen signal drifted");
    require(trace.equals(descriptor.getProperty("expected.trace")), "frozen trace drifted");
    require(signature.equals(descriptor.getProperty("expected.signature")),
        "frozen signature drifted");
    Files.writeString(build.resolve("evidence.txt"), canonical, StandardCharsets.UTF_8);
    System.out.println("WORLDLINE_B173_ENTITY_PERSISTENCE_SET=" + signal);
    System.out.println("WORLDLINE_B173_ENTITY_PERSISTENCE_TRACE=" + trace);
    System.out.println("WORLDLINE_B173_ENTITY_PERSISTENCE_SIGNATURE=" + signature);
    System.out.println("b173 entity persistence envelope cycle passed");
  }

  private void capture(Path output, String classpath) throws Exception {
    SmokeSupport.recreate(root, output);
    Path evidence = output.resolve("entity-persistence.wlevidence");
    Process process = new ProcessBuilder("java", "-classpath", classpath,
            "worldline.smoke.b173entitypersistence.B173EntityPersistenceSmoke", "capture",
            evidence.toString()).directory(root.toFile()).redirectErrorStream(true).start();
    String text = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    require(process.waitFor() == 0
            && text.contains("WORLDLINE_B173_ENTITY_PERSISTENCE_CAPTURE="),
        "entity persistence capture failed: " + summarize(text));
  }

  private String compile() throws Exception {
    Path headless = build.resolve("headless-classes");
    Files.createDirectories(headless);
    List<String> stubs = new ArrayList<>(Arrays.asList("javac", "--release", "8",
        "-encoding", "UTF-8", "-Xlint:all,-options", "-Werror", "-d", headless.toString()));
    stubs.addAll(SmokeSupport.javaFiles(root.resolve("adapters/b173-client/headless-src")));
    run(stubs, "headless compilation");
    Path classes = build.resolve("smoke-classes");
    Files.createDirectories(classes);
    List<Path> runtime = runtime(headless);
    Path source = root.resolve("smokes").resolve(ID).resolve(
        "src/worldline/smoke/b173entitypersistence/B173EntityPersistenceSmoke.java");
    String dependencies = join(runtime);
    run(Arrays.asList("javac", "--release", "8", "-encoding", "UTF-8",
        "-Xlint:all,-options", "-Werror", "-classpath", dependencies, "-d",
        classes.toString(), source.toString()), "smoke compilation");
    runtime.add(0, classes);
    return join(runtime);
  }

  private Outcome smoke(Path evidence, String classpath) throws Exception {
    Process run = new ProcessBuilder("java", "-classpath", classpath,
        "worldline.smoke.b173entitypersistence.B173EntityPersistenceSmoke",
        "verify", evidence.toString()).directory(root.toFile()).redirectErrorStream(true).start();
    String output = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    require(run.waitFor() == 0, "entity persistence smoke failed: " + summarize(output));
    return new Outcome(line(output, "WORLDLINE_B173_ENTITY_PERSISTENCE_SET="),
        line(output, "WORLDLINE_B173_ENTITY_PERSISTENCE_TRACE="),
        line(output, "WORLDLINE_B173_ENTITY_PERSISTENCE_SIGNATURE="));
  }

  private List<Path> runtime(Path headless) throws Exception {
    List<Path> paths = new ArrayList<>(Arrays.asList(product("api"), product("trace"),
        product("mods"), product("analysis"), product("modtest"), product("minimization"),
        product("testmodel"), product("testapi"), product("testkit"), headless,
        root.resolve("local/workspaces/b1.7.3/minecraft/bin"),
        root.resolve("local/workspaces/b1.7.3/jars/minecraft.jar")));
    for (Path path : paths) require(Files.exists(path), "missing runtime input " + path);
    return paths;
  }

  private Path product(String module) { return SmokeSupport.product(root, module); }

  private void run(List<String> command, String label) throws Exception {
    Process process = new ProcessBuilder(command).directory(root.toFile())
        .redirectErrorStream(true).start();
    String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    require(process.waitFor() == 0, "entity persistence " + label + " failed: "
        + summarize(output));
  }

  private static String join(List<Path> paths) {
    StringBuilder value = new StringBuilder();
    for (Path path : paths) {
      if (value.length() > 0) value.append(System.getProperty("path.separator"));
      value.append(path);
    }
    return value.toString();
  }

  private static String line(String output, String prefix) {
    String found = null;
    for (String value : output.split("\\R")) if (value.startsWith(prefix)) {
      require(found == null, "duplicate entity persistence smoke output " + prefix);
      found = value.substring(prefix.length());
    }
    require(found != null, "missing entity persistence smoke output " + prefix);
    return found;
  }

  private static String summarize(String value) {
    String normalized = value.replace('\r', ' ').replace('\n', ' ').trim();
    return normalized.length() <= 500 ? normalized : normalized.substring(0, 500) + "...";
  }

  private static void require(boolean value, String message) {
    if (!value) throw new IllegalStateException(message);
  }

  private record Outcome(String signal, String trace, String signature) { }
}
