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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Runs the M13 Aero persistence, scene differential, and budget experiment. */
public final class AeroDifferentialCycle {
  private static final String ID = "m13-aero-differential";
  private static final Pattern READY =
      Pattern.compile("ready blockEntities=(\\d+) entityBlocks=(\\d+)");
  private final Path root = Paths.get("").toAbsolutePath().normalize();
  private final Properties config = new Properties();

  public static void main(String[] arguments) {
    if (!Arrays.equals(arguments, new String[] {ID})) {
      System.err.println("usage: java tools/smoke/AeroDifferentialCycle.java " + ID);
      System.exit(2);
    }
    try {
      new AeroDifferentialCycle().execute();
    } catch (Exception error) {
      System.err.println("Aero differential cycle failed: " + error.getMessage());
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
    Capture dense = capture(test, build, "dense", value("capture.ticks"));
    require(Files.isRegularFile(save.resolve("level.dat")), "fresh Aero save missing");
    Capture reload = capture(test, build, "reload", "1");
    delete(save);
    Capture empty = capture(test, build, "empty", value("capture.ticks"));
    delete(save);
    Capture budget = capture(test, build, "budget", value("capture.ticks"));
    Path classes = build.resolve("classes");
    Files.createDirectories(classes);
    run(root,
        command("javac", "-encoding", "UTF-8", "--release", "8", "-Xlint:all,-options", "-Werror",
            "-d", classes.toString(),
            root.resolve(
                    "adapters/aero-model-lib/src/main/java/worldline/aero/AeroDiagnostics.java")
                .toString(),
            smoke.resolve("src/worldline/smoke/m13/AeroDifferentialSmoke.java").toString()));
    String output = output(root,
        command("java", "-classpath", classes.toString(),
            "worldline.smoke.m13.AeroDifferentialSmoke", dense.log.toString(), empty.log.toString(),
            budget.log.toString(), Long.toString(dense.global), Long.toString(dense.blocks),
            Long.toString(reload.global), Long.toString(reload.blocks), value("analysis.frames")));
    require(output.contains("WORLDLINE_M13_DIFFERENTIAL=PASS"), "M13 proof absent");
    require(line(output, "evidence.sha256=").equals(value("expected.signature")),
        "M13 signature drift");
    String evidence = "id=" + ID + "\naero.revision=" + value("aero.revision")
        + "\nseed=" + value("seed") + "\n" + output;
    Files.write(build.resolve("evidence.txt"), evidence.getBytes(StandardCharsets.UTF_8));
    System.out.println("Aero differential cycle passed");
    System.out.print(output);
    System.out.println("  evidence: " + root.relativize(build.resolve("evidence.txt")));
  }

  private Capture capture(Path test, Path build, String mode, String ticks) throws Exception {
    Path raw = build.resolve(mode + "-raw.log"), measured = build.resolve(mode + ".log");
    Files.deleteIfExists(raw);
    Files.deleteIfExists(measured);
    String wrapper =
        System.getProperty("os.name").startsWith("Windows") ? "gradlew.bat" : "gradlew";
    String text = output(test,
        command(test.resolve(wrapper).toString(), "--no-daemon", "--init-script",
            root.resolve(value("runner")).toString(), "runClient", "-PworldlineTicks=" + ticks,
            "-PworldlineWarmupTicks=" + value("warmup.ticks"), "-PworldlineSpikeMs=1",
            "-PworldlineMode=" + mode, "-PworldlineLog=" + raw));
    require(text.contains("[WorldlineCapture] complete") && text.contains("BUILD SUCCESSFUL"),
        "Aero " + mode + " capture failed\n" + text);
    List<String> frames = new ArrayList<>();
    long global = -1, blocks = -1;
    boolean recording = false;
    for (String row : text.split("\\R")) {
      int marker = row.indexOf("[WorldlineCapture] ready");
      if (marker >= 0) {
        Matcher match = READY.matcher(row.substring(marker));
        require(match.find(), "invalid readiness marker");
        global = Long.parseLong(match.group(1));
        blocks = Long.parseLong(match.group(2));
        recording = true;
        continue;
      }
      if (row.contains("[WorldlineCapture] complete"))
        break;
      if (recording && row.startsWith("[Aero_"))
        frames.add(row);
    }
    require(global >= 0 && blocks >= 0, "missing " + mode + " readiness marker");
    Files.write(measured, frames, StandardCharsets.UTF_8);
    if (!mode.equals("reload"))
      require(frames.size() >= Integer.parseInt(value("analysis.frames")),
          "too few measured " + mode + " frames: " + frames.size());
    return new Capture(measured, global, blocks);
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
    final Path log;
    final long global, blocks;
    Capture(Path log, long global, long blocks) {
      this.log = log;
      this.global = global;
      this.blocks = blocks;
    }
  }
}
