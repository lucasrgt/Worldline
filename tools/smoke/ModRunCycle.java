import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Qualifies the one-command attested mod test run: inspection, execution,
 * determinism, comparison, and corruption rejection through the public CLI.
 */
public final class ModRunCycle {
  private static final String ID = "m12-mod-run";
  private static final String SEED = "17320110707";
  private final Path root = Paths.get("").toAbsolutePath().normalize();
  private final Path build = root.resolve(".worldline/smokes").resolve(ID);
  private final Path client = root.resolve(".worldline/smokes/controlled-client-tick");

  public static void main(String[] arguments) {
    if (!Arrays.equals(arguments, new String[] {ID})) {
      System.err.println("usage: java tools/smoke/ModRunCycle.java " + ID);
      System.exit(2);
    }
    try {
      new ModRunCycle().execute();
    } catch (Exception error) {
      System.err.println("M12 mod run cycle failed: " + error.getMessage());
      System.exit(1);
    }
  }

  private void execute() throws Exception {
    recreate(build);
    Path adapter = client.resolve("adapter-classes");
    require(Files.isDirectory(adapter), "run ClientCycle before ModRunCycle");
    Path modClasses = compile(root.resolve("smokes").resolve(ID).resolve("mod-src"),
        build.resolve("mod-classes"), Arrays.asList(adapter, product("api")));
    Path jar = build.resolve("run-probe.jar");
    run(Arrays.asList("jar", "--create", "--file", jar.toString(), "-C", modClasses.toString(), ".",
        "-C", root.resolve("smokes").resolve(ID).resolve("descriptors").resolve("probe").toString(),
        "META-INF/worldline-mod.properties"));
    Result inspected = launcher("mod", "inspect", jar.toString());
    require(inspected.code == 0 && inspected.text.contains("compatibility=COMPATIBLE")
            && inspected.text.contains("id=worldline.run-probe")
            && inspected.text.contains("requires="),
        "format 2 inspection failed");
    Path first = build.resolve("first.wlmtest"), second = build.resolve("second.wlmtest");
    Result firstRun = launcher("mod", "test", "run", jar.toString(), SEED, "6", first.toString());
    require(firstRun.code == 0 && firstRun.text.contains("WORLDLINE_MOD_TEST_RUN=PASS")
            && firstRun.text.contains("execution=controlled-runtime")
            && firstRun.text.contains("seed=" + SEED) && firstRun.text.contains("ticks=6"),
        "attested mod test run failed");
    Result secondRun = launcher("mod", "test", "run", jar.toString(), SEED, "6", second.toString());
    require(
        secondRun.code == 0 && Arrays.equals(Files.readAllBytes(first), Files.readAllBytes(second)),
        "attested run is not deterministic");
    Result equal = launcher("mod", "test", "diff", first.toString(), second.toString());
    require(equal.code == 0 && equal.text.contains("WORLDLINE_MOD_TEST_DIFF=EQUAL"),
        "equal executed results diverged");
    byte[] original = Files.readAllBytes(first);
    byte[] corrupt = original.clone();
    corrupt[corrupt.length - 3] ^= 1;
    Path broken = build.resolve("broken.wlmtest");
    Files.write(broken, corrupt);
    Result rejected = launcher("mod", "test", "diff", broken.toString(), second.toString());
    require(rejected.code == 1, "corrupt result was accepted");
    String resultHash = line(firstRun.text, "result.sha256=");
    String traceHash = line(firstRun.text, "trace.sha256=");
    require(resultHash.matches("[0-9a-f]{64}"), "missing executed result digest");
    String report = "mod=worldline.run-probe@1.0.0\nseed=" + SEED + "\nticks=6"
        + "\nexecution=controlled-runtime\nrun.deterministic=true\ndiff=EQUAL\n"
        + "corrupt.rejected=true\ntrace.sha256=" + traceHash + "\n";
    String signature = sha256(report);
    Properties expected = new Properties();
    try (java.io.Reader reader = Files.newBufferedReader(
             root.resolve("smokes").resolve(ID).resolve("smoke.properties"))) {
      expected.load(reader);
    }
    require(signature.equals(expected.getProperty("expected.signature")),
        "M12 run evidence diverged: " + signature);
    Files.write(build.resolve("evidence.txt"), report.getBytes(StandardCharsets.UTF_8));
    System.out.println("M12 mod test run cycle passed");
    System.out.println("  attested: execution=controlled-runtime seed=" + SEED + " ticks=6");
    System.out.println("  deterministic across processes; diff EQUAL; corrupt rejected");
    System.out.println("  evidence SHA-256: " + signature);
  }

  private Result launcher(String... arguments) throws Exception {
    List<String> command = new ArrayList<>(Arrays.asList("java", "tools/replay/Replay.java"));
    command.addAll(Arrays.asList(arguments));
    Process process =
        new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
    String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    return new Result(process.waitFor(), output);
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

  private void run(List<String> command) throws Exception {
    Process process =
        new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
    String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    int code = process.waitFor();
    if (code != 0)
      throw new IllegalStateException(command.get(0) + " failed\n" + output);
  }

  private void recreate(Path target) throws Exception {
    if (Files.exists(target)) {
      try (Stream<Path> paths = Files.walk(target)) {
        for (Path path :
            paths.sorted(java.util.Comparator.reverseOrder()).collect(Collectors.toList()))
          Files.delete(path);
      }
    }
    Files.createDirectories(target);
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
