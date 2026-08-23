import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/** Executes one declarative official-server cycle with a composite observation. */
public final class CompositeCycle {
  private final Path root = Path.of("").toAbsolutePath().normalize();
  private final String id;
  private final Path smoke, build;
  private final Properties config = new Properties(), artifact = new Properties();
  private CompositeCyclePlan plan;

  private CompositeCycle(String id) {
    this.id = id;
    smoke = root.resolve("smokes").resolve(id);
    build = root.resolve(".worldline/smokes").resolve(id);
  }
  public static void main(String[] arguments) {
    if (arguments.length != 1) {
      System.err.println("usage: CompositeCycle ID");
      System.exit(2);
    }
    try {
      new CompositeCycle(arguments[0]).execute();
    } catch (Exception error) {
      System.err.println(arguments[0] + " composite cycle failed: " + error.getMessage());
      System.exit(1);
    }
  }
  private void execute() throws Exception {
    plan = CompositeCyclePlan.load(root, id);
    DataDrivenSupport.load(smoke.resolve("smoke.properties"), config);
    DataDrivenSupport.load(root.resolve(plan.artifact), artifact);
    require(value("server.jar.sha256").equals(DataDrivenSupport.value(artifact, "expected.sha256")),
        "official artifact descriptor drift");
    Path official = root.resolve(DataDrivenSupport.value(artifact, "local.path")).normalize();
    DataDrivenSupport.verifyArtifact(official, artifact);
    DataDrivenSupport.recreate(root, build);
    Path classes = compile();
    Outcome first = run(classes, official, build.resolve("first"));
    Outcome second = run(classes, official, build.resolve("second"));
    require(first.trace.equals(second.trace) && first.signature.equals(second.signature),
        "fresh cycle identity diverged");
    if (plan.compareSignal)
      require(first.signal.equals(second.signal), "fresh observations diverged");
    if (plan.requireExpectedSignal)
      require(first.signal.equals(value("expected.signal")), "frozen signal drift");
    require(first.signature.equals(value("expected.signature")), "frozen signature drift");
    assertContains(first.signal, plan.signalContains, "signal");
    assertExcludes(first.signal, plan.signalExcludes, "signal");
    assertContains(first.trace, plan.traceContains, "trace");
    assertExcludes(first.trace, plan.traceExcludes, "trace");
    String evidence = "id=" + id + "\nruns=2\nfirst=" + first.signal + "\nsecond=" + second.signal
        + "\ntrace=" + first.trace + "\nsignature=" + first.signature + "\n";
    Files.writeString(build.resolve("evidence.txt"), evidence, StandardCharsets.UTF_8);
    System.out.println(id + " composite cycle passed\n  " + first.signal
        + "\n  trace: " + first.trace + "\n  signature: " + first.signature + "\nFROZEN");
  }
  private Path compile() throws Exception {
    Path output = build.resolve("adapter-classes");
    Files.createDirectories(output);
    List<String> command = new ArrayList<>(Arrays.asList(
        "javac", "-encoding", "UTF-8", "--release", "8", "-Xlint:all,-options", "-Werror"));
    String classpath = classpath(plan.compileProducts, null);
    if (!classpath.isEmpty())
      command.addAll(List.of("-classpath", classpath));
    command.addAll(List.of("-d", output.toString()));
    for (String input : plan.inputs)
      command.addAll(DataDrivenSupport.javaFiles(root.resolve(input)));
    command.addAll(DataDrivenSupport.javaFiles(smoke.resolve("src")));
    DataDrivenSupport.capture(root, command);
    return output;
  }
  private Outcome run(Path classes, Path official, Path workspace) throws Exception {
    return SmokeRetry.onceOnEof(id, attempt -> {
      DataDrivenSupport.recreate(root, workspace);
      List<String> command = new ArrayList<>(List.of("java", "-classpath",
          classpath(plan.runtimeProducts, classes), plan.mainClass, official.toString(),
          workspace.toString(), Integer.toString(DataDrivenSupport.freePort())));
      for (String key : plan.arguments)
        command.add(value(key));
      String output = DataDrivenSupport.capture(root, command);
      assertContains(output, plan.outputContains, "process output");
      List<String> signals = new ArrayList<>();
      for (String prefix : plan.signalPrefixes)
        signals.add(DataDrivenSupport.line(output, prefix));
      return new Outcome(DataDrivenSupport.line(output, plan.tracePrefix),
          DataDrivenSupport.line(output, plan.signaturePrefix), String.join(";", signals));
    });
  }
  private String classpath(List<String> products, Path classes) {
    List<String> values = new ArrayList<>();
    if (classes != null)
      values.add(classes.toString());
    for (String product : products)
      values.add(DataDrivenSupport.product(root, product).toString());
    return String.join(System.getProperty("path.separator"), values);
  }
  private String value(String key) {
    return DataDrivenSupport.value(config, key);
  }
  private static void assertContains(String value, List<String> fragments, String label) {
    for (String fragment : fragments)
      require(value.contains(fragment), label + " lacks " + fragment);
  }
  private static void assertExcludes(String value, List<String> fragments, String label) {
    for (String fragment : fragments)
      require(!value.contains(fragment), label + " unexpectedly contains " + fragment);
  }
  private static void require(boolean value, String message) {
    if (!value)
      throw new IllegalStateException(message);
  }
  private record Outcome(String trace, String signature, String signal) {}
}
