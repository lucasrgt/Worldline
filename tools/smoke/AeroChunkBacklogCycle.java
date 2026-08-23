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

/** Runs the M14 vanilla backlog and bounded non-retry experiment. */
public final class AeroChunkBacklogCycle {
  private static final String ID = "m14-chunk-backlog";
  private final Path root = Paths.get("").toAbsolutePath().normalize();
  private final Properties config = new Properties();

  public static void main(String[] arguments) {
    if (!Arrays.equals(arguments, new String[] {ID})) {
      System.err.println("usage: java tools/smoke/AeroChunkBacklogCycle.java " + ID);
      System.exit(2);
    }
    try {
      new AeroChunkBacklogCycle().execute();
    } catch (Exception error) {
      System.err.println("Aero chunk backlog cycle failed: " + error.getMessage());
      System.exit(1);
    }
  }

  private void execute() throws Exception {
    Path smoke = root.resolve("smokes").resolve(ID);
    load(smoke.resolve("smoke.properties"));
    require(ID.equals(value("id")), "smoke descriptor id mismatch");
    Path checkout = root.resolve(value("aero.path")).normalize();
    verifyCheckout(checkout);
    Path test = checkout.resolve("stationapi/test"), run = test.resolve("run").normalize();
    Path save = run.resolve("saves/WorldlineAero").normalize();
    require(save.startsWith(run) && !save.equals(run), "unsafe Aero save path");
    Path build = root.resolve(".worldline/smokes").resolve(ID).normalize();
    recreate(build);
    delete(save);
    Capture baseline = capture(test, build, "probe");
    delete(save);
    Capture bounded = capture(test, build, "bounded");
    delete(save);
    Path classes = build.resolve("classes");
    Files.createDirectories(classes);
    run(root,
        command("javac", "-encoding", "UTF-8", "--release", "8", "-Xlint:all,-options", "-Werror",
            "-d", classes.toString(),
            root.resolve(
                    "adapters/aero-model-lib/src/main/java/worldline/aero/AeroDiagnostics.java")
                .toString(),
            root.resolve("adapters/aero-model-lib/src/main/java/worldline/aero/AeroChunkProbe.java")
                .toString(),
            smoke.resolve("src/worldline/smoke/m14/ChunkBacklogSmoke.java").toString()));
    String output = output(root,
        command("java", "-classpath", classes.toString(), "worldline.smoke.m14.ChunkBacklogSmoke",
            baseline.probe.toString(), bounded.probe.toString(), baseline.aero.toString(),
            bounded.aero.toString(), value("analysis.frames")));
    require(output.contains("WORLDLINE_M14_CHUNK_BACKLOG=PASS"), "M14 proof absent");
    require(line(output, "evidence.sha256=").equals(value("expected.signature")),
        "M14 signature drift");
    String evidence = "id=" + ID + "\naero.revision=" + value("aero.revision")
        + "\nseed=" + value("seed") + "\n" + output;
    Files.write(build.resolve("evidence.txt"), evidence.getBytes(StandardCharsets.UTF_8));
    System.out.println("Aero chunk backlog cycle passed");
    System.out.print(output);
    System.out.println("  evidence: " + root.relativize(build.resolve("evidence.txt")));
  }

  private Capture capture(Path test, Path build, String mode) throws Exception {
    Path raw = build.resolve(mode + "-raw.log");
    Path probe = build.resolve(mode + "-probe.log"), aero = build.resolve(mode + "-aero.log");
    Files.deleteIfExists(raw);
    Files.deleteIfExists(probe);
    Files.deleteIfExists(aero);
    String wrapper =
        System.getProperty("os.name").startsWith("Windows") ? "gradlew.bat" : "gradlew";
    String text = output(test,
        command(test.resolve(wrapper).toString(), "--no-daemon", "--init-script",
            root.resolve(value("runner")).toString(), "runClient",
            "-PworldlineTicks=" + value("capture.ticks"),
            "-PworldlineWarmupTicks=" + value("warmup.ticks"), "-PworldlineSpikeMs=1",
            "-PworldlineMode=" + mode, "-PworldlineLog=" + raw));
    require(text.contains("[WorldlineCapture] complete") && text.contains("BUILD SUCCESSFUL"),
        "Aero " + mode + " capture failed\n" + text);
    List<String> probes = new ArrayList<>(), frames = new ArrayList<>();
    boolean recording = false;
    for (String row : text.split("\\R")) {
      if (row.contains("[WorldlineCapture] ready")) {
        recording = true;
        continue;
      }
      if (row.contains("[WorldlineCapture] complete"))
        break;
      if (!recording)
        continue;
      if (row.startsWith("[WorldlineChunkProbe]"))
        probes.add(row);
      if (row.startsWith("[Aero_"))
        frames.add(row);
    }
    int minimum = Integer.parseInt(value("analysis.frames"));
    require(probes.size() >= minimum, "too few " + mode + " probe frames: " + probes.size());
    require(frames.size() >= minimum, "too few " + mode + " Aero frames: " + frames.size());
    Files.write(probe, probes, StandardCharsets.UTF_8);
    Files.write(aero, frames, StandardCharsets.UTF_8);
    return new Capture(probe, aero);
  }

  private void verifyCheckout(Path checkout) throws Exception {
    require(Files.isDirectory(checkout.resolve(".git")), "Aero checkout missing");
    require(output(root, command("git", "-C", checkout.toString(), "remote", "get-url", "origin"))
                .trim()
                .equals(value("aero.repository")),
        "unexpected Aero origin");
    require(output(root, command("git", "-C", checkout.toString(), "rev-parse", "HEAD"))
                .trim()
                .equals(value("aero.revision")),
        "unexpected Aero revision");
    require(output(root, command("git", "-C", checkout.toString(), "status", "--porcelain"))
                .trim()
                .isEmpty(),
        "Aero checkout has tracked changes");
  }

  private void recreate(Path path) throws IOException {
    delete(path);
    Files.createDirectories(path);
  }
  private void delete(Path path) throws IOException {
    if (!Files.exists(path))
      return;
    require(path.startsWith(root) && !path.equals(root), "unsafe delete path " + path);
    try (Stream<Path> paths = Files.walk(path)) {
      for (Path item : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList()))
        Files.delete(item);
    }
  }
  private void load(Path path) throws IOException {
    try (java.io.Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      config.load(reader);
    }
  }
  private String value(String key) {
    String result = config.getProperty(key);
    require(result != null && !result.trim().isEmpty(), "missing smoke property " + key);
    return result.trim();
  }
  private String line(String text, String prefix) {
    return Arrays.stream(text.split("\\R"))
        .filter(row -> row.startsWith(prefix))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("missing " + prefix))
        .substring(prefix.length());
  }
  private List<String> command(String... values) {
    return new ArrayList<>(Arrays.asList(values));
  }
  private void run(Path directory, List<String> command) throws Exception {
    output(directory, command);
  }
  private String output(Path directory, List<String> command) throws Exception {
    Process process =
        new ProcessBuilder(command).directory(directory.toFile()).redirectErrorStream(true).start();
    String text = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    if (process.waitFor() != 0)
      throw new IllegalStateException(command.get(0) + " failed\n" + text);
    return text;
  }
  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
  private static final class Capture {
    final Path probe, aero;
    Capture(Path probe, Path aero) {
      this.probe = probe;
      this.aero = aero;
    }
  }
}
