import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Properties;

/**
 * Qualifies differential fuzzing end to end: two mod versions are fuzzed
 * through the public launcher until the first divergence, which must arrive
 * pre-minimized and DSL-valid; vanilla-only campaigns must come back clean.
 */
public final class FuzzCycle {
  private static final String ID = "m15-fuzz";
  private static final String SEED = "17320110707";
  private final Path root = Paths.get("").toAbsolutePath().normalize();
  private final Path build = root.resolve(".worldline/smokes").resolve(ID);
  private final Path m8 = root.resolve(".worldline/smokes/m8-mod-version-diff");

  public static void main(String[] arguments) {
    if (!Arrays.equals(arguments, new String[] {ID})) {
      System.err.println("usage: java tools/smoke/FuzzCycle.java " + ID);
      System.exit(2);
    }
    try {
      new FuzzCycle().execute();
    } catch (Exception error) {
      System.err.println("M15 fuzz cycle failed: " + error.getMessage());
      System.exit(1);
    }
  }

  private void execute() throws Exception {
    recreate();
    Path v1 = m8.resolve("v1-a.jar"), v2 = m8.resolve("v2-a.jar");
    require(
        Files.isRegularFile(v1) && Files.isRegularFile(v2), "run VersionCycle before FuzzCycle");
    Result differential = launcher("fuzz", build.resolve("differential").toString(), SEED, "24",
        "6", v1.toString(), v2.toString());
    require(differential.code == 3 && differential.text.contains("WORLDLINE_FUZZ=FINDINGS"),
        "differential campaign did not report findings");
    require(differential.text.contains("findings=1") && differential.text.contains("subjects=mod:"),
        "unexpected differential campaign shape");
    Path finding = build.resolve("differential").resolve("finding-0.wlscenario");
    require(Files.isRegularFile(finding), "finding scenario was not written");
    Result validated = launcher("scenario", "validate", finding.toString());
    require(validated.code == 0 && validated.text.contains("WORLDLINE_SCENARIO_VALIDATE=PASS"),
        "finding scenario is not DSL-valid");
    int minimizedSteps = Integer.parseInt(lineOf(validated.text, "steps="));
    Result vanilla = launcher("fuzz", build.resolve("vanilla").toString(), SEED, "12", "5");
    require(vanilla.code == 0 && vanilla.text.contains("WORLDLINE_FUZZ=CLEAN")
            && vanilla.text.contains("subjects=vanilla"),
        "vanilla campaign was not clean");
    String reportHash = lineOf(differential.text, "report.sha256=");
    String report = "seed=" + SEED + "\ncases=24\nmax-steps=6\nstop-on-first=true"
        + "\nminimized.steps=" + minimizedSteps
        + "\nvanilla.cases=12\nvanilla.clean=true\nreport.sha256=" + reportHash + "\n";
    String signature = sha256(report);
    Properties expected = new Properties();
    try (java.io.Reader reader = Files.newBufferedReader(
             root.resolve("smokes").resolve(ID).resolve("smoke.properties"))) {
      expected.load(reader);
    }
    require(signature.equals(expected.getProperty("expected.signature")),
        "M15 fuzz evidence diverged: " + signature);
    Files.write(build.resolve("evidence.txt"), report.getBytes(StandardCharsets.UTF_8));
    System.out.println("M15 fuzz cycle passed");
    System.out.println("  differential: first divergence found and auto-minimized to "
        + minimizedSteps + " steps");
    System.out.println("  vanilla: clean under the same seed budget");
    System.out.println("  evidence SHA-256: " + signature);
  }

  private Result launcher(String... arguments) throws Exception {
    java.util.List<String> command =
        new java.util.ArrayList<>(Arrays.asList("java", "tools/replay/Replay.java"));
    command.addAll(Arrays.asList(arguments));
    Process process =
        new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
    String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    return new Result(process.waitFor(), output.replace('\r', '\n'));
  }

  private String lineOf(String text, String prefix) {
    for (String row : text.split("\n", -1)) {
      if (row.startsWith(prefix))
        return row.substring(prefix.length());
    }
    throw new IllegalStateException("missing " + prefix);
  }

  private void recreate() throws Exception {
    if (Files.exists(build)) {
      try (java.util.stream.Stream<Path> paths = Files.walk(build)) {
        for (Path path : paths.sorted(java.util.Comparator.reverseOrder())
                 .collect(java.util.stream.Collectors.toList()))
          Files.delete(path);
      }
    }
    Files.createDirectories(build);
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
