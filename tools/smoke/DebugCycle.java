import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Qualifies deterministic time-travel debugging through the public launcher:
 * scripted forward steps, reverse jumps, goto, and field watchpoints over one
 * public-grammar scenario.
 */
public final class DebugCycle {
  private static final String ID = "m16-debug";
  private static final String SEED = "4242";
  private static final String[] SCRIPT = {"scenario", "watch block65", "step", "observe", "step 2",
      "back 2", "goto 4", "observe", "goto 99", "observe", "bogus", "unwatch", "quit"};
  private final Path root = Paths.get("").toAbsolutePath().normalize();
  private final Path build = root.resolve(".worldline/smokes").resolve(ID);

  public static void main(String[] arguments) {
    if (!Arrays.equals(arguments, new String[] {ID})) {
      System.err.println("usage: java tools/smoke/DebugCycle.java " + ID);
      System.exit(2);
    }
    try {
      new DebugCycle().execute();
    } catch (Exception error) {
      System.err.println("M16 debug cycle failed: " + error.getMessage());
      System.exit(1);
    }
  }

  private void execute() throws Exception {
    recreate();
    Path scenario = build.resolve("timeline.wlscenario");
    Result created = launcher("scenario", "create", scenario.toString(), "observe:before",
        "block:8,65,8:20", "tick", "tick", "observe:after");
    require(
        created.code == 0 && created.text.contains("steps=5"), "debug scenario creation failed");
    String transcript = debug(scenario);
    for (String expected : new String[] {"WORLDLINE_DEBUG_TRIGGER=block65:none->0@before",
             "WORLDLINE_DEBUG_OBSERVE=before tick=0 block65=0",
             "WORLDLINE_DEBUG_UNCHANGED=block65=0", "WORLDLINE_DEBUG_TRIGGER=block65:0->20@after",
             "WORLDLINE_DEBUG_OBSERVE=after tick=2 block65=20",
             "WORLDLINE_DEBUG_ERROR=unknown command bogus", "WORLDLINE_DEBUG_WATCH=off"}) {
      require(transcript.contains(expected), "missing transcript line: " + expected);
    }
    require(transcript.contains("WORLDLINE_DEBUG_CMD=back 2")
            && transcript.contains("WORLDLINE_DEBUG_CMD=goto 99"),
        "script echo lost");
    require(count(transcript, "WORLDLINE_DEBUG_CMD=") == SCRIPT.length,
        "scripted command count drifted");
    String report = "seed=" + SEED + "\ncommands=" + SCRIPT.length
        + "\nreverse.deterministic=true\ntranscript.sha256=" + sha256(transcript) + "\n";
    String signature = sha256(report);
    Properties expected = new Properties();
    try (java.io.Reader reader = Files.newBufferedReader(
             root.resolve("smokes").resolve(ID).resolve("smoke.properties"))) {
      expected.load(reader);
    }
    require(signature.equals(expected.getProperty("expected.signature")),
        "M16 debug evidence diverged: " + signature);
    Files.write(build.resolve("evidence.txt"), report.getBytes(StandardCharsets.UTF_8));
    System.out.println("M16 debug cycle passed");
    System.out.println("  scripted time travel: step/back/goto/watchpoints verified");
    System.out.println("  transcript SHA-256: " + sha256(transcript));
    System.out.println("  evidence SHA-256: " + signature);
  }

  /** Pipes the script into one debug session and returns its DEBUG lines. */
  private String debug(Path scenario) throws Exception {
    List<String> command = new ArrayList<>(
        Arrays.asList("java", "tools/replay/Replay.java", "debug", scenario.toString(), SEED));
    Process process =
        new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
    Files.write(build.resolve("script.txt"),
        (String.join("\n", SCRIPT) + "\n").getBytes(StandardCharsets.UTF_8));
    process.getOutputStream().write(
        (String.join("\n", SCRIPT) + "\n").getBytes(StandardCharsets.UTF_8));
    process.getOutputStream().flush();
    process.getOutputStream().close();
    String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    int exit = process.waitFor();
    require(exit == 0, "debug session exited " + exit);
    StringBuilder filtered = new StringBuilder();
    for (String row : output.replace('\r', '\n').split("\n", -1)) {
      if (row.startsWith("WORLDLINE_DEBUG_"))
        filtered.append(row).append('\n');
    }
    return filtered.toString();
  }

  private int count(String text, String marker) {
    int total = 0;
    for (String row : text.split("\n", -1)) {
      if (row.startsWith(marker))
        total++;
    }
    return total;
  }

  private Result launcher(String... arguments) throws Exception {
    List<String> command = new ArrayList<>(Arrays.asList("java", "tools/replay/Replay.java"));
    command.addAll(Arrays.asList(arguments));
    Process process =
        new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
    String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    return new Result(process.waitFor(), output.replace('\r', '\n'));
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
