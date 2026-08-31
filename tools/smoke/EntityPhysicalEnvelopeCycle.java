import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;

/** Qualifies the concrete Beta 1.7.3 EntityList physical-envelope matrix twice. */
public final class EntityPhysicalEnvelopeCycle {
  private static final String ID = "b173-entity-physical-envelope-cycle";
  private final Path root = Path.of("").toAbsolutePath().normalize();
  private final Path build = root.resolve(".worldline/smokes").resolve(ID);

  public static void main(String[] arguments) {
    if (!Arrays.equals(arguments, new String[] {ID})) {
      System.err.println("usage: java tools/smoke/EntityPhysicalEnvelopeCycle.java " + ID);
      System.exit(2);
    }
    try { new EntityPhysicalEnvelopeCycle().execute(); }
    catch (Exception error) {
      System.err.println("entity physical envelope cycle failed: " + error.getMessage());
      System.exit(1);
    }
  }

  private void execute() throws Exception {
    SmokeSupport.recreate(root, build);
    capture(build.resolve("first"));
    capture(build.resolve("second"));
    Path first = build.resolve("first/entity-physical-envelope.wlevidence");
    Path second = build.resolve("second/entity-physical-envelope.wlevidence");
    byte[] evidence = Files.readAllBytes(first);
    require(Arrays.equals(evidence, Files.readAllBytes(second)),
        "fresh entity physical envelope captures diverged");
    String canonical = new String(evidence, StandardCharsets.UTF_8);
    Outcome outcome = smoke(first);
    String signal = outcome.signal, trace = outcome.trace, signature = outcome.signature;
    Properties descriptor = new Properties();
    try (java.io.Reader reader = Files.newBufferedReader(
            root.resolve("smokes").resolve(ID).resolve("smoke.properties"))) {
      descriptor.load(reader);
    }
    String expected = descriptor.getProperty("expected.signature");
    if (!"pending".equals(expected)) {
      require(signal.equals(descriptor.getProperty("expected.signal")), "frozen signal drifted");
      require(trace.equals(descriptor.getProperty("expected.trace")), "frozen trace drifted");
      require(signature.equals(expected), "frozen signature drifted");
    }
    Files.writeString(build.resolve("evidence.txt"), canonical, StandardCharsets.UTF_8);
    System.out.println("WORLDLINE_B173_ENTITY_PHYSICAL_SET=" + signal);
    System.out.println("WORLDLINE_B173_ENTITY_PHYSICAL_TRACE=" + trace);
    System.out.println("WORLDLINE_B173_ENTITY_PHYSICAL_SIGNATURE=" + signature);
    System.out.println("b173 entity physical envelope cycle "
        + ("pending".equals(expected) ? "diagnostic passed" : "passed"));
  }

  private void capture(Path output) throws Exception {
    SmokeSupport.recreate(root, output);
    Process process = new ProcessBuilder("java", "tools/replay/Replay.java", "census",
            output.toString()).directory(root.toFile()).redirectErrorStream(true).start();
    String text = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    require(process.waitFor() == 0 && text.contains("WORLDLINE_CENSUS=PASS")
            && text.contains("entity-physical-envelope.sha256="),
        "entity physical envelope capture failed: " + summarize(text));
  }

  private Outcome smoke(Path evidence) throws Exception {
    Path classes = build.resolve("smoke-classes");
    Files.createDirectories(classes);
    Path source = root.resolve("smokes").resolve(ID).resolve(
        "src/worldline/smoke/b173entityphysical/B173EntityPhysicalEnvelopeSmoke.java");
    Process compile = new ProcessBuilder("javac", "--release", "8", "-encoding", "UTF-8",
        "-Xlint:all,-options", "-Werror", "-d", classes.toString(), source.toString())
        .directory(root.toFile()).redirectErrorStream(true).start();
    String compileText = new String(compile.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    require(compile.waitFor() == 0, "entity physical smoke compilation failed: "
        + summarize(compileText));
    Process run = new ProcessBuilder("java", "-classpath", classes.toString(),
        "worldline.smoke.b173entityphysical.B173EntityPhysicalEnvelopeSmoke",
        evidence.toString()).directory(root.toFile()).redirectErrorStream(true).start();
    String output = new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    require(run.waitFor() == 0, "entity physical smoke failed: " + summarize(output));
    return new Outcome(line(output, "WORLDLINE_B173_ENTITY_PHYSICAL_SET="),
        line(output, "WORLDLINE_B173_ENTITY_PHYSICAL_TRACE="),
        line(output, "WORLDLINE_B173_ENTITY_PHYSICAL_SIGNATURE="));
  }

  private static String line(String output, String prefix) {
    String found = null;
    for (String value : output.split("\\R")) if (value.startsWith(prefix)) {
      require(found == null, "duplicate entity physical smoke output " + prefix);
      found = value.substring(prefix.length());
    }
    require(found != null, "missing entity physical smoke output " + prefix);
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
