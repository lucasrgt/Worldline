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

/** Forces a 60-chunk dirty set and captures native 40-tick autosave batches. */
public final class AeroForcedAutosaveCycle {
    private static final String ID = "m19-forced-autosave";
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Properties config = new Properties();

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: java tools/smoke/AeroForcedAutosaveCycle.java " + ID);
            System.exit(2);
        }
        try { new AeroForcedAutosaveCycle().execute(); }
        catch (Exception error) {
            System.err.println("Aero forced autosave failed: " + error.getMessage());
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
        delete(save);
        capture(test, build, "seed", "true", "0", "0");
        Path snapshot = build.resolve("snapshot-dense");
        copy(save, snapshot);
        restore(save, snapshot);
        Path live = capture(test, build, "live", "false", value("dirty.chunks"), "0");
        restore(save, snapshot);
        Path budgeted = capture(test, build, "budgeted", "false", value("dirty.chunks"),
                value("save.budget"));
        restore(save, snapshot);
        Path skipped = capture(test, build, "skipped", "true", value("dirty.chunks"), "0");
        delete(save); delete(snapshot);
        Path classes = build.resolve("classes"); Files.createDirectories(classes);
        run(root, command("javac", "-encoding", "UTF-8", "--release", "8", "-Xlint:all,-options",
                "-Werror", "-d", classes.toString(),
                root.resolve("adapters/aero-model-lib/src/main/java/worldline/aero/AeroSaveProbe.java")
                        .toString(),
                smoke.resolve("src/worldline/smoke/m19/ForcedAutosaveSmoke.java").toString()));
        String output = output(root, command("java", "-classpath", classes.toString(),
                "worldline.smoke.m19.ForcedAutosaveSmoke", live.toString(), budgeted.toString(),
                skipped.toString(), value("analysis.frames")));
        require(output.contains("WORLDLINE_M19_FORCED_AUTOSAVE=PASS"), "M19 proof absent");
        require(line(output, "evidence.sha256=").equals(value("expected.signature")),
                "M19 signature drift");
        String evidence = "id=" + ID + "\naero.revision=" + value("aero.revision")
                + "\nseed=" + value("seed") + "\n" + output;
        Files.write(build.resolve("evidence.txt"), evidence.getBytes(StandardCharsets.UTF_8));
        System.out.println("Aero forced autosave cycle passed");
        System.out.print(output);
        System.out.println("  evidence: " + root.relativize(build.resolve("evidence.txt")));
    }

    private Path capture(Path test, Path build, String name, String skip, String dirty,
            String budget) throws Exception {
        require(name.matches("[a-z0-9-]+"), "unsafe capture name");
        System.out.println("M19 capture " + name);
        Path raw = build.resolve(name + "-raw.log"), measured = build.resolve(name + "-aero.log");
        Files.deleteIfExists(raw); Files.deleteIfExists(measured);
        String wrapper = System.getProperty("os.name").startsWith("Windows") ? "gradlew.bat" : "gradlew";
        String text = output(test, command(test.resolve(wrapper).toString(), "--no-daemon",
                "--init-script", root.resolve(value("runner")).toString(), "runClient",
                "-PworldlineTicks=" + value("capture.ticks"), "-PworldlineWarmupTicks=0",
                "-PworldlineSpikeMs=1", "-PworldlineMode=dense", "-PworldlineMinBEs=500",
                "-PworldlinePath=" + (name.equals("seed") ? "stationary" : "look"),
                "-PworldlineViewDistance=0",
                "-PworldlineSkipSaves=" + skip, "-PworldlineSaveTick=-1",
                "-PworldlineDirtyTick=" + (name.equals("seed") ? "-1" : value("dirty.tick")),
                "-PworldlineDirtyChunks=" + dirty, "-PworldlineSaveBudget=" + budget,
                "-PworldlineLog=" + raw));
        require(text.contains("[WorldlineCapture] complete") && text.contains("BUILD SUCCESSFUL"),
                "Aero " + name + " capture failed\n" + text);
        if (!name.equals("seed"))
            require(text.contains("[WorldlineCapture] dirtyChunks="),
                    "forced dirty marker missing for " + name);
        List<String> frames = new ArrayList<String>();
        boolean recording = false;
        for (String row : text.split("\\R")) {
            if (row.contains("[WorldlineCapture] ready")) { recording = true; continue; }
            if (row.contains("[WorldlineCapture] complete")) break;
            if (recording && row.startsWith("[Aero_")) frames.add(row);
        }
        if (!name.equals("seed"))
            require(frames.size() >= Integer.parseInt(value("analysis.frames")),
                    "too few measured frames for " + name);
        Files.write(measured, frames, StandardCharsets.UTF_8);
        return measured;
    }

    private void restore(Path save, Path snapshot) throws IOException {
        delete(save); copy(snapshot, save);
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
                Path destination = target.resolve(source.relativize(item));
                if (Files.isDirectory(item)) Files.createDirectories(destination);
                else Files.copy(item, destination);
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
    private void load(Path path) throws IOException { try (java.io.Reader reader =
            Files.newBufferedReader(path, StandardCharsets.UTF_8)) { config.load(reader); } }
    private String value(String key) { String result = config.getProperty(key);
        require(result != null && !result.trim().isEmpty(), "missing smoke property " + key);
        return result.trim(); }
    private String line(String text, String prefix) { return Arrays.stream(text.split("\\R"))
            .filter(row -> row.startsWith(prefix)).findFirst().orElseThrow(
                () -> new IllegalStateException("missing " + prefix)).substring(prefix.length()); }
    private List<String> command(String... values) { return new ArrayList<String>(Arrays.asList(values)); }
    private void run(Path directory, List<String> command) throws Exception { output(directory, command); }
    private String output(Path directory, List<String> command) throws Exception {
        Process process = new ProcessBuilder(command).directory(directory.toFile())
                .redirectErrorStream(true).start();
        String text = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (process.waitFor() != 0) throw new IllegalStateException(command.get(0) + " failed\n" + text);
        return text;
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
