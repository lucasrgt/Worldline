import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Qualifies per-tick wall-clock profiling through the public launcher:
 * structural report invariants, a deliberately tight budget that must fail,
 * and a generous budget that must pass. Timing values are machine-relative
 * and never frozen; only structural facts are.
 */
public final class ProfileCycle {
  private static final String ID = "m17-profile";
  private static final String SEED = "4242";
  private final Path root = Paths.get("").toAbsolutePath().normalize();
  private final Path build = root.resolve(".worldline/smokes").resolve(ID);

  public static void main(String[] arguments) {
    if (!Arrays.equals(arguments, new String[] {ID})) {
      System.err.println("usage: java tools/smoke/ProfileCycle.java " + ID);
      System.exit(2);
    }
    try {
      new ProfileCycle().execute();
    } catch (Exception error) {
      System.err.println("M17 profile cycle failed: " + error.getMessage());
      System.exit(1);
    }
  }

  private void execute() throws Exception {
    recreate();
    Path scenario = build.resolve("load.wlscenario");
    Result created = launcher(
        "scenario", "create", scenario.toString(), "observe:before", "tick:8", "observe:after");
    require(
        created.code == 0 && created.text.contains("steps=3"), "profile scenario creation failed");
    Result free = launcher("profile", scenario.toString(), SEED);
    require(free.code == 0 && free.text.contains("WORLDLINE_PROFILE=PASS"),
        "unbudgeted profiling failed");
    require(lineOf(free.text, "ticks=").equals("8"), "sample count drifted");
    require(orderingHolds(free.text), "aggregate ordering violated");
    Path tight = build.resolve("tight.properties");
    Files.write(
        tight, "tick.mean.nanos.max=1\ntick.p95.nanos.max=1\n".getBytes(StandardCharsets.UTF_8));
    Result exceeded = launcher("profile", scenario.toString(), SEED, tight.toString());
    require(exceeded.code == 3 && exceeded.text.contains("WORLDLINE_PROFILE_BUDGET=EXCEEDED")
            && exceeded.text.contains("violation=tick.mean.nanos=")
            && exceeded.text.contains("violation=tick.p95.nanos="),
        "tight budget was not enforced");
    Path generous = build.resolve("generous.properties");
    Files.write(generous,
        ("tick.total.nanos.max=999999999999\n"
            + "mod.share.percent.max=100\n")
            .getBytes(StandardCharsets.UTF_8));
    Result passing = launcher("profile", scenario.toString(), SEED, generous.toString());
    require(passing.code == 0 && passing.text.contains("WORLDLINE_PROFILE_BUDGET=PASS"),
        "generous budget failed");
    require(lineOf(passing.text, "trace.sha256=").equals(lineOf(free.text, "trace.sha256=")),
        "behavioral trace changed between profiled runs");
    String report = "scenario.steps=3\nticks=8\nfree.exit=0\nordering.holds=true"
        + "\ntight.violations=2\ngenerous.passed=true\ntraces.match=true\n";
    String signature = sha256(report);
    Properties expected = new Properties();
    try (java.io.Reader reader = Files.newBufferedReader(
             root.resolve("smokes").resolve(ID).resolve("smoke.properties"))) {
      expected.load(reader);
    }
    require(signature.equals(expected.getProperty("expected.signature")),
        "M17 profile evidence diverged: " + signature);
    Files.write(build.resolve("evidence.txt"), report.getBytes(StandardCharsets.UTF_8));
    System.out.println("M17 profile cycle passed");
    System.out.println("  samples: 8 controlled ticks; aggregates ordered; traces stable");
    System.out.println("  budget gate: tight EXCEEDED (exit 3), generous PASS");
    System.out.println("  evidence SHA-256: " + signature);
  }

  private boolean orderingHolds(String text) {
    long mean = Long.parseLong(lineOf(text, "tick.mean.nanos="));
    long median = Long.parseLong(lineOf(text, "tick.median.nanos="));
    long p95 = Long.parseLong(lineOf(text, "tick.p95.nanos="));
    long share = Long.parseLong(lineOf(text, "mod.share.percent="));
    return mean > 0L && median >= 0L && median <= p95 && share >= 0L && share <= 100L;
  }

  private Result launcher(String... arguments) throws Exception {
    List<String> command =
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
