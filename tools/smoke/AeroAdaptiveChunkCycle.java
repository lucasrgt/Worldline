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

/** Runs the M16 adaptive scheduler and frozen-tick framebuffer comparison. */
public final class AeroAdaptiveChunkCycle {
    private static final String ID = "m16-adaptive-chunks";
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Properties config = new Properties();

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: java tools/smoke/AeroAdaptiveChunkCycle.java " + ID);
            System.exit(2);
        }
        try { new AeroAdaptiveChunkCycle().execute(); }
        catch (Exception error) {
            System.err.println("Aero adaptive chunk cycle failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        Path smoke = root.resolve("smokes").resolve(ID); load(smoke.resolve("smoke.properties"));
        require(ID.equals(value("id")), "smoke descriptor id mismatch");
        Path checkout = root.resolve(value("aero.path")).normalize(); verifyCheckout(checkout);
        Path test = checkout.resolve("stationapi/test"), run = test.resolve("run").normalize();
        Path save = run.resolve("saves/WorldlineAero").normalize();
        require(save.startsWith(run) && !save.equals(run), "unsafe Aero save path");
        Path build = root.resolve(".worldline/smokes").resolve(ID).normalize(); recreate(build);
        Path snapshot = build.resolve("seed-save");
        delete(save); capture(test, build, "m16baseline"); copy(save, snapshot);
        delete(save); copy(snapshot, save); Capture baseline = capture(test, build, "m16baseline");
        delete(save); copy(snapshot, save); Capture adaptive = capture(test, build, "m16adaptive");
        delete(save); delete(snapshot);
        Path classes = build.resolve("classes"); Files.createDirectories(classes);
        run(root, command("javac", "-encoding", "UTF-8", "--release", "8", "-Xlint:all,-options",
                "-Werror", "-d", classes.toString(),
                root.resolve("adapters/aero-model-lib/src/main/java/worldline/aero/AeroDiagnostics.java").toString(),
                root.resolve("adapters/aero-model-lib/src/main/java/worldline/aero/AeroChunkReadiness.java").toString(),
                root.resolve("adapters/aero-model-lib/src/main/java/worldline/aero/AeroFrameOracle.java").toString(),
                smoke.resolve("src/worldline/smoke/m16/FrameDiff.java").toString(),
                smoke.resolve("src/worldline/smoke/m16/AdaptiveChunkSmoke.java").toString()));
        String output = output(root, command("java", "-classpath", classes.toString(),
                "worldline.smoke.m16.AdaptiveChunkSmoke", baseline.probe.toString(),
                adaptive.probe.toString(), baseline.aero.toString(), adaptive.aero.toString(),
                baseline.frame.toString(), adaptive.frame.toString(), baseline.image.toString(),
                adaptive.image.toString(), value("analysis.frames")));
        require(output.contains("WORLDLINE_M16_ADAPTIVE_CHUNKS=PASS"), "M16 proof absent");
        require(line(output, "evidence.sha256=").equals(value("expected.signature")),
                "M16 signature drift");
        String evidence = "id=" + ID + "\naero.revision=" + value("aero.revision")
                + "\nseed=" + value("seed") + "\n" + output;
        Files.write(build.resolve("evidence.txt"), evidence.getBytes(StandardCharsets.UTF_8));
        System.out.println("Aero adaptive chunk cycle passed"); System.out.print(output);
        System.out.println("  evidence: " + root.relativize(build.resolve("evidence.txt")));
    }

    private Capture capture(Path test, Path build, String mode) throws Exception {
        Path raw = build.resolve(mode + "-raw.log"), probe = build.resolve(mode + "-probe.log");
        Path aero = build.resolve(mode + "-aero.log"), frame = build.resolve(mode + "-frame.log");
        Path image = build.resolve(mode + "-frame.png");
        Files.deleteIfExists(raw); Files.deleteIfExists(probe);
        Files.deleteIfExists(aero); Files.deleteIfExists(frame);
        Files.deleteIfExists(image);
        String wrapper = System.getProperty("os.name").startsWith("Windows") ? "gradlew.bat" : "gradlew";
        String text = output(test, command(test.resolve(wrapper).toString(), "--no-daemon", "--init-script",
                root.resolve(value("runner")).toString(), "runClient", "-PworldlineTicks=10000",
                "-PworldlineWarmupTicks=0", "-PworldlineSpikeMs=1", "-PworldlineMode=" + mode,
                "-PworldlineAdaptiveMax=" + value("adaptive.max"),
                "-PworldlineBudgetUs=" + value("adaptive.budget.us"),
                "-PworldlineFrameOutput=" + image, "-PworldlineLog=" + raw));
        require(text.contains("[WorldlineCapture] complete") && text.contains("BUILD SUCCESSFUL"),
                "Aero " + mode + " capture failed\n" + text);
        List<String> probes = new ArrayList<>(), frames = new ArrayList<>(), oracle = new ArrayList<>();
        boolean recording = false;
        for (String row : text.split("\\R")) {
            if (row.contains("[WorldlineCapture] ready")) { recording = true; continue; }
            if (recording && row.startsWith("[WorldlineChunkProbe]")) probes.add(row);
            if (recording && row.startsWith("[Aero_")) frames.add(row);
            int marker = row.indexOf("[WorldlineFrameOracle]");
            if (recording && marker >= 0) oracle.add(row.substring(marker));
            if (row.contains("[WorldlineCapture] complete")) break;
        }
        int minimum = Integer.parseInt(value("analysis.frames"));
        require(probes.size() >= minimum, "too few " + mode + " readiness frames: " + probes.size());
        require(frames.size() >= minimum, "too few " + mode + " Aero frames: " + frames.size());
        require(oracle.size() == 1 && !oracle.get(0).contains("timeout"),
                "invalid " + mode + " frame oracle: " + oracle);
        Files.write(probe, probes, StandardCharsets.UTF_8);
        Files.write(aero, frames, StandardCharsets.UTF_8);
        Files.write(frame, oracle, StandardCharsets.UTF_8);
        return new Capture(probe, aero, frame, image);
    }

    private void verifyCheckout(Path checkout) throws Exception {
        require(Files.isDirectory(checkout.resolve(".git")), "Aero checkout missing");
        require(output(root, command("git", "-C", checkout.toString(), "remote", "get-url", "origin"))
                .trim().equals(value("aero.repository")), "unexpected Aero origin");
        require(output(root, command("git", "-C", checkout.toString(), "rev-parse", "HEAD"))
                .trim().equals(value("aero.revision")), "unexpected Aero revision");
        require(output(root, command("git", "-C", checkout.toString(), "status", "--porcelain"))
                .trim().isEmpty(), "Aero checkout has tracked changes");
    }

    private void recreate(Path path) throws IOException { delete(path); Files.createDirectories(path); }
    private void copy(Path source, Path target) throws IOException {
        require(Files.isDirectory(source), "snapshot source missing " + source);
        require(target.startsWith(root) && !target.equals(root), "unsafe snapshot target " + target);
        try (Stream<Path> paths = Files.walk(source)) {
            for (Path item : paths.collect(Collectors.toList())) {
                Path output = target.resolve(source.relativize(item));
                if (Files.isDirectory(item)) Files.createDirectories(output);
                else Files.copy(item, output);
            }
        }
    }
    private void delete(Path path) throws IOException {
        if (!Files.exists(path)) return;
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
    private String value(String key) { String result = config.getProperty(key);
        require(result != null && !result.trim().isEmpty(), "missing smoke property " + key);
        return result.trim(); }
    private String line(String text, String prefix) { return Arrays.stream(text.split("\\R"))
        .filter(row -> row.startsWith(prefix)).findFirst().orElseThrow(
            () -> new IllegalStateException("missing " + prefix)).substring(prefix.length()); }
    private List<String> command(String... values) { return new ArrayList<>(Arrays.asList(values)); }
    private void run(Path directory, List<String> command) throws Exception { output(directory, command); }
    private String output(Path directory, List<String> command) throws Exception {
        Process process = new ProcessBuilder(command).directory(directory.toFile()).redirectErrorStream(true).start();
        String text = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (process.waitFor() != 0) throw new IllegalStateException(command.get(0) + " failed\n" + text);
        return text;
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
    private static final class Capture {
        final Path probe, aero, frame, image;
        Capture(Path probe, Path aero, Path frame, Path image) {
            this.probe = probe; this.aero = aero; this.frame = frame; this.image = image;
        }
    }
}
